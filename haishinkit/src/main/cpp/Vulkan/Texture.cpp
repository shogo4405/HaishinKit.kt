#include "Kernel.h"
#include "Texture.h"
#include "Util.h"
#include "ImageStorage.h"

namespace Vulkan {
    vk::Format Texture::GetFormat(int32_t format) {
        switch (format) {
            case WINDOW_FORMAT_RGBA_8888:
                return vk::Format::eR8G8B8A8Unorm;
            case WINDOW_FORMAT_RGBX_8888:
                return vk::Format::eR8G8B8A8Unorm;
            case WINDOW_FORMAT_RGB_565:
                return vk::Format::eR5G6B5UnormPack16;
            default:
                return vk::Format::eR8G8B8A8Unorm;
        }
    }

    Texture::Texture(vk::Extent2D extent, vk::Format format) {
        image.extent = extent;
        image.format = format;
        stage.extent = extent;
        stage.format = format;
    }

    Texture::~Texture() = default;

    void Texture::SetUp(Kernel &kernel) {
        mode = HasLinearTilingFeatures(kernel) ? Mode::Linear : Mode::Stage;

        image.SetUp(kernel, image.CreateImageCreateInfo()
                .setInitialLayout(
                        mode == Linear ?
                        vk::ImageLayout::ePreinitialized :
                        vk::ImageLayout::eUndefined
                )
                .setUsage(
                        mode == Linear ?
                        vk::ImageUsageFlagBits::eSampled :
                        vk::ImageUsageFlagBits::eSampled |
                        vk::ImageUsageFlagBits::eTransferDst)
                .setTiling(
                        mode == Linear ?
                        vk::ImageTiling::eLinear :
                        vk::ImageTiling::eOptimal)
        );

        allocationSize = BindImageMemory(kernel, image.memory, image.image.get(),
                                         mode == Linear ? vk::MemoryPropertyFlagBits::eHostVisible
                                                        : vk::MemoryPropertyFlagBits::eDeviceLocal);

        switch (mode) {
            case Linear: {
                LOGI("%s", "This device has a linear tiling feature.");
                rowPitch = kernel.context.device->getImageSubresourceLayout(
                        image.image.get(),
                        vk::ImageSubresource()
                                .setMipLevel(0)
                                .setArrayLayer(0)
                                .setAspectMask(vk::ImageAspectFlagBits::eColor)
                ).rowPitch;
                auto commandBuffer = kernel.commandBuffer.Allocate(kernel);
                commandBuffer.begin(vk::CommandBufferBeginInfo());
                image.SetLayout(
                        commandBuffer,
                        vk::ImageLayout::eShaderReadOnlyOptimal,
                        vk::PipelineStageFlagBits::eHost,
                        vk::PipelineStageFlagBits::eFragmentShader
                );
                commandBuffer.end();
                kernel.context.Submit(commandBuffer);
                memory = kernel.context.device->mapMemory(image.memory.get(), 0,
                                                          allocationSize);
                break;
            }
            case Stage: {
                LOGI("%s", "This device has no a linear tiling feature.");
                stage.SetUp(kernel, stage.CreateImageCreateInfo()
                        .setUsage(vk::ImageUsageFlagBits::eTransferSrc)
                        .setTiling(vk::ImageTiling::eLinear)
                );
                allocationSize = BindImageMemory(kernel, stage.memory, stage.image.get(),
                                                 vk::MemoryPropertyFlagBits::eHostVisible);
                rowPitch = kernel.context.device->getImageSubresourceLayout(
                        stage.image.get(),
                        vk::ImageSubresource()
                                .setMipLevel(0)
                                .setArrayLayer(0)
                                .setAspectMask(vk::ImageAspectFlagBits::eColor)
                ).rowPitch;
                memory = kernel.context.device->mapMemory(stage.memory.get(), 0,
                                                          allocationSize);
                break;
            }
        }

        sampler = kernel.context.device->createSamplerUnique(
                vk::SamplerCreateInfo()
                        .setMagFilter(vk::Filter::eNearest)
                        .setMinFilter(vk::Filter::eNearest)
                        .setAddressModeU(vk::SamplerAddressMode::eRepeat)
                        .setAddressModeV(vk::SamplerAddressMode::eRepeat)
                        .setAddressModeW(vk::SamplerAddressMode::eRepeat)
                        .setMipLodBias(0.0f)
                        .setMaxAnisotropy(1)
                        .setCompareOp(vk::CompareOp::eNever)
                        .setMinLod(0.0f)
                        .setMaxLod(0.0f)
                        .setBorderColor(vk::BorderColor::eFloatOpaqueWhite)
                        .setUnnormalizedCoordinates(false)
        );

        imageView = kernel.context.CreateImageView(image.image.get(), image.format);
    }

    void Texture::TearDown(Kernel &kernel) {
        if (memory == nullptr) {
            return;
        }
        memory = nullptr;
    }

    void Texture::Update(Kernel &kernel, ANativeWindow_Buffer *buffer) {
        if (buffer->bits == nullptr || memory == nullptr) {
            return;
        }
        switch (image.format) {
            case vk::Format::eR5G6B5UnormPack16:
                switch (buffer->format) {
                    case WINDOW_FORMAT_RGBA_8888:
                        throw std::runtime_error(
                                "unsupported formats eR5G6B5UnormPack16:WINDOW_FORMAT_RGBA_8888");
                    case WINDOW_FORMAT_RGBX_8888:
                        throw std::runtime_error(
                                "unsupported formats eR5G6B5UnormPack16:WINDOW_FORMAT_RGBX_8888");
                    case WINDOW_FORMAT_RGB_565:
                        memcpy(memory, buffer->bits, allocationSize);
                        break;
                }
                break;
            case vk::Format::eR8G8B8A8Unorm:
                switch (buffer->format) {
                    case WINDOW_FORMAT_RGBA_8888:
                        for (int32_t y = 0; y < image.extent.height; y++) {
                            auto *row = reinterpret_cast<unsigned char *>((char *) memory +
                                                                          rowPitch *
                                                                          y);
                            auto *src = reinterpret_cast<unsigned char *>((char *) buffer->bits +
                                                                          4 * buffer->stride * y);
                            memcpy(row, src, (size_t) (4 * buffer->stride));
                        }
                        break;
                    case WINDOW_FORMAT_RGBX_8888:
                        throw std::runtime_error(
                                "unsupported formats eR8G8B8A8Unorm:WINDOW_FORMAT_RGBX_8888");
                    case WINDOW_FORMAT_RGB_565:
                        throw std::runtime_error(
                                "unsupported formats eR8G8B8A8Unorm:WINDOW_FORMAT_RGB_565");
                }
                break;
            default:
                throw std::runtime_error("unsupported formats");
        }

        CopyImage(kernel);
    }

    vk::DescriptorImageInfo Texture::CreateDescriptorImageInfo() {
        return vk::DescriptorImageInfo()
                .setImageLayout(image.layout)
                .setSampler(sampler.get())
                .setImageView(imageView.get());
    }

    bool Texture::HasLinearTilingFeatures(Kernel &kernel) const {
        auto properties = kernel.context.physicalDevice.getFormatProperties(image.format);
        if (properties.linearTilingFeatures & vk::FormatFeatureFlagBits::eSampledImage) {
            return true;
        }
        return false;
    }

    int32_t
    Texture::BindImageMemory(Kernel &kernel, vk::UniqueDeviceMemory &memory, vk::Image image,
                             vk::MemoryPropertyFlags properties) {
        const auto requirements = kernel.context.device->getImageMemoryRequirements(image);
        memory = kernel.context.device->allocateMemoryUnique(
                vk::MemoryAllocateInfo()
                        .setAllocationSize(requirements.size)
                        .setMemoryTypeIndex(
                                kernel.context.FindMemoryType(
                                        requirements.memoryTypeBits,
                                        properties
                                ))
        );
        kernel.context.device->bindImageMemory(image, memory.get(), 0);
        return requirements.size;
    }

    void Texture::CopyImage(Kernel &kernel) {
        if (mode == Linear) {
            return;
        }
        auto commandBuffer = kernel.commandBuffer.Allocate(kernel);
        commandBuffer.begin(vk::CommandBufferBeginInfo());
        stage.SetLayout(
                commandBuffer,
                vk::ImageLayout::eTransferSrcOptimal,
                vk::PipelineStageFlagBits::eHost,
                vk::PipelineStageFlagBits::eTransfer
        );
        image.SetLayout(
                commandBuffer,
                vk::ImageLayout::eTransferDstOptimal,
                vk::PipelineStageFlagBits::eHost,
                vk::PipelineStageFlagBits::eTransfer);

        const auto imageCopy = vk::ImageCopy()
                .setSrcSubresource({vk::ImageAspectFlagBits::eColor, 0, 0, 1})
                .setSrcOffset({0, 0, 0})
                .setDstSubresource({vk::ImageAspectFlagBits::eColor, 0, 0, 1})
                .setDstOffset({0, 0, 0})
                .setExtent({stage.extent.width, stage.extent.height, 1});

        commandBuffer.copyImage(
                stage.image.get(),
                vk::ImageLayout::eTransferSrcOptimal,
                image.image.get(),
                vk::ImageLayout::eTransferDstOptimal,
                imageCopy);

        image.SetLayout(
                commandBuffer,
                vk::ImageLayout::eShaderReadOnlyOptimal,
                vk::PipelineStageFlagBits::eTransfer,
                vk::PipelineStageFlagBits::eFragmentShader);
        commandBuffer.end();
        kernel.context.Submit(commandBuffer);
    }
}

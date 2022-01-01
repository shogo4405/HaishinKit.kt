#include "Kernel.h"
#include "Texture.h"
#include "Util.h"

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

    Texture::Texture(vk::Extent2D extent2D, vk::Format format) :
            extent2D(extent2D), format(format) {
    }

    Texture::~Texture() = default;

    void Texture::SetUp(Kernel &kernel) {
        const auto hasLinearTilingFeatures = HasLinearTilingFeatures(kernel);

        image = kernel.context.device->createImageUnique(
                vk::ImageCreateInfo()
                        .setImageType(vk::ImageType::e2D)
                        .setExtent(vk::Extent3D(extent2D.width, extent2D.height, 1))
                        .setMipLevels(1)
                        .setArrayLayers(1)
                        .setFormat(format)
                        .setTiling(vk::ImageTiling::eLinear)
                        .setInitialLayout(imageLayout)
                        .setUsage(hasLinearTilingFeatures ? vk::ImageUsageFlagBits::eSampled
                                                          : vk::ImageUsageFlagBits::eTransferSrc)
                        .setSharingMode(vk::SharingMode::eExclusive)
                        .setSamples(vk::SampleCountFlagBits::e1));

        SetMode(kernel, hasLinearTilingFeatures ? Linear : Stage);

        const auto imageMemoryRequirements = kernel.context.device->getImageMemoryRequirements(
                image.get());

        const auto layout = kernel.context.device->getImageSubresourceLayout(
                image.get(),
                vk::ImageSubresource()
                        .setMipLevel(0)
                        .setArrayLayer(0)
                        .setAspectMask(vk::ImageAspectFlagBits::eColor)
        );

        rowPitch = layout.rowPitch;
        allocationSize = imageMemoryRequirements.size;
        deviceMemory = kernel.context.device->allocateMemoryUnique(
                vk::MemoryAllocateInfo()
                        .setAllocationSize(imageMemoryRequirements.size)
                        .setMemoryTypeIndex(
                                kernel.context.FindMemoryType(
                                        imageMemoryRequirements.memoryTypeBits,
                                        vk::MemoryPropertyFlagBits::eHostVisible))
        );
        kernel.context.device->bindImageMemory(image.get(), deviceMemory.get(), 0);

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

        imageView = kernel.context.CreateImageView(image.get(), format);

        mapped = kernel.context.device->mapMemory(deviceMemory.get(), 0, allocationSize);
    }

    void Texture::TearDown(Kernel &kernel) {
        if (mapped == nullptr) {
            return;
        }
        mapped = nullptr;
        kernel.context.device->unmapMemory(deviceMemory.get());
        imageLayout = vk::ImageLayout::ePreinitialized;
    }

    void Texture::Update(Kernel &kernel, ANativeWindow_Buffer *buffer) {
        if (buffer->bits == nullptr || mapped == nullptr) {
            return;
        }
        switch (format) {
            case vk::Format::eR5G6B5UnormPack16:
                switch (buffer->format) {
                    case WINDOW_FORMAT_RGBA_8888:
                        throw std::runtime_error(
                                "unsupported formats eR5G6B5UnormPack16:WINDOW_FORMAT_RGBA_8888");
                    case WINDOW_FORMAT_RGBX_8888:
                        throw std::runtime_error(
                                "unsupported formats eR5G6B5UnormPack16:WINDOW_FORMAT_RGBX_8888");
                    case WINDOW_FORMAT_RGB_565:
                        memcpy(mapped, buffer->bits, allocationSize);
                        break;
                }
                break;
            case vk::Format::eR8G8B8A8Unorm:
                switch (buffer->format) {
                    case WINDOW_FORMAT_RGBA_8888:
                        for (int32_t y = 0; y < extent2D.height; y++) {
                            auto *row = reinterpret_cast<unsigned char *>((char *) mapped +
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
    }

    vk::DescriptorImageInfo Texture::CreateDescriptorImageInfo() {
        return vk::DescriptorImageInfo()
                .setImageLayout(imageLayout)
                .setSampler(sampler.get())
                .setImageView(imageView.get());
    }

    void Texture::SetImageLayout(vk::CommandBuffer &commandBuffer,
                                 vk::ImageLayout newImageLayout,
                                 vk::PipelineStageFlagBits srcStageMask,
                                 vk::PipelineStageFlagBits dstStageMask) {

        const auto imageMemoryBarrier = Util::CreateImageMemoryBarrier(imageLayout, newImageLayout)
                .setImage(image.get())
                .setSubresourceRange({vk::ImageAspectFlagBits::eColor, 0, 1, 0, 1});

        commandBuffer.pipelineBarrier(
                srcStageMask,
                dstStageMask,
                vk::DependencyFlags(),
                nullptr,
                nullptr,
                imageMemoryBarrier
        );

        imageLayout = newImageLayout;
    }

    void Texture::SetMode(Kernel &kernel, Mode mode) {
        auto commandBuffer = kernel.commandBuffer.Allocate(kernel);
        commandBuffer.begin(vk::CommandBufferBeginInfo());
        switch (mode) {
            case Linear:
                LOGI("%s", "This device has a linear tiling feature.");
                SetImageLayout(
                        commandBuffer,
                        vk::ImageLayout::eShaderReadOnlyOptimal,
                        vk::PipelineStageFlagBits::eHost,
                        vk::PipelineStageFlagBits::eFragmentShader
                );
                break;
            case Stage:
                LOGI("%s", "This device has no a linear tiling feature.");
                break;
        }
        commandBuffer.end();
        kernel.context.Submit(commandBuffer);
    }

    bool Texture::HasLinearTilingFeatures(Kernel &kernel) {
        auto properties = kernel.context.physicalDevice.getFormatProperties(format);
        if (properties.linearTilingFeatures & vk::FormatFeatureFlagBits::eSampledImage) {
            return true;
        }
        return false;
    }
}

#include "Kernel.h"
#include "Texture.h"
#include "Util.h"
#include "ImageStorage.h"
#include "ColorSpace.h"
#include <glm/ext/matrix_transform.hpp>

namespace Graphics {
    Texture::Texture(vk::Extent2D extent, int32_t format) : colorSpace(new ColorSpace()) {
        colorSpace->format = format;
        colorSpace->extent = extent;
        image.extent = extent;
        image.format = colorSpace->GetFormat();
        stage.extent = extent;
        stage.format = colorSpace->GetFormat();
    }

    Texture::~Texture() = default;

    PushConstants Texture::GetPushConstants() const {
        auto degrees = 0.f;

        switch (imageOrientation) {
            case UP:
                degrees = 0.f;
                break;
            case DOWN:
                degrees = 180.f;
                break;
            case LEFT:
                degrees = 90.f;
                break;
            case RIGHT:
                degrees = 270.f;
                break;
            case UP_MIRRORED:
                degrees = 0.f;
                break;
            case DOWN_MIRRORED:
                degrees = 180.0;
                break;
            case LEFT_MIRRORED:
                degrees = 90.f;
                break;
            case RIGHT_MIRRORED:
                degrees = 180.f;
                break;
        }

        return {
                .mvp = glm::scale(glm::mat4(1.f), glm::vec3(1.f, 1.f, 1.f)),
                .preRotate = glm::rotate(glm::mat4(1.f), glm::radians(degrees),
                                         glm::vec3(0.f, 0.f, 1.f)),
        };
    }

    vk::Viewport Texture::GetViewport(const vk::Extent2D surface) const {
        vk::Viewport viewport = vk::Viewport();

        auto newImageExtent = image.extent;

        if (imageOrientation == LEFT || imageOrientation == RIGHT ||
            imageOrientation == LEFT_MIRRORED || imageOrientation || RIGHT_MIRRORED) {
            newImageExtent = vk::Extent2D()
                    .setWidth(image.extent.height)
                    .setHeight(image.extent.width);
        }

        switch (videoGravity) {
            case RESIZE_ASPECT: {
                const float xRatio = (float) surface.width / (float) newImageExtent.width;
                const float yRatio =
                        (float) surface.height / (float) newImageExtent.height;
                if (yRatio < xRatio) {
                    viewport
                            .setX((surface.width - newImageExtent.width * yRatio) / 2)
                            .setY(0)
                            .setWidth(surface.width * yRatio)
                            .setHeight(newImageExtent.height);
                } else {
                    viewport
                            .setX(0)
                            .setY((surface.height - newImageExtent.height * xRatio) / 2)
                            .setWidth(surface.width)
                            .setHeight(newImageExtent.height * xRatio);
                }
                break;
            }
            case RESIZE_ASPECT_FILL: {
                const float iRatio = (float) surface.width / (float) surface.height;
                const float fRatio = (float) newImageExtent.width / (float) newImageExtent.height;
                if (iRatio < fRatio) {
                    viewport
                            .setX(((float) surface.width - (float) surface.height * fRatio) / 2)
                            .setY(0)
                            .setWidth(surface.height * fRatio)
                            .setHeight(surface.height);
                } else {
                    viewport
                            .setX(0)
                            .setY(((float) surface.height - (float) surface.width / fRatio) / 2)
                            .setWidth(surface.width)
                            .setHeight(surface.width / fRatio);
                }
                break;
            }
            case RESIZE: {
                viewport.setWidth(surface.width);
                viewport.setHeight(surface.height);
                break;
            }
        }
        return viewport;
    }

    bool Texture::IsInvalidateLayout() {
        return invalidateLayout;
    }

    void Texture::SetImageOrientation(ImageOrientation newImageOrientation) {
        imageOrientation = newImageOrientation;
        invalidateLayout = true;
    }

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

        colorSpace->size = BindImageMemory(kernel, image.memory, image.image.get(),
                                           mode == Linear ? vk::MemoryPropertyFlagBits::eHostVisible
                                                          : vk::MemoryPropertyFlagBits::eDeviceLocal);

        switch (mode) {
            case Linear: {
                LOGI("%s", "This device has a linear tiling feature.");
                colorSpace->layout = kernel.device->getImageSubresourceLayout(
                        image.image.get(),
                        vk::ImageSubresource()
                                .setMipLevel(0)
                                .setArrayLayer(0)
                                .setAspectMask(vk::ImageAspectFlagBits::eColor)
                );
                auto commandBuffer = kernel.commandBuffer.Allocate(kernel);
                commandBuffer.begin(vk::CommandBufferBeginInfo());
                image.SetLayout(
                        commandBuffer,
                        vk::ImageLayout::eShaderReadOnlyOptimal,
                        vk::PipelineStageFlagBits::eHost,
                        vk::PipelineStageFlagBits::eFragmentShader
                );
                commandBuffer.end();
                kernel.Submit(commandBuffer);
                colorSpace->memory = kernel.device->mapMemory(image.memory.get(), 0,
                                                              colorSpace->size);
                break;
            }
            case Stage: {
                LOGI("%s", "This device has no a linear tiling feature.");
                stage.SetUp(kernel, stage.CreateImageCreateInfo()
                        .setUsage(vk::ImageUsageFlagBits::eTransferSrc)
                        .setTiling(vk::ImageTiling::eLinear)
                );
                colorSpace->size = BindImageMemory(kernel, stage.memory, stage.image.get(),
                                                   vk::MemoryPropertyFlagBits::eHostVisible);
                colorSpace->layout = kernel.device->getImageSubresourceLayout(
                        stage.image.get(),
                        vk::ImageSubresource()
                                .setMipLevel(0)
                                .setArrayLayer(0)
                                .setAspectMask(vk::ImageAspectFlagBits::eColor));
                colorSpace->memory = kernel.device->mapMemory(stage.memory.get(), 0,
                                                              colorSpace->size);
                break;
            }
        }

        vk::Filter filter = vk::Filter::eLinear;
        switch (resampleFilter) {
            case LINEAR:
                filter = vk::Filter::eLinear;
                break;
            case NEAREST:
                filter = vk::Filter::eNearest;
                break;
            case CUBIC:
                filter = vk::Filter::eCubicIMG;
                break;
        }

        sampler = kernel.device->createSamplerUnique(
                vk::SamplerCreateInfo()
                        .setMagFilter(filter)
                        .setMinFilter(filter)
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

        imageView = kernel.CreateImageView(image.image.get(), image.format);
    }

    void Texture::TearDown(Kernel &kernel) {
    }

    void
    Texture::Update(Kernel &kernel, void *y, void *u, void *v, int32_t yStride, int32_t uvStride,
                    int32_t uvPixelStride) {
        if (colorSpace->convert(y, u, v, yStride, uvStride, uvPixelStride)) {
            CopyImage(kernel);
        }
    }

    vk::DescriptorImageInfo Texture::CreateDescriptorImageInfo() {
        return vk::DescriptorImageInfo()
                .setImageLayout(image.layout)
                .setSampler(sampler.get())
                .setImageView(imageView.get());
    }

    bool Texture::HasLinearTilingFeatures(Kernel &kernel) const {
        auto properties = kernel.physicalDevice.getFormatProperties(image.format);
        if (properties.linearTilingFeatures & vk::FormatFeatureFlagBits::eSampledImage) {
            return true;
        }
        return false;
    }

    int32_t
    Texture::BindImageMemory(Kernel &kernel, vk::UniqueDeviceMemory &memory, vk::Image image,
                             vk::MemoryPropertyFlags properties) {
        const auto requirements = kernel.device->getImageMemoryRequirements(image);
        memory = kernel.device->allocateMemoryUnique(
                vk::MemoryAllocateInfo()
                        .setAllocationSize(requirements.size)
                        .setMemoryTypeIndex(
                                kernel.FindMemoryType(
                                        requirements.memoryTypeBits,
                                        properties
                                ))
        );
        kernel.device->bindImageMemory(image, memory.get(), 0);
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
        kernel.Submit(commandBuffer);
    }
}

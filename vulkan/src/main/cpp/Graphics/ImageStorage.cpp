#include "Kernel.h"
#include "Util.h"
#include "ImageStorage.h"

using namespace Graphics;

void ImageStorage::SetExternalFormat(uint64_t newExternalFormat) {
    externalFormat.setExternalFormat(newExternalFormat);
}

void ImageStorage::SetUp(Kernel &kernel, vk::ImageCreateInfo info) {
    layout = info.initialLayout;
    image = kernel.device->createImageUnique(info);
}

void ImageStorage::SetUp(Kernel &kernel, AHardwareBuffer *newBuffer) {
    this->buffer = newBuffer;
    layout = vk::ImageLayout::eUndefined;

    image = kernel.device->createImageUnique(
            CreateImageCreateInfo()
                    .setInitialLayout(layout)
                    .setUsage(vk::ImageUsageFlagBits::eSampled)
                    .setTiling(vk::ImageTiling::eOptimal)
                    .setPNext(&vk::ExternalMemoryImageCreateInfo()
                            .setHandleTypes(
                                    vk::ExternalMemoryHandleTypeFlagBits::eAndroidHardwareBufferANDROID)
                            .setPNext(
                                    &externalFormat
                            )));

    const auto hardwareBufferProperties = kernel.device->getAndroidHardwareBufferPropertiesANDROID(
            *buffer);
    memory = kernel.device->allocateMemoryUnique(
            vk::MemoryAllocateInfo()
                    .setAllocationSize(hardwareBufferProperties.allocationSize)
                    .setMemoryTypeIndex(
                            kernel.FindMemoryType(
                                    hardwareBufferProperties.memoryTypeBits,
                                    vk::MemoryPropertyFlagBits::eHostVisible
                            ))
                    .setPNext(&vk::MemoryDedicatedAllocateInfo()
                            .setImage(image.get())
                            .setPNext(&vk::ImportAndroidHardwareBufferInfoANDROID()
                                    .setBuffer(buffer)))
    );

    kernel.device->bindImageMemory(image.get(), memory.get(), 0);
}

void ImageStorage::TearDown(Kernel &kernel) {
}

void ImageStorage::SetLayout(vk::CommandBuffer &commandBuffer,
                             vk::ImageLayout newImageLayout,
                             vk::PipelineStageFlagBits srcStageMask,
                             vk::PipelineStageFlagBits dstStageMask) {

    const auto barrier = Util::CreateImageMemoryBarrier(layout, newImageLayout)
            .setImage(image.get())
            .setSubresourceRange({vk::ImageAspectFlagBits::eColor, 0, 1, 0, 1});

    commandBuffer.pipelineBarrier(
            srcStageMask,
            dstStageMask,
            vk::DependencyFlags(),
            nullptr,
            nullptr,
            barrier
    );

    layout = newImageLayout;
}

vk::UniqueImageView
ImageStorage::CreateImageView(Kernel &kernel, vk::UniqueSamplerYcbcrConversion &conversion) const {
    auto info = vk::ImageViewCreateInfo()
            .setImage(image.get())
            .setViewType(vk::ImageViewType::e2D)
            .setFormat(format)
            .setComponents(
                    vk::ComponentMapping()
                            .setR(vk::ComponentSwizzle::eR)
                            .setG(vk::ComponentSwizzle::eG)
                            .setB(vk::ComponentSwizzle::eB)
                            .setA(vk::ComponentSwizzle::eA))
            .setSubresourceRange(
                    vk::ImageSubresourceRange()
                            .setAspectMask(vk::ImageAspectFlagBits::eColor)
                            .setBaseMipLevel(0)
                            .setLevelCount(1)
                            .setBaseArrayLayer(0)
                            .setLayerCount(1));

    if (conversion) {
        const auto conversionInfo = vk::SamplerYcbcrConversionInfo()
                .setConversion(
                        conversion.get());
        info.setPNext(&conversionInfo);
    }

    return kernel.device->createImageViewUnique(info);
}

vk::ImageCreateInfo ImageStorage::CreateImageCreateInfo() const {
    return vk::ImageCreateInfo()
            .setImageType(vk::ImageType::e2D)
            .setExtent(vk::Extent3D(extent.width, extent.height, 1))
            .setMipLevels(1)
            .setArrayLayers(1)
            .setFormat(format)
            .setInitialLayout(layout)
            .setSharingMode(vk::SharingMode::eExclusive)
            .setSamples(vk::SampleCountFlagBits::e1);
}


#include "Kernel.h"
#include "Util.h"
#include "ImageStorage.h"

namespace Vulkan {
    void ImageStorage::SetUp(Kernel &kernel, vk::ImageCreateInfo info) {
        layout = info.initialLayout;
        image = kernel.device->createImageUnique(info);
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

    vk::ImageCreateInfo ImageStorage::CreateImageCreateInfo() {
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
}

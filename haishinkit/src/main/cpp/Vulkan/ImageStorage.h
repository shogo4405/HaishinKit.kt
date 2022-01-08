#ifndef HAISHINKIT_KT_IMAGESTORAGE_H
#define HAISHINKIT_KT_IMAGESTORAGE_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>

namespace Vulkan {
    class Kernel;

    struct ImageStorage {
        vk::Format format;
        vk::UniqueImage image;
        vk::UniqueDeviceMemory memory;
        vk::ImageLayout layout = vk::ImageLayout::ePreinitialized;
        vk::Extent2D extent = vk::Extent2D(0, 0);

        void SetUp(Kernel &kernel, vk::ImageCreateInfo info);

        void TearDown(Kernel &kernel);

        void SetLayout(
                vk::CommandBuffer &commandBuffer,
                vk::ImageLayout newLayout,
                vk::PipelineStageFlagBits srcStageMask,
                vk::PipelineStageFlagBits dstStageMask);

        vk::ImageCreateInfo CreateImageCreateInfo() const;
    };
}

#endif //HAISHINKIT_KT_IMAGESTORAGE_H

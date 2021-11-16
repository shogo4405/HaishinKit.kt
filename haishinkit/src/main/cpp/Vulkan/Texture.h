#ifndef HAISHINKIT_KT_TEXTURE_H
#define HAISHINKIT_KT_TEXTURE_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>
#include <android/native_window.h>

namespace Vulkan {
    class Kernel;

    struct Texture {
        enum Mode {
            Linear,
            Stage
        };

        static const vk::Format format;

        Texture(vk::Extent2D extent2D);

        ~Texture();

        void SetUp(Kernel &kernel);

        void TearDown(Kernel &kernel);

        void Update(Kernel &kernel, ANativeWindow_Buffer *buffer);

        vk::DescriptorImageInfo CreateDescriptorImageInfo();

    private:
        vk::UniqueImage image;
        vk::UniqueSampler sampler;
        vk::UniqueImageView imageView;
        vk::UniqueDeviceMemory deviceMemory;
        vk::Extent2D extent2D = vk::Extent2D(0, 0);
        vk::ImageLayout imageLayout = vk::ImageLayout::ePreinitialized;
        vk::DeviceSize allocationSize = 0;
        vk::DeviceSize rowPitch = 0;
        void *mapped = nullptr;

        static bool HasLinearTilingFeatures(Kernel &kernel);

        void SetMode(Kernel &kernel, Mode mode);

        void SetImageLayout(
                vk::CommandBuffer &commandBuffer,
                vk::ImageLayout newImageLayout,
                vk::PipelineStageFlagBits srcStageMask,
                vk::PipelineStageFlagBits dstStageMask);
    };
}

#endif //HAISHINKIT_KT_TEXTURE_H

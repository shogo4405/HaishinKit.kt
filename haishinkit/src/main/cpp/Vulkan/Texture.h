#ifndef HAISHINKIT_KT_TEXTURE_H
#define HAISHINKIT_KT_TEXTURE_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>
#include <android/native_window.h>
#include "ImageStorage.h"

namespace Vulkan {
    class Kernel;

    struct ImageStorage;

    struct Texture {
        enum Mode {
            Linear,
            Stage
        };

        static vk::Format GetFormat(int32_t format);

        Texture(vk::Extent2D extent, vk::Format format);

        ~Texture();

        void SetUp(Kernel &kernel);

        void TearDown(Kernel &kernel);

        void Update(Kernel &kernel, ANativeWindow_Buffer *buffer);

        vk::DescriptorImageInfo CreateDescriptorImageInfo();

    private:
        Mode mode = Mode::Linear;

        ImageStorage image;
        ImageStorage stage;

        vk::UniqueSampler sampler;
        vk::UniqueImageView imageView;

        vk::DeviceSize allocationSize = 0;
        vk::DeviceSize rowPitch = 0;
        void *memory = nullptr;

        bool HasLinearTilingFeatures(Kernel &kernel) const;

        static int32_t
        BindImageMemory(Kernel &kernel, vk::UniqueDeviceMemory &memory, vk::Image image,
                        vk::MemoryPropertyFlags properties);

        void CopyImage(Kernel &kernel);
    };
}

#endif //HAISHINKIT_KT_TEXTURE_H

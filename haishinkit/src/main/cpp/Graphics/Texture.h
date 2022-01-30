#ifndef HAISHINKIT_KT_TEXTURE_H
#define HAISHINKIT_KT_TEXTURE_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>
#include <android/native_window.h>
#include "ImageStorage.h"
#include "VideoGravity.h"
#include "ResampleFilter.h"
#include "ColorSpace.h"
#include "ImageOrientation.h"
#include "PushConstants.hpp"

namespace Graphics {
    class Kernel;

    struct ImageStorage;

    struct Texture {
        enum Mode {
            Linear,
            Stage
        };

        VideoGravity videoGravity = RESIZE_ASPECT;
        ResampleFilter resampleFilter = CUBIC;

        Texture(vk::Extent2D extent, int32_t format);

        ~Texture();

        bool IsInvalidateLayout();

        void SetUp(Kernel &kernel);

        void SetImageOrientation(ImageOrientation newImageOrientation);

        void TearDown(Kernel &kernel);

        void Update(Kernel &kernel, void *y, void *u, void *v, int32_t yStride, int32_t uvStride,
                    int32_t uvPixelStride);

        vk::Viewport GetViewport(const vk::Extent2D surface) const;

        PushConstants GetPushConstants() const;

        vk::DescriptorImageInfo CreateDescriptorImageInfo();

    private:
        Mode mode = Mode::Linear;
        ImageStorage image;
        ImageStorage stage;
        vk::UniqueSampler sampler;
        vk::UniqueImageView imageView;
        bool invalidateLayout = true;
        ImageOrientation imageOrientation = UP;
        ColorSpace *colorSpace;

        bool HasLinearTilingFeatures(Kernel &kernel) const;

        static int32_t
        BindImageMemory(Kernel &kernel, vk::UniqueDeviceMemory &memory, vk::Image image,
                        vk::MemoryPropertyFlags properties);

        void CopyImage(Kernel &kernel);
    };
}

#endif //HAISHINKIT_KT_TEXTURE_H

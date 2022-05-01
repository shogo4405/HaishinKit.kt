#ifndef HAISHINKIT_KT_TEXTURE_H
#define HAISHINKIT_KT_TEXTURE_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>
#include <android/native_window.h>
#include "ImageStorage.h"
#include "VideoGravity.h"
#include "ResampleFilter.h"
#include "ImageOrientation.h"

namespace Graphics {
    class Kernel;

    struct ImageStorage;

    struct Texture {
        VideoGravity videoGravity = RESIZE_ASPECT;
        ResampleFilter resampleFilter = CUBIC;

        Texture(vk::Extent2D extent, int32_t format);

        ~Texture();

        void SetUp(Kernel &kernel, AHardwareBuffer *buffer);

        void TearDown(Kernel &kernel);

        void SetImageOrientation(ImageOrientation newImageOrientation);

        void UpdateAt(Kernel &kernel, uint32_t currentFrame, AHardwareBuffer *buffer);

        void LayoutAt(Kernel &kernel, uint32_t currentFrame);

    private:
        static vk::ClearColorValue CLEAR_COLOR;

        int32_t format;
        vk::Extent2D extent;
        std::vector<ImageStorage> storages;
        vk::UniqueSampler sampler;
        ImageOrientation imageOrientation = UP;
        vk::UniqueSamplerYcbcrConversion conversion;
        uint64_t externalFormat = 0;

        std::vector<vk::Rect2D> scissors;
        std::vector<vk::ClearValue> colors;
        std::vector<vk::Viewport> viewports;
    };
}

#endif //HAISHINKIT_KT_TEXTURE_H

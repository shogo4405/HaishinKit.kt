#ifndef HAISHINKIT_KT_COLOR_SPACE_H
#define HAISHINKIT_KT_COLOR_SPACE_H

#include <jni.h>
#include <vulkan/vulkan.hpp>
#include <android/native_window.h>

namespace Graphics {

    class Kernel;
    struct ImageStorage;

    class ColorSpace {
    public:
        int32_t format;
        vk::Extent2D extent;

        ColorSpace();

        ~ColorSpace();

        vk::Format GetFormat() const;

        bool
        Map(void *y, void *u, void *v, int32_t yStride, int32_t uvStride,
            int32_t uvPixelStride) const;

        void Bind(Kernel &kernel, ImageStorage &storage, vk::MemoryPropertyFlags properties);

    private:
        int32_t size = 0;
        vk::SubresourceLayout layout;
        void *memory = nullptr;

        inline static uint32_t YuvToRgb(int y, int u, int v);
    };
}

#endif //HAISHINKIT_KT_COLOR_SPACE_H

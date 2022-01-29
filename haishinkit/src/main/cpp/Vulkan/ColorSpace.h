#ifndef HAISHINKIT_KT_COLORSPACE_H
#define HAISHINKIT_KT_COLORSPACE_H

#include <jni.h>
#include <vulkan/vulkan.hpp>

namespace Vulkan {

    class ColorSpace {
    public:
        ColorSpace();

        ~ColorSpace();

        int32_t format;
        vk::SubresourceLayout layout;
        void *memory = nullptr;
        int32_t size = 0;
        vk::Extent2D extent;

        vk::Format GetFormat() const;

        bool
        convert(void *y, void *u, void *v, int32_t yStride, int32_t uvStride,
                int32_t uvPixelStride) const;

    private:
        inline static uint32_t YuvToRgb(int y, int u, int v);
    };
}

#endif //HAISHINKIT_KT_COLORSPACE_H

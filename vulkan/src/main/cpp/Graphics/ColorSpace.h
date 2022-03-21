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
    };
}

#endif //HAISHINKIT_KT_COLOR_SPACE_H

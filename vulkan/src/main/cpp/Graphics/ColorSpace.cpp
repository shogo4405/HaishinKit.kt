#include "Kernel.h"
#include "ImageStorage.h"
#include "ColorSpace.h"

using namespace Graphics;

ColorSpace::ColorSpace() = default;

ColorSpace::~ColorSpace() = default;

vk::Format ColorSpace::GetFormat() const {
    switch (format) {
        case WINDOW_FORMAT_RGBA_8888:
            return vk::Format::eR8G8B8A8Unorm;
        case WINDOW_FORMAT_RGBX_8888:
            return vk::Format::eR8G8B8A8Unorm;
        case WINDOW_FORMAT_RGB_565:
            return vk::Format::eR5G6B5UnormPack16;
        case 35: // ImageFormat.YUV_420_888
            return vk::Format::eUndefined;
        default:
            return vk::Format::eR8G8B8A8Unorm;
    }
}

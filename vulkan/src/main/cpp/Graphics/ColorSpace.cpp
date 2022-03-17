#include "Kernel.h"
#include "ImageStorage.h"
#include "ColorSpace.h"

using namespace Graphics;

ColorSpace::ColorSpace() = default;

ColorSpace::~ColorSpace() = default;

bool
ColorSpace::Map(void *buffer0, void *buffer1, void *buffer2, int32_t yStride, int32_t uvStride,
                int32_t uvPixelStride) const {
    if (memory == nullptr) {
        return false;
    }
    switch (format) {
        case WINDOW_FORMAT_RGBA_8888:
        case WINDOW_FORMAT_RGBX_8888:
            for (int32_t y = 0; y < extent.height; ++y) {
                auto *row = reinterpret_cast<unsigned char *>((char *) memory +
                                                              layout.rowPitch *
                                                              y);
                auto *src = reinterpret_cast<unsigned char *>((char *) buffer0 +
                                                              yStride * y);
                memcpy(row, src, (size_t) (4 * extent.width));
            }
            break;
        case WINDOW_FORMAT_RGB_565:
            memcpy(memory, buffer0, size);
            break;
        case 35: { // ImageFormat.YUV_420_888
            auto *yPixel = static_cast<uint8_t *>(buffer0);
            auto *uPixel = static_cast<uint8_t *>(buffer1);
            auto *vPixel = static_cast<uint8_t *>(buffer2);
            auto *out = static_cast<uint32_t *>(memory);
            for (int32_t y = 0; y < extent.height; ++y) {
                const uint8_t *pY = yPixel + yStride * y;
                int32_t uvRowStart = uvStride * (y >> 1);
                const uint8_t *pU = uPixel + uvRowStart;
                const uint8_t *pV = vPixel + uvRowStart;
                for (int32_t x = 0; x < extent.width; ++x) {
                    const int32_t uvOffset = (x >> 1) * uvPixelStride;
                    out[x] = YuvToRgb(pY[x], pV[uvOffset], pU[uvOffset]);
                }
                out += extent.width;
            }
            break;
        }
        default:
            throw std::runtime_error("unsupported formats");
    }
    return true;
}

vk::Format ColorSpace::GetFormat() const {
    switch (format) {
        case WINDOW_FORMAT_RGBA_8888:
            return vk::Format::eR8G8B8A8Unorm;
        case WINDOW_FORMAT_RGBX_8888:
            return vk::Format::eR8G8B8A8Unorm;
        case WINDOW_FORMAT_RGB_565:
            return vk::Format::eR5G6B5UnormPack16;
        case 35: // ImageFormat.YUV_420_888
            return vk::Format::eR8G8B8A8Unorm;
        default:
            return vk::Format::eR8G8B8A8Unorm;
    }
}

void ColorSpace::Bind(Kernel &kernel, ImageStorage &storage, vk::MemoryPropertyFlags properties) {
    const auto requirements = kernel.device->getImageMemoryRequirements(storage.image.get());
    storage.memory = kernel.device->allocateMemoryUnique(
            vk::MemoryAllocateInfo()
                    .setAllocationSize(requirements.size)
                    .setMemoryTypeIndex(
                            kernel.FindMemoryType(
                                    requirements.memoryTypeBits,
                                    properties
                            ))
    );
    kernel.device->bindImageMemory(storage.image.get(), storage.memory.get(), 0);
    layout = kernel.device->getImageSubresourceLayout(
            storage.image.get(),
            vk::ImageSubresource()
                    .setMipLevel(0)
                    .setArrayLayer(0)
                    .setAspectMask(vk::ImageAspectFlagBits::eColor)
    );
    memory = kernel.device->mapMemory(storage.memory.get(), 0, requirements.size);
}

uint32_t ColorSpace::YuvToRgb(int y, int u, int v) {
    y -= 16;
    u -= 128;
    v -= 128;
    if (y < 0) y = 0;

    int r = 1192 * y + 1634 * v;
    int g = 1192 * y - 833 * v - 400 * u;
    int b = 1192 * y + 2066 * u;

    r = std::min(262143, std::max(0, r));
    g = std::min(262143, std::max(0, g));
    b = std::min(262143, std::max(0, b));

    r = (r >> 10) & 0xff;
    g = (g >> 10) & 0xff;
    b = (b >> 10) & 0xff;

    return 0xff000000 | (r << 16) | (g << 8) | b;
}

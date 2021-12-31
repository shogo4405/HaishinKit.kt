#ifndef HAISHINKIT_KT_UTIL_H
#define HAISHINKIT_KT_UTIL_H

namespace Vulkan {
    struct Util {
        static vk::ImageMemoryBarrier CreateImageMemoryBarrier(
                vk::ImageLayout oldImageLayout,
                vk::ImageLayout newImageLayout
        );
    };
};

#endif //HAISHINKIT_KT_UTIL_H
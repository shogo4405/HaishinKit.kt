#ifndef HAISHINKIT_KT_BUFFER_H
#define HAISHINKIT_KT_BUFFER_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>

namespace Vulkan {
    class Kernel;

    class Buffer {
    public:
        Buffer(Kernel &kernel);

        ~Buffer();

        vk::Buffer Build(void *data, vk::DeviceSize size);

    private:
        Kernel &kernel;
    };
}

#endif //HAISHINKIT_KT_BUFFER_H

#ifndef HAISHINKIT_KT_COMMANDBUFFER_H
#define HAISHINKIT_KT_COMMANDBUFFER_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>

namespace Vulkan {
    class Kernel;

    struct CommandBuffer {
        std::vector<vk::UniqueCommandBuffer> commandBuffers;

        CommandBuffer();

        ~CommandBuffer();

        void SetUp(Kernel &kernel);

        void TearDown(Kernel &kernel);

        void Build(Kernel &kernel);

        vk::CommandBuffer Allocate(Kernel &kernel);

    private:
        static const float vertices[];
        vk::UniqueCommandPool commandPool;
        std::vector<vk::Buffer> buffers;
        std::vector<vk::DeviceSize> offsets;
    };
}

#endif //HAISHINKIT_KT_COMMANDBUFFER_H

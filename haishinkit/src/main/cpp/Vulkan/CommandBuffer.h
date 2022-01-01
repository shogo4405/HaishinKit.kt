#ifndef HAISHINKIT_KT_COMMANDBUFFER_H
#define HAISHINKIT_KT_COMMANDBUFFER_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>

namespace Vulkan {
    class Kernel;
    struct Texture;

    struct CommandBuffer {
        std::vector<vk::UniqueCommandBuffer> commandBuffers;

        CommandBuffer();

        ~CommandBuffer();

        void SetTextures(Kernel &kernel, const std::vector<Texture *> &textures);

        void SetUp(Kernel &kernel);

        void TearDown(Kernel &kernel);

        vk::CommandBuffer Allocate(Kernel &kernel);

    private:
        static const float VERTICES[];

        static vk::Buffer CreateBuffer(Kernel &kernel, void *data, vk::DeviceSize size);

        vk::UniqueCommandPool commandPool;
        std::vector<vk::Buffer> buffers;
        std::vector<vk::DeviceSize> offsets;
        std::vector<vk::Framebuffer> framebuffers;
    };
}

#endif //HAISHINKIT_KT_COMMANDBUFFER_H

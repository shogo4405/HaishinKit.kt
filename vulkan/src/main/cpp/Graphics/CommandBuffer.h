#ifndef HAISHINKIT_KT_COMMANDBUFFER_H
#define HAISHINKIT_KT_COMMANDBUFFER_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>
#include "Vertex.hpp"

namespace Graphics {
    class Kernel;

    struct Texture;

    struct CommandBuffer {
        std::vector<vk::UniqueCommandBuffer> commandBuffers;
        std::vector<vk::Buffer> buffers;
        std::vector<vk::DeviceSize> offsets;
        std::vector<vk::Framebuffer> framebuffers;

        CommandBuffer();

        ~CommandBuffer();

        void SetUp(Kernel &kernel);

        void TearDown(Kernel &kernel);

        vk::CommandBuffer Allocate(Kernel &kernel);

    private:
        static const Vertex VERTICES[];

        static vk::Buffer CreateBuffer(Kernel &kernel, void *data, vk::DeviceSize size);

        vk::UniqueCommandPool commandPool;
    };
}

#endif //HAISHINKIT_KT_COMMANDBUFFER_H

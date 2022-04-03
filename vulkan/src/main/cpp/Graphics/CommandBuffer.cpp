#include "../haishinkit.hpp"
#include "Kernel.h"
#include "Util.h"
#include "CommandBuffer.h"
#include "PushConstants.hpp"

using namespace Graphics;

const Vertex CommandBuffer::VERTICES[] = {
        {{-1.f, 1.f},  {0.f, 1.f}},
        {{1.f,  1.f},  {1.f, 1.f}},
        {{-1.f, -1.f}, {0.f, 0.f}},
        {{1.f,  -1.f}, {1.f, 0.f}},
};

CommandBuffer::CommandBuffer() = default;

CommandBuffer::~CommandBuffer() = default;

void CommandBuffer::SetUp(Kernel &kernel) {
    commandPool = kernel.device->createCommandPoolUnique(
            vk::CommandPoolCreateInfo()
                    .setPNext(nullptr)
                    .setFlags(
                            vk::CommandPoolCreateFlags(
                                    vk::CommandPoolCreateFlagBits::eResetCommandBuffer))
                    .setQueueFamilyIndex(kernel.queue.queueFamilyIndex)
    );

    auto imagesCount = kernel.swapChain.GetImagesCount();
    commandBuffers = kernel.device->allocateCommandBuffersUnique(
            vk::CommandBufferAllocateInfo()
                    .setPNext(nullptr)
                    .setCommandPool(commandPool.get())
                    .setLevel(vk::CommandBufferLevel::ePrimary)
                    .setCommandBufferCount(imagesCount)
    );

    framebuffers.resize(imagesCount);
    for (auto i = 0; i < imagesCount; ++i) {
        framebuffers[i] = kernel.swapChain.CreateFramebuffer(kernel, i);
    }

    buffers.resize(1);
    buffers[0] = CreateBuffer(kernel, (void *) CommandBuffer::VERTICES,
                              sizeof(CommandBuffer::VERTICES) * sizeof(Graphics::Vertex));

    offsets.resize(1);
    offsets[0] = {0};
}

void CommandBuffer::TearDown(Kernel &kernel) {
    commandBuffers.clear();
    commandBuffers.shrink_to_fit();
    for (auto &buffer : buffers) {
        kernel.device->destroy(buffer);
    }
    for (auto &framebuffer : framebuffers) {
        kernel.device->destroy(framebuffer);
    }
}

vk::CommandBuffer CommandBuffer::Allocate(Kernel &kernel) {
    const auto commandBuffers = kernel.device->allocateCommandBuffers(
            vk::CommandBufferAllocateInfo()
                    .setCommandBufferCount(1)
                    .setCommandPool(commandPool.get())
                    .setLevel(vk::CommandBufferLevel::ePrimary)
    );
    return commandBuffers[0];
}

vk::Buffer CommandBuffer::CreateBuffer(Kernel &kernel, void *data, vk::DeviceSize size) {
    const auto result = kernel.device->createBuffer(
            vk::BufferCreateInfo()
                    .setSize(size)
                    .setUsage(vk::BufferUsageFlagBits::eVertexBuffer)
                    .setQueueFamilyIndexCount(1)
                    .setSharingMode(vk::SharingMode::eExclusive)
                    .setQueueFamilyIndices(kernel.queue.queueFamilyIndex)
    );
    const auto memoryRequirements = kernel.device->getBufferMemoryRequirements(
            result);
    const auto memory = kernel.device->allocateMemory(
            vk::MemoryAllocateInfo()
                    .setAllocationSize(
                            memoryRequirements.size)
                    .setMemoryTypeIndex(
                            kernel.FindMemoryType(memoryRequirements.memoryTypeBits,
                                                  vk::MemoryPropertyFlagBits::eHostVisible |
                                                  vk::MemoryPropertyFlagBits::eHostCoherent))
    );
    void *map = kernel.device->mapMemory(memory, 0, memoryRequirements.size);
    memcpy(map, data, size);
    kernel.device->unmapMemory(memory);
    kernel.device->bindBufferMemory(result, memory, 0);
    return result;
}

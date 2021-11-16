#include "../haishinkit.hpp"
#include "Kernel.h"
#include "Buffer.h"

namespace Vulkan {
    Buffer::Buffer(Kernel &kernel) : kernel(kernel) {
    }

    Buffer::~Buffer() = default;

    vk::Buffer Buffer::Build(void *data, vk::DeviceSize size) {
        const auto result = kernel.context.device->createBuffer(
                vk::BufferCreateInfo()
                        .setSize(size)
                        .setUsage(vk::BufferUsageFlagBits::eVertexBuffer)
                        .setQueueFamilyIndexCount(1)
                        .setQueueFamilyIndices(kernel.context.queueFamilyIndex)
        );
        const auto memoryRequirements = kernel.context.device->getBufferMemoryRequirements(
                result);
        const auto memory = kernel.context.device->allocateMemory(
                vk::MemoryAllocateInfo()
                        .setAllocationSize(
                                memoryRequirements.size)
                        .setMemoryTypeIndex(
                                kernel.context.FindMemoryType(memoryRequirements.memoryTypeBits,
                                                              vk::MemoryPropertyFlagBits::eHostVisible |
                                                              vk::MemoryPropertyFlagBits::eHostCoherent))
        );
        void *map = kernel.context.device->mapMemory(memory, 0, memoryRequirements.size);
        memcpy(map, data, size);
        kernel.context.device->unmapMemory(memory);
        kernel.context.device->bindBufferMemory(result, memory, 0);
        return result;
    }
}

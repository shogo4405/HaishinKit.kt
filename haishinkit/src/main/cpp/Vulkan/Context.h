#ifndef HAISHINKIT_KT_CONTEXT_H
#define HAISHINKIT_KT_CONTEXT_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>

namespace Vulkan {

    class Kernel;

    struct Context {
        vk::UniqueDevice device;
        vk::PhysicalDevice physicalDevice;
        vk::Queue queue;
        uint32_t queueFamilyIndex{};
        int32_t selectedPhysicalDevice = -1;

        Context();

        ~Context();

        void SelectPhysicalDevice(Kernel &kernel);

        void Submit(vk::CommandBuffer &commandBuffer);

        void CreateBuffer(vk::UniqueBuffer &buffer);

        bool IsReady() const;

        vk::UniqueImageView CreateImageView(vk::Image image, vk::Format);

        uint32_t FindMemoryType(uint32_t typeFilter, vk::MemoryPropertyFlags properties) const;
    };

}

#endif //HAISHINKIT_KT_CONTEXT_H

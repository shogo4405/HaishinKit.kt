#ifndef HAISHINKIT_KT_QUEUE_H
#define HAISHINKIT_KT_QUEUE_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>

namespace Graphics {
    class Kernel;

    class Queue {
    public:
        const int32_t DEFAULT_MAX_FRAMES = 2;

        uint32_t queueFamilyIndex{};

        void SetImagesCount(Kernel &kernel, int32_t imagesCount);

        void SetUp(Kernel &kernel, uint32_t queueFamilyIndex);

        void TearDown(Kernel &kernel);

        int32_t Acquire(Kernel &kernel);

        void Wait(Kernel &kernel);

        void Submit(Kernel &kernel, vk::CommandBuffer &commandBuffer);

        vk::Result
        Present(Kernel &kernel, uint32_t nextIndex, const std::function<void(uint32_t)> &lambda);

    private:
        int32_t currentFrame = 0;
        vk::Queue queue;
        std::vector<vk::UniqueSemaphore> waitSemaphores;
        std::vector<vk::UniqueSemaphore> signalSemaphores;
        std::vector<vk::Fence> fences;
        std::vector<vk::Fence> images;
    };
}

#endif //HAISHINKIT_KT_QUEUE_H

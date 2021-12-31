#ifndef HAISHINKIT_KT_RENDERPASS_H
#define HAISHINKIT_KT_RENDERPASS_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>

namespace Vulkan {

    class Kernel;

    struct Texture;

    struct RenderPass {
        const int32_t DEFAULT_MAX_FRAMES = 2;

        vk::UniqueRenderPass renderPass;
        int32_t currentFrame = 0;
        std::vector<vk::UniqueSemaphore> waitSemaphores;
        std::vector<vk::UniqueSemaphore> signalSemaphores;
        std::vector<vk::Fence> fences;
        std::vector<vk::Fence> images;

        void SetUp(Kernel &kernel);

        void TearDown(Kernel &kernel);

        void Next();
    };
}

#endif //HAISHINKIT_KT_RENDERPASS_H

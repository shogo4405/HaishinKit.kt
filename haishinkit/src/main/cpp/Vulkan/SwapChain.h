#ifndef HAISHINKIT_KT_SWAPCHAIN_H
#define HAISHINKIT_KT_SWAPCHAIN_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>
#include <android/native_window.h>

namespace Vulkan {
    class Kernel;

    struct SwapChain {
        vk::UniqueSwapchainKHR swapchain;
        vk::Extent2D size;
        vk::Format format;
        std::vector<vk::Image> images;
        std::vector<vk::UniqueImageView> imageViews;
        std::vector<vk::Framebuffer> framebuffers;

        void SetUp(Kernel &kernel);

        void SetUp(Kernel &kernel, vk::RenderPass renderPass);

        void TearDown(Kernel &kernel);
    };
}

#endif //HAISHINKIT_KT_SWAPCHAIN_H

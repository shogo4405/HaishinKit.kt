#ifndef HAISHINKIT_KT_SWAPCHAIN_H
#define HAISHINKIT_KT_SWAPCHAIN_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>
#include <android/native_window.h>

namespace Graphics {
    class Kernel;

    struct SwapChain {
        vk::UniqueSwapchainKHR swapchain;
        vk::Extent2D size;
        vk::Format format;
        vk::UniqueRenderPass renderPass;

        void SetUp(Kernel &kernel);

        void TearDown(Kernel &kernel);

        int32_t GetImagesCount();

        vk::Framebuffer CreateFramebuffer(Kernel &kernel, int32_t index);

    private:
        std::vector<vk::Image> images;
        std::vector<vk::UniqueImageView> imageViews;
    };
}

#endif //HAISHINKIT_KT_SWAPCHAIN_H

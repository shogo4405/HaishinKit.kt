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
        vk::UniqueRenderPass renderPass;

        void SetImageExtent(int32_t width, int32_t height);

        vk::Extent2D GetImageExtent() const;

        int32_t GetImagesCount();

        bool IsInvalidate() const;

        void SetUp(Kernel &kernel, bool requestRecreate);

        void TearDown(Kernel &kernel);

        vk::Framebuffer CreateFramebuffer(Kernel &kernel, int32_t index);

    private:
        bool isCreated = false;
        bool isInvalidate = false;
        vk::SwapchainCreateInfoKHR info;
        std::vector<vk::Image> images;
        std::vector<vk::UniqueImageView> imageViews;
    };
}

#endif //HAISHINKIT_KT_SWAPCHAIN_H

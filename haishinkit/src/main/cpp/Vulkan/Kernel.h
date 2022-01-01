#ifndef HAISHINKIT_KT_KERNEL_H
#define HAISHINKIT_KT_KERNEL_H

#define VK_USE_PLATFORM_ANDROID_KHR 1

#include "../haishinkit.hpp"
#include <vulkan/vulkan.h>
#include <vulkan/vulkan_core.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>
#include <android/asset_manager.h>
#include "Context.h"
#include "CommandBuffer.h"
#include "SwapChain.h"
#include "RenderPass.h"
#include "Pipeline.h"
#include "Texture.h"

namespace Vulkan {
    class Kernel {
    public:
        vk::UniqueInstance instance;
        vk::UniqueSurfaceKHR surface;

        Context context;
        SwapChain swapChain;
        RenderPass renderPass;
        Pipeline pipeline;
        CommandBuffer commandBuffer;

        Kernel();

        ~Kernel();

        void SetTextures(const std::vector<Texture *> &textures);

        void SetAssetManager(AAssetManager *newAssetManager);

        void SetUp(ANativeWindow *window);

        void TearDown();

        vk::Result DrawFrame();

        bool IsAvailable() const;

        std::string InspectDevices();

        vk::ShaderModule LoadShader(const std::string &fileName);

        static VKAPI_ATTR VkBool32 VKAPI_CALL
        callback(VkDebugUtilsMessageSeverityFlagBitsEXT messageSeverity,
                 VkDebugUtilsMessageTypeFlagsEXT messageType,
                 const VkDebugUtilsMessengerCallbackDataEXT *pCallbackData, void *pUserData);

    private:
        const std::string applicationName = "HaishinKit";
        const std::string engineName = "Vulkan::Kernel";
        const std::vector<const char *> validationLayers = {
                "VK_LAYER_KHRONOS_validation"
        };

        bool isValidationLayersEnabled;
        bool isAvailable = false;

        AAssetManager *assetManager;

        std::vector<char> ReadFile(const std::string &fileName);

        bool IsValidationLayersSupported();
    };
}

#endif //HAISHINKIT_KT_KERNEL_H

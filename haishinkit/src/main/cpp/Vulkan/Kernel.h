#ifndef HAISHINKIT_KT_KERNEL_H
#define HAISHINKIT_KT_KERNEL_H

#define VK_USE_PLATFORM_ANDROID_KHR 1

#include "../haishinkit.hpp"
#include <vulkan/vulkan.h>
#include <vulkan/vulkan_core.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>
#include <android/asset_manager.h>
#include "CommandBuffer.h"
#include "SwapChain.h"
#include "Pipeline.h"
#include "Texture.h"
#include "Queue.h"

namespace Vulkan {
    class Kernel {
    public:
        vk::UniqueInstance instance;
        vk::UniqueSurfaceKHR surface;
        vk::UniqueDevice device;
        vk::PhysicalDevice physicalDevice;
        int32_t selectedPhysicalDevice = -1;

        SwapChain swapChain;
        Pipeline pipeline;
        Queue queue;
        CommandBuffer commandBuffer;

        Kernel();

        ~Kernel();

        void SetTextures(const std::vector<Texture *> &textures);

        void SetAssetManager(AAssetManager *newAssetManager);

        void SetUp(ANativeWindow *window);

        void TearDown();

        vk::Result DrawFrame();

        void Submit(vk::CommandBuffer &commandBuffer);

        bool IsAvailable() const;

        std::string InspectDevices();

        vk::ShaderModule LoadShader(const std::string &fileName);

        void SelectPhysicalDevice();

        vk::UniqueImageView CreateImageView(vk::Image image, vk::Format);

        uint32_t FindMemoryType(uint32_t typeFilter, vk::MemoryPropertyFlags properties) const;

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

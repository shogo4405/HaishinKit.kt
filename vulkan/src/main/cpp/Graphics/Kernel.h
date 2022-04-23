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
#include "Feature/FeatureManager.h"
#include "SurfaceRotation.hpp"

namespace Graphics {
    class Kernel {
    public:
        vk::UniqueInstance instance;
        vk::UniqueSurfaceKHR surface;
        vk::UniqueDevice device;
        vk::PhysicalDevice physicalDevice;
        int32_t selectedPhysicalDevice = -1;
        bool invalidateSurfaceRotation = true;
        SwapChain swapChain;
        Pipeline pipeline;
        Queue queue;
        CommandBuffer commandBuffer;

        Kernel();

        ~Kernel();

        SurfaceRotation GetSurfaceRotation();

        void SetSurfaceRotation(SurfaceRotation surfaceRotation);

        void SetAssetManager(AAssetManager *newAssetManager);

        void SetUp(ANativeWindow *window);

        void TearDown();

        vk::Result DrawFrame(const std::function<void(uint32_t)> &lambda);

        void Submit(const std::function<void(vk::CommandBuffer)> &transaction);

        bool IsAvailable() const;

        std::string InspectDevices();

        vk::ShaderModule LoadShader(const std::string &fileName);

        void SelectPhysicalDevice();

        vk::UniqueImageView CreateImageView(vk::Image image, vk::Format);

        uint32_t FindMemoryType(uint32_t typeFilter, vk::MemoryPropertyFlags properties) const;

        void ReadPixels(void *buffer);

        void OnOutOfDate();

        bool HasFeatures();

    private:
        const std::string applicationName = "HaishinKit";
        const std::string engineName = "Vulkan::Kernel";
        const std::vector<const char *> validationLayers = {
                "VK_LAYER_KHRONOS_validation"
        };
        SurfaceRotation surfaceRotation = ROTATION_0;
        bool isValidationLayersEnabled;
        bool isAvailable = false;
        AAssetManager *assetManager;
        FeatureManager *featureManager;

        std::vector<char> ReadFile(const std::string &fileName);

        bool IsValidationLayersSupported();
    };
}

#endif //HAISHINKIT_KT_KERNEL_H

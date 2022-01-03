#include "Kernel.h"
#include "SwapChain.h"
#include "Texture.h"
#include "stdexcept"
#include <picojson.h>
#include <jni.h>
#include "../haishinkit.hpp"
#include "vulkan/vulkan.h"
#include "vulkan/vulkan.hpp"
#include "vulkan/vulkan_android.h"
#include "DynamicLoader.h"
#include <android/native_window.h>

VULKAN_HPP_DEFAULT_DISPATCH_LOADER_DYNAMIC_STORAGE

namespace Vulkan {
    VkBool32 Kernel::callback(VkDebugUtilsMessageSeverityFlagBitsEXT messageSeverity,
                              VkDebugUtilsMessageTypeFlagsEXT messageType,
                              const VkDebugUtilsMessengerCallbackDataEXT *pCallbackData,
                              void *pUserData) {
        switch (messageSeverity) {
            case VkDebugUtilsMessageSeverityFlagBitsEXT::VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT:
                LOGI("%s", pCallbackData->pMessage);
                break;
            case VkDebugUtilsMessageSeverityFlagBitsEXT::VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT:
                LOGE("%s", pCallbackData->pMessage);
                break;
            default:
                LOGI("%s", pCallbackData->pMessage);
                break;
        }
        return false;
    }

    Kernel::Kernel() : isValidationLayersEnabled(true), assetManager(nullptr) {
        if (!DynamicLoader::GetInstance().Load()) {
            return;
        }

        const auto useValidationLayers = isValidationLayersEnabled && IsValidationLayersSupported();

        const auto appInfo = vk::ApplicationInfo()
                .setPNext(nullptr)
                .setPApplicationName(applicationName.c_str())
                .setApplicationVersion(VK_MAKE_VERSION(1, 0, 0))
                .setPEngineName(engineName.c_str())
                .setEngineVersion(VK_MAKE_VERSION(1, 0, 0))
                .setApiVersion(VK_API_VERSION_1_2);

        std::vector<const char *> extensions;
        extensions.push_back("VK_KHR_surface");
        extensions.push_back("VK_KHR_android_surface");
        if (useValidationLayers) {
            extensions.push_back(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
        }

        auto createInfo = vk::InstanceCreateInfo()
                .setFlags(vk::InstanceCreateFlags())
                .setPApplicationInfo(&appInfo)
                .setEnabledLayerCount(0)
                .setPEnabledLayerNames(nullptr)
                .setEnabledExtensionCount(extensions.size())
                .setPEnabledExtensionNames(extensions);

        if (useValidationLayers) {
            const auto messengerCreateInfo = vk::DebugUtilsMessengerCreateInfoEXT()
                    .setFlags(vk::DebugUtilsMessengerCreateFlagsEXT())
                    .setMessageSeverity(
                            vk::DebugUtilsMessageSeverityFlagBitsEXT::eVerbose |
                            vk::DebugUtilsMessageSeverityFlagBitsEXT::eWarning |
                            vk::DebugUtilsMessageSeverityFlagBitsEXT::eInfo |
                            vk::DebugUtilsMessageSeverityFlagBitsEXT::eError
                    )
                    .setMessageType(
                            vk::DebugUtilsMessageTypeFlagBitsEXT::eGeneral |
                            vk::DebugUtilsMessageTypeFlagBitsEXT::eValidation |
                            vk::DebugUtilsMessageTypeFlagBitsEXT::ePerformance
                    )
                    .setPfnUserCallback(callback)
                    .setPUserData(nullptr);

            createInfo
                    .setPNext(&messengerCreateInfo)
                    .setEnabledLayerCount(validationLayers.size())
                    .setPpEnabledLayerNames(validationLayers.data());
        }

        instance = vk::createInstanceUnique(createInfo);
        VULKAN_HPP_DEFAULT_DISPATCHER.init(instance.get());
        isAvailable = true;
    }

    Kernel::~Kernel() {
        if (isAvailable) {
            context.device->waitIdle();
        }
        TearDown();
    }

    void Kernel::SetAssetManager(AAssetManager *newAssetManager) {
        assetManager = newAssetManager;
    }

    void Kernel::SetTextures(const std::vector<Texture *> &textures) {
        if (!(isAvailable && context.IsReady())) {
            return;
        }
        for (auto *texture: textures) {
            texture->SetUp(*this);
        }
        pipeline.SetTextures(*this, textures);
        commandBuffer.SetTextures(*this, textures);
    }

    void Kernel::SetUp(ANativeWindow *nativeWindow) {
        if (!isAvailable) {
            return;
        }
        context.SelectPhysicalDevice(*this);

        surface = instance->createAndroidSurfaceKHRUnique(
                vk::AndroidSurfaceCreateInfoKHR()
                        .setFlags(vk::AndroidSurfaceCreateFlagsKHR())
                        .setPNext(nullptr)
                        .setWindow(nativeWindow));

        swapChain.SetUp(*this);
        pipeline.SetUp(*this);
        queue.SetImagesCount(*this, swapChain.GetImagesCount());
        commandBuffer.SetUp(*this);
    }

    void Kernel::TearDown() {
        if (!isAvailable) {
            return;
        }
        commandBuffer.TearDown(*this);
        pipeline.TearDown(*this);
        swapChain.TearDown(*this);
    }

    vk::Result Kernel::DrawFrame() {
        if (!isAvailable) {
            return vk::Result::eErrorInitializationFailed;
        }
        uint32_t index = queue.Acquire(*this);
        return queue.Present(*this, index, commandBuffer.commandBuffers[index].get());
    }

    bool Kernel::IsAvailable() const {
        return isAvailable;
    }

    vk::ShaderModule Kernel::LoadShader(const std::string &fileName) {
        const auto code = ReadFile(fileName);
        return context.device->createShaderModule(
                vk::ShaderModuleCreateInfo()
                        .setCodeSize(code.size())
                        .setPCode(reinterpret_cast<const uint32_t *>(code.data()))
        );
    }

    bool Kernel::IsValidationLayersSupported() {
        const auto properties = vk::enumerateInstanceLayerProperties();
        for (auto layerName : validationLayers) {
            for (auto layerProperties : properties) {
                if (strcmp(layerName, layerProperties.layerName) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    void Kernel::Submit(vk::CommandBuffer &commandBuffer) {
        queue.Submit(*this, commandBuffer);
    }

    std::string Kernel::InspectDevices() {
        picojson::array inspect;
        auto devices = instance->enumeratePhysicalDevices();
        for (const auto &device: devices) {
            picojson::object data;
            const auto properties = device.getProperties();
            data.emplace(std::make_pair("device_name", (std::string) properties.deviceName));
            if (properties.deviceType == vk::PhysicalDeviceType::eCpu) {
                data.emplace(std::make_pair("device_type", "cpu"));
            } else if (properties.deviceType == vk::PhysicalDeviceType::eIntegratedGpu) {
                data.emplace(std::make_pair("device_type", "integrated_gpu"));
            } else if (properties.deviceType == vk::PhysicalDeviceType::eDiscreteGpu) {
                data.emplace(std::make_pair("device_type", "discrete_gpu"));
            } else if (properties.deviceType == vk::PhysicalDeviceType::eVirtualGpu) {
                data.emplace(std::make_pair("device_type", "virtual_gpu"));
            } else {
                data.emplace(std::make_pair("device_type", "other"));
            }
            data.emplace(std::make_pair("api_version", (double) properties.apiVersion));
            data.emplace(std::make_pair("driver_version", (double) properties.driverVersion));
            data.emplace(std::make_pair("vendor_id", (double) properties.vendorID));
            data.emplace(std::make_pair("device_id", (double) properties.deviceID));
            inspect.push_back(picojson::value(data));
        }
        return picojson::value(inspect).serialize();
    }

    std::vector<char> Kernel::ReadFile(const std::string &fileName) {
        AAsset *file = AAssetManager_open(assetManager, fileName.c_str(), AASSET_MODE_BUFFER);
        if (file == nullptr) {
            throw std::runtime_error("");
        }
        const auto length = AAsset_getLength(file);
        std::vector<char> contents(length);
        AAsset_read(file, static_cast<void *>(contents.data()), length);
        AAsset_close(file);
        return contents;
    }
}

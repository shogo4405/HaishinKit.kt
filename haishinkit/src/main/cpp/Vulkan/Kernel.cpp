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
        vk::DynamicLoader dl;
        const auto vkGetInstanceProcAddr = dl.getProcAddress<PFN_vkGetInstanceProcAddr>(
                "vkGetInstanceProcAddr");
        VULKAN_HPP_DEFAULT_DISPATCHER.init(vkGetInstanceProcAddr);

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
    }

    Kernel::~Kernel() = default;

    void Kernel::SetAssetManager(AAssetManager *assetManager) {
        this->assetManager = assetManager;
    }

    void Kernel::SetUp(std::vector<Texture *> textures) {
        if (!context.IsReady()) {
            return;
        }
        for (auto *texture: textures) {
            texture->SetUp(*this);
        }
        pipeline.SetUp(*this, textures);
        commandBuffer.Build(*this);
    }

    void Kernel::SetUp(ANativeWindow *nativeWindow) {
        context.SelectPhysicalDevice(*this);

        surface = instance->createAndroidSurfaceKHRUnique(
                vk::AndroidSurfaceCreateInfoKHR()
                        .setFlags(vk::AndroidSurfaceCreateFlagsKHR())
                        .setPNext(nullptr)
                        .setWindow(nativeWindow));

        swapChain.SetUp(*this);
        renderPass.SetUp(*this);
        pipeline.SetUp(*this);
        commandBuffer.SetUp(*this);
    }

    void Kernel::TearDown() {
        commandBuffer.TearDown(*this);
        pipeline.TearDown(*this);
        renderPass.TearDown(*this);
        swapChain.TearDown(*this);
    }

    vk::Result Kernel::DrawFrame() {
        const auto currentFrame = renderPass.currentFrame;

        context.device->waitForFences(renderPass.fences[currentFrame], true,
                                      std::numeric_limits<uint64_t>::max());

        vk::Result result;
        uint32_t nextIndex;

        result = context.device->acquireNextImageKHR(
                swapChain.swapchain.get(),
                std::numeric_limits<uint64_t>::max(),
                renderPass.waitSemaphores[currentFrame].get(),
                nullptr,
                &nextIndex);

        if (result == vk::Result::eErrorOutOfDateKHR) {
            LOGI("%s", "error out of date");
        } else if (result != vk::Result::eSuccess && result != vk::Result::eSuboptimalKHR) {
            throw std::runtime_error("failed to acquire swap chain");
        }

        const auto waitStageMask =
                vk::PipelineStageFlags(vk::PipelineStageFlagBits::eColorAttachmentOutput);

        if (renderPass.images[nextIndex]) {
            context.device->waitForFences(renderPass.images[nextIndex], true,
                                          std::numeric_limits<uint64_t>::max());
        }
        renderPass.images[nextIndex] = renderPass.fences[currentFrame];

        context.device->resetFences(renderPass.fences[currentFrame]);

        context.queue.submit(
                vk::SubmitInfo()
                        .setWaitSemaphores(renderPass.waitSemaphores[currentFrame].get())
                        .setWaitDstStageMask(waitStageMask)
                        .setCommandBuffers(
                                commandBuffer.commandBuffers[nextIndex].get())
                        .setSignalSemaphores(renderPass.signalSemaphores[currentFrame].get()),
                renderPass.fences[currentFrame]);

        result = context.queue.presentKHR(
                vk::PresentInfoKHR()
                        .setSwapchains(swapChain.swapchain.get())
                        .setImageIndices(nextIndex)
                        .setWaitSemaphores(renderPass.signalSemaphores[currentFrame].get())
                        .setPResults(&result)
        );

        renderPass.Next();

        if (result == vk::Result::eErrorOutOfDateKHR || result == vk::Result::eSuboptimalKHR) {
            LOGI("%s", "error out of date");
        } else if (result != vk::Result::eSuccess) {
            throw std::runtime_error("failed to present image");
        }

        return result;
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

#include "Kernel.h"
#include "Context.h"

namespace Vulkan {
    Context::Context() = default;

    Context::~Context() = default;

    void Context::SelectPhysicalDevice(Kernel &kernel) {
        if (0 <= selectedPhysicalDevice) {
            return;
        }

        auto physicalDevices = kernel.instance->enumeratePhysicalDevices();
        for (auto i = 0; i < physicalDevices.size(); i++) {
            this->physicalDevice = physicalDevices[i];
            selectedPhysicalDevice = i;
            break;
        }
        uint32_t propertyCount;
        physicalDevice.getQueueFamilyProperties(&propertyCount, nullptr);
        const auto queueFamilyProperties = physicalDevice.getQueueFamilyProperties();
        size_t graphicsQueueFamilyIndex = std::distance(
                queueFamilyProperties.begin(),
                std::find_if(
                        queueFamilyProperties.begin(), queueFamilyProperties.end(),
                        [](vk::QueueFamilyProperties const &qfp) {
                            return qfp.queueFlags & vk::QueueFlagBits::eGraphics;
                        }));
        assert(graphicsQueueFamilyIndex < queueFamilyProperties.size());

        std::vector<float> queuePriorities{1.0f};
        std::vector<const char *> extensions;
        extensions.push_back("VK_KHR_swapchain");

        device = physicalDevice.createDeviceUnique(
                vk::DeviceCreateInfo()
                        .setEnabledExtensionCount(extensions.size())
                        .setPpEnabledExtensionNames(extensions.data())
                        .setQueueCreateInfoCount(1)
                        .setQueueCreateInfos(
                                vk::DeviceQueueCreateInfo()
                                        .setFlags(vk::DeviceQueueCreateFlags())
                                        .setQueueFamilyIndex(
                                                static_cast<uint32_t>( graphicsQueueFamilyIndex ))
                                        .setQueueCount(1)
                                        .setQueuePriorities(queuePriorities)
                        )
        );
        VULKAN_HPP_DEFAULT_DISPATCHER.init(device.get());
        kernel.queue.SetUp(kernel, graphicsQueueFamilyIndex);
    }

    vk::UniqueImageView Context::CreateImageView(vk::Image image, vk::Format format) {
        return device->createImageViewUnique(
                vk::ImageViewCreateInfo()
                        .setImage(image)
                        .setViewType(vk::ImageViewType::e2D)
                        .setFormat(format)
                        .setComponents(
                                vk::ComponentMapping()
                                        .setR(vk::ComponentSwizzle::eR)
                                        .setG(vk::ComponentSwizzle::eG)
                                        .setB(vk::ComponentSwizzle::eB)
                                        .setA(vk::ComponentSwizzle::eA))
                        .setSubresourceRange(
                                vk::ImageSubresourceRange()
                                        .setAspectMask(vk::ImageAspectFlagBits::eColor)
                                        .setBaseMipLevel(0)
                                        .setLevelCount(1)
                                        .setBaseArrayLayer(0)
                                        .setLayerCount(1)));
    }

    bool
    Context::IsReady() const {
        return selectedPhysicalDevice != -1;
    }

    uint32_t
    Context::FindMemoryType(uint32_t typeFilter, vk::MemoryPropertyFlags properties) const {
        auto memoryProperties = physicalDevice.getMemoryProperties();
        for (uint32_t i = 0; i < memoryProperties.memoryTypeCount; i++) {
            if ((typeFilter & (1 << i)) &&
                (memoryProperties.memoryTypes[i].propertyFlags & properties) == properties) {
                return i;
            }
        }
        throw std::runtime_error("failed to find suitable memory type!");
    }
}

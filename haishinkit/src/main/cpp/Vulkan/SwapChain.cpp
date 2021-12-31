#include "Kernel.h"
#include "SwapChain.h"
#include "RenderPass.h"

namespace Vulkan {
    void SwapChain::SetUp(Kernel &kernel) {
        const auto capabilities = kernel.context.physicalDevice.getSurfaceCapabilitiesKHR(
                kernel.surface.get());
        const auto formats = kernel.context.physicalDevice.getSurfaceFormatsKHR(
                kernel.surface.get());

        uint32_t chosenFormat;
        for (chosenFormat = 0; chosenFormat < formats.size(); chosenFormat++) {
            if (formats[chosenFormat].format == vk::Format::eR8G8B8A8Unorm) {
                break;
            }
        }

        size = capabilities.currentExtent;
        format = formats[chosenFormat].format;

        swapchain = kernel.context.device->createSwapchainKHRUnique(
                vk::SwapchainCreateInfoKHR()
                        .setSurface(kernel.surface.get())
                        .setMinImageCount(capabilities.minImageCount)
                        .setImageFormat(formats[chosenFormat].format)
                        .setImageColorSpace(formats[chosenFormat].colorSpace)
                        .setImageExtent(capabilities.currentExtent)
                        .setImageArrayLayers(1)
                        .setImageUsage(vk::ImageUsageFlagBits::eColorAttachment)
                        .setImageSharingMode(vk::SharingMode::eExclusive)
                        .setQueueFamilyIndexCount(1)
                        .setQueueFamilyIndices(kernel.context.queueFamilyIndex)
                        .setPreTransform(vk::SurfaceTransformFlagBitsKHR::eIdentity)
                        .setPresentMode(vk::PresentModeKHR::eFifo)
                        .setClipped(false)
                        .setCompositeAlpha(vk::CompositeAlphaFlagBitsKHR::eInherit)
                        .setOldSwapchain(nullptr));

        images = kernel.context.device->getSwapchainImagesKHR(swapchain.get());

        const auto imagesCount = images.size();
        imageViews.resize(imagesCount);
        for (uint32_t i = 0; i < imagesCount; i++) {
            imageViews[i] = kernel.context.CreateImageView(images[i], format);
        }
    }

    void SwapChain::SetUp(Kernel &kernel, vk::RenderPass renderPass) {
        auto imagesCount = images.size();
        framebuffers.resize(imagesCount);
        for (auto i = 0; i < imagesCount; i++) {
            vk::ImageView attachments[] = {
                    imageViews[i].get()
            };
            framebuffers[i] = kernel.context.device->createFramebuffer(
                    vk::FramebufferCreateInfo()
                            .setPNext(nullptr)
                            .setRenderPass(renderPass)
                            .setAttachmentCount(1)
                            .setPAttachments(attachments)
                            .setWidth(size.width)
                            .setHeight(size.height)
                            .setLayers(1)
            );
        }
    }

    void SwapChain::TearDown(Kernel &kernel) {
        const auto imagesCount = images.size();
        for (auto i = 0; i < imagesCount; i++) {
            kernel.context.device->destroy(images[i]);
        }
    }
}

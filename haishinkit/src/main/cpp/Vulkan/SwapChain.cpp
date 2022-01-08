#include "Kernel.h"
#include "SwapChain.h"

namespace Vulkan {
    void SwapChain::SetUp(Kernel &kernel) {
        const auto capabilities = kernel.physicalDevice.getSurfaceCapabilitiesKHR(
                kernel.surface.get());
        const auto formats = kernel.physicalDevice.getSurfaceFormatsKHR(
                kernel.surface.get());

        uint32_t chosenFormat;
        for (chosenFormat = 0; chosenFormat < formats.size(); chosenFormat++) {
            if (formats[chosenFormat].format == vk::Format::eR8G8B8A8Unorm) {
                break;
            }
        }

        size = capabilities.currentExtent;
        format = formats[chosenFormat].format;

        swapchain = kernel.device->createSwapchainKHRUnique(
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
                        .setQueueFamilyIndices(kernel.queue.queueFamilyIndex)
                        .setPreTransform(capabilities.currentTransform)
                        .setPresentMode(vk::PresentModeKHR::eFifo)
                        .setClipped(false)
                        .setCompositeAlpha(vk::CompositeAlphaFlagBitsKHR::eInherit)
                        .setOldSwapchain(nullptr));

        images = kernel.device->getSwapchainImagesKHR(swapchain.get());

        const auto imagesCount = images.size();
        imageViews.resize(imagesCount);
        for (uint32_t i = 0; i < imagesCount; i++) {
            imageViews[i] = kernel.CreateImageView(images[i], format);
        }

        const auto attachmentDescription = vk::AttachmentDescription()
                .setFormat(format)
                .setSamples(vk::SampleCountFlagBits::e1)
                .setLoadOp(vk::AttachmentLoadOp::eClear)
                .setStoreOp(vk::AttachmentStoreOp::eStore)
                .setStencilLoadOp(vk::AttachmentLoadOp::eDontCare)
                .setStencilStoreOp(vk::AttachmentStoreOp::eDontCare)
                .setInitialLayout(vk::ImageLayout::eUndefined)
                .setFinalLayout(vk::ImageLayout::ePresentSrcKHR);

        const auto colorAttachment = vk::AttachmentReference()
                .setAttachment(0)
                .setLayout(vk::ImageLayout::eColorAttachmentOptimal);

        const auto subpassDescription = vk::SubpassDescription()
                .setFlags(vk::SubpassDescriptionFlags())
                .setPipelineBindPoint(vk::PipelineBindPoint::eGraphics)
                .setInputAttachmentCount(0)
                .setPInputAttachments(nullptr)
                .setColorAttachmentCount(1)
                .setPColorAttachments(&colorAttachment)
                .setPreserveAttachmentCount(0)
                .setPPreserveAttachments(nullptr);

        renderPass = kernel.device->createRenderPassUnique(
                vk::RenderPassCreateInfo()
                        .setAttachmentCount(1)
                        .setAttachments(attachmentDescription)
                        .setSubpassCount(1)
                        .setPSubpasses(&subpassDescription)
                        .setDependencyCount(0)
                        .setDependencies(nullptr)
        );
    }

    void SwapChain::TearDown(Kernel &kernel) {
    }

    int32_t SwapChain::GetImagesCount() {
        return images.size();
    }

    vk::Framebuffer SwapChain::CreateFramebuffer(Kernel &kernel, int32_t index) {
        vk::ImageView attachments[] = {
                imageViews[index].get()
        };
        return kernel.device->createFramebuffer(
                vk::FramebufferCreateInfo()
                        .setRenderPass(renderPass.get())
                        .setAttachmentCount(1)
                        .setPAttachments(attachments)
                        .setWidth(size.width)
                        .setHeight(size.height)
                        .setLayers(1)
        );
    }
}

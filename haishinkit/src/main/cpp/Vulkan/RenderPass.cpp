#include "../haishinkit.hpp"
#include "Kernel.h"
#include "RenderPass.h"

namespace Vulkan {
    void RenderPass::SetUp(Kernel &kernel) {
        const auto attachmentDescription = vk::AttachmentDescription()
                .setFormat(kernel.swapChain.format)
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

        renderPass = kernel.context.device->createRenderPassUnique(
                vk::RenderPassCreateInfo()
                        .setAttachmentCount(1)
                        .setAttachments(attachmentDescription)
                        .setSubpassCount(1)
                        .setPSubpasses(&subpassDescription)
                        .setDependencyCount(0)
                        .setDependencies(nullptr)
        );

        waitSemaphores.resize(DEFAULT_MAX_FRAMES);
        signalSemaphores.resize(DEFAULT_MAX_FRAMES);
        fences.resize(DEFAULT_MAX_FRAMES);
        images.resize(kernel.swapChain.GetImagesCount());
        for (auto i = 0; i < DEFAULT_MAX_FRAMES; ++i) {
            waitSemaphores[i] = kernel.context.device->createSemaphoreUnique({});
            signalSemaphores[i] = kernel.context.device->createSemaphoreUnique({});
            fences[i] = kernel.context.device->createFence(
                    {vk::FenceCreateFlagBits::eSignaled});
        }
    }

    void RenderPass::TearDown(Kernel &kernel) {
        for (auto &fence : fences) {
            kernel.context.device->destroy(fence);
        }
        for (auto &image : images) {
            kernel.context.device->destroy(image);
        }
    }

    void RenderPass::Next() {
        currentFrame = (currentFrame + 1) % DEFAULT_MAX_FRAMES;
    }
}

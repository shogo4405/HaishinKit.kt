#include "../haishinkit.hpp"
#include "Kernel.h"
#include "Util.h"
#include "Buffer.h"
#include "CommandBuffer.h"

namespace Vulkan {
    const float CommandBuffer::vertices[] = {
            -1.f, -1.f, 0, 1.f, 0.f, 0.f,
            1.f, -1.f, 0, 1.f, 1.f, 0.f,
            -1.f, 1.f, 0, 1.f, 0.f, 1.f,
            1.f, 1.f, 0, 1.f, 1.f, 1.f,
            1.f, -1.f, 0, 1.f, 1.f, 0.f,
            -1.f, 1.f, 0, 1.f, 0.f, 1.f
    };

    CommandBuffer::CommandBuffer() = default;

    CommandBuffer::~CommandBuffer() = default;

    void CommandBuffer::SetUp(Kernel &kernel) {
        commandPool = kernel.context.device->createCommandPoolUnique(
                vk::CommandPoolCreateInfo()
                        .setPNext(nullptr)
                        .setFlags(
                                vk::CommandPoolCreateFlags(
                                        vk::CommandPoolCreateFlagBits::eResetCommandBuffer))
                        .setQueueFamilyIndex(kernel.context.queueFamilyIndex)
        );

        commandBuffers = kernel.context.device->allocateCommandBuffersUnique(
                vk::CommandBufferAllocateInfo()
                        .setPNext(nullptr)
                        .setCommandPool(commandPool.get())
                        .setLevel(vk::CommandBufferLevel::ePrimary)
                        .setCommandBufferCount(kernel.swapChain.images.size())
        );

        buffers.resize(1);
        buffers[0] = Buffer(kernel).Build(
                (void *) CommandBuffer::vertices, sizeof(CommandBuffer::vertices));

        offsets.resize(1);
        offsets[0] = {0};
    }

    void CommandBuffer::TearDown(Kernel &kernel) {
    }

    void CommandBuffer::Build(Kernel &kernel) {
        const auto colors = {vk::ClearValue().setColor(
                vk::ClearColorValue().setFloat32({0.f, 0.f, 0.f, 1.f}))};

        for (auto i = 0; i < commandBuffers.size(); i++) {
            auto &commandBuffer = commandBuffers[i].get();
            commandBuffer.begin(
                    vk::CommandBufferBeginInfo()
                            .setFlags(vk::CommandBufferUsageFlagBits::eRenderPassContinue));

            commandBuffer.beginRenderPass(
                    vk::RenderPassBeginInfo()
                            .setRenderPass(kernel.renderPass.renderPass.get())
                            .setFramebuffer(kernel.swapChain.framebuffers[i])
                            .setRenderArea(
                                    vk::Rect2D().setOffset({0, 0}).setExtent(kernel.swapChain.size))
                            .setClearValues(colors),
                    vk::SubpassContents::eInline);

            commandBuffer.bindPipeline(vk::PipelineBindPoint::eGraphics,
                                       kernel.pipeline.pipeline.get());

            commandBuffer.bindVertexBuffers(0, buffers, offsets);
            commandBuffer.bindDescriptorSets(
                    vk::PipelineBindPoint::eGraphics,
                    kernel.pipeline.pipelineLayout,
                    0,
                    1,
                    &kernel.pipeline.descriptorSets[0].get(),
                    0,
                    nullptr);

            commandBuffer.draw(6, 1, 0, 0);
            commandBuffer.endRenderPass();
            commandBuffer.end();
        }
    }

    vk::CommandBuffer CommandBuffer::Allocate(Kernel &kernel) {
        const auto commandBuffers = kernel.context.device->allocateCommandBuffers(
                vk::CommandBufferAllocateInfo()
                        .setCommandBufferCount(1)
                        .setCommandPool(commandPool.get())
                        .setLevel(vk::CommandBufferLevel::ePrimary)
        );
        return commandBuffers[0];
    }
}

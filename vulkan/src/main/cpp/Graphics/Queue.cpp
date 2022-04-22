#include "Kernel.h"
#include "Queue.h"

using namespace Graphics;

void Queue::SetImagesCount(Kernel &kernel, int32_t imagesCount) {
    currentFrame = 0;
    images.resize(imagesCount);
}

void Queue::SetUp(Kernel &kernel, uint32_t newQueueFamilyIndex) {
    queueFamilyIndex = newQueueFamilyIndex;
    queue = kernel.device->getQueue(newQueueFamilyIndex, 0);

    waitSemaphores.resize(DEFAULT_MAX_FRAMES);
    signalSemaphores.resize(DEFAULT_MAX_FRAMES);
    fences.resize(DEFAULT_MAX_FRAMES);
    for (auto i = 0; i < DEFAULT_MAX_FRAMES; ++i) {
        waitSemaphores[i] = kernel.device->createSemaphoreUnique({});
        signalSemaphores[i] = kernel.device->createSemaphoreUnique({});
        fences[i] = kernel.device->createFence(
                {vk::FenceCreateFlagBits::eSignaled});
    }
}

void Queue::TearDown(Kernel &kernel) {
    for (auto &fence : fences) {
        kernel.device->destroy(fence);
    }
    for (auto &image : images) {
        kernel.device->destroy(image);
    }
}

int32_t Queue::Acquire(Kernel &kernel) {
    uint32_t nextIndex;
    vk::Result result = kernel.device->acquireNextImageKHR(
            kernel.swapChain.swapchain.get(),
            std::numeric_limits<uint64_t>::max(),
            waitSemaphores[currentFrame].get(),
            nullptr,
            &nextIndex);
    if (result == vk::Result::eErrorOutOfDateKHR) {
        LOGI("%s", "error out of date");
        kernel.OnOutOfDate();
    } else if (result != vk::Result::eSuccess && result != vk::Result::eSuboptimalKHR) {
        throw std::runtime_error("failed to acquire swap chain");
    }
    return nextIndex;
}

void Queue::Submit(Kernel &kernel, vk::CommandBuffer &commandBuffer) {
    auto fence = kernel.device->createFence({});
    queue.submit(vk::SubmitInfo().setCommandBuffers(commandBuffer), fence);
    kernel.device->waitForFences(fence, true, std::numeric_limits<uint64_t>::max());
    kernel.device->destroy(fence);
}

void Queue::Wait(Kernel &kernel) {
    kernel.device->waitForFences(fences[currentFrame], true,
                                 std::numeric_limits<uint64_t>::max());
}

vk::Result
Queue::Present(Kernel &kernel, uint32_t nextIndex, const std::function<void(uint32_t)> &lambda) {

    vk::Result result;
    const auto waitStageMask =
            vk::PipelineStageFlags(vk::PipelineStageFlagBits::eColorAttachmentOutput);

    if (images[nextIndex]) {
        kernel.device->waitForFences(images[nextIndex], true,
                                     std::numeric_limits<uint64_t>::max());
    }
    images[nextIndex] = fences[currentFrame];
    kernel.device->resetFences(fences[currentFrame]);

    vk::CommandBuffer commandBuffer = kernel.commandBuffer.commandBuffers[nextIndex].get();
    lambda(nextIndex);

    queue.submit(
            submitInfo
                    .setWaitSemaphores(waitSemaphores[currentFrame].get())
                    .setWaitDstStageMask(waitStageMask)
                    .setCommandBuffers(commandBuffer)
                    .setSignalSemaphores(signalSemaphores[currentFrame].get()),
            fences[currentFrame]);

    result = queue.presentKHR(
            presentInfo
                    .setSwapchains(kernel.swapChain.swapchain.get())
                    .setImageIndices(nextIndex)
                    .setWaitSemaphores(signalSemaphores[currentFrame].get())
                    .setPResults(&result)
    );

    currentFrame = (currentFrame + 1) % DEFAULT_MAX_FRAMES;

    if (result == vk::Result::eErrorOutOfDateKHR || result == vk::Result::eSuboptimalKHR) {
        LOGI("%s", "error out of date");
        kernel.OnOutOfDate();
    } else if (result != vk::Result::eSuccess) {
        throw std::runtime_error("failed to present image");
    }

    return result;
}

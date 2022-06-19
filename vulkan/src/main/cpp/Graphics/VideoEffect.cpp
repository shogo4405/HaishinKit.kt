#include "VideoEffect.h"

using namespace Graphics;

VideoEffect::VideoEffect(JNIEnv *env, jobject object) {
    binding = new Binding::VisualEffect(env, object);
}

VideoEffect::~VideoEffect() {
    delete binding;
}

void VideoEffect::SetUp(Kernel &kernel) {
    for (const auto &uniform: binding->GetUniforms()) {
        auto stageFlags = vk::ShaderStageFlagBits::eAll;
        switch (uniform->GetShaderStage()->GetOrdinal()) {
            case 0:
                stageFlags = vk::ShaderStageFlagBits::eAll;
                break;
            case 1:
                stageFlags = vk::ShaderStageFlagBits::eVertex;
                break;
            case 2:
                stageFlags = vk::ShaderStageFlagBits::eFragment;
                break;
            default:
                break;
        }

        descriptorSetLayoutBindings.push_back(
                vk::DescriptorSetLayoutBinding()
                        .setBinding(uniform->GetBinding())
                        .setDescriptorType(vk::DescriptorType::eUniformBuffer)
                        .setDescriptorCount(1)
                        .setStageFlags(stageFlags));
    }

    for (auto &method: binding->GetMethods()) {
        const auto value = binding->GetFloatValues(method);
        vk::Buffer buffer = CreateBuffer(kernel, (void *) &value,
                                         sizeof(float) * value.size());
        descriptorBufferInfo.push_back(
                vk::DescriptorBufferInfo()
                        .setBuffer(buffer)
                        .setOffset(0)
                        .setRange(sizeof(float) * value.size()));
    }
}

std::vector<vk::DescriptorBufferInfo> VideoEffect::GetDescriptorBufferInfo() {
    return descriptorBufferInfo;
}

std::vector<vk::DescriptorSetLayoutBinding> VideoEffect::GetDescriptorSetLayoutBindings() {
    return descriptorSetLayoutBindings;
}

std::string VideoEffect::GetShaderPath(const std::string &type) {
    return "shaders/" + binding->GetName() + "." + type + ".spv";
}

vk::Buffer VideoEffect::CreateBuffer(Kernel &kernel, void *data, vk::DeviceSize size) {
    const auto result = kernel.device->createBuffer(
            vk::BufferCreateInfo()
                    .setSize(size)
                    .setUsage(vk::BufferUsageFlagBits::eUniformBuffer)
                    .setQueueFamilyIndexCount(1)
                    .setSharingMode(vk::SharingMode::eExclusive)
                    .setQueueFamilyIndices(kernel.queue.queueFamilyIndex)
    );
    const auto memoryRequirements = kernel.device->getBufferMemoryRequirements(
            result);
    const auto memory = kernel.device->allocateMemory(
            vk::MemoryAllocateInfo()
                    .setAllocationSize(
                            memoryRequirements.size)
                    .setMemoryTypeIndex(
                            kernel.FindMemoryType(memoryRequirements.memoryTypeBits,
                                                  vk::MemoryPropertyFlagBits::eHostVisible |
                                                  vk::MemoryPropertyFlagBits::eHostCoherent))
    );
    void *map = kernel.device->mapMemory(memory, 0, memoryRequirements.size);
    memcpy(map, data, size);
    kernel.device->unmapMemory(memory);
    kernel.device->bindBufferMemory(result, memory, 0);
    return result;
}


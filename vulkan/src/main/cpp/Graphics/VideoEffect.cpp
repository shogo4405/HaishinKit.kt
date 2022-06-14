#include "VideoEffect.h"

using namespace Graphics;

VideoEffect::VideoEffect(JNIEnv *env, jobject object) : env(env), object(object) {
    ref = env->NewGlobalRef(object);

    auto value = (jstring) env->CallObjectMethod(
            object,
            env->GetMethodID(env->GetObjectClass(object), "getName",
                             "()Ljava/lang/String;"));
    name = std::string(env->GetStringUTFChars(value, nullptr));
}

VideoEffect::~VideoEffect() {
    env->DeleteGlobalRef(ref);
    env = nullptr;
}

void VideoEffect::SetUp(Kernel &kernel) {
    const auto uniformClass = env->FindClass("com/haishinkit/graphics/glsl/Uniform");
    const auto shaderStageClass = env->FindClass("com/haishinkit/graphics/glsl/ShaderStage");

    const auto uniforms = (jobjectArray) env->CallObjectMethod(
            object,
            env->GetMethodID(env->GetObjectClass(object),
                             "getUniforms",
                             "()[Lcom/haishinkit/graphics/glsl/Uniform;"));
    const auto uniformsCount = env->GetArrayLength(uniforms);
    for (auto i = 0; i < uniformsCount; ++i) {
        const auto uniform = env->GetObjectArrayElement(uniforms, i);
        const auto binding = env->CallIntMethod(
                uniform,
                env->GetMethodID(uniformClass, "binding", "()I"));

        const auto shaderStage = env->CallIntMethod(
                (jobject) env->CallObjectMethod(
                        uniform,
                        env->GetMethodID(
                                uniformClass,
                                "shaderStage",
                                "()Lcom/haishinkit/graphics/glsl/ShaderStage;")),
                env->GetMethodID(shaderStageClass, "ordinal", "()I"));

        auto stageFlags = vk::ShaderStageFlagBits::eAll;
        switch (shaderStage) {
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
                        .setBinding(binding)
                        .setDescriptorType(vk::DescriptorType::eUniformBuffer)
                        .setDescriptorCount(1)
                        .setStageFlags(stageFlags));
    }

    const auto methods = (jobjectArray) env->CallObjectMethod(
            object,
            env->GetMethodID(env->GetObjectClass(object),
                             "getMethods",
                             "()[Ljava/lang/reflect/Method;"));
    const auto methodsCount = env->GetArrayLength(uniforms);
    for (auto i = 0; i < methodsCount; ++i) {
        const auto method = env->GetObjectArrayElement(methods, i);
        const auto name = (jstring) env->CallObjectMethod(
                method,
                env->GetMethodID(env->GetObjectClass(method), "getName", "()Ljava/lang/String;"));
        const auto returnType = env->CallObjectMethod(
                method,
                env->GetMethodID(env->GetObjectClass(method), "getReturnType",
                                 "()Ljava/lang/Class;"));
        const auto typeString = (jstring) env->CallObjectMethod(
                returnType,
                env->GetMethodID(env->GetObjectClass(returnType), "toString",
                                 "()Ljava/lang/String;"));

        const auto cTypeString = std::string(env->GetStringUTFChars(typeString, nullptr));
        if (cTypeString == "float") {
            const auto cMethod = env->GetStringUTFChars(name, nullptr);
            const float value = env->CallFloatMethod(
                    object,
                    env->GetMethodID(env->GetObjectClass(object),
                                     cMethod, "()F")
            );
            vk::Buffer buffer = CreateBuffer(kernel, (void *) &value,
                                             sizeof(float));
            descriptorBufferInfo.push_back(
                    vk::DescriptorBufferInfo()
                            .setBuffer(buffer)
                            .setOffset(0)
                            .setRange(sizeof(float)));
        } else if (cTypeString == "class [F") {
            const auto cMethod = env->GetStringUTFChars(name, nullptr);
            const auto values = (jfloatArray) env->CallObjectMethod(
                    object,
                    env->GetMethodID(env->GetObjectClass(object),
                                     cMethod,
                                     "()[F"));

            const auto value = env->GetFloatArrayElements(values, nullptr);
            const auto methodCount = env->GetArrayLength(values);
            vk::Buffer buffer = CreateBuffer(kernel, (void *) &value,
                                             sizeof(float) * methodCount);
            descriptorBufferInfo.push_back(
                    vk::DescriptorBufferInfo()
                            .setBuffer(buffer)
                            .setOffset(0)
                            .setRange(sizeof(float) * methodCount));
        }
    }
}

std::vector<vk::DescriptorBufferInfo> VideoEffect::GetDescriptorBufferInfo() {
    return descriptorBufferInfo;
}

std::vector<vk::DescriptorSetLayoutBinding> VideoEffect::GetDescriptorSetLayoutBindings() {
    return descriptorSetLayoutBindings;
}

std::string VideoEffect::GetShaderPath(const std::string &type) {
    return "shaders/" + name + "." + type + ".spv";
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


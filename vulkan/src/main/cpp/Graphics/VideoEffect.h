#ifndef HAISHINKIT_KT_VIDEOEFFECT_H
#define HAISHINKIT_KT_VIDEOEFFECT_H

#include <string>
#include <jni.h>
#include "Kernel.h"
#include "../Binding/VisualEffect.h"

namespace Graphics {
    class VideoEffect {
    public:
        VideoEffect(JNIEnv *env, jobject object);

        ~VideoEffect();

        void SetUp(Kernel &kernel);

        std::string GetShaderPath(const std::string &type);

        std::vector<vk::DescriptorSetLayoutBinding> GetDescriptorSetLayoutBindings();

        std::vector<vk::DescriptorBufferInfo> GetDescriptorBufferInfo();

    private:
        static vk::Buffer CreateBuffer(Kernel &kernel, void *data, vk::DeviceSize size);

        Binding::VisualEffect *binding;
        std::vector<vk::DescriptorBufferInfo> descriptorBufferInfo;
        std::vector<vk::DescriptorSetLayoutBinding> descriptorSetLayoutBindings;
    };
}
#endif //HAISHINKIT_KT_VIDEOEFFECT_H

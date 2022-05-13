#ifndef HAISHINKIT_KT_PIPELINE_H
#define HAISHINKIT_KT_PIPELINE_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>

namespace Graphics {
    class Kernel;

    class VideoEffect;

    struct ImageStorage;

    struct Pipeline {
        vk::UniqueDescriptorSetLayout descriptorSetLayout;
        vk::UniqueDescriptorPool descriptorPool;
        std::vector<vk::UniqueDescriptorSet> descriptorSets;
        vk::UniquePipelineLayout pipelineLayout;
        vk::UniquePipelineCache pipelineCache;
        vk::UniquePipeline pipeline;

        void SetUp(Kernel &kernel, std::vector<vk::Sampler> &samplers, VideoEffect *videoEffect);

        void TearDown(Kernel &kernel);

        void UpdateDescriptorSets(Kernel &kernel, ImageStorage &storage);
    };
}

#endif //HAISHINKIT_KT_PIPELINE_H

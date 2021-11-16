#ifndef HAISHINKIT_KT_PIPELINE_H
#define HAISHINKIT_KT_PIPELINE_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>

namespace Vulkan {
    class Kernel;

    struct Texture;

    struct Pipeline {
        vk::DescriptorSetLayout descriptorSetLayout;
        vk::DescriptorPool descriptorPool;
        std::vector<vk::UniqueDescriptorSet> descriptorSets;
        vk::PipelineLayout pipelineLayout;
        vk::PipelineCache pipelineCache;
        vk::UniquePipeline pipeline;

        void SetUp(Kernel &kernel);

        void SetUp(Kernel &kernel, std::vector<Texture *> textures);

        void TearDown(Kernel &kernel);
    };
}

#endif //HAISHINKIT_KT_PIPELINE_H

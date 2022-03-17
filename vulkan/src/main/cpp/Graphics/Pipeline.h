#ifndef HAISHINKIT_KT_PIPELINE_H
#define HAISHINKIT_KT_PIPELINE_H

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan.hpp>

namespace Graphics {
    class Kernel;

    struct Texture;

    struct Pipeline {
        vk::UniqueDescriptorSetLayout descriptorSetLayout;
        vk::UniqueDescriptorPool descriptorPool;
        std::vector<vk::UniqueDescriptorSet> descriptorSets;
        vk::UniquePipelineLayout pipelineLayout;
        vk::UniquePipelineCache pipelineCache;
        vk::UniquePipeline pipeline;

        void SetTextures(Kernel &kernel, std::vector<Texture *> textures);

        void SetUp(Kernel &kernel);

        void TearDown(Kernel &kernel);
    };
}

#endif //HAISHINKIT_KT_PIPELINE_H

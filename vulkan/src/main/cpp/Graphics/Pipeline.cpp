#include "Kernel.h"
#include "Pipeline.h"
#include "SwapChain.h"
#include "Texture.h"
#include "Vertex.hpp"
#include "PushConstants.hpp"

using namespace Graphics;

void Pipeline::SetUp(Kernel &kernel, std::vector<vk::Sampler> &samplers, VideoEffect *videoEffect) {
    descriptorSetLayout = kernel.device->createDescriptorSetLayoutUnique(
            vk::DescriptorSetLayoutCreateInfo()
                    .setBindingCount(1)
                    .setBindings(
                            vk::DescriptorSetLayoutBinding()
                                    .setBinding(0)
                                    .setDescriptorType(
                                            vk::DescriptorType::eCombinedImageSampler)
                                    .setDescriptorCount(1)
                                    .setImmutableSamplers(samplers)
                                    .setStageFlags(vk::ShaderStageFlagBits::eFragment)
                    )
    );

    descriptorPool = kernel.device->createDescriptorPoolUnique(
            vk::DescriptorPoolCreateInfo()
                    .setMaxSets(1)
                    .setPoolSizes(
                            vk::DescriptorPoolSize()
                                    .setType(vk::DescriptorType::eCombinedImageSampler)
                                    .setDescriptorCount(1))
    );

    descriptorSets = kernel.device->allocateDescriptorSetsUnique(
            vk::DescriptorSetAllocateInfo()
                    .setDescriptorSetCount(1)
                    .setDescriptorPool(descriptorPool.get())
                    .setSetLayouts(descriptorSetLayout.get())
    );

    pipelineLayout = kernel.device->createPipelineLayoutUnique(
            vk::PipelineLayoutCreateInfo()
                    .setSetLayoutCount(1)
                    .setSetLayouts(
                            descriptorSetLayout.get())
                    .setPushConstantRangeCount(1)
                    .setPPushConstantRanges(&vk::PushConstantRange()
                            .setOffset(0)
                            .setStageFlags(vk::ShaderStageFlagBits::eVertex)
                            .setSize(sizeof(Graphics::PushConstants)))
    );

    pipelineCache = kernel.device->createPipelineCacheUnique(vk::PipelineCacheCreateInfo());

    const auto vert = kernel.LoadShader(videoEffect->GetShaderPath("vert"));
    const auto frag = kernel.LoadShader(videoEffect->GetShaderPath("frag"));

    // pipeline
    std::vector<vk::PipelineShaderStageCreateInfo> shaderStages = {
            vk::PipelineShaderStageCreateInfo()
                    .setStage(vk::ShaderStageFlagBits::eVertex)
                    .setModule(vert)
                    .setPName("main"),
            vk::PipelineShaderStageCreateInfo()
                    .setStage(vk::ShaderStageFlagBits::eFragment)
                    .setModule(frag)
                    .setPName("main")
    };

    std::vector<vk::VertexInputAttributeDescription> vertexInputAttributeDescriptions = Vertex::CreateAttributeDescriptions();

    std::vector<vk::PipelineColorBlendAttachmentState> colorBlendAttachmentStates = {
            vk::PipelineColorBlendAttachmentState()
                    .setBlendEnable(false)
                    .setColorWriteMask(
                            vk::ColorComponentFlags(
                                    vk::ColorComponentFlagBits::eR |
                                    vk::ColorComponentFlagBits::eG |
                                    vk::ColorComponentFlagBits::eB |
                                    vk::ColorComponentFlagBits::eA
                            )
                    )
    };

    std::vector<vk::DynamicState> dynamicStates = {
            vk::DynamicState::eViewport,
            vk::DynamicState::eScissor,
    };

    const auto bindingDescription = Vertex::CreateBindingDescription();

    vk::Extent2D imageExtent = kernel.swapChain.GetImageExtent();
    pipeline = kernel.device->createGraphicsPipelineUnique(
            pipelineCache.get(),
            vk::GraphicsPipelineCreateInfo()
                    .setStageCount(shaderStages.size())
                    .setStages(shaderStages)
                    .setPVertexInputState(&vk::PipelineVertexInputStateCreateInfo()
                            .setVertexBindingDescriptionCount(1)
                            .setPVertexBindingDescriptions(&bindingDescription)
                            .setVertexAttributeDescriptions(vertexInputAttributeDescriptions)
                    )
                    .setPInputAssemblyState(&vk::PipelineInputAssemblyStateCreateInfo()
                            .setTopology(vk::PrimitiveTopology::eTriangleStrip)
                            .setPrimitiveRestartEnable(false)
                    )
                    .setPViewportState(&vk::PipelineViewportStateCreateInfo()
                            .setViewportCount(1)
                            .setViewports(vk::Viewport()
                                                  .setX(0)
                                                  .setY(0)
                                                  .setWidth(imageExtent.width)
                                                  .setHeight(imageExtent.height)
                                                  .setMinDepth(0.0f)
                                                  .setMaxDepth(1.0f)
                            )
                            .setScissorCount(1)
                            .setScissors(
                                    vk::Rect2D()
                                            .setExtent(imageExtent)
                                            .setOffset(vk::Offset2D(0, 0))
                            )
                    )
                    .setPRasterizationState(&vk::PipelineRasterizationStateCreateInfo()
                            .setDepthClampEnable(false)
                            .setRasterizerDiscardEnable(false)
                            .setPolygonMode(vk::PolygonMode::eFill)
                            .setCullMode(vk::CullModeFlagBits::eNone)
                            .setFrontFace(vk::FrontFace::eClockwise)
                            .setDepthBiasEnable(false)
                            .setLineWidth(1)
                    )
                    .setPMultisampleState(&vk::PipelineMultisampleStateCreateInfo()
                            .setRasterizationSamples(vk::SampleCountFlagBits::e1)
                            .setSampleShadingEnable(false)
                            .setAlphaToCoverageEnable(false)
                            .setAlphaToCoverageEnable(false)
                    )
                    .setPColorBlendState(&vk::PipelineColorBlendStateCreateInfo()
                            .setLogicOp(vk::LogicOp::eCopy)
                            .setAttachmentCount(1)
                            .setAttachments(colorBlendAttachmentStates)
                    )
                    .setPDynamicState(&vk::PipelineDynamicStateCreateInfo()
                            .setDynamicStates(dynamicStates)
                    )
                    .setLayout(pipelineLayout.get())
                    .setRenderPass(kernel.swapChain.renderPass.get())
    );

    kernel.device->destroy(vert);
    kernel.device->destroy(frag);
}

void Pipeline::TearDown(Kernel &kernel) {
}

void Pipeline::UpdateDescriptorSets(Kernel &kernel, ImageStorage &storage) {
    std::vector<vk::DescriptorImageInfo> images(1);
    for (auto i = 0; i < images.size(); ++i) {
        images[i] = storage.GetDescriptorImageInfo();
    }
    for (auto &descriptorSet: descriptorSets) {
        kernel.device->updateDescriptorSets(
                vk::WriteDescriptorSet()
                        .setDstSet(descriptorSet.get())
                        .setDescriptorCount(1)
                        .setDescriptorType(vk::DescriptorType::eCombinedImageSampler)
                        .setImageInfo(images),
                nullptr
        );
    }
}
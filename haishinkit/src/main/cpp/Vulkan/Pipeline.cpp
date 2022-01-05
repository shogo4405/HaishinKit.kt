#include "Kernel.h"
#include "Pipeline.h"
#include "SwapChain.h"
#include "Texture.h"

namespace Vulkan {
    void Pipeline::SetTextures(Kernel &kernel, std::vector<Texture *> textures) {
        std::vector<vk::DescriptorImageInfo> images(textures.size());
        for (auto i = 0; i < images.size(); ++i) {
            images[i] = textures[i]->CreateDescriptorImageInfo();
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

    void Pipeline::SetUp(Kernel &kernel) {
        descriptorSetLayout = kernel.device->createDescriptorSetLayoutUnique(
                vk::DescriptorSetLayoutCreateInfo()
                        .setBindingCount(1)
                        .setBindings(
                                vk::DescriptorSetLayoutBinding()
                                        .setBinding(0)
                                        .setDescriptorType(
                                                vk::DescriptorType::eCombinedImageSampler)
                                        .setDescriptorCount(1)
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
        );

        pipelineCache = kernel.device->createPipelineCacheUnique(vk::PipelineCacheCreateInfo());

        const auto vert = kernel.LoadShader("shaders/main.vert.spv");
        const auto frag = kernel.LoadShader("shaders/main.frag.spv");

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

        std::vector<vk::VertexInputAttributeDescription> vertexInputAttributeDescriptions = {
                vk::VertexInputAttributeDescription()
                        .setLocation(0)
                        .setBinding(0)
                        .setFormat(vk::Format::eR32G32B32A32Sfloat)
                        .setOffset(0),
                vk::VertexInputAttributeDescription()
                        .setLocation(1)
                        .setBinding(0)
                        .setFormat(vk::Format::eR32G32Sfloat)
                        .setOffset(sizeof(float) * 4)
        };

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

        pipeline = kernel.device->createGraphicsPipelineUnique(
                pipelineCache.get(),
                vk::GraphicsPipelineCreateInfo()
                        .setStageCount(shaderStages.size())
                        .setStages(shaderStages)
                        .setPVertexInputState(&vk::PipelineVertexInputStateCreateInfo()
                                .setVertexBindingDescriptionCount(1)
                                .setPVertexBindingDescriptions(&vk::VertexInputBindingDescription()
                                        .setBinding(0)
                                        .setStride(6 * sizeof(float))
                                        .setInputRate(vk::VertexInputRate::eVertex)
                                )
                                .setVertexAttributeDescriptions(vertexInputAttributeDescriptions)
                        )
                        .setPInputAssemblyState(&vk::PipelineInputAssemblyStateCreateInfo()
                                .setTopology(vk::PrimitiveTopology::eTriangleList)
                                .setPrimitiveRestartEnable(false)
                        )
                        .setPViewportState(&vk::PipelineViewportStateCreateInfo()
                                .setViewportCount(1)
                                .setViewports(vk::Viewport()
                                                      .setX(0)
                                                      .setY(0)
                                                      .setWidth(kernel.swapChain.size.width)
                                                      .setHeight(kernel.swapChain.size.height)
                                                      .setMinDepth(0.0f)
                                                      .setMaxDepth(1.0f)
                                )
                                .setScissorCount(1)
                                .setScissors(
                                        vk::Rect2D()
                                                .setExtent(kernel.swapChain.size)
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
                                .setDynamicStateCount(0)
                        )
                        .setLayout(pipelineLayout.get())
                        .setRenderPass(kernel.swapChain.renderPass.get())
        );

        kernel.device->destroy(vert);
        kernel.device->destroy(frag);
    }

    void Pipeline::TearDown(Kernel &kernel) {
    }
}

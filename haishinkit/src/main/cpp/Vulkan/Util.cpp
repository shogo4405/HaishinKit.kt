#include <jni.h>
#include <string>
#include "../Unmanaged.hpp"
#include "Kernel.h"
#include "Util.h"

namespace Vulkan {
    vk::ImageMemoryBarrier Util::CreateImageMemoryBarrier(vk::ImageLayout oldImageLayout,
                                                          vk::ImageLayout newImageLayout) {
        auto imageMemoryBarrier = vk::ImageMemoryBarrier()
                .setDstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                .setSrcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                .setOldLayout(oldImageLayout)
                .setNewLayout(newImageLayout);

        switch (oldImageLayout) {
            case vk::ImageLayout::eColorAttachmentOptimal:
                imageMemoryBarrier.setSrcAccessMask(vk::AccessFlagBits::eColorAttachmentWrite);
                break;
            case vk::ImageLayout::eTransferDstOptimal:
                imageMemoryBarrier.setSrcAccessMask(vk::AccessFlagBits::eTransferWrite);
                break;
            case vk::ImageLayout::ePreinitialized:
                imageMemoryBarrier.setSrcAccessMask(vk::AccessFlagBits::eHostWrite);
                break;
            default:
                break;
        }

        switch (newImageLayout) {
            case vk::ImageLayout::eTransferDstOptimal:
                imageMemoryBarrier.setDstAccessMask(vk::AccessFlagBits::eTransferWrite);
                break;
            case vk::ImageLayout::eTransferSrcOptimal:
                imageMemoryBarrier.setDstAccessMask(vk::AccessFlagBits::eTransferRead);
                break;
            case vk::ImageLayout::eShaderReadOnlyOptimal:
                imageMemoryBarrier.setDstAccessMask(vk::AccessFlagBits::eShaderRead);
                break;
            case vk::ImageLayout::eColorAttachmentOptimal:
                imageMemoryBarrier.setDstAccessMask(vk::AccessFlagBits::eColorAttachmentWrite);
                break;
            case vk::ImageLayout::eDepthStencilAttachmentOptimal:
                imageMemoryBarrier.setDstAccessMask(
                        vk::AccessFlagBits::eDepthStencilAttachmentWrite);
                break;
            default:
                break;
        }

        return imageMemoryBarrier;
    }
}

extern "C"
{
JNIEXPORT jboolean JNICALL
Java_com_haishinkit_vk_VKUtil_isAvailable(JNIEnv *env, jobject thiz) {
    return true;
}

JNIEXPORT jstring JNICALL
Java_com_haishinkit_vk_VKUtil_inspectDevices(JNIEnv *env, jobject thiz) {
    std::string string = Vulkan::Kernel().InspectDevices();
    return env->NewStringUTF(string.c_str());
}
}

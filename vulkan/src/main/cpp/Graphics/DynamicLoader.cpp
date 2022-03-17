#include "DynamicLoader.h"
#include "Kernel.h"

using namespace Graphics;

bool DynamicLoader::Load() {
    try {
        if (loaded) {
            return true;
        }
        vk::DynamicLoader dl;
        const auto vkGetInstanceProcAddr = dl.getProcAddress<PFN_vkGetInstanceProcAddr>(
                "vkGetInstanceProcAddr");
        VULKAN_HPP_DEFAULT_DISPATCHER.init(vkGetInstanceProcAddr);
        loaded = true;
        return vkGetInstanceProcAddr != nullptr;
    } catch (...) {
        return false;
    }
}

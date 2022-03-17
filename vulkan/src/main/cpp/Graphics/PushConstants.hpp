#ifndef HAISHINKIT_KT_PUSHCONSTANTS_HPP
#define HAISHINKIT_KT_PUSHCONSTANTS_HPP

#include <vulkan/vulkan.hpp>
#include <glm/glm.hpp>

namespace Graphics {
    struct PushConstants {
        glm::mat4 mvp;
        glm::mat2 preRotate;
    };
}

#endif //HAISHINKIT_KT_PUSHCONSTANTS_HPP

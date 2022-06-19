#ifndef HAISHINKIT_KT_UNIFORM_H
#define HAISHINKIT_KT_UNIFORM_H

#include "Object.h"
#include "ShaderStage.h"

namespace Binding {
    class Uniform : Object {
    public:
        Uniform(JNIEnv *env, jobject object);

        int GetBinding();

        ShaderStage *GetShaderStage();

    private:
        ShaderStage *shaderStage = nullptr;
    };
}

#endif //HAISHINKIT_KT_UNIFORM_H

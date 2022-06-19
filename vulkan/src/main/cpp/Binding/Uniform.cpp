#include "Uniform.h"
#include "../haishinkit.hpp"

using namespace Binding;

Uniform::Uniform(JNIEnv *env, jobject object) : Object(env, object) {
    clazz = env->FindClass("com/haishinkit/graphics/glsl/Uniform");
}

int Uniform::GetBinding() {
    return env->CallIntMethod(
            object,
            env->GetMethodID(clazz, "binding", "()I"));
}

ShaderStage *Uniform::GetShaderStage() {
    if (shaderStage == nullptr) {
        shaderStage = new ShaderStage(env, (jobject) env->CallObjectMethod(
                object,
                env->GetMethodID(
                        clazz,
                        "shaderStage",
                        "()Lcom/haishinkit/graphics/glsl/ShaderStage;")));
    }
    return shaderStage;
}


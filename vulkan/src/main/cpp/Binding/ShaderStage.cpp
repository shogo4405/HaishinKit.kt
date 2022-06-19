#include "ShaderStage.h"

using namespace Binding;

ShaderStage::ShaderStage(JNIEnv *env, jobject object) : Object(env, object) {
}

int ShaderStage::GetOrdinal() {
    return env->CallIntMethod(
            object,
            env->GetMethodID(clazz, "ordinal", "()I"));
}
#include "VideoEffect.h"

using namespace Graphics;

VideoEffect::VideoEffect(JNIEnv *env, jobject object) : env(env), object(object) {
    ref = env->NewGlobalRef(object);

    jclass clazz = env->GetObjectClass(object);
    auto value = env->CallObjectMethod(object,
                                       env->GetMethodID(clazz, "getName", "()Ljava/lang/String;"));
    name = std::string(env->GetStringUTFChars(static_cast<jstring>(value), nullptr));
}

VideoEffect::~VideoEffect() {
    env->DeleteGlobalRef(ref);
    env = nullptr;
}

std::string VideoEffect::GetShaderPath(const std::string &type) {
    return "shaders/" + name + "." + type + ".spv";
}

#include "VisualEffect.h"

using namespace Binding;

VisualEffect::VisualEffect(JNIEnv *env, jobject object) : Object(env, object) {
    auto value = (jstring) env->CallObjectMethod(
            object,
            env->GetMethodID(env->GetObjectClass(object), "getName",
                             "()Ljava/lang/String;"));
    name = ToString(value);
}

std::string VisualEffect::GetName() {
    return name;
}

std::vector<Uniform *> VisualEffect::GetUniforms() {
    const auto uniforms = (jobjectArray) env->CallObjectMethod(
            object,
            env->GetMethodID(env->GetObjectClass(object),
                             "getUniforms",
                             "()[Lcom/haishinkit/graphics/glsl/Uniform;"));
    const auto counts = env->GetArrayLength(uniforms);
    std::vector<Uniform *> results;
    for (auto i = 0; i < counts; ++i) {
        const auto object = env->GetObjectArrayElement(uniforms, i);
        results.push_back(new Uniform(env, object));
    }
    return results;
}

std::vector<Method *> VisualEffect::GetMethods() {
    const auto methods = (jobjectArray) env->CallObjectMethod(
            object,
            env->GetMethodID(env->GetObjectClass(object),
                             "getMethods",
                             "()[Ljava/lang/reflect/Method;"));
    const auto counts = env->GetArrayLength(methods);
    std::vector<Method *> results;
    for (auto i = 0; i < counts; ++i) {
        const auto object = env->GetObjectArrayElement(methods, i);
        results.push_back(new Method(env, object));
    }
    return results;
}

std::vector<float> VisualEffect::GetFloatValues(Method *method) {
    const auto name = method->GetName();
    const auto type = method->GetReturnType()->ToString();
    std::vector<float> result;
    if (type == "float") {
        const float value = env->CallFloatMethod(
                object,
                env->GetMethodID(env->GetObjectClass(object),
                                 name.c_str(), "()F")
        );
        result.push_back(value);
    } else if (type == "class [F") {
        const auto values = (jfloatArray) env->CallObjectMethod(
                object,
                env->GetMethodID(env->GetObjectClass(object),
                                 name.c_str(),
                                 "()[F"));
        const auto floatArray = env->GetFloatArrayElements(values, nullptr);
        const auto floatArrayCount = env->GetArrayLength(values);
        for (auto i = 0; i < floatArrayCount; ++i) {
            result.push_back(floatArray[i]);
        }
    }
    return result;
}

#include "Method.h"

using namespace Binding;

Method::Method(JNIEnv *env, jobject object) : Object(env, object) {
}

Method::~Method() {
    if (returnType) {
        delete returnType;
    }
}

std::string Method::GetName() {
    auto value = (jstring) env->CallObjectMethod(
            object,
            env->GetMethodID(clazz, "getName", "()Ljava/lang/String;"));
    return ToString(value);
}

Class *Method::GetReturnType() {
    if (returnType == nullptr) {
        returnType = new Class(env, (jobject) env->CallObjectMethod(
                object,
                env->GetMethodID(clazz, "getReturnType", "()Ljava/lang/Class;")));
    }
    return returnType;
}


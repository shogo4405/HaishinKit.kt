#include "Class.h"

using namespace Binding;

Class::Class(JNIEnv *env, jobject object) : Object(env, object) {
}

std::string Class::ToString() {
    auto value = (jstring) env->CallObjectMethod(
            object,
            env->GetMethodID(clazz, "toString",
                             "()Ljava/lang/String;"));
    return Object::ToString(value);
}

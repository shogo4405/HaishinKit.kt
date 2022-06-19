#include "Object.h"

using namespace Binding;

Object::Object(JNIEnv *env, jobject object) : env(env), object(env->NewGlobalRef(object)) {
    clazz = env->GetObjectClass(object);
}

Object::~Object() {
    env->DeleteGlobalRef(object);
    env = nullptr;
}

std::string Object::ToString(jstring value) {
    auto convertedValue = env->GetStringUTFChars(value, nullptr);
    std::string name = convertedValue;
    env->ReleaseStringUTFChars(value, convertedValue);
    return name;
}

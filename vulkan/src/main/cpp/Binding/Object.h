#ifndef HAISHINKIT_KT_OBJECT_H
#define HAISHINKIT_KT_OBJECT_H

#include <jni.h>
#include <string>

namespace Binding {
    class Object {
    public:
        Object(JNIEnv *env, jobject object);

        ~Object();

    protected:
        JNIEnv *env;
        jobject object;
        jclass clazz;

        std::string ToString(jstring value);
    };
}

#endif //HAISHINKIT_KT_OBJECT_H

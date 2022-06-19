#ifndef HAISHINKIT_KT_CLASS_H
#define HAISHINKIT_KT_CLASS_H

#include "Object.h"

namespace Binding {
    class Class : Object {
    public:
        Class(JNIEnv *env, jobject object);

        std::string ToString();
    };
}

#endif //HAISHINKIT_KT_CLASS_H

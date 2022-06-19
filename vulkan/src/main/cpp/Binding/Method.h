#ifndef HAISHINKIT_KT_METHOD_H
#define HAISHINKIT_KT_METHOD_H

#include "Object.h"
#include "Class.h"

namespace Binding {
    class Method : Object {
    public:
        Method(JNIEnv *env, jobject object);

        ~Method();

        std::string GetName();

        Class *GetReturnType();

    protected:
        Class *returnType = nullptr;
    };
}

#endif //HAISHINKIT_KT_METHOD_H

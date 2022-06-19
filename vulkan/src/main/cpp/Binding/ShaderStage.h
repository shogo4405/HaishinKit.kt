#ifndef HAISHINKIT_KT_SHADERSTAGE_H
#define HAISHINKIT_KT_SHADERSTAGE_H

#include "Object.h"

namespace Binding {
    class ShaderStage : Object {
    public:
        ShaderStage(JNIEnv *env, jobject object);

        int GetOrdinal();
    };
}

#endif //HAISHINKIT_KT_SHADERSTAGE_H

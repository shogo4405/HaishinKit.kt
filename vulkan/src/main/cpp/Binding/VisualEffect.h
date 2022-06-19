#ifndef HAISHINKIT_KT_VISUALEFFECT_H
#define HAISHINKIT_KT_VISUALEFFECT_H

#include "Object.h"
#include "Uniform.h"
#include "Method.h"
#include <string>
#include <vector>

namespace Binding {
    class VisualEffect : Object {
    public:
        VisualEffect(JNIEnv *env, jobject object);

        std::string GetName();

        std::vector<float> GetFloatValues(Method *method);

        std::vector<Uniform *> GetUniforms();

        std::vector<Method *> GetMethods();

    private:
        std::string name;
    };
}

#endif //HAISHINKIT_KT_VISUALEFFECT_H

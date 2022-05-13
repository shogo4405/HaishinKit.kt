#ifndef HAISHINKIT_KT_VIDEOEFFECT_H
#define HAISHINKIT_KT_VIDEOEFFECT_H

#include <string>
#include <jni.h>
#include "Kernel.h"

namespace Graphics {
    class VideoEffect {
    public:
        VideoEffect(JNIEnv *env, jobject object);

        ~VideoEffect();

        std::string GetShaderPath(const std::string &type);

    private:
        std::string name;
        JNIEnv *env;
        jobject object;
        jobject ref;
    };
}
#endif //HAISHINKIT_KT_VIDEOEFFECT_H

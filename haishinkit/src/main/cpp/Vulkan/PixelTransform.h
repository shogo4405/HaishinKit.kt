#ifndef HAISHINKIT_KT_PIXELTRANSFORM_H
#define HAISHINKIT_KT_PIXELTRANSFORM_H

#include <jni.h>
#include "Kernel.h"

namespace Vulkan {

    class PixelTransform {
    public:
        jobject surface = nullptr;

        PixelTransform();

        ~PixelTransform();

        void SetAssetManager(AAssetManager *assetManager);

        void SetNativeWindow(ANativeWindow *nativeWindow);

        ANativeWindow *GetNativeWindow();

        void SetInputNativeWindow(ANativeWindow *inputNativeWindow);

        void UpdateTexture();

    private:
        ANativeWindow *inputNativeWindow;
        ANativeWindow *nativeWindow;
        std::vector<Texture *> textures;
        Kernel *kernel;
    };

}

#endif //HAISHINKIT_KT_PIXELTRANSFORM_H

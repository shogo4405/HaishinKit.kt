#ifndef HAISHINKIT_KT_PIXELTRANSFORM_H
#define HAISHINKIT_KT_PIXELTRANSFORM_H

#include <jni.h>
#include "Kernel.h"

namespace Vulkan {

    class PixelTransform {
    public:
        jobject surface = nullptr;
        jobject inputSurface = nullptr;

        PixelTransform();

        ~PixelTransform();

        void SetVideoGravity(VideoGravity newVideoGravity);

        VideoGravity GetVideoGravity();

        void SetAssetManager(AAssetManager *assetManager);

        void SetNativeWindow(ANativeWindow *nativeWindow);

        void SetInputNativeWindow(ANativeWindow *inputNativeWindow);

        void UpdateTexture();

        std::string InspectDevices();

        bool IsReady();

    private:
        ANativeWindow *inputNativeWindow;
        ANativeWindow *nativeWindow;
        std::vector<Texture *> textures;
        Kernel *kernel;
        VideoGravity videoGravity = VideoGravity::RESIZE_ASPECT;
    };
}

#endif //HAISHINKIT_KT_PIXELTRANSFORM_H

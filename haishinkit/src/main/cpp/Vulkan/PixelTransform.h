#ifndef HAISHINKIT_KT_PIXELTRANSFORM_H
#define HAISHINKIT_KT_PIXELTRANSFORM_H

#include <jni.h>
#include "Kernel.h"
#include "ResampleFilter.h"

namespace Vulkan {

    class PixelTransform {
    public:
        PixelTransform();

        ~PixelTransform();

        void SetUpTexture(int32_t width, int32_t height, int32_t format);

        void SetVideoGravity(VideoGravity newVideoGravity);

        void SetResampleFilter(ResampleFilter newResampleFilter);

        void SetAssetManager(AAssetManager *assetManager);

        void SetNativeWindow(ANativeWindow *nativeWindow);

        void SetInputNativeWindow(ANativeWindow *inputNativeWindow);

        void UpdateTexture(void *data, int32_t stride);

        std::string InspectDevices();

        bool IsReady();

    private:
        ANativeWindow *inputNativeWindow;
        ANativeWindow *nativeWindow;
        std::vector<Texture *> textures;
        Kernel *kernel;
        VideoGravity videoGravity = VideoGravity::RESIZE_ASPECT;
        ResampleFilter resampleFilter = ResampleFilter::CUBIC;
    };
}

#endif //HAISHINKIT_KT_PIXELTRANSFORM_H

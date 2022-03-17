#ifndef HAISHINKIT_KT_PIXELTRANSFORM_H
#define HAISHINKIT_KT_PIXELTRANSFORM_H

#include <jni.h>
#include "Kernel.h"
#include "SurfaceRotation.hpp"
#include "ResampleFilter.h"

namespace Graphics {

    class PixelTransform {
    public:
        PixelTransform();

        ~PixelTransform();

        bool IsReady();

        void SetTexture(int32_t width, int32_t height, int32_t format);

        void SetVideoGravity(VideoGravity newVideoGravity);

        void SetResampleFilter(ResampleFilter newResampleFilter);

        void SetAssetManager(AAssetManager *assetManager);

        void SetNativeWindow(ANativeWindow *nativeWindow);

        void SetImageOrientation(ImageOrientation imageOrientation);

        void SetSurfaceRotation(SurfaceRotation surfaceRotation);

        void UpdateTexture(void *y, void *u, void *v, int32_t yStride, int32_t uvStride,
                           int32_t uvPixelStride);

        std::string InspectDevices();

    private:
        ANativeWindow *nativeWindow;
        std::vector<Texture *> textures;
        Kernel *kernel;
        VideoGravity videoGravity = RESIZE_ASPECT_FILL;
        ResampleFilter resampleFilter = NEAREST;
        ImageOrientation imageOrientation = UP;
    };
}

#endif //HAISHINKIT_KT_PIXELTRANSFORM_H

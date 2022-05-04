#ifndef HAISHINKIT_KT_PIXELTRANSFORM_H
#define HAISHINKIT_KT_PIXELTRANSFORM_H

#include <jni.h>
#include <android/hardware_buffer_jni.h>
#include <media/NdkImageReader.h>
#include "Kernel.h"
#include "SurfaceRotation.hpp"
#include "ResampleFilter.h"
#include "ImageReader.h"

namespace Graphics {

    class PixelTransform {
    public:
        static void OnImageAvailable(void *ctx, AImageReader *reader);

        PixelTransform();

        ~PixelTransform();

        void SetImageExtent(int32_t width, int32_t height);

        void SetImageReader(int32_t width, int32_t height, int32_t format);

        void SetVideoGravity(VideoGravity newVideoGravity);

        void SetResampleFilter(ResampleFilter newResampleFilter);

        void SetAssetManager(AAssetManager *assetManager);

        void SetNativeWindow(ANativeWindow *nativeWindow);

        void SetImageOrientation(ImageOrientation imageOrientation);

        void SetSurfaceRotation(SurfaceRotation surfaceRotation);

        void SetExpectedOrientationSynchronize(bool expectedOrientationSynchronize);

        ANativeWindow *GetInputSurface();

        bool HasFeatures();

        void OnImageAvailable(AImageReader *reader);

        std::string InspectDevices();

    private:
        std::vector<Texture *> textures;
        Kernel *kernel;
        VideoGravity videoGravity = RESIZE_ASPECT_FILL;
        ResampleFilter resampleFilter = LINEAR;
        ImageOrientation imageOrientation = UP;
        ImageReader *imageReader;
    };
}

#endif //HAISHINKIT_KT_PIXELTRANSFORM_H

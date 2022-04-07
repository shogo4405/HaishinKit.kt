#include "../Unmanaged.hpp"
#include "PixelTransform.h"
#include "vulkan/vulkan_android.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/asset_manager_jni.h>
#include <media/NdkImageReader.h>
#include "../haishinkit.hpp"
#include "DynamicLoader.h"

namespace Graphics {
    void PixelTransform::OnImageAvailable(void *ctx, AImageReader *reader) {
        reinterpret_cast<PixelTransform *>(ctx)->OnImageAvailable(reader);
    }

    PixelTransform::PixelTransform() :
            kernel(new Kernel()),
            textures(std::vector<Texture *>(0)),
            nativeWindow(nullptr),
            imageReader(nullptr) {
    }

    PixelTransform::~PixelTransform() {
        delete kernel;
        if (nativeWindow != nullptr) {
            ANativeWindow_release(nativeWindow);
        }
    }

    void PixelTransform::SetVideoGravity(VideoGravity newVideoGravity) {
        videoGravity = newVideoGravity;
        for (auto &texture: textures) {
            texture->videoGravity = newVideoGravity;
        }
    }

    void PixelTransform::SetImageOrientation(ImageOrientation newImageOrientation) {
        imageOrientation = newImageOrientation;
        for (auto &texture : textures) {
            texture->SetImageOrientation(newImageOrientation);
        }
    }

    void PixelTransform::SetResampleFilter(ResampleFilter newResampleFilter) {
        resampleFilter = newResampleFilter;
        for (auto &texture : textures) {
            texture->resampleFilter = newResampleFilter;
        }
    }

    void PixelTransform::SetImageReader(int32_t width, int32_t height, int32_t format) {
        auto texture = new Texture(vk::Extent2D(width, height), format);
        texture->videoGravity = videoGravity;
        texture->resampleFilter = resampleFilter;
        texture->SetImageOrientation(imageOrientation);
        textures.clear();
        textures.push_back(texture);

        AImageReader_newWithUsage(
                width,
                height,
                format,
                AHARDWAREBUFFER_USAGE_GPU_SAMPLED_IMAGE |
                AHARDWAREBUFFER_USAGE_CPU_READ_RARELY,
                2,
                &imageReader);

        AImageReader_ImageListener listener{
                .context = this,
                .onImageAvailable = OnImageAvailable
        };

        AImageReader_setImageListener(imageReader, &listener);
    }

    void PixelTransform::SetSurfaceRotation(SurfaceRotation surfaceRotation) {
        kernel->SetSurfaceRotation(surfaceRotation);
    }

    void PixelTransform::SetAssetManager(AAssetManager *assetManager) {
        kernel->SetAssetManager(assetManager);
    }

    void PixelTransform::SetNativeWindow(ANativeWindow *newNativeWindow) {
        ANativeWindow *oldNativeWindow = nativeWindow;
        nativeWindow = nullptr;
        if (oldNativeWindow != nullptr && newNativeWindow == nullptr) {
            kernel->TearDown();
        } else {
            if (oldNativeWindow != nullptr && oldNativeWindow != newNativeWindow) {
                kernel->TearDown();
            }
            kernel->SetUp(newNativeWindow);
        }
        if (oldNativeWindow != nullptr) {
            ANativeWindow_release(oldNativeWindow);
        }
        nativeWindow = newNativeWindow;
    }

    void PixelTransform::ReadPixels(void *byteBuffer) {
        if (!IsReady()) {
            return;
        }
        kernel->ReadPixels(byteBuffer);
    }

    bool PixelTransform::IsReady() {
        return kernel->IsAvailable() && nativeWindow != nullptr;
    }

    std::string PixelTransform::InspectDevices() {
        return kernel->InspectDevices();
    }

    void PixelTransform::OnImageAvailable(AImageReader *reader) {
        if (!IsReady()) {
            return;
        }
        AImage *image;
        media_status_t status = AImageReader_acquireLatestImage(reader, &image);
        if (status != AMEDIA_OK) {
            return;
        }
        AHardwareBuffer *buffer;
        status = AImage_getHardwareBuffer(image, &buffer);
        if (status != AMEDIA_OK) {
            AImage_delete(image);
            return;
        }
        const auto &texture = textures[0];
        texture->SetUp(*kernel, buffer);
        kernel->DrawFrame([=](uint32_t index) {
            texture->UpdateAt(*kernel, index, buffer);
            texture->Layout(*kernel);
        });
        AImage_delete(image);
    }
}

extern "C"
{
JNIEXPORT jboolean JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_00024Companion_isSupported(JNIEnv *env,
                                                                       jobject thiz) {
    return Graphics::DynamicLoader::GetInstance().Load();
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeSetVideoGravity(JNIEnv *env, jobject thiz,
                                                                  jint value) {
    Unmanaged<Graphics::PixelTransform>::fromOpaque(env,
                                                    thiz)->takeRetainedValue()->SetVideoGravity(
            static_cast<Graphics::VideoGravity>(value));
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeSetSurface(JNIEnv *env, jobject thiz,
                                                             jobject surface) {
    ANativeWindow *window = nullptr;
    if (surface != nullptr) {
        window = ANativeWindow_fromSurface(env, surface);
    }
    Unmanaged<Graphics::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Graphics::PixelTransform *self) {
                self->SetNativeWindow(window);
            });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeSetImageOrientation(JNIEnv *env, jobject thiz,
                                                                      jint value) {
    Unmanaged<Graphics::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Graphics::PixelTransform *self) {
                self->SetImageOrientation(static_cast<Graphics::ImageOrientation>(value));
            });
}

JNIEXPORT jobject JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeCreateInputSurface(JNIEnv *env, jobject thiz,
                                                                     jint width,
                                                                     jint height, jint format) {
    ANativeWindow *window;
    Unmanaged<Graphics::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Graphics::PixelTransform *self) {
                self->SetImageReader(width, height, format);
            });
    AImageReader_getWindow(Unmanaged<Graphics::PixelTransform>::fromOpaque(env,
                                                                           thiz)->takeRetainedValue()->imageReader,
                           &window);
    return ANativeWindow_toSurface(env, window);
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeSetSurfaceRotation(JNIEnv *env, jobject thiz,
                                                                     jint value) {
    Unmanaged<Graphics::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Graphics::PixelTransform *self) {
                self->SetSurfaceRotation(static_cast<Graphics::SurfaceRotation>(value));
            });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeSetResampleFilter(JNIEnv *env, jobject thiz,
                                                                    jint value) {
    Unmanaged<Graphics::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Graphics::PixelTransform *self) {
                self->SetResampleFilter(static_cast<Graphics::ResampleFilter>(value));
            });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeSetAssetManager(JNIEnv *env, jobject thiz,
                                                                  jobject asset_manager) {
    AAssetManager *manager = AAssetManager_fromJava(env, asset_manager);
    Unmanaged<Graphics::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Graphics::PixelTransform *self) {
                self->SetAssetManager(manager);
            });
}

JNIEXPORT jstring JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_inspectDevices(JNIEnv *env, jobject thiz) {
    std::string string = Unmanaged<Graphics::PixelTransform>::fromOpaque(env,
                                                                         thiz)->takeRetainedValue()->InspectDevices();
    return env->NewStringUTF(string.c_str());
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeReadPixels(JNIEnv *env, jobject thiz,
                                                             jobject byteBuffer) {
    void *directBuffer = env->GetDirectBufferAddress(byteBuffer);
    if (directBuffer != nullptr) {
        Unmanaged<Graphics::PixelTransform>::fromOpaque(env, thiz)->safe(
                [=](Graphics::PixelTransform *self) {
                    self->ReadPixels(directBuffer);
                });
    }
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeDispose(JNIEnv *env, jobject thiz) {
    Unmanaged<Graphics::PixelTransform>::fromOpaque(env, thiz)->release();
}
}
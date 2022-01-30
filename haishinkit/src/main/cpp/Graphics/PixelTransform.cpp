#include "../Unmanaged.hpp"
#include "PixelTransform.h"
#include "vulkan/vulkan_android.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/asset_manager_jni.h>
#include "../haishinkit.hpp"
#include "DynamicLoader.h"

namespace Graphics {

    PixelTransform::PixelTransform() :
            kernel(new Kernel()),
            textures(std::vector<Texture *>(0)),
            inputNativeWindow(nullptr),
            nativeWindow(nullptr) {
    }

    PixelTransform::~PixelTransform() {
        delete kernel;
        if (inputNativeWindow != nullptr) {
            ANativeWindow_release(inputNativeWindow);
        }
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

    void PixelTransform::SetResampleFilter(ResampleFilter newResampleFilter) {
        resampleFilter = newResampleFilter;
        for (auto &texture : textures) {
            texture->resampleFilter = newResampleFilter;
        }
    }

    void PixelTransform::SetUpTexture(int32_t width, int32_t height, int32_t format) {
        auto texture = new Texture(vk::Extent2D(width, height), format);
        texture->videoGravity = videoGravity;
        texture->resampleFilter = resampleFilter;
        textures.clear();
        textures.push_back(texture);
        kernel->SetTextures(textures);
    }

    void PixelTransform::SetAssetManager(AAssetManager *assetManager) {
        kernel->SetAssetManager(assetManager);
    }

    void PixelTransform::SetInputNativeWindow(ANativeWindow *newInputNativeWindow) {
        if (newInputNativeWindow == nullptr) {
            for (auto &texture: textures) {
                texture->TearDown(*kernel);
            }
        } else {
            if (inputNativeWindow != newInputNativeWindow) {
                for (auto &texture: textures) {
                    texture->TearDown(*kernel);
                }
            }
            const auto width = ANativeWindow_getWidth(newInputNativeWindow);
            const auto height = ANativeWindow_getHeight(newInputNativeWindow);
            const auto format = ANativeWindow_getFormat(newInputNativeWindow);
            auto texture = new Texture(vk::Extent2D(width, height), format);
            texture->videoGravity = videoGravity;
            textures.clear();
            textures.push_back(texture);
            kernel->SetTextures(textures);
        }
        if (inputNativeWindow != nullptr) {
            ANativeWindow_release(inputNativeWindow);
        }
        inputNativeWindow = newInputNativeWindow;
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
            if (!textures.empty()) {
                kernel->SetTextures(textures);
            }
        }
        if (oldNativeWindow != nullptr) {
            ANativeWindow_release(oldNativeWindow);
        }
        nativeWindow = newNativeWindow;
    }

    void PixelTransform::UpdateTexture(void *y, void *u, void *v, int32_t yStride, int32_t uvStride,
                                       int32_t uvPixelStride) {
        if (!IsReady()) {
            return;
        }
        if (y == nullptr) {
            ANativeWindow_acquire(inputNativeWindow);
            ANativeWindow_Buffer buffer;
            int result = ANativeWindow_lock(inputNativeWindow, &buffer, nullptr);
            if (result == 0) {
                ANativeWindow_unlockAndPost(inputNativeWindow);
                textures[0]->Update(*kernel, buffer.bits, nullptr, nullptr, buffer.stride, 0, 0);
                kernel->DrawFrame();
                ANativeWindow_release(inputNativeWindow);
            } else {
                ANativeWindow_release(inputNativeWindow);
            }
        } else {
            textures[0]->Update(*kernel, y, u, v, yStride, uvStride, uvPixelStride);
            kernel->DrawFrame();
        }
    }

    bool PixelTransform::IsReady() {
        return kernel->IsAvailable() && nativeWindow != nullptr;
    }

    std::string PixelTransform::InspectDevices() {
        return kernel->InspectDevices();
    }
}

extern "C"
{
JNIEXPORT jboolean JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_00024Companion_isSupported(JNIEnv *env,
                                                                         jobject thiz) {
    return Graphics::DynamicLoader::GetInstance().Load();
}

JNIEXPORT void JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_nativeSetVideoGravity(JNIEnv *env, jobject thiz,
                                                                    jint value) {
    Unmanaged<Graphics::PixelTransform>::fromOpaque(env, thiz)->takeRetainedValue()->SetVideoGravity(
            static_cast<Graphics::VideoGravity>(value));
}

JNIEXPORT void JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_nativeSetSurface(JNIEnv *env, jobject thiz,
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
Java_com_haishinkit_graphics_VkPixelTransform_setTexture(JNIEnv *env, jobject thiz, jint width,
                                                         jint height, jint format) {
    Unmanaged<Graphics::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Graphics::PixelTransform *self) {
                self->SetUpTexture(width, height, format);
            });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_nativeSetInputSurface(JNIEnv *env, jobject thiz,
                                                                    jobject surface) {
    ANativeWindow *window = nullptr;
    if (surface != nullptr) {
        window = ANativeWindow_fromSurface(env, surface);
    }
    Unmanaged<Graphics::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Graphics::PixelTransform *self) {
                self->SetInputNativeWindow(window);
            });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_nativeSetResampleFilter(JNIEnv *env, jobject thiz,
                                                                      jint value) {
    Unmanaged<Graphics::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Graphics::PixelTransform *self) {
                self->SetResampleFilter(static_cast<Graphics::ResampleFilter>(value));
            });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_nativeSetAssetManager(JNIEnv *env, jobject thiz,
                                                                    jobject asset_manager) {
    AAssetManager *manager = AAssetManager_fromJava(env, asset_manager);
    Unmanaged<Graphics::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Graphics::PixelTransform *self) {
                self->SetAssetManager(manager);
            });
}

JNIEXPORT jstring JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_inspectDevices(JNIEnv *env, jobject thiz) {
    std::string string = Unmanaged<Graphics::PixelTransform>::fromOpaque(env,
                                                                       thiz)->takeRetainedValue()->InspectDevices();
    return env->NewStringUTF(string.c_str());
}

JNIEXPORT void JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_updateTexture(JNIEnv *env, jobject thiz,
                                                            jobject buffer0,
                                                            jobject buffer1,
                                                            jobject buffer2,
                                                            jint stride1,
                                                            jint stride2,
                                                            jint pixelStride) {
    Unmanaged<Graphics::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Graphics::PixelTransform *self) {
                if (buffer0 == nullptr) {
                    self->UpdateTexture(nullptr, nullptr, nullptr, 0, 0, 0);
                } else {
                    void *bufferAddress0 = env->GetDirectBufferAddress(buffer0);
                    void *bufferAddress1 = nullptr;
                    if (buffer1 != nullptr) {
                        bufferAddress1 = env->GetDirectBufferAddress(buffer1);
                    }
                    void *bufferAddress2 = nullptr;
                    if (buffer2 != nullptr) {
                        bufferAddress2 = env->GetDirectBufferAddress(buffer2);
                    }
                    self->UpdateTexture(bufferAddress0, bufferAddress1, bufferAddress2, stride1,
                                        stride2, pixelStride);
                }
            });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_dispose(JNIEnv *env, jobject thiz) {
    Unmanaged<Graphics::PixelTransform>::fromOpaque(env, thiz)->release();
}
}
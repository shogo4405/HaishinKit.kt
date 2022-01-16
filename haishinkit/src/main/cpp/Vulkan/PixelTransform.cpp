#include "../Unmanaged.hpp"
#include "PixelTransform.h"
#include "vulkan/vulkan_android.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/asset_manager_jni.h>
#include "../haishinkit.hpp"
#include "DynamicLoader.h"

namespace Vulkan {

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
        if (!textures.empty()) {
            textures[0]->videoGravity = newVideoGravity;
        }
    }

    void PixelTransform::SetUpTexture(int32_t width, int32_t height, int32_t format) {
        auto texture = new Texture(vk::Extent2D(width, height), Texture::GetFormat(format));
        texture->videoGravity = videoGravity;
        textures.clear();
        textures.push_back(texture);
        kernel->SetTextures(textures);
    }

    VideoGravity PixelTransform::GetVideoGravity() {
        return videoGravity;
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
            auto texture = new Texture(vk::Extent2D(width, height), Texture::GetFormat(format));
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

    void PixelTransform::UpdateTexture(void *data, int32_t stride) {
        if (!IsReady()) {
            return;
        }
        if (data == nullptr) {
            ANativeWindow_acquire(inputNativeWindow);
            ANativeWindow_Buffer buffer;
            int result = ANativeWindow_lock(inputNativeWindow, &buffer, nullptr);
            if (result == 0) {
                ANativeWindow_unlockAndPost(inputNativeWindow);
                textures[0]->Update(*kernel, buffer.bits, buffer.stride);
                kernel->DrawFrame();
                ANativeWindow_release(inputNativeWindow);
            } else {
                ANativeWindow_release(inputNativeWindow);
            }
        } else {
            textures[0]->Update(*kernel, data, stride);
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
    return Vulkan::DynamicLoader::GetInstance().Load();
}

JNIEXPORT void JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_nativeSetVideoGravity(JNIEnv *env, jobject thiz,
                                                                    jint value) {
    Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->takeRetainedValue()->SetVideoGravity(
            static_cast<Vulkan::VideoGravity>(value));
}


JNIEXPORT jobject JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_getSurface(JNIEnv *env, jobject thiz) {
    return Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->takeRetainedValue()->surface;
}

JNIEXPORT void JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_setSurface(JNIEnv *env, jobject thiz,
                                                         jobject surface) {
    ANativeWindow *window = nullptr;
    if (surface != nullptr) {
        window = ANativeWindow_fromSurface(env, surface);
    }
    Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Vulkan::PixelTransform *self) {
                self->surface = surface;
                self->SetNativeWindow(window);
            });
}

JNIEXPORT jobject JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_getInputSurface(JNIEnv *env, jobject thiz) {
    return Unmanaged<Vulkan::PixelTransform>::fromOpaque(env,
                                                         thiz)->takeRetainedValue()->inputSurface;
}

JNIEXPORT void JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_setTexture(JNIEnv *env, jobject thiz, jint width,
                                                         jint height, jint format) {
    Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Vulkan::PixelTransform *self) {
                self->SetUpTexture(width, height, format);
            });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_setInputSurface(JNIEnv *env, jobject thiz,
                                                              jobject surface) {
    ANativeWindow *window = nullptr;
    if (surface != nullptr) {
        window = ANativeWindow_fromSurface(env, surface);
    }
    Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Vulkan::PixelTransform *self) {
                self->inputSurface = surface;
                self->SetInputNativeWindow(window);
            });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_nativeSetResampleFilter(JNIEnv *env, jobject thiz,
                                                                      jint resampleFilter) {
}

JNIEXPORT void JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_nativeSetAssetManager(JNIEnv *env, jobject thiz,
                                                                    jobject asset_manager) {
    AAssetManager *manager = AAssetManager_fromJava(env, asset_manager);
    Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Vulkan::PixelTransform *self) {
                self->SetAssetManager(manager);
            });
}

JNIEXPORT jstring JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_inspectDevices(JNIEnv *env, jobject thiz) {
    std::string string = Unmanaged<Vulkan::PixelTransform>::fromOpaque(env,
                                                                       thiz)->takeRetainedValue()->InspectDevices();
    return env->NewStringUTF(string.c_str());
}

JNIEXPORT void JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_updateTexture(JNIEnv *env, jobject thiz,
                                                            jobject buffer, jint stride) {
    Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Vulkan::PixelTransform *self) {
                if (buffer == nullptr) {
                    self->UpdateTexture(nullptr, 0);
                } else {
                    void *data = env->GetDirectBufferAddress(buffer);
                    self->UpdateTexture(data, stride);
                }
            });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_graphics_VkPixelTransform_dispose(JNIEnv *env, jobject thiz) {
    Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->release();
}
}

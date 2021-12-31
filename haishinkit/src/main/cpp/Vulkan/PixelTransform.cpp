#include "../Unmanaged.hpp"
#include "PixelTransform.h"
#include "vulkan/vulkan_android.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/asset_manager_jni.h>
#include "../haishinkit.hpp"

namespace Vulkan {
    PixelTransform::PixelTransform() :
            kernel(new Kernel()),
            textures(std::vector<Texture *>(0)),
            inputNativeWindow(nullptr),
            nativeWindow(nullptr) {
    }

    PixelTransform::~PixelTransform() {
        delete kernel;
    }

    void PixelTransform::SetAssetManager(AAssetManager *assetManager) {
        this->kernel->SetAssetManager(assetManager);
    }

    void PixelTransform::SetInputNativeWindow(ANativeWindow *inputNativeWindow) {
        if (inputNativeWindow == nullptr) {
            for (auto &texture: textures) {
                texture->TearDown(*kernel);
            }
        } else {
            if (this->inputNativeWindow != inputNativeWindow) {
                for (auto &texture: textures) {
                    texture->TearDown(*kernel);
                }
            }
            const auto width = ANativeWindow_getWidth(inputNativeWindow);
            const auto height = ANativeWindow_getHeight(inputNativeWindow);
            ANativeWindow_setBuffersGeometry(inputNativeWindow, width,
                                             height, WINDOW_FORMAT_RGBA_8888);
            textures.clear();
            textures.push_back(new Texture(vk::Extent2D(width, height)));
            kernel->SetUp(textures);
        }
        this->inputNativeWindow = inputNativeWindow;
    }

    void PixelTransform::SetNativeWindow(ANativeWindow *nativeWindow) {
        if (nativeWindow == nullptr) {
            kernel->TearDown();
        } else {
            if (GetNativeWindow() != nativeWindow) {
                kernel->TearDown();
            }
            kernel->SetUp(nativeWindow);
            if (!textures.empty()) {
                kernel->SetUp(textures);
            }
        }
        this->nativeWindow = nativeWindow;
    }

    ANativeWindow *PixelTransform::GetNativeWindow() {
        return nativeWindow;
    }

    void PixelTransform::UpdateTexture() {
        if (inputNativeWindow == nullptr || nativeWindow == nullptr) {
            return;
        }
        ANativeWindow_acquire(inputNativeWindow);
        ANativeWindow_Buffer buffer;
        int result = ANativeWindow_lock(inputNativeWindow, &buffer, nullptr);
        if (result == 0) {
            ANativeWindow_unlockAndPost(inputNativeWindow);
            ANativeWindow_release(inputNativeWindow);
            textures[0]->Update(*kernel, &buffer);
            kernel->DrawFrame();
        } else {
            ANativeWindow_release(inputNativeWindow);
        }
    }
}

extern "C"
{
JNIEXPORT jboolean JNICALL
Java_com_haishinkit_vk_VKPixelTransform_00024Companion_isSupported(JNIEnv *env, jobject thiz) {
    return true;
}

JNIEXPORT jobject JNICALL
Java_com_haishinkit_vk_VKPixelTransform_getSurface(JNIEnv *env, jobject thiz) {
    return Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->takeRetainedValue()->surface;
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vk_VKPixelTransform_setSurface(JNIEnv *env, jobject thiz, jobject surface) {
    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Vulkan::PixelTransform *self) {
                self->surface = surface;
                self->SetNativeWindow(window);
            });
}

JNIEXPORT jobject JNICALL
Java_com_haishinkit_vk_VKPixelTransform_getInputSurface(JNIEnv *env, jobject thiz,
                                                        jobject surface) {
    return nullptr;
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vk_VKPixelTransform_setInputSurface(JNIEnv *env, jobject thiz,
                                                        jobject surface) {
    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Vulkan::PixelTransform *self) {
                self->SetInputNativeWindow(window);
            });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vk_VKPixelTransform_setAssetManager(JNIEnv *env, jobject thiz,
                                                        jobject asset_manager) {
    AAssetManager *manager = AAssetManager_fromJava(env, asset_manager);
    Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Vulkan::PixelTransform *self) {
                self->SetAssetManager(manager);
            });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vk_VKPixelTransform_updateTexture(JNIEnv *env, jobject thiz) {
    Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Vulkan::PixelTransform *self) {
                self->UpdateTexture();
            });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vk_VKPixelTransform_dispose(JNIEnv *env, jobject thiz) {
    Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->release();
}
}
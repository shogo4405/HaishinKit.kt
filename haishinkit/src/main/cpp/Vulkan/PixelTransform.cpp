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
        if (inputNativeWindow != nullptr) {
            ANativeWindow_release(nativeWindow);
        }
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
            const auto format = ANativeWindow_getFormat(inputNativeWindow);
            textures.clear();
            textures.push_back(
                    new Texture(vk::Extent2D(width, height), Texture::GetFormat(format)));
            kernel->SetTextures(textures);
        }
        if (this->inputNativeWindow != nullptr) {
            ANativeWindow_release(this->inputNativeWindow);
        }
        this->inputNativeWindow = inputNativeWindow;
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

    void PixelTransform::UpdateTexture() {
        if (IsReady()) {
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

    bool PixelTransform::IsReady() {
        return kernel->IsAvailable() && inputNativeWindow == nullptr || nativeWindow == nullptr;
    }

    std::string PixelTransform::InspectDevices() {
        return kernel->InspectDevices();
    }
}

extern "C"
{
JNIEXPORT jboolean JNICALL
Java_com_haishinkit_vk_VkPixelTransform_00024Companion_isSupported(JNIEnv *env, jobject thiz) {
    return Vulkan::DynamicLoader::GetInstance().Load();
}

JNIEXPORT jobject JNICALL
Java_com_haishinkit_vk_VkPixelTransform_getSurface(JNIEnv *env, jobject thiz) {
    return Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->takeRetainedValue()->surface;
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vk_VkPixelTransform_setSurface(JNIEnv *env, jobject thiz, jobject surface) {
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
Java_com_haishinkit_vk_VkPixelTransform_getInputSurface(JNIEnv *env, jobject thiz) {
    return Unmanaged<Vulkan::PixelTransform>::fromOpaque(env,
                                                         thiz)->takeRetainedValue()->inputSurface;
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vk_VkPixelTransform_setInputSurface(JNIEnv *env, jobject thiz,
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
Java_com_haishinkit_vk_VkPixelTransform_setAssetManager(JNIEnv *env, jobject thiz,
                                                        jobject asset_manager) {
    AAssetManager *manager = AAssetManager_fromJava(env, asset_manager);
    Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Vulkan::PixelTransform *self) {
                self->SetAssetManager(manager);
            });
}

JNIEXPORT jstring JNICALL
Java_com_haishinkit_vk_VkPixelTransform_inspectDevices(JNIEnv *env, jobject thiz) {
    std::string string = Unmanaged<Vulkan::PixelTransform>::fromOpaque(env,
                                                                       thiz)->takeRetainedValue()->InspectDevices();
    return env->NewStringUTF(string.c_str());
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vk_VkPixelTransform_updateTexture(JNIEnv *env, jobject thiz) {
    Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->safe(
            [=](Vulkan::PixelTransform *self) {
                self->UpdateTexture();
            });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vk_VkPixelTransform_dispose(JNIEnv *env, jobject thiz) {
    Unmanaged<Vulkan::PixelTransform>::fromOpaque(env, thiz)->release();
}
}
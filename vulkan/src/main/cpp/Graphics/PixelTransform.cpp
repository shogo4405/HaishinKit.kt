#include "../Unmanaged.hpp"
#include "PixelTransform.h"
#include "vulkan/vulkan_android.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/asset_manager_jni.h>
#include <media/NdkImageReader.h>
#include "../haishinkit.hpp"
#include "DynamicLoader.h"

using namespace Graphics;

void *PixelTransform::OnRunning(void *data) {
    reinterpret_cast<PixelTransform *>(data)->OnRunning();
    return (void *) nullptr;
}

void PixelTransform::OnFrame(long frameTimeNanos, void *data) {
    reinterpret_cast<PixelTransform *>(data)->OnFrame(frameTimeNanos);
}

PixelTransform::PixelTransform() :
        kernel(new Kernel()),
        textures(std::vector<Texture *>(0)),
        imageReader(new ImageReader()),
        fpsController(new FpsController()) {
}

PixelTransform::~PixelTransform() {
    StopRunning();
    delete fpsController;
    delete imageReader;
    delete kernel;
}

void PixelTransform::SetFrameRate(int frameRate) {
    std::lock_guard<std::mutex> lock(mutex);
    fpsController->SetFrameRate(frameRate);
}

void PixelTransform::SetImageExtent(int32_t width, int32_t height) {
    std::lock_guard<std::mutex> lock(mutex);
    kernel->SetImageExtent(width, height);
}

ANativeWindow *PixelTransform::GetInputSurface() {
    return imageReader->GetWindow();
}

void PixelTransform::SetVideoGravity(VideoGravity newVideoGravity) {
    std::lock_guard<std::mutex> lock(mutex);
    videoGravity = newVideoGravity;
    for (auto &texture: textures) {
        texture->SetVideoGravity(newVideoGravity);
    }
}

void PixelTransform::SetImageOrientation(ImageOrientation newImageOrientation) {
    std::lock_guard<std::mutex> lock(mutex);
    imageOrientation = newImageOrientation;
    for (auto &texture: textures) {
        texture->SetImageOrientation(newImageOrientation);
    }
}

void PixelTransform::SetResampleFilter(ResampleFilter newResampleFilter) {
    std::lock_guard<std::mutex> lock(mutex);
    resampleFilter = newResampleFilter;
    for (auto &texture: textures) {
        texture->SetResampleFilter(newResampleFilter);
    }
}

void PixelTransform::SetImageReader(int32_t width, int32_t height, int32_t format) {
    auto texture = new Texture(vk::Extent2D(width, height), format);
    texture->SetVideoGravity(videoGravity);
    texture->SetResampleFilter(resampleFilter);
    texture->SetVideoEffect(videoEffect);
    texture->SetImageOrientation(imageOrientation);
    textures.clear();
    textures.push_back(texture);
    imageReader->SetUp(width, height, format);
    if (kernel->surface) {
        StartRunning();
    }
}

void PixelTransform::SetDeviceOrientation(SurfaceRotation surfaceRotation) {
    std::lock_guard<std::mutex> lock(mutex);
    kernel->SetDeviceOrientation(surfaceRotation);
}

void PixelTransform::SetAssetManager(AAssetManager *assetManager) {
    std::lock_guard<std::mutex> lock(mutex);
    kernel->SetAssetManager(assetManager);
}

void PixelTransform::SetNativeWindow(ANativeWindow *nativeWindow) {
    if (nativeWindow == nullptr) {
        StopRunning();
    }
    kernel->SetNativeWindow(nativeWindow);
    if (nativeWindow != nullptr && !textures.empty()) {
        StartRunning();
    }
}

std::string PixelTransform::InspectDevices() {
    return kernel->InspectDevices();
}

bool PixelTransform::HasFeatures() {
    return kernel->HasFeatures();
}

void PixelTransform::SetRotatesWithContent(bool expectedOrientationSynchronize) {
    std::lock_guard<std::mutex> lock(mutex);
    kernel->SetRotatesWithContent(expectedOrientationSynchronize);
}

void PixelTransform::StartRunning() {
    if (running) {
        return;
    }
    running = true;
    fpsController->Clear();
    auto result = pthread_create(&pthread, nullptr, &OnRunning, this);
    if (result != 0) {
        LOGE("failed to create pthread error no: %d", result);
    }
}

void PixelTransform::StopRunning() {
    if (!running) {
        return;
    }
    running = false;
    pthread_join(pthread, nullptr);
}

void PixelTransform::OnRunning() {
    looper = ALooper_prepare(ALOOPER_PREPARE_ALLOW_NON_CALLBACKS);
    ALooper_acquire(looper);
    choreographer = AChoreographer_getInstance();
    if (choreographer == nullptr) {
        LOGI("failed get an AChoreographer instance.");
        return;
    }
    AChoreographer_postFrameCallback(choreographer, OnFrame, this);
    while (running && ALooper_pollOnce(-1, nullptr, nullptr, nullptr));
    ALooper_release(looper);
    looper = nullptr;
    choreographer = nullptr;
}

void PixelTransform::OnFrame(long frameTimeNanos) {
    mutex.lock();
    if (choreographer) {
        AChoreographer_postFrameCallback(choreographer, OnFrame, this);
    }
    if (fpsController->Advanced(frameTimeNanos)) {
        AHardwareBuffer *buffer = imageReader->GetLatestBuffer();
        if (!kernel->IsAvailable() || buffer == nullptr) {
            mutex.unlock();
            return;
        }
        const auto &texture = textures[0];
        texture->SetUp(*kernel, buffer);
        kernel->DrawFrame([=](uint32_t index) {
            texture->UpdateAt(*kernel, index, buffer);
            texture->LayoutAt(*kernel, index);
        });
    }
    mutex.unlock();
}

void PixelTransform::SetVideoEffect(VideoEffect *newVideoEffect) {
    std::lock_guard<std::mutex> lock(mutex);
    videoEffect = newVideoEffect;
    for (auto &texture: textures) {
        texture->SetVideoEffect(newVideoEffect);
    }
}

void *PixelTransform::ReadPixels() {
    return kernel->ReadPixels();
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
    Unmanaged<PixelTransform>::FromOpaque(env, thiz)->Safe([=](PixelTransform *self) {
        self->SetVideoGravity(static_cast<VideoGravity>(value));
        return nullptr;
    });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeSetSurface(JNIEnv *env, jobject thiz,
                                                             jobject surface) {
    ANativeWindow *window = nullptr;
    if (surface != nullptr) {
        window = ANativeWindow_fromSurface(env, surface);
    }
    Unmanaged<PixelTransform>::FromOpaque(env, thiz)->Safe([=](PixelTransform *self) {
        self->SetNativeWindow(window);
        return nullptr;
    });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeSetImageOrientation(JNIEnv *env, jobject thiz,
                                                                      jint value) {
    Unmanaged<PixelTransform>::FromOpaque(env, thiz)->Safe([=](PixelTransform *self) {
        self->SetImageOrientation(static_cast<ImageOrientation>(value));
        return nullptr;
    });
}

JNIEXPORT jobject JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeCreateInputSurface(JNIEnv *env, jobject thiz,
                                                                     jint width,
                                                                     jint height, jint format) {
    return static_cast<jobject>(Unmanaged<PixelTransform>::FromOpaque(env, thiz)->Safe(
            [=](PixelTransform *self) {
                self->SetImageReader(width, height, format);
                return ANativeWindow_toSurface(env, self->GetInputSurface());
            }));
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeSetDeviceOrientation(JNIEnv *env, jobject thiz,
                                                                       jint value) {
    Unmanaged<PixelTransform>::FromOpaque(env, thiz)->Safe([=](PixelTransform *self) {
        self->SetDeviceOrientation(static_cast<SurfaceRotation>(value));
        return nullptr;
    });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeSetResampleFilter(JNIEnv *env, jobject thiz,
                                                                    jint value) {
    Unmanaged<PixelTransform>::FromOpaque(env, thiz)->Safe([=](PixelTransform *self) {
        self->SetResampleFilter(static_cast<ResampleFilter>(value));
        return nullptr;
    });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeSetAssetManager(JNIEnv *env, jobject thiz,
                                                                  jobject asset_manager) {
    AAssetManager *manager = AAssetManager_fromJava(env, asset_manager);
    Unmanaged<PixelTransform>::FromOpaque(env, thiz)->Safe([=](PixelTransform *self) {
        self->SetAssetManager(manager);
        return nullptr;
    });
}

JNIEXPORT jstring JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_inspectDevices(JNIEnv *env, jobject thiz) {
    return static_cast<jstring>(Unmanaged<PixelTransform>::FromOpaque(env, thiz)->Safe(
            [=](PixelTransform *self) {
                std::string string = self->InspectDevices();
                return env->NewStringUTF(string.c_str());
            }));
}

JNIEXPORT jboolean JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeIsSupported(JNIEnv *env, jobject thiz) {
    if (!DynamicLoader::GetInstance().Load()) {
        return false;
    }
    return Unmanaged<PixelTransform>::FromOpaque(env, thiz)->TakeRetainedValue()->HasFeatures();
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeDispose(JNIEnv *env, jobject thiz) {
    Unmanaged<PixelTransform>::FromOpaque(env, thiz)->Release();
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeSetImageExtent(JNIEnv *env, jobject thiz,
                                                                 jint width,
                                                                 jint height) {
    Unmanaged<PixelTransform>::FromOpaque(env, thiz)->Safe([=](PixelTransform *self) {
        self->SetImageExtent(width, height);
        return nullptr;
    });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeSetRotatesWithContent(JNIEnv *env,
                                                                        jobject thiz,
                                                                        jboolean expectedOrientationSynchronize) {
    Unmanaged<PixelTransform>::FromOpaque(env, thiz)->Safe([=](PixelTransform *self) {
        self->SetRotatesWithContent(expectedOrientationSynchronize);
        return nullptr;
    });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeSetFrameRate(JNIEnv *env, jobject thiz,
                                                               jint frameRate) {
    Unmanaged<PixelTransform>::FromOpaque(env, thiz)->Safe([=](PixelTransform *self) {
        self->SetFrameRate(frameRate);
        return nullptr;
    });
}

JNIEXPORT void JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeSetVideoEffect(JNIEnv *env, jobject thiz,
                                                                 jobject videoEffect) {
    Unmanaged<PixelTransform>::FromOpaque(env, thiz)->Safe([=](PixelTransform *self) {
        self->SetVideoEffect(new VideoEffect(env, videoEffect));
        return nullptr;
    });
}

JNIEXPORT jobject JNICALL
Java_com_haishinkit_vulkan_VkPixelTransform_nativeReadPixels(JNIEnv *env, jobject thiz) {
    Unmanaged<PixelTransform>::FromOpaque(env, thiz)->Safe([=](PixelTransform *self) {
        return self->ReadPixels();
    });
}
}

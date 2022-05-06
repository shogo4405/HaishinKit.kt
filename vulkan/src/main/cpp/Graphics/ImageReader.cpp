#include "ImageReader.h"

using namespace Graphics;

ImageReader::~ImageReader() {
    listener = nullptr;
}

void ImageReader::SetUp(int32_t width, int32_t height, int32_t format) {
    TearDown();

    cursor = 0;
    buffers.resize(maxImages);
    images.resize(maxImages);

    AImageReader_newWithUsage(
            width,
            height,
            format,
            AHARDWAREBUFFER_USAGE_GPU_SAMPLED_IMAGE |
            AHARDWAREBUFFER_USAGE_CPU_READ_RARELY,
            maxImages + 2,
            &reader);

    AImageReader_getWindow(reader, &window);
}

void ImageReader::TearDown() {
    if (reader) {
        AImageReader_delete(reader);
        reader = nullptr;
    }
    window = nullptr;
}

ANativeWindow *ImageReader::GetWindow() {
    return window;
}

AHardwareBuffer *ImageReader::GetLatestBuffer() {
    if (cursor == buffers.size()) {
        cursor = 0;
    }
    AImage *image = nullptr;
    if (AImageReader_acquireLatestImage(reader, &image) != AMEDIA_OK) {
        return nullptr;
    }
    if (images[cursor]) {
        AImage_delete(images[cursor]);
    }
    images[cursor] = image;
    AHardwareBuffer *buffer;
    if (AImage_getHardwareBuffer(image, &buffer) != AMEDIA_OK) {
        return nullptr;
    }
    buffers[cursor] = buffer;
    ++cursor;
    return buffer;
}
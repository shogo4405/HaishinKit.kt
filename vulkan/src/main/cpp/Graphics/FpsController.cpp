#include "FpsController.h"

using namespace Graphics;

FpsController::~FpsController() = default;

void FpsController::SetFrameRate(int newFrameRate) {
    frameRate = newFrameRate;
}

bool FpsController::Advanced(long timestamp) {
    return true;
}

long FpsController::Timestamp(long timestamp) {
    return 0;
}

void FpsController::Clear() {
}

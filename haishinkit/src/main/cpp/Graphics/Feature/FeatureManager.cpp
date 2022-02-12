#include "Feature.h"
#include "FeatureManager.h"

using namespace Graphics;

FeatureManager::FeatureManager() = default;

FeatureManager::~FeatureManager() = default;

std::vector<const char *> FeatureManager::GetExtensions() {
    std::vector<const char *> extensions;
    for (const auto feature : features) {
        extensions.push_back(feature->name);
    }
    return extensions;
}

const void *FeatureManager::GetNext() {
    const void *next = nullptr;
    for (auto &feature : features) {
        feature->Create(&next);
    }
    return next;
}

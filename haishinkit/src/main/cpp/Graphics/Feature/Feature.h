#ifndef HAISHINKIT_KT_FEATURE_H
#define HAISHINKIT_KT_FEATURE_H

#include "../../haishinkit.hpp"

namespace Graphics {
    class Feature {
    public:
        const char *name;

        Feature(const char *name);

        virtual void Create(const void **next);
    };
}

#endif //HAISHINKIT_KT_FEATURE_H

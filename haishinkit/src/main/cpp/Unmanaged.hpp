#ifndef HAISHINKIT_KT_UNMANAGED_HPP
#define HAISHINKIT_KT_UNMANAGED_HPP

#include <jni.h>
#include <memory>
#include <string>
#include <map>

template<typename T>
class Unmanaged {
public:
    static Unmanaged<T> *fromOpaque(JNIEnv *env, jobject object) {
        int64_t key = reinterpret_cast<int64_t>(object);
        if (instances.count(key)) {
            int64_t value = instances[key];
            return reinterpret_cast<Unmanaged<T> *>(value);
        }
        int64_t instance = reinterpret_cast<int64_t>(new Unmanaged<T>(env, object));
        instances.insert(std::make_pair(key, instance));
        return reinterpret_cast<Unmanaged<T> *>(instance);
    }

    explicit Unmanaged(JNIEnv *env, jobject object) : env(env), object(object) {
        fieldID = env->GetFieldID(env->GetObjectClass(object), "memory", "J");
    }

    void retain() {
        jlong address = env->GetLongField(object, fieldID);
        if (address == 0) {
            env->SetLongField(object, fieldID, reinterpret_cast<jlong>(new T()));
        }
    }

    void release() {
        jlong address = env->GetLongField(object, fieldID);
        if (address != 0) {
            env->SetLongField(object, fieldID, reinterpret_cast<jlong>(nullptr));
            delete reinterpret_cast<T *>(address);
            instances.erase(reinterpret_cast<int64_t>(object));
            delete this;
        }
    }

    T *takeRetainedValue() {
        retain();
        return reinterpret_cast<T *>(env->GetLongField(object, fieldID));
    }

    template<typename Lambda>
    void safe(Lambda lambda) {
        retain();
        try {
            lambda(reinterpret_cast<T *>(env->GetLongField(object, fieldID)));
        } catch (std::runtime_error error) {
            jclass clazz = env->FindClass("java/lang/RuntimeException");
            if (clazz == nullptr) {
                return;
            }
            env->ThrowNew(clazz, error.what());
            env->DeleteLocalRef(clazz);
        } catch (std::exception &exception) {
            jclass clazz = env->FindClass("java/lang/Exception");
            if (clazz == nullptr) {
                return;
            }
            env->ThrowNew(clazz, exception.what());
            env->DeleteLocalRef(clazz);
        } catch (...) {
            jclass clazz = env->FindClass("java/lang/Exception");
            if (clazz == nullptr) {
                return;
            }
            env->ThrowNew(clazz, "");
            env->DeleteLocalRef(clazz);
        }
    }

private:
    JNIEnv *env;
    jobject object;
    jfieldID fieldID;

    static std::map<int64_t, int64_t> instances;
};

template<typename T>
std::map<int64_t, int64_t> Unmanaged<T>::instances = std::map<int64_t, int64_t>();

#endif //HAISHINKIT_KT_UNMANAGED_HPP


# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("haishinkit")

add_definitions(-DVULKAN_HPP_DISPATCH_LOADER_DYNAMIC)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library(
        # Sets the name of the library.
        haishinkit
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        haishinkit.hpp
        Graphics/Kernel.cpp
        Graphics/ColorSpace.cpp
        Graphics/ImageStorage.cpp
        Graphics/DynamicLoader.cpp
        Graphics/CommandBuffer.cpp
        Graphics/Queue.cpp
        Graphics/PixelTransform.cpp
        Graphics/SwapChain.cpp
        Graphics/Pipeline.cpp
        Graphics/Texture.cpp
        Graphics/Util.cpp
        Graphics/Vertex.hpp
        Graphics/PushConstants.hpp
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library(
        # Sets the name of the path variable.
        log-lib
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log
)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries(
        # Specifies the target library.
        haishinkit
        # Links the target library to the log library
        # included in the NDK.
        -landroid
        ${log-lib}
)

include_directories(
        ${PROJECT_SOURCE_DIR}/../../../vendor
        ${PROJECT_SOURCE_DIR}/../../../vendor/Vulkan-Headers/include
        ${PROJECT_SOURCE_DIR}/../../../vendor/glm
)

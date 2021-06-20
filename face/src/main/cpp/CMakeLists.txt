cmake_minimum_required(VERSION 3.10.2)

project("face")

set(ncnn_DIR ${CMAKE_SOURCE_DIR}/ncnn-20201218-android-vulkan/${ANDROID_ABI}/lib/cmake/ncnn)
set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/opencv-mobile-4.5.1-android/sdk/native/jni)

find_package(ncnn REQUIRED)
find_package(OpenCV REQUIRED)

include_directories(${OpenCV_INCLUDE_DIRS})

add_library(
        native-lib
        SHARED
        DetectFace.cpp
        DetectFaceDB.cpp
        InterfaceArcFace.cpp
        InterfaceArcFaceDB.cpp
        retinaface/RetinaFace.cpp
        arcface/ArcFace.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        native-lib
        ${log-lib}
        ncnn
        ${OpenCV_LIBS}
        jnigraphics
)
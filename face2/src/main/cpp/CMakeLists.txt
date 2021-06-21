cmake_minimum_required(VERSION 3.4.1)

project("learnc01")

set(ncnn_DIR ${CMAKE_SOURCE_DIR}/ncnn-20201218-android-vulkan/${ANDROID_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)

##设置OpenCV-android-sdk路径
#set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/OpenCV-android-sdk/sdk/native/jni)
#find_package(OpenCV REQUIRED )
#if(OpenCV_FOUND)
#    include_directories(${OpenCV_INCLUDE_DIRS})
#    message(STATUS "OpenCV library status:")
#    message(STATUS "    version: ${OpenCV_VERSION}")
#    message(STATUS "    libraries: ${OpenCV_LIBS}")
#    message(STATUS "    include path: ${OpenCV_INCLUDE_DIRS}")
#else(OpenCV_FOUND)
#    message(FATAL_ERROR "OpenCV library not found")
#endif(OpenCV_FOUND)


add_library(
        native-lib
        SHARED
#        MyTest.cpp
        InterfaceArcFace.cpp
        InterfaceRetinaFace.cpp
        arcface/ArcFace.cpp
        retinaface/RetinaFace.cpp
)



find_library(
        log-lib
        log
)

target_link_libraries(
        native-lib
        ncnn
        ${log-lib}
        vulkan
#        ${OpenCV_LIBS}
        jnigraphics
)
#target_link_libraries(
#        native-lib2
#)

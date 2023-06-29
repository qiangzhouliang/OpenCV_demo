
cmake_minimum_required(VERSION 3.22.1)


project("opencv_demo")
# 导入头文件
include_directories(${CMAKE_SOURCE_DIR}/../jniLibs/include)
# 批量导入c文件
file(GLOB openCV ${CMAKE_SOURCE_DIR}/../jniLibs/*.hpp)

add_library( # Sets the name of the library.
        opencv_demo

        SHARED
        ${openCV}
        native-lib.cpp)

# 设置一个环境变量 目录为 armeabi-v7a
set(my_lib_path ${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI})
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${my_lib_path}")


find_library( # Sets the name of the path variable.
        log-lib

        log)


target_link_libraries( # Specifies the target library.
        opencv_demo

        # Links the target library to the log library
        opencv_java3
        # included in the NDK.
        #加入该依赖库 解决 undefined reference to 'AndroidBitmap_getInfo'
        jnigraphics
        ${log-lib})
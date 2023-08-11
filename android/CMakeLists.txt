cmake_minimum_required(VERSION 3.4.1)

add_compile_options(-fvisibility=hidden -fvisibility-inlines-hidden)

# Library name, also .so file name
set(LIBRARY_NAME abauth)

# CPP
set(CPP_DIR src/main/cpp)

# Header
include_directories(${CPP_DIR}/include)

# AES
set(AES_DIR ${CPP_DIR}/aes)
include_directories(${AES_DIR})

# Target
add_library( ${LIBRARY_NAME}
             SHARED
             ${CPP_DIR}/abauth.cpp
             ${AES_DIR}/aes.c)


# Link
target_link_libraries( ${LIBRARY_NAME}
                       log )

cmake_minimum_required(VERSION 3.22.1)
project(animegen_llama)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

option(ANIMEGEN_ENABLE_LLAMA_CPP "Enable llama.cpp inference backend" OFF)

add_library(
    animegen_llama
    SHARED
    animegen_llama_jni.cpp
)

find_library(log-lib log)

target_link_libraries(
    animegen_llama
    ${log-lib}
)

if (ANIMEGEN_ENABLE_LLAMA_CPP)
    # Expected local source path:
    # app/src/main/cpp/third_party/llama.cpp
    set(LLAMA_CPP_DIR ${CMAKE_CURRENT_SOURCE_DIR}/third_party/llama.cpp)

    if (EXISTS ${LLAMA_CPP_DIR}/include/llama.h)
        message(STATUS "animegen_llama: enabling llama.cpp at ${LLAMA_CPP_DIR}")
        target_compile_definitions(animegen_llama PRIVATE ANIMEGEN_ENABLE_LLAMA_CPP=1)
        target_include_directories(animegen_llama PRIVATE ${LLAMA_CPP_DIR}/include)
        target_sources(
            animegen_llama
            PRIVATE
            ${LLAMA_CPP_DIR}/src/llama.cpp
            ${LLAMA_CPP_DIR}/src/unicode.cpp
            ${LLAMA_CPP_DIR}/src/unicode-data.cpp
            ${LLAMA_CPP_DIR}/ggml/src/ggml.c
            ${LLAMA_CPP_DIR}/ggml/src/ggml-alloc.c
            ${LLAMA_CPP_DIR}/ggml/src/ggml-backend.cpp
            ${LLAMA_CPP_DIR}/ggml/src/ggml-backend-reg.cpp
            ${LLAMA_CPP_DIR}/ggml/src/ggml-quants.c
            ${LLAMA_CPP_DIR}/ggml/src/ggml-threading.cpp
            ${LLAMA_CPP_DIR}/ggml/src/ggml-cpu/ggml-cpu.c
            ${LLAMA_CPP_DIR}/ggml/src/ggml-cpu/ggml-cpu.cpp
            ${LLAMA_CPP_DIR}/ggml/src/ggml-cpu/ggml-cpu-quants.c
        )
        target_include_directories(
            animegen_llama
            PRIVATE
            ${LLAMA_CPP_DIR}
            ${LLAMA_CPP_DIR}/ggml/include
            ${LLAMA_CPP_DIR}/ggml/src
            ${LLAMA_CPP_DIR}/ggml/src/ggml-cpu
        )
    else()
        message(WARNING "animegen_llama: ANIMEGEN_ENABLE_LLAMA_CPP=ON but llama.cpp source not found, fallback stub will be used")
    endif()
endif()

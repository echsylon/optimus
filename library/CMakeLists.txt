# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             optimus-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.

             # Optimus source code
             src/main/cpp/optimus.c

             # JQ source code
             src/main/cpp/jq/builtin.c
             src/main/cpp/jq/builtin.h
             src/main/cpp/jq/builtin.inc
             src/main/cpp/jq/builtin.jq
             src/main/cpp/jq/bytecode.c
             src/main/cpp/jq/bytecode.h
             src/main/cpp/jq/compile.c
             src/main/cpp/jq/compile.h
             src/main/cpp/jq/execute.c
             src/main/cpp/jq/exec_stack.h
             src/main/cpp/jq/inject_errors.c
             src/main/cpp/jq/jq.h
             src/main/cpp/jq/jq_parser.h
             src/main/cpp/jq/jv.c
             src/main/cpp/jq/jv.h
             src/main/cpp/jq/jv_alloc.c
             src/main/cpp/jq/jv_alloc.h
             src/main/cpp/jq/jv_aux.c
             src/main/cpp/jq/jv_dtoa.c
             src/main/cpp/jq/jv_dtoa.h
             src/main/cpp/jq/jv_file.c
             src/main/cpp/jq/jv_parse.c
             src/main/cpp/jq/jv_print.c
             src/main/cpp/jq/jv_unicode.c
             src/main/cpp/jq/jv_unicode.h
             src/main/cpp/jq/jv_utf8_tables.h
             src/main/cpp/jq/lexer.c
             src/main/cpp/jq/lexer.h
             src/main/cpp/jq/lexer.l
             src/main/cpp/jq/libm.h
             src/main/cpp/jq/linker.c
             src/main/cpp/jq/linker.h
             src/main/cpp/jq/locfile.c
             src/main/cpp/jq/locfile.h
             src/main/cpp/jq/opcode_list.h
             src/main/cpp/jq/parser.c
             src/main/cpp/jq/parser.h
             src/main/cpp/jq/parser.y
             src/main/cpp/jq/util.c
             src/main/cpp/jq/util.h
             src/main/cpp/jq/version.h )

include_directories( # Specifies a path to native header files.
                     src/main/cpp/
                     src/main/cpp/jq/ )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       optimus-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )

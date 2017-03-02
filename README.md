# Optimus - A JQ subset wrapper for Android

## Rationale
The main focus of this project is to proove that data structure transformation can be done in the earliest possible stage on the client side as well. Ideally this service should be provided by the server.

The typical issue solved is when the server returns JSON data structured in an inconvenient way from a client business model perspective. Rather than enforcing the server structures on the client model, one can now re-model the JSON response on the client before delivering it to the DTO parser.

## Choice of underlying technology
This project demonstrates an example implementation of a Java wrapper around the native [JQ C-library](https://github.com/stedolan/jq). The Java layer exposes a narrow feature set of the full terminal invokation syntaxt supported by JQ, which translates into exactly `jq -f {jq-filter-file} {input-JSON-file} > {output-JSON-file}` terminal command.

## How IPC overhead is handled
JNI and the IPC-protocol (on Android?) isn't optimized for large data transfers between the C and the Java runtimes. One of the main issues addressed by this example is to operate on - given the context - very large input data and filter out any undesired parts, involving large data transfers. This is solved by internally caching the input/output data in text files and passing around the absolute paths to these. It also fits well with how JQ preferrs to operate.

## Short note on the Android project structure

The inplementation is found in the `library` module (found in the folder with the same name). It contains the JQ native source code (`library/src/main/cpp/jq/`), a JNI native C interface (`library/src/main/cpp/`) and the corresponding Java interface (`library/src/main/java/`).

The `app` module contains a basic example app where you can choose your input data files and have the `library` module process it for you.

## Android Native Development Kit
Also, you'll need to have the NDK downloaded and properly set up in order to run the project "from code". The [official documentation explains how this is done](https://developer.android.com/studio/projects/add-native-code.html)

## Changes made to the JQ source code
Good to know is that there is a ugly quick-fix to handle JSON Number parsing properly. The original implementation doesn't seem to work properly on Android (the fix is docummented and found in the `library/src/main/cpp/jq/jv_print.c` file).

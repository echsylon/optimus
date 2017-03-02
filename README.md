# Optimus - A JQ subset wrapper for Android

## Rationale
The main focus of this project is to prove that data structure transformation can be done on the client side as well. Ideally this service should be provided by the server, though.

## Example
The typical issue solved is when the server returns JSON data, structured in an inconvenient way from a client model perspective. Rather than enforcing the server structure on the client model, one can now re-model the JSON response on the client before delivering it to the DTO parser.

## Choice of underlying technology
This project demonstrates an example implementation of a Java wrapper around the native [JQ C library](https://github.com/stedolan/jq). The example exposes a sub-set of the JQ command line API: `jq -f {jq-filter-file} {input-JSON-file} > {output-JSON-file}`.

## How IPC overhead is handled
JNI and the IPC-protocol on Android isn't optimized for large data transfers between the C and the Java runtimes. One of the main issues addressed by this example is to operate on - given the context - very large input data. This conflict is resolved by internally caching the input/output data in text files and passing around the absolute paths to these. It also fits well with how JQ preferrs to operate.

## Short note on the Android project structure
The inplementation is found in the `library` module. It contains the JQ native source code (`library/src/main/cpp/jq/`), a JNI native C interface (`library/src/main/cpp/`) and the corresponding Java interface (`library/src/main/java/`).

The `app` module contains a simple example app where you can choose your input data files and have the `library` module process it for you.

## The Android Native Development Kit (NDK)
You'll need to have the NDK downloaded and properly set up in order to run the project "from code". The [official documentation explains how this is done](https://developer.android.com/studio/projects/add-native-code.html). Don't worry it's easy.

## Changes made to the JQ source code
Also good to know is that there is an ugly quick-fix to have JQ handle JSON Number parsing properly. The original implementation doesn't seem to work at all on Android. The fix isn't graceful at all, but it prevents JQ from crashing at least. Fix done + commented in `library/src/main/cpp/jq/jv_print.c`. The topic is [touched upon also in the JQ community](https://github.com/stedolan/jq/issues/529).

#include <jni.h>
#include <string.h>
#include <libgen.h>
#include <jq/util.h>
#include "jq.h"

#define  LOG_TAG    "OPTIMUS_NATIVE"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)


/*
 * Does some magic with a directory path.
 *
 * This is a full copy/paste from the main.cpp file.
 */
static const char *skip_shebang(const char *p) {
    if (strncmp(p, "#!", sizeof("#!") - 1) != 0)
        return p;
    const char *n = strchr(p, '\n');
    if (n == NULL || n[1] != '#')
        return p;
    n = strchr(n + 1, '\n');
    if (n == NULL || n[1] == '#' || n[1] == '\0' || n[-1] != '\\' ||
        n[-2] == '\\')
        return p;
    n = strchr(n + 1, '\n');
    if (n == NULL)
        return p;
    return n + 1;
}


/*
 * Processes the input JSON structure against the JQ template.
 *
 * This is a stripped down version of the real version found
 * in main.cpp file.
 */
static int process(jq_state *jq, jv value, FILE *out, int flags, int opts) {
    int status = 14;
    jv_kind kind;
    jv chunk;

    jq_start(jq, value, flags);
    while (jv_is_valid(chunk = jq_next(jq))) {
        kind = jv_get_kind(chunk);
        status = (kind == JV_KIND_FALSE || kind == JV_KIND_NULL) ? 11 : 0;
        jv_dumpf(chunk, out, opts);
        priv_fwrite("\n", 1, out, 0);
    }

    if (jv_invalid_has_msg(jv_copy(chunk)))
        status = 5;

    jv_free(chunk);
    return status;
}


/*
 * Exposes a stripped down subset of the JQ API that corresponds to:
 *
 * jq -f [pattern_file_path] [input_json_path] > [output_file_path]
 */
JNIEXPORT jint JNICALL Java_com_echsylon_optimus_Optimus_transformJson(
        JNIEnv *env, jobject obj, jstring dataFile, jstring patternFile,
        jstring resultFile) {

    const char *pattern = NULL;
    const char *output = NULL;
    const char *input = NULL;
    const char *err = NULL;
    int dumpopts = JV_PRINT_INDENT_FLAGS(2);
    int status = 0;


    // Initialize a JQ instance
    jq_state *jq = jq_init();;
    if (jq == NULL) {
        err = "couldn't init jq\0";
        goto end;
    }


    // C-ify the JNI input
    pattern = (*env)->GetStringUTFChars(env, patternFile, NULL);
    output = (*env)->GetStringUTFChars(env, resultFile, NULL);
    input = (*env)->GetStringUTFChars(env, dataFile, NULL);


    // Setup the JQ runtime environment
    jq_util_input_state *input_state = jq_util_input_init(NULL, NULL);
    jq_util_input_add_input(input_state, pattern);
    jq_util_input_set_parser(input_state, jv_parser_new(0), 0);
    jq_set_input_cb(jq, jq_util_input_next_input_cb, input_state);
    jq_set_attr(jq, jv_string("JQ_LIBRARY_PATH"), jv_array());
    jq_set_attr(jq, jv_string("PROGRAM_ORIGIN"), jv_string(dirname(input)));


    // Try to read the transformation pattern...
    jv ptrn = jv_load_file(input, 1);
    if (!jv_is_valid(ptrn)) {
        err = "couldn't load pattern\0";
        jv_free(ptrn);
        status = 2;
        goto end;
    }

    // ...and compile it.
    if (!jq_compile_args(jq, skip_shebang(jv_string_value(ptrn)), jv_array())) {
        err = "couldn't compile pattern\n";
        jv_free(ptrn);
        status = 3;
        goto end;
    }

    jv_free(ptrn);


    // Init the output
    FILE *dest = fopen(output, "w");
    if (!dest) {
        err = "couldn't open output file\0";
        status = 2;
        goto end;
    }

    // Process the input JSON and write the result to the output file
    dumpopts |= JV_PRINT_SORTED;
    dumpopts &= ~JV_PRINT_COLOR;
    jv value;

    while (jq_util_input_errors(input_state) == 0 &&
           (jv_is_valid((value = jq_util_input_next_input(input_state))) ||
            jv_invalid_has_msg(jv_copy(value)))) {

        if (jv_is_valid(value)) {
            status = process(jq, value, dest, 0, dumpopts);
            jv_free(value);
            continue;
        }

        jv msg = jv_invalid_get_msg(value);
        err = jv_string_value(msg);
        jv_free(value);
        jv_free(msg);
        status = 4;
        break;
    }

    fclose(dest);


    // End of story. Clean up.
    end:
    jq_util_input_free(&input_state);
    jq_teardown(&jq);

    // Write any error to the output file
    if (!status && err) {
        FILE *log = fopen(output, "w");
        if (log) {
            fwrite(err, 1, strlen(err), log);
            fclose(log);
        }
    }

    // Release the JNI resources
    if (pattern)
        (*env)->ReleaseStringUTFChars(env, patternFile, pattern);

    if (output)
        (*env)->ReleaseStringUTFChars(env, resultFile, output);

    if (input)
        (*env)->ReleaseStringUTFChars(env, dataFile, input);

    return status;
}

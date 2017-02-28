#include <jni.h>
#include <string.h>
#include "jq.h"
#include "util.h"
#include "jv_source.h"

static jv process(jq_state *jq, jv value, int flags, int dumpopts) {
    jq_start(jq, value, flags);
    jv result;
    jv chunk;

    result = jv_string("");
    while (jv_is_valid(chunk = jq_next(jq))) {
        result = jv_string_concat(result, jv_dump_string(chunk, dumpopts));
    }

    jv_free(chunk);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_echsylon_optimus_Optimus_transformJsonString(
        JNIEnv *env, jobject obj, jstring jpattern, jstring jdata) {

    const char *pattern = NULL;
    const char *json = NULL;
    const char *err = NULL;
    int dumpopts = JV_PRINT_INDENT_FLAGS(2);
    int compiled = 0;

    jq_util_input_state *input_state = jq_util_input_init(NULL, NULL);
    jv program_arguments = jv_array();
    jq_state *jq;

    err = NULL;

    // Prepare JQ
    jq = jq_init();
    if (jq == NULL) {
        err = "couldn't init jq\0";
        goto end;
    }

    jq_set_attr(jq, jv_string("PROGRAM_ORIGIN"), jq_realpath(jv_string(".")));
    jq_set_attr(jq, jv_string("VERSION_DIR"), jv_string("0"));
    jq_set_attr(jq, jv_string("JQ_ORIGIN"), jv_string("."));

    // Load the json to transform into the machinery
    json = (*env)->GetStringUTFChars(env, jdata, NULL);
    jv data = jv_load_json(json, strlen(json));
    if (!jv_is_valid(data)) {
        data = jv_invalid_get_msg(data);
        err = jv_string_value(data);
        jv_free(data);
        goto end;
    }

    pattern = (*env)->GetStringUTFChars(env, jpattern, NULL);
    compiled = jq_compile_args(jq, pattern, jv_copy(program_arguments));
    if (!compiled) {
        err = "couldn't compile pattern\0";
        goto end;
    }

    jq_util_input_set_parser(input_state, jv_parser_new(0), 0);
    jq_util_input_add_input(input_state, "-");
    jq_set_input_cb(jq, jq_util_input_next_input_cb, input_state);

    dumpopts |= JV_PRINT_SORTED;
    dumpopts &= ~JV_PRINT_COLOR;
    jv result = jv_string("");
    jv processed;
    jv value;

    while (jq_util_input_errors(input_state) == 0) {
        if (jv_is_valid(value = jq_util_input_next_input(input_state)) &&
            jv_is_valid(processed = process(jq, value, 0, dumpopts))) {
            result = jv_string_concat(result, processed);
            continue;
        }

        err = jq_util_input_test(input_state) ? "NULL parser"
                                          : "Object parser";//"Couldn't process json";
        jv_free(processed);
        jv_free(value);
        break;
    }

    // End of story. Clean up.
    end:
    jq_util_input_free(&input_state);
    jv_free(program_arguments);
    jq_teardown(&jq);

    if (pattern)
        (*env)->ReleaseStringUTFChars(env, jpattern, pattern);

    if (json)
        (*env)->ReleaseStringUTFChars(env, jdata, json);

    if (err) {
        const char *template = "{\"error\":\"%s\"}\0";
        char buf[strlen(template) + strlen(err)];
        sprintf(buf, template, err);
        jv_free(result);
        return (*env)->NewStringUTF(env, buf);
    } else {
        const char *buf = jv_string_value(result);
        jv_free(result);
        return (*env)->NewStringUTF(env, buf);
    }
}

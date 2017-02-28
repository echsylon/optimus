#include "jv.h"

jv jv_load_json(const char *utf_string, int len) {
    struct jv_parser *parser;
    parser = jv_parser_new(0);
    jv data;
    data = jv_array();

    jv_parser_set_buf(parser, utf_string, len, 0);
    jv value;

    while (jv_is_valid((value = jv_parser_next(parser))))
        data = jv_array_append(data, value);

    if (jv_invalid_has_msg(jv_copy(value))) {
        jv_free(data);
        data = value;
    }

    return data;
}

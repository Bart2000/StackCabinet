package com.floppa.stackcabinet.models

import com.floppa.stackcabinet.R

enum class ProblemState(val txt: Int) {
    EMPTY(R.string.txt_empty_list_components),
    NO_CONNECTION(R.string.txt_no_connection),
    COULD_NOT_LOAD(R.string.txt_could_not_load),
    UNKNOWN(R.string.txt_unknown),
}
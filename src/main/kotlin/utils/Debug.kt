package utils

object Debug {

    enum class DebugLevel {
        ERROR, DEBUG, INFO, NONE
    }

    /*          |  ERROR  | INFO  |  DEBUG
       NONE     |    -    |   -   |   -   |
       DEBUG    |    -    |   -   |   x   |
       INFO     |    -    |   x   |   x   |
       ERROR    |    x    |   x   |   x   |

        Depending on DebugLevel we see:
            DEBUG   ->  logd, logi, loge
            INFO    ->  logi, loge
            ERROR   ->  loge
     */

    var DEBUG_LEVEL = DebugLevel.DEBUG

    fun logd(tag: String, msg: String) {
        if (DEBUG_LEVEL == DebugLevel.DEBUG)
            println("$tag: $msg")
    }

    fun logi(tag: String, msg: String) {
        if (DEBUG_LEVEL == DebugLevel.INFO || DEBUG_LEVEL == DebugLevel.DEBUG)
            println("$tag: $msg")
    }

    fun loge(tag: String, msg: String) {
        if (DEBUG_LEVEL != DebugLevel.NONE)
            System.err.println("$tag: $msg")
    }
}

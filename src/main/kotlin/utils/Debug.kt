package utils

object Debug {

    enum class DebugLevel {
        ERROR, DEBUG, INFO, NONE
    }

    private const val ANSI_BLACK_BOLD = "\u001B[1;30m"
    private const val ANSI_RED = "\u001B[0;31m"
    private const val ANSI_GREEN = "\u001B[0;32m"
    private const val ANSI_BLUE = "\u001B[0;34m";


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
            println("$ANSI_BLACK_BOLD$tag: $ANSI_GREEN $msg")
    }

    fun logi(tag: String, msg: String) {
        if (DEBUG_LEVEL == DebugLevel.INFO || DEBUG_LEVEL == DebugLevel.DEBUG)
            println("$ANSI_BLACK_BOLD$tag: $ANSI_BLUE $msg")
    }

    fun loge(tag: String, msg: String) {
        if (DEBUG_LEVEL != DebugLevel.NONE)
            System.err.println("$ANSI_BLACK_BOLD$tag: $ANSI_RED $msg")
    }
}

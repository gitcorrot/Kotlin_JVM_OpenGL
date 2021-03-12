object Debug {

    public enum class DebugLevel() {
        ERROR, DEBUG, INFO, NONE
    }

    /*          |  ERROR  | INFO  |  DEBUG
       DEBUG    |    x    |   x   |   x   |
       INFO     |    x    |   x   |   -   |
       ERROR    |    x    |   -   |   -   |
       NONE     |    -    |   -   |   -   |

       ex. When log level = debug, then there will be
           shown all logs, for ERROR only error messages etc.
     */


    public var DEBUG_LEVEL = DebugLevel.DEBUG

    fun logd(tag: String, msg: String) {
        if (DEBUG_LEVEL == DebugLevel.DEBUG ||
            DEBUG_LEVEL == DebugLevel.INFO ||
            DEBUG_LEVEL == DebugLevel.ERROR
        ) {
            printLog(tag, msg)
        }
    }

    fun logi(tag: String, msg: String) {
        if (DEBUG_LEVEL == DebugLevel.INFO ||
            DEBUG_LEVEL == DebugLevel.DEBUG
        ) {
            printLog(tag, msg)
        }
    }

    fun loge(tag: String, msg: String) {
        if (DEBUG_LEVEL == DebugLevel.ERROR)
            printLog(tag, msg)
    }


    private fun printLog(tag: String, msg: String) {
        println("$tag: $msg")
    }
}

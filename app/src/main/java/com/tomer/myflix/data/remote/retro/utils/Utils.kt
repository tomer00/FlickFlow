package com.tomer.myflix.data.remote.retro.utils


fun String.toINT(defVal: Int = -1): Int {
    return try {
        this.toInt()
    } catch (_: Exception) {
        defVal
    }
}

fun String.toFLOAT(defVal: Float = -1f): Float {
    return try {
        this.toFloat()
    } catch (_: Exception) {
        defVal
    }
}
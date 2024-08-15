package com.example.straymaps.misc

import android.util.Log

sealed class Resource<out T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T): Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null): Resource<T>(data)
}

fun logError (TAG: String, message: String, exception: Exception? = null) {
    if (exception != null) {
        Log.e(TAG, message, exception)
    } else {
        Log.e(TAG, message)
    }
}

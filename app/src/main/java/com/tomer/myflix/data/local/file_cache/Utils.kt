package com.tomer.myflix.data.local.file_cache

import android.content.Context
import java.io.File

fun getCacheDir(context: Context, id: String): File {
    return File(context.getExternalFilesDir("CACHE"), id).also { it.mkdirs() }
}

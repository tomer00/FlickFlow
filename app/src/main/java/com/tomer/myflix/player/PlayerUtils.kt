package com.tomer.myflix.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.tomer.myflix.data.models.LinkPair
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.suspendCoroutine


fun Long.timeTextFromMs(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    return if (hours == 0L)
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    else String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}

fun View.performHaptic(constant: Int): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        performHapticFeedback(constant, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
    else performHapticFeedback(
        HapticFeedbackConstants.VIRTUAL_KEY,
        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
    )
}

enum class PlayingState {
    INITIAL, LOADED, ERROR, PLAYING, ENDED
}

fun getUrlFromQuality(quality: Int, list: List<LinkPair>): String {
    fun getMinFromList(): Int {
        return list.stream().mapToInt { it.disName.removeSuffix(" P").toInt() }.min().orElse(480)
    }
    return when (quality) {
        240 -> list.find { it.disName.contains("240") }?.url
            ?: getUrlFromQuality(120, list)

        360 -> list.find { it.disName.contains("360") }?.url
            ?: getUrlFromQuality(240, list)

        480 -> list.find { it.disName.contains("480") }?.url
            ?: getUrlFromQuality(360, list)

        720 -> list.find { it.disName.contains("720") }?.url
            ?: getUrlFromQuality(480, list)

        1080 -> list.find { it.disName.contains("1080") }?.url
            ?: getUrlFromQuality(720, list)

        2160 -> list.find { it.disName.contains("2160") }?.url
            ?: getUrlFromQuality(1080, list)

        else -> getUrlFromQuality(getMinFromList(), list)
    }
}

fun getLanguageName(langCode: String?): String {
    return if (langCode == null) "Unknown"
    else Locale(langCode).displayLanguage
}


suspend fun getVibrantCol(bmp: Bitmap): Int {
    return withContext(Dispatchers.Default) {
        return@withContext Palette.from(bmp).generate().getVibrantColor(Color.RED)
    }
}

suspend fun String.urlToBitmap(con: Context, scope: CoroutineScope): Bitmap {
    return suspendCoroutine { continuation ->
        scope.launch(Dispatchers.IO) {
            val loader = ImageLoader(con)
            val req = ImageRequest.Builder(con)
                .data(this@urlToBitmap)
                .allowHardware(false)
                .allowConversionToBitmap(true)
                .build()
            val result = loader.execute(req)
            if (result is SuccessResult)
                continuation.resumeWith(Result.success((result.drawable as BitmapDrawable).bitmap))
            else continuation.resumeWith(Result.failure(Exception("Error Occurred")))
        }
    }
}
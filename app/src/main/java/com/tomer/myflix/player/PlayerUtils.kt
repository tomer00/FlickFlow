package com.tomer.myflix.player

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
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
import com.tomer.myflix.data.models.TimePair
import com.tomer.myflix.presentation.ui.models.ModelPLayerUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.TimeUnit


fun Activity.isDarkModeEnabled(): Boolean {
    val currentNightMode = resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)
    return currentNightMode == Configuration.UI_MODE_NIGHT_YES
}

fun getVideoLink(id: String) =
    "https://flick.devhimu.in/files/$id/master.m3u8"

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

fun getLanguageName(langCode: String?): String {
    return if (langCode == null) "Unknown"
    else Locale(langCode).displayLanguage
}


suspend fun getVibrantCol(bmp: Bitmap): Int {
    return withContext(Dispatchers.Default) {
        return@withContext Palette.from(bmp).generate().getVibrantColor(Color.RED)
    }
}

suspend fun String.urlToBitmap(con: Context): Bitmap {
    return withContext(Dispatchers.IO) {
        val loader = ImageLoader(con)
        val req = ImageRequest.Builder(con)
            .data(this@urlToBitmap)
            .allowHardware(false)
            .allowConversionToBitmap(true)
            .build()
        val result = loader.execute(req)
        return@withContext if (result is SuccessResult)
            (result.drawable as BitmapDrawable).bitmap
        else throw Exception("")
    }
}

fun getSampleVideoModel() = ModelPLayerUI(
    "ivqtjv",
    "Kuch Bhi Name",
    TimePair(1000, 10000),
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ3mNRv_ktQx8yYdbTQzz7KN2EydERqgwMkx0KWXs2-X__DUaglPAYOvodR&s=10",
    Color.RED,0L, 0, 3,null, null, null, 1f, .3f

)
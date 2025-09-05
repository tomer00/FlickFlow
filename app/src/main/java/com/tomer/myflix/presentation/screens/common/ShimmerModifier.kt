package com.tomer.myflix.presentation.screens.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize

fun Modifier.shimmerEffect(
    startColor: Color = Color(0xFFB8B5B5),
    endColor: Color = Color(0xFF6F6C6C)
): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition()
//    SideEffect { Log.d("TAG--", "shimmerEffect: $size") }
    val startOff by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Restart
        )
    )
    background(
        brush = Brush.linearGradient(
            colors = listOf(
                startColor,
                endColor,
                startColor
            ),
            start = Offset(startOff, 0f),
            end = Offset(startOff + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned(
        onGloballyPositioned = {
            size = it.size
        }
    )
}
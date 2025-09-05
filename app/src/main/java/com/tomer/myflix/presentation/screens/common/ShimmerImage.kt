package com.tomer.myflix.presentation.screens.common


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size


@Composable
fun ShimmerImage(modifier: Modifier, model: Any) {
    var isLoaded by remember { mutableStateOf(false) }
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(model)
            .size(Size.ORIGINAL)
            .build(),
        contentScale = ContentScale.Crop,
        onSuccess = { isLoaded = true }
    )


    AnimatedContent(
        isLoaded, modifier,
        transitionSpec = {
            fadeIn().togetherWith(fadeOut())
        }
    ) { isL ->
        if (isL)
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        else Box(
            Modifier
                .fillMaxSize()
                .shimmerEffect()
        )
    }

}

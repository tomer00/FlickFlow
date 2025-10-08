package com.tomer.myflix.presentation.screens.detailMovie

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tomer.myflix.data.local.models.ModelMovie
import com.tomer.myflix.player.getNameLogoLink
import com.tomer.myflix.presentation.screens.common.ShimmerImage
import com.tomer.myflix.presentation.ui.views.YtPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DetailMoviesScreen(onBack: () -> Unit, onMore: (String) -> Unit) {
    val vm: VMDetailMovies = hiltViewModel()
    val mod = vm.mod.collectAsState().value
    var size by remember { mutableStateOf(IntSize.Zero) }
    val colAccent = vm.col.collectAsState().value

    Box(Modifier.fillMaxSize()) {
        //region TOP LAYER
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colAccent, colAccent,
                            Color.Black, Color.Black,
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height.toFloat())
                    )
                )
                .onGloballyPositioned(
                    onGloballyPositioned = {
                        size = it.size
                    }
                )
        )
        //endregion TOP LAYER

        if (mod == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(
                Modifier
                    .fillMaxHeight(.6f)
                    .fillMaxWidth()
                    .blur(60.dp)
            ) {
                AsyncImage(
                    mod.posterHorizontal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            LogoImageComp(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                mod.flickId, mod.title
            )


            // Middel Layer
            Column(
                Modifier.fillMaxSize()
            ) {
                val paddtop = size.width.div(LocalContext.current.resources.displayMetrics.density)
                    .times(.5625f * .7f)
                Spacer(Modifier.size(paddtop.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        mod.posterVertical,
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .width(140.dp)
                            .height(210.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        Modifier
                            .height(200.dp)
                            .padding(20.dp, 4.dp)
                            .weight(1f)
                    ) {
                        DetailsPlayButton(mod, vm)
                    }
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(vm.scroll)
                ) {
                    Spacer(Modifier.size(20.dp))
                    ScreenShots(mod.screenShots)
                    Spacer(Modifier.size(20.dp))
                    Box(Modifier.fillMaxSize()) {
                        YtPlayer(
                            Modifier
                                .fillMaxWidth(.86f)
                                .widthIn(1080.dp)
                                .border(1.4.dp, Color.White.copy(.2f), RoundedCornerShape(12.dp))
                                .align(Alignment.Center)
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(12.dp)), mod.trailerUrl
                        )
                    }
                    Spacer(Modifier.size(20.dp))
                    Suggestions(vm,onMore)
                }
            }


        }

        //region BACK BUTTON
        //Back Button Top Layer
        Box(
            Modifier
                .safeContentPadding()
                .absolutePadding(left = 12.dp)
                .size(40.dp)
                .background(Color(0x60000000), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    onBack()
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
            )
        }
        //endregion BACK BUTTON
    }
}

@Composable
fun DetailsPlayButton(mod: ModelMovie, vm: VMDetailMovies) {
    val context = LocalContext.current
    Column {
        Row(Modifier.fillMaxWidth()) {
            Text(text = "Audio : ")
            Text(
                text = mod.audioTracks.joinToString(" / "),
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
        }
        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White.copy(.6f), CircleShape)
                .clickable {
                    vm.getCanPlayNow(context)
                }, contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = vm.showProg.collectAsState().value
            ) {
                if (it) {
                    CircularProgressIndicator(
                        Modifier.size(28.dp),
                        color = Color.White
                    )
                } else
                    Row(
                        Modifier.padding(0.dp, 8.dp)
                    ) {
                        val textButton = vm.textPlayButton.collectAsState().value
                        if (textButton == "Watch Now")
                            Image(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        Text(textButton, color = Color.White)
                    }
            }
        }
    }
}

@Composable
fun LogoImageComp(modifier: Modifier, flickId: String, title: String) {
    Box(modifier.padding(12.dp), Alignment.Center) {
        var isError by remember { mutableStateOf(false) }
        var isSuccess by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val scale = animateFloatAsState(
            if (isSuccess) 1f else 0.2f,
            label = flickId + "logo",
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy
            )
        )
        val textScale = animateFloatAsState(
            if (isError) 1f else 0f,
            label = flickId + "txt",
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy
            )
        )
        AsyncImage(
            model = getNameLogoLink(flickId),
            contentDescription = title,
            onSuccess = {
                scope.launch {
                    delay(200)
                    isSuccess = true
                }
            },
            onError = { isError = true },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                },
            contentScale = ContentScale.Inside
        )
        Text(
            text = title,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                scaleX = textScale.value
                scaleY = textScale.value
            }
        )
    }
}

@Composable
fun Suggestions(vm: VMDetailMovies,onMore: (String) -> Unit) {
    val itmes = vm.listSuggestions.collectAsState().value
    Column(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.size(20.dp))
        Text(
            "More like this...",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .absolutePadding(8.dp)
        )
        if (itmes.isEmpty()) return@Column
        val modifier = Modifier
            .weight(1f)
            .wrapContentHeight()
        for (i in 1..5 step 3) {
            Row {
                Spacer(Modifier.size(8.dp))
                val mod = itmes[i + 0].collectAsState().value
                SuggestItem(
                    mod.isShimmer.not(),
                    mod.posterVerti,
                    mod.title, mod.isHd,
                    modifier
                ) {
                    onMore(mod.flickId)
                }
                Spacer(Modifier.size(8.dp))
                val mod1 = itmes[i + 1].collectAsState().value
                SuggestItem(
                    mod1.isShimmer.not(),
                    mod1.posterVerti,
                    mod1.title, mod1.isHd,
                    modifier
                ) {
                    onMore(mod1.flickId)
                }
                Spacer(Modifier.size(8.dp))
                val mod2 = itmes[i + 2].collectAsState().value
                SuggestItem(
                    mod2.isShimmer.not(),
                    mod2.posterVerti,
                    mod2.title, mod2.isHd,
                    modifier
                ) {
                    onMore(mod2.flickId)
                }
                Spacer(Modifier.size(8.dp))
            }
            Spacer(Modifier.size(8.dp))
        }
    }
}

@Composable
fun SuggestItem(
    isLoaded: Boolean,
    posterVerti: String,
    title: String,
    isHD: Boolean? = null,
    modifier: Modifier,
    onItemClicked: () -> Unit
) {
    Box(modifier) {
        Column {
            ShimmerImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(.2f), RoundedCornerShape(12.dp))
                    .clickable {
                        if (isLoaded)
                            onItemClicked()
                    },
                model = posterVerti
            )
            Spacer(Modifier.size(2.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee()
            )
        }
    }
}

@Composable
fun ScreenShots(items: List<String>) {
    Text(
        "Screen Shots",
        color = Color.White,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.size(8.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.size(40.dp))
        for (i in items) {
            ShimmerImage(
                modifier = Modifier
                    .width(220.dp)
                    .aspectRatio(16f / 9f)
                    .border(1.4.dp, Color.White.copy(.4f), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
                model = i
            )
            Spacer(Modifier.size(12.dp))
        }

        Spacer(Modifier.size(12.dp))
    }
}



















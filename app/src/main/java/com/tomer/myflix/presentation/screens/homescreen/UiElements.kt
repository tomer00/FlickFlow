package com.tomer.myflix.presentation.screens.homescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.tomer.myflix.data.local.file_cache.getCacheDir
import com.tomer.myflix.data.local.models.ModelLastPlayed
import com.tomer.myflix.presentation.screens.common.ShimmerImage
import com.tomer.myflix.presentation.screens.common.shimmerEffect
import com.tomer.myflix.presentation.screens.detailMovie.Heading
import com.tomer.myflix.presentation.ui.models.PlayableItemModel
import java.io.File
import kotlin.math.absoluteValue

@Composable
fun PagerComp(link: String, index: Int, pagerState: PagerState, onClick: () -> Unit) {
    val pageOffset = pagerState.offsetForPage(index)
    Box(
        Modifier
            .padding(2.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .graphicsLayer {
                lerp(
                    start = 0.86f.dp,
                    stop = 1f.dp,
                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                ).also { scale ->
                    scaleX = scale.value
                    scaleY = scale.value
                }
            }
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    Color.Black.copy(
                        alpha = lerp(
                            start = 0.dp,
                            stop = .5.dp,
                            fraction = pageOffset.absoluteValue.coerceIn(0f, 1f)
                        ).value
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(16.dp))
    ) { ShimmerImage(modifier = Modifier.fillMaxSize(), model = link) }
}

@Composable
fun MovieItem(model: PlayableItemModel, onItemClicked: () -> Unit) {
    Box(
        Modifier
            .width(110.dp)
            .wrapContentHeight()
            .clickable {
                if (model.isShimmer.not())
                    onItemClicked()
            }
    ) {
        if (model.isShimmer)
            Box(
                Modifier
                    .width(110.dp)
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shimmerEffect()
            )
        else
            Column {
                ShimmerImage(
                    modifier = Modifier
                        .width(110.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    model = model.posterVerti
                )
                Spacer(Modifier.size(10.dp))
                Text(
                    text = model.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
            }

    }
}

@Composable
fun FeaturedRow(
    items: List<PlayableItemModel>,
    heading: String,
    onItemClicked: (Boolean, String) -> Unit,
) {
    Column(Modifier.padding(top = 20.dp)) {
        Heading(heading, 25.sp)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Spacer(Modifier.size(32.dp))
            }
            items(
                items = items,
                key = { it.id }
            ) { mod ->
                MovieItem(mod) {
                    onItemClicked.invoke(mod.isMovie, mod.flickId + mod.imdbId)
                }
            }
            item {
                Spacer(Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun LastPlayedRow(
    items: List<ModelLastPlayed>,
    onItemClicked: (Int) -> Unit,
) {
    Column(Modifier.padding(top = 20.dp)) {
        Heading("\uD83D\uDD25 Continue Watching", 25.sp)
        Spacer(Modifier.size(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Spacer(Modifier.size(32.dp))
            }
            items(
                items = items,
                key = { it.id }
            ) { mod ->
                Box(
                    Modifier
                        .width(200.dp)
                        .wrapContentHeight()
                        .clickable {
                            onItemClicked(mod.id)
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f)
                            .clip(RoundedCornerShape(16.dp)),
                    ) {
                        ShimmerImage(
                            modifier = Modifier
                                .fillMaxSize(),
                            model = File(
                                getCacheDir(LocalContext.current, mod.flickId),
                                "poster.webp"
                            )
                        )
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(.2f))
                        )
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(16 / 3f)
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(.8f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(Color.White.copy(.4f))
                                .align(Alignment.BottomCenter)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(mod.progress)
                                    .fillMaxHeight()
                                    .background(Color(mod.accentColor))
                            )
                        }
                        Text(
                            text = mod.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .basicMarquee()
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.size(12.dp))
            }
        }
    }
}
package com.tomer.myflix.presentation.screens.homescreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.tomer.myflix.presentation.screens.common.shimmerEffect
import com.tomer.myflix.presentation.ui.models.PlayableItemModel
import kotlin.math.absoluteValue

@Composable
fun ShimmerHome() {
    val tempList = listOf(
        PlayableItemModel(0, "", "", "", "", "",true),
        PlayableItemModel(1, "", "", "", "", "",true),
        PlayableItemModel(2, "", "", "", "", "",true),
        PlayableItemModel(3, "", "", "", "", "",true),
        PlayableItemModel(4, "", "", "", "", "",true),
        PlayableItemModel(5, "", "", "", "", "",true),
    )
    val pagerState = rememberPagerState(2) { tempList.size }
    Column(
        Modifier
            .fillMaxSize()
            .safeContentPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.size(60.dp))
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 60.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        ) { index ->
            val pageOffset = pagerState.offsetForPage(index)
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(2.dp)
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
                    .shimmerEffect()
            )
        }
        Spacer(Modifier.size(80.dp))
        Box(
            Modifier
                .fillMaxWidth(.3f)
                .height(40.dp)
                .padding(12.dp, 4.dp)
                .align(Alignment.Start)
                .clip(RoundedCornerShape(8.dp))
                .shimmerEffect()
        )

        Spacer(Modifier.size(20.dp))
        LazyRow(
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(Modifier.size(12.dp))
            }
            items(
                items = tempList,
                key = { it.id }
            ) { mod ->
                MovieItem(mod) {}
            }
            item {
                Spacer(Modifier.size(12.dp))
            }
        }
        Spacer(Modifier.size(40.dp))
        Box(
            Modifier
                .fillMaxWidth(.3f)
                .height(40.dp)
                .padding(12.dp, 4.dp)
                .align(Alignment.Start)
                .clip(RoundedCornerShape(8.dp))
                .shimmerEffect()
        )

        Spacer(Modifier.size(20.dp))
        LazyRow(
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(Modifier.size(12.dp))
            }
            items(
                items = tempList,
                key = { it.id }
            ) { mod ->
                MovieItem(mod) {}
            }
            item {
                Spacer(Modifier.size(12.dp))
            }
        }
        Spacer(Modifier.size(40.dp))
        Box(
            Modifier
                .fillMaxWidth(.3f)
                .height(40.dp)
                .padding(12.dp, 4.dp)
                .align(Alignment.Start)
                .clip(RoundedCornerShape(8.dp))
                .shimmerEffect()
        )

        Spacer(Modifier.size(20.dp))
        LazyRow(
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(Modifier.size(12.dp))
            }
            items(
                items = tempList,
                key = { it.id }
            ) { mod ->
                MovieItem(mod) {}
            }
            item {
                Spacer(Modifier.size(12.dp))
            }
        }
    }
}
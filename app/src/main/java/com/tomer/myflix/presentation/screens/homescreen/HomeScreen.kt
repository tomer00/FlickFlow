package com.tomer.myflix.presentation.screens.homescreen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.tomer.myflix.R
import com.tomer.myflix.common.gson
import com.tomer.myflix.player.PlayerActivity
import com.tomer.myflix.presentation.ui.models.DtoPlayerView
import com.tomer.myflix.presentation.ui.models.PlayingType
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(onMovieClicked: (String) -> Unit, onSeriesClicked: (String) -> Unit) {
    val vm: HomeViewModel = hiltViewModel<HomeViewModel>()
    val featuredList = vm.featuredList.collectAsState().value

    LaunchedEffect(Unit) {
        snapshotFlow {
            Pair(vm.posterPagerState.currentPage, vm.posterPagerState.currentPageOffsetFraction)
        }.collect { (page, off) ->
            vm.titlePagerState.scrollToPage(page, off)
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (featuredList.isEmpty()) {
            ShimmerHome()
            return
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(vm.mainContScrollState)
        ) {

            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 12f)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(featuredList[vm.posterPagerState.currentPage].posterHori)
                            .crossfade(true)
                            .size(Size.ORIGINAL)
                            .build(),
                        contentDescription = "Background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 12f)
                            .blur(8.dp)
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    Color.Black.copy(
                                        androidx.compose.ui.util.lerp(
                                            start = .2f,
                                            stop = 1.4f,
                                            fraction = vm.posterPagerState
                                                .offsetForPage(vm.posterPagerState.currentPage)
                                                .absoluteValue
                                                .coerceIn(0f, 1f)
                                        )
                                    )
                                )
                            }
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 2f)
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    )
                }
                Column(
                    Modifier.safeContentPadding()
                ) {
                    VerticalPager(
                        state = vm.titlePagerState,
                        userScrollEnabled = false,
                        modifier = Modifier
                            .height(60.dp)
                            .absolutePadding(left = 12.dp, right = 56.dp),
                        horizontalAlignment = Alignment.Start
                    ) { page ->
                        Text(
                            text = featuredList[page].title,
                            fontFamily = FontFamily(Font(R.font.poppins_semibold)),
                            fontSize = 32.sp,
                            maxLines = 1,
                            color = Color.White,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                    HorizontalPager(
                        state = vm.posterPagerState,
                        contentPadding = PaddingValues(horizontal = 60.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                    ) {
                        PagerComp(featuredList[it].posterVerti, it, vm.posterPagerState) {
                            if (featuredList[it].isMovie)
                                onMovieClicked(featuredList[it].flickId + featuredList[it].imdbId)
                            else onSeriesClicked(featuredList[it].flickId)
                        }
                    }
                    Spacer(Modifier.size(20.dp))
                }
            }

            val lastPlayed = vm.lastPlayedList.collectAsState().value
            val con = LocalContext.current
            if (lastPlayed.isNotEmpty())
                LastPlayedRow(lastPlayed) { id ->
                    val clickedMod = lastPlayed.find { it.id == id } ?: return@LastPlayedRow
                    val intent = Intent(con, PlayerActivity::class.java).apply {
                        putExtra(
                            "data",
                            gson.toJson(
                                DtoPlayerView(
                                    clickedMod.flickId, "",
                                    if (clickedMod.isMovie) PlayingType.MOVIE else PlayingType.EPISODE
                                )
                            )
                        )
                    }
                    con.startActivity(intent)
                }

            val featuredMap = vm.featuredMap.collectAsState().value
            featuredMap.entries.forEach { (categoryTitle, movies) ->
                FeaturedRow(movies, categoryTitle) { isMovie, id ->
                    if (isMovie) onMovieClicked(id) else onSeriesClicked(id)
                }
            }
            Spacer(Modifier.size(24.dp))
        }
    }
}


fun PagerState.offsetForPage(page: Int) = (currentPage - page) + currentPageOffsetFraction
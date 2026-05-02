package com.tomer.myflix.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tomer.myflix.presentation.screens.detailMovie.DetailMoviesScreen
import com.tomer.myflix.presentation.screens.detailSeries.DetailSeriesScreen
import com.tomer.myflix.presentation.screens.homescreen.HomeScreen
import com.tomer.myflix.presentation.ui.theme.FlickFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlickFlowTheme {
                var socketConnection by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()
                Box(Modifier.fillMaxSize()) {
                    App()
                    AnimatedContent(
                        targetState = socketConnection,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .safeContentPadding()
                            .padding(8.dp, 4.dp)
                            .background(Color.Green, RoundedCornerShape(20.dp)),
                        label = "Socket"
                    ) {
                        if (it) {
                            Box(
                                Modifier
                                    .fillMaxWidth(.6f)
                                    .height(200.dp)
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch { socketConnection = false }
                                    },
                                    modifier = Modifier.align(Alignment.Center)
                                ) {
                                    Text("CLOSE")
                                }
                            }
                        } else {
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .padding(4.dp)
                                    .clickable {
                                        scope.launch {
                                            socketConnection = true
                                        }
                                    }
                            ) {
                                Image(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun App() {
        val navController = rememberNavController()
        NavHost(navController, "home") {
            composable(route = "home") {
                HomeScreen(
                    onMovieClicked = { navController.navigate("movies/$it") },
                    onSeriesClicked = { navController.navigate("series/$it") }
                )
            }
            composable(
                route = "movies/{id}",
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                    }
                )) {
                DetailMoviesScreen(
                    onBack = { navController.popBackStack() },
                    onMore = { navController.navigate("movies/$it") })
            }
            composable(
                route = "series/{id}",
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                    }
                )) {
                DetailSeriesScreen(
                    onBack = { navController.popBackStack() },
                    onMore = { navController.navigate("series/$it") })
            }
        }
    }
}
package com.tomer.myflix.presentation.screens.detailSeries

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DetailSeriesScreen(onBack: (Int) -> Unit, onMore: () -> Unit) {
    val vm: VMDetailSeries = hiltViewModel()
    var s by remember { mutableStateOf("") }
}
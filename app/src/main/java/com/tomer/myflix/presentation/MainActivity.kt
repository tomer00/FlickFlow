package com.tomer.myflix.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.gson.Gson
import com.tomer.myflix.data.models.LinkPair
import com.tomer.myflix.data.models.TimePair
import com.tomer.myflix.player.PlayerActivity
import com.tomer.myflix.presentation.ui.models.MoviePlayerModalUi
import com.tomer.myflix.presentation.ui.theme.FlickFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlickFlowTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        startActivity(
            Intent(this, PlayerActivity::class.java)
                .apply {
                    putExtra("data",Gson().toJson(
                        MoviePlayerModalUi(
                            name = "Kraven the Hunter (2024) WEB-DL Dual Audio {Hindi-English}",
                            links = listOf(
                                LinkPair(
                                    "480 P",
                                    "480 p Hindi 620MB",
                                    "https://pixeldra.in/api/file/D3pyFtgJ?download"
                                ),
                                LinkPair(
                                    "720 P",
                                    "720 p Hindi 1.2 GB",
                                    "https://pixeldra.in/api/file/Yetv6hdp?download"
                                ),
                                LinkPair(
                                    "1080 P",
                                    "1080 p Hindi 2.4 GB",
                                    "https://pixeldra.in/api/file/ASWDYLCk?download"
                                ),LinkPair(
                                    "2160 P",
                                    "2160 p Hindi 11 GB",
                                    "https://video-downloads.googleusercontent.com/ADGPM2lQkzjuY-GAJWHQmAqizw9PYQSlaJlJDFx1dU6kQrwYkcEm-b7MCeqr4WUuBopo-Xd7dMuTRyFhpls9BV21SczNnL3IEDaDV-rQofIaUTs44pMSdeYamNZbcwxeIztAAOkuHNhfa4qp4doKZGWCTWrXDaIGJWugHpPLjSeFLNplXa9LBWw1Qxr_R244mEL_pE8YnihQnUS_alZ9Il5H78jqSDQA8Ihi26bFm0mOotdefMjA56Q"
                                ),
                            ),
                            introTime = TimePair(12000, 24000),
                            "https://static1.cbrimages.com/wordpress/wp-content/uploads/2023/06/kraven-the-hunter-poster-aaron-taylor-johnson.jpg"
                        )
                    ))
                }
        )
        finish()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FlickFlowTheme {
        Greeting("Android")
    }
}
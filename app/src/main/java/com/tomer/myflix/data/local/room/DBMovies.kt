package com.tomer.myflix.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tomer.myflix.data.local.models.ModelCollection
import com.tomer.myflix.data.local.models.ModelEpisode
import com.tomer.myflix.data.local.models.ModelFeatured
import com.tomer.myflix.data.local.models.ModelLastPlayed
import com.tomer.myflix.data.local.models.ModelMovie
import com.tomer.myflix.data.local.models.ModelSeries
import com.tomer.myflix.data.local.models.ModelSuggestions
import com.tomer.myflix.presentation.ui.models.ModelPLayerUI

@Database(
    entities = [
        ModelMovie::class,
        ModelEpisode::class,
        ModelSeries::class,
        ModelPLayerUI::class,
        ModelFeatured::class,
        ModelCollection::class,
        ModelSuggestions::class,
        ModelLastPlayed::class,
    ], version = 1, exportSchema = false
)
@TypeConverters(TypeConverterMovie::class)
abstract class DBMovies : RoomDatabase() {
    abstract fun seriesDao(): DaoSeries
    abstract fun playingDao(): DaoPlaying
    abstract fun settingsDao(): DaoSettings
    abstract fun suggestionDao(): DaoSuggestions
    abstract fun featuredDao(): DaoFeaturedCollection
}
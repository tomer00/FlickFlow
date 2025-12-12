package com.tomer.myflix.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tomer.myflix.data.local.models.ModelEpisode
import com.tomer.myflix.data.local.models.ModelSeries

@Dao
abstract class DaoSeries {

    @Query("SELECT * FROM series WHERE flickId = :id")
    abstract fun getSeriesFromId(id: String): ModelSeries?

    @Insert(entity = ModelSeries::class, onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertSeries(series: ModelSeries)


    @Insert(entity = ModelEpisode::class, onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertEpisode(episode: ModelEpisode)

    @Query("SELECT * FROM episodes WHERE seriesFlickId = :seriesFlickId AND season = :season ORDER BY episode ASC")
    abstract fun getAllEpisodesOfSeries(seriesFlickId: String, season: Int): List<ModelEpisode>

}
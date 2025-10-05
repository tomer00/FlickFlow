package com.tomer.myflix.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tomer.myflix.data.local.models.ModelEpisode
import com.tomer.myflix.data.local.models.ModelFeatured
import com.tomer.myflix.data.local.models.ModelMovie

@Dao
abstract class DaoPlaying {

    @Insert(entity = ModelMovie::class, onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertMovie(movie: ModelMovie)

    @Query("SELECT * FROM movies WHERE flickId = :id")
    abstract fun getMovieFromId(id: String): ModelMovie?


    @Insert(entity = ModelEpisode::class, onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertEpisode(movie: ModelEpisode)

    @Query("SELECT * FROM episodes WHERE flickId = :id")
    abstract fun getEpisodeFromId(id: String): ModelEpisode?

    @Query("SELECT posterHorizontal FROM series WHERE flickId = :id")
    abstract fun getHoriPosterOfSeries(id: String): String?

}
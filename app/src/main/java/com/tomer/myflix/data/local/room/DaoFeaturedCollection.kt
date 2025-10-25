package com.tomer.myflix.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tomer.myflix.data.local.models.ModelCollection
import com.tomer.myflix.data.local.models.ModelFeatured
import com.tomer.myflix.data.local.models.ModelLastPlayed

@Dao
abstract class DaoFeaturedCollection {

    @Insert(entity = ModelFeatured::class, onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAllFeatured(featured: List<ModelFeatured>)

    @Query("DELETE FROM featured")
    abstract fun deleteAllFeatured()

    @Query("SELECT * FROM featured")
    abstract fun getAllFeatured(): List<ModelFeatured>

    @Insert(entity = ModelCollection::class, onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAllCollections(featured: List<ModelCollection>)

    @Query("DELETE FROM collection")
    abstract fun deleteAllCollection()

    @Query("SELECT * FROM collection")
    abstract fun getAllCollections(): List<ModelCollection>

    @Insert(entity = ModelLastPlayed::class, onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertLastPlayed(lastPlayed: ModelLastPlayed)

    @Query("DELETE FROM last_played WHERE flickId = :flickId")
    abstract fun deleteLastPlayed(flickId: String)

    @Query("SELECT * FROM last_played ORDER BY id DESC")
    abstract fun getLastPlayedItems(): List<ModelLastPlayed>
}
package com.tomer.myflix.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tomer.myflix.data.local.models.ModelCollection
import com.tomer.myflix.data.local.models.ModelFeatured

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
}
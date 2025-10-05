package com.tomer.myflix.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tomer.myflix.data.local.models.ModelSuggestions

@Dao
abstract class DaoSuggestions {

    @Query("DELETE FROM suggestions")
    abstract fun deleteAllSuggestions()

    @Insert(entity = ModelSuggestions::class, onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertSuggestions(suggestions: ModelSuggestions)

    @Query("SELECT * FROM suggestions WHERE flickId = :flickId")
    abstract fun getSuggestionsFromId(flickId: String): ModelSuggestions?
}
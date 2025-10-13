package com.tomer.myflix.data.local.room

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.tomer.myflix.common.gson
import com.tomer.myflix.data.models.TimePair
import com.tomer.myflix.presentation.ui.models.TrackInfo

class TypeConverterMovie {

    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        try {
            val type = object : TypeToken<List<String>>() {}.type
            return gson.fromJson(value, type)
        } catch (_: Exception) {
            return emptyList()
        }
    }

    @TypeConverter
    fun fromTrackInfo(info: TrackInfo?): String {
        return if (info == null) ""
        else gson.toJson(info)
    }

    @TypeConverter
    fun toTrackInfo(value: String): TrackInfo? {
        if (value.isEmpty()) return null
        return try {
            gson.fromJson(value, TrackInfo::class.java)
        } catch (_: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromTimePair(timePair: TimePair): String {
        return gson.toJson(timePair)
    }

    @TypeConverter
    fun toTimePair(timePairString: String): TimePair? {
        return try {
            gson.fromJson(timePairString, TimePair::class.java)
        } catch (_: Exception) {
            TimePair(0, 0)
        }
    }

    @TypeConverter
    fun fromPairList(list: List<Pair<String, String>>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toPairList(data: String): List<Pair<String, String>> {
        try {
            val type = object : TypeToken<List<Pair<String, String>>>() {}.type
            return gson.fromJson(data, type)
        } catch (_: Exception) {
            return emptyList()
        }
    }
}
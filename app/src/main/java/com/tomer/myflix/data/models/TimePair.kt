package com.tomer.myflix.data.models


import com.google.gson.annotations.SerializedName

data class TimePair(
    @SerializedName("startTime")
    val startTime: Long,
    @SerializedName("endTime")
    val endTime: Long
)
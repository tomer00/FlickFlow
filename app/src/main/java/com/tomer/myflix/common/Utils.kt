package com.tomer.myflix.common

import com.google.gson.Gson
import kotlin.random.Random

val gson by lazy { Gson() }
const val linkServer = "https://flick.devhimu.in"
const val linkWebSocket = "https://test.devhimu.in/socket"
const val linkOmDb = "http://www.omdbapi.com"

fun getDefaultHoriPoster() = ""

fun getRandomName(): String {
    val charPool: List<Char> = ('a'..'z').toList()
    return (1..6)
        .map { i -> Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}

fun Int.fromSecsToTimeStr(): String{
    val hour = this / 3600
    val min = (this % 3600) / 60

    val h = if (hour == 0) "" else "${hour}h "
    val m = if (min == 0 && hour == 0) "" else "${min}m"

    return "$h$m".trimEnd()

}
package com.tomer.myflix.data.remote.retro

import com.tomer.myflix.data.remote.retro.modals.DtoOmdb
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiOmDBServer {
    @GET("/")
    suspend fun getDetail(
        @Query("i") i :String,
        @Query("apikey") apikey :String = "1bb004b1",
        @Query("plot") plot :String = "full"
    ): DtoOmdb
}
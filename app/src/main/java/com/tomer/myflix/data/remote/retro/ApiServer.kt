package com.tomer.myflix.data.remote.retro

import com.tomer.myflix.data.local.models.ModelFeatured
import com.tomer.myflix.data.remote.retro.modals.Content
import com.tomer.myflix.data.remote.retro.modals.DtoCanPlay
import com.tomer.myflix.data.remote.retro.modals.DtoEpisode
import com.tomer.myflix.data.remote.retro.modals.DtoIds
import com.tomer.myflix.data.remote.retro.modals.DtoMovie
import com.tomer.myflix.data.remote.retro.modals.DtoSeries
import com.tomer.myflix.data.remote.retro.modals.DtoSuggest
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiFlickServer {

    @GET("/api/v1/all/movies")
    suspend fun getAllMoviesPaged(
        @Query("pageNo") pageNumber: Int
    ): DtoIds

    @GET("/api/v1/all/series")
    suspend fun getAllSeriesPaged(
        @Query("pageNo") pageNumber: Int
    ): DtoIds

    @GET("/api/v1/all/episodes")
    suspend fun getAllEpisodesOfSeries(
        @Query("seriesId") seriesId: String,
        @Query("season") season: Int,
    ): List<Content>

    @GET("/api/v1/featured")
    suspend fun getFeatured(): Response<List<ModelFeatured>>


    @GET("/api/v1/movie/{flickId}")
    suspend fun getMovie(
        @Path("flickId") flickId: String
    ): Response<DtoMovie>

    @GET("/api/v1/series/{flickId}")
    suspend fun getSeries(
        @Path("flickId") flickId: String
    ): Response<DtoSeries>

    @GET("/api/v1/episode/{flickId}")
    suspend fun getEpisode(
        @Path("flickId") flickId: String
    ): Response<DtoEpisode>


    @GET("/api/v1/canPlay/movie/{flickId}")
    suspend fun canPlayMovie(
        @Path("flickId") flickId: String
    ): Response<DtoCanPlay>


    @GET("/api/v1/canPlay/episode/{flickId}")
    suspend fun canPlayEpisode(
        @Path("flickId") flickId: String
    ): Response<DtoCanPlay>

    @GET("/api/v1/canPlay/onTV")
    suspend fun canPlayOnTV(): Response<DtoCanPlay>

    @GET("/api/v1/suggest/{flickId}")
    suspend fun getSuggestionList(
        @Path("flickId") flickId: String
    ): Response<List<DtoSuggest>>
}
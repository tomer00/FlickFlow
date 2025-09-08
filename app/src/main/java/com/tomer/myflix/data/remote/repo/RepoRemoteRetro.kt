package com.tomer.myflix.data.remote.repo

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.tomer.myflix.data.local.models.ModelEpisode
import com.tomer.myflix.data.local.models.ModelFeatured
import com.tomer.myflix.data.local.models.ModelMovie
import com.tomer.myflix.data.local.models.ModelSeries
import com.tomer.myflix.data.remote.retro.ApiFlickServer
import com.tomer.myflix.data.remote.retro.ApiOmDBServer
import com.tomer.myflix.data.remote.retro.modals.DtoCanPlay
import com.tomer.myflix.data.remote.retro.modals.DtoOmdb
import com.tomer.myflix.data.remote.retro.modals.DtoSuggest
import com.tomer.myflix.data.remote.retro.utils.toFLOAT
import com.tomer.myflix.data.remote.retro.utils.toINT
import com.tomer.myflix.player.getVibrantCol
import com.tomer.myflix.player.urlToBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject

class RepoRemoteRetro @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiFlickServer: ApiFlickServer,
    private val apiOmDBServer: ApiOmDBServer
) : RepoRemote {
    override suspend fun getMovieModel(flickId: String, imdbId: String): Result<ModelMovie> {
        return withContext(Dispatchers.IO) {
            val (resServer, modOMDB) =
                if (imdbId.isEmpty()) {
                    val modServer = apiFlickServer.getMovie(flickId)
                    val modOMDB: DtoOmdb? = try {
                        apiOmDBServer.getDetail(imdbId)
                    } catch (_: Exception) {
                        null
                    }
                    modServer to modOMDB
                } else {
                    val modServer = async { apiFlickServer.getMovie(flickId) }
                    val modOMDB = async {
                        try {
                            apiOmDBServer.getDetail(imdbId)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    modServer.await() to modOMDB.await()
                }

            if (resServer.isSuccessful.not())
                return@withContext Result.failure(Exception(resServer.errorBody().toString()))
            val modServer = resServer.body()
                ?: return@withContext Result.failure(Exception("NETWORK ERROR MOD SERVER"))

            if (modOMDB == null) return@withContext Result.failure(Exception("NETWORK ERROR MOD OMDB"))
            if (modOMDB.response != "True")
                return@withContext Result.failure(Exception("FAILED TO SCRAPE"))

            val col = try {
                getVibrantCol(modServer.posterHorizontal.urlToBitmap(context))
            } catch (_: Exception) {
                Color.Red.toArgb()
            }
            Result.success(
                ModelMovie(
                    flickId, imdbId, modServer.title,
                    releaseDate = modServer.releasedTime,
                    runtimeSecs = modServer.runtimeSeconds,
                    year = modOMDB.year.toINT(2025),
                    rating = modOMDB.imdbRating.toFLOAT(5.0f),
                    country = modOMDB.country,
                    summary = modOMDB.plot,
                    director = modOMDB.director,
                    writer = modOMDB.writer,
                    cast = modOMDB.actors.split(",").map { it.trim() },
                    screenShots = modServer.screenShots,
                    posterHorizontal = modServer.posterHorizontal,
                    posterVertical = modServer.posterVertical,
                    introTime = modServer.skipIntro,
                    serverNo = modServer.serverNo,
                    isHd = modServer.isHD,
                    accentCol = col,
                    audioTracks = modServer.audioTracks,
                    trailerUrl = modServer.trailerUrl,
                )
            )
        }
    }

    override suspend fun getEpisodeModel(flickId: String, imdbId: String): Result<ModelEpisode> {
        return withContext(Dispatchers.IO) {
            val resServer = apiFlickServer.getEpisode(flickId)

            if (resServer.isSuccessful.not())
                return@withContext Result.failure(Exception(resServer.errorBody().toString()))
            val modServer = resServer.body()
                ?: return@withContext Result.failure(Exception("NETWORK ERROR"))

            Result.success(
                ModelEpisode(
                    flickId, modServer.seriesFlickId, imdbId,
                    title = modServer.title,
                    introTime = modServer.skipIntro,
                    serverNo = modServer.serverNo,
                    releaseDate = modServer.releasedTime,
                    season = modServer.season,
                    episode = modServer.episode,
                    audioTracks = modServer.audioTracks,
                    runtimeSecs = modServer.runtimeSeconds
                )
            )
        }
    }

    override suspend fun getSeriesModel(flickId: String, imdbId: String): Result<ModelSeries> {
        return withContext(Dispatchers.IO) {
            val (resServer, modOMDB) =
                if (imdbId.isEmpty()) {
                    val modServer = apiFlickServer.getSeries(flickId)
                    val modOMDB: DtoOmdb? = try {
                        apiOmDBServer.getDetail(imdbId)
                    } catch (_: Exception) {
                        null
                    }
                    modServer to modOMDB
                } else {
                    val modServer = async { apiFlickServer.getSeries(flickId) }
                    val modOMDB = async {
                        try {
                            apiOmDBServer.getDetail(imdbId)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    modServer.await() to modOMDB.await()
                }

            if (resServer.isSuccessful.not())
                return@withContext Result.failure(Exception(resServer.errorBody().toString()))
            val modServer = resServer.body()
                ?: return@withContext Result.failure(Exception("NETWORK ERROR"))

            if (modOMDB == null) return@withContext Result.failure(Exception("NETWORK ERROR"))
            if (modOMDB.response != "True")
                return@withContext Result.failure(Exception("FAILED TO SCRAPE"))
            val col = try {
                getVibrantCol(modServer.posterHorizontal.urlToBitmap(context))
            } catch (_: Exception) {
                Color.Red.toArgb()
            }
            Result.success(
                ModelSeries(
                    flickId, imdbId, modServer.title,
                    releaseDate = modServer.releasedTime,
                    year = modOMDB.year.toINT(2025),
                    rating = modOMDB.imdbRating.toFLOAT(5.0f),
                    country = modOMDB.country,
                    summary = modOMDB.plot,
                    director = modOMDB.director,
                    writer = modOMDB.writer,
                    cast = modOMDB.actors.split(",").map { it.trim() },
                    screenShots = modServer.screenShots,
                    posterHorizontal = modServer.posterHorizontal,
                    posterVertical = modServer.posterVertical,
                    seasonCount = modServer.seasonCount,
                    accentCol = col
                )
            )
        }
    }

    override suspend fun getFeaturedList(): List<ModelFeatured> {
        val resFea = try {
            apiFlickServer.getFeatured()
        } catch (_: Exception) {
            retrofit2.Response.error<List<ModelFeatured>>(400, "".toResponseBody(null))
        }
        if (resFea.isSuccessful) return resFea.body() ?: emptyList()
        return emptyList()
    }

    override suspend fun getCanPlay(
        flickId: String,
        isMovie: Boolean
    ): Boolean {
        val res = try {
            if (isMovie)
                apiFlickServer.canPlayMovie(flickId)
            else
                apiFlickServer.canPlayEpisode(flickId)
        } catch (_: Exception) {
            retrofit2.Response.error<DtoCanPlay>(400, "".toResponseBody(null))
        }
        return if (res.isSuccessful) {
            res.body()?.canPlay == true
        } else false
    }

    override suspend fun getSuggestionList(flickId: String): List<DtoSuggest> {
        val resSuggestion = try {
            apiFlickServer.getSuggestionList(flickId)
        } catch (_: Exception) {
            retrofit2.Response.error<List<DtoSuggest>>(400, "".toResponseBody(null))
        }
        if (resSuggestion.isSuccessful) return resSuggestion.body() ?: emptyList()
        return emptyList()
    }

}
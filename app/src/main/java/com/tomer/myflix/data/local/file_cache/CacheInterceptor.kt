package com.tomer.myflix.data.local.file_cache

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.BufferedSink
import okio.buffer
import okio.sink
import java.io.ByteArrayOutputStream
import java.io.File

class CacheInterceptor(
    val context: Context
) : Interceptor {

    private var id: String? = null
    private val MAX_PREV_COUNT = 9

    private var idFolder = getCacheDir(context, "tmp")

    fun setId(id: String) {
        this.id = id
        idFolder = getCacheDir(context, id)
    }

    fun getPacketNo(uri: String): Int {
        val i = uri.indexOf(".ts")
        if (i < 0) return -1
        val s = uri.substring(i - 4, i)
        return s.toInt()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        if (id == null) chain.proceed(chain.request())
        val request = chain.request()
        val url = request.url.toString()


        //Handling of .m3u8 Files

        return if (url.endsWith(".m3u8")) {
            if (url.endsWith("master.m3u8"))
                returnResponse(
                    File(idFolder, "master.m3u8"),
                    request, chain
                )
            else {
                val name = url.substringAfter("$id/").replace('/', '-')
                returnResponse(
                    File(idFolder, name),
                    request, chain, true
                )
            }


            ////////////////////////////////////////////////////////////////////////////////////////
            ///////////////////////////HANDLING OF TS FILES/////////////////////////////////////////

        } else if (url.endsWith(".ts")) {
            val name = url.substringAfter("$id/")
            val fileTs = File(idFolder, name)
            if (fileTs.exists()) return returnResponse(fileTs, request, chain)

            val parentFol = fileTs.parentFile!! //{ID}/1080 --- {ID}/audio0 etc
            parentFol.mkdirs()
            val curPacketNo = getPacketNo(name)

            val filesTs =
                parentFol.listFiles { it -> it.name.endsWith(".ts") } ?: return returnResponse(
                    fileTs, request, chain
                )
            for (file in filesTs) {
                val packetNo = getPacketNo(file.name)
                if (packetNo > curPacketNo) continue
                if (packetNo == 0 || packetNo == 1) continue
                if (curPacketNo - packetNo < MAX_PREV_COUNT) continue
                file.delete().also { Log.d("TAG--", "DELETE: $url ${idFolder.name}/${file.name}") }
            }
            returnResponse(fileTs, request, chain)
        } else chain.proceed(request)
    }

    private fun returnResponse(
        cachedFile: File,
        request: Request,
        chain: Interceptor.Chain,
        doCheckEndTag: Boolean = false
    ): Response {
        return if (cachedFile.exists() && cachedFile.length() > 0) {
            // Serve cached file
            Log.d("TAG--", "returnResponseFrom---Cache: ${cachedFile.canonicalPath}")
            val responseBody = ResponseBody.create(null, cachedFile.readBytes())
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(responseBody)
                .build()
        } else {
            // Fetch from network, save to cache, and return response
            val response = chain.proceed(request)
            val bufferSize = 4 * 1024 // 4KB buffer
            val byteStream = ByteArrayOutputStream()
            val bufferedSink: BufferedSink = cachedFile.sink().buffer()
            Log.d(
                "TAG--", "returnResponseFrom---Network: ${
                    response.headers["Cf-Cache-Status"] ?: "NaN"
                        .removePrefix("[").removeSuffix("]")
                } ${
                    request.url.toString()
                        .substring(request.url.toString().indexOf(".in/") + 4)
                }"
            )
            response.body?.let { responseBody ->
                responseBody.byteStream().use { input ->
                    val buffer = ByteArray(bufferSize)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        byteStream.write(buffer, 0, bytesRead)
                        bufferedSink.write(buffer, 0, bytesRead)
                    }
                }
                bufferedSink.close()
                if (doCheckEndTag) {
                    var haveEnd = false
                    cachedFile.bufferedReader().use {
                        while (it.ready()) {
                            val line = it.readLine().trim()
                            if (line.isEmpty()) continue
                            if (line.startsWith("#EXT-X-ENDLIST")) haveEnd = true
                        }
                    }
                    if (!haveEnd) cachedFile.delete()
                }

                return response.newBuilder()
                    .body(ResponseBody.create(responseBody.contentType(), byteStream.toByteArray()))
                    .build()
            }
            response
        }
    }
}
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
import java.util.LinkedList
import java.util.Queue

class CacheInterceptor(
    val context: Context
) : Interceptor {

    private var id: String? = null

    private var idFolder = getCacheDir(context, "tmp")
    private val queueVideoTSFiles: Queue<String> = LinkedList<String>()
    private val queueAudioTSFiles: Queue<String> = LinkedList<String>()

    fun setId(id: String) {
        this.id = id
        idFolder = getCacheDir(context, id)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        if (id == null) chain.proceed(chain.request())
        val request = chain.request()
        val url = request.url.toString()

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
        } else if (url.endsWith(".ts")) {
            val name = url.substringAfter("$id/").replace('/', '-')
            if (name.contains("audio")) {
                queueAudioTSFiles.offer(name)
                if (queueAudioTSFiles.size >= 6) File(idFolder, queueAudioTSFiles.poll()!!).delete()
            } else {
                queueVideoTSFiles.offer(name)
                if (queueVideoTSFiles.size >= 6) {
                    File(idFolder, queueVideoTSFiles.poll()!!).delete()
                    val filesTs = idFolder.listFiles { it -> it.name.endsWith(".ts") }
                    filesTs?.forEach { file ->
                        if (!queueVideoTSFiles.contains(file.name)
                            && !queueAudioTSFiles.contains(file.name)
                        ) file.delete()
                    }
                }
            }
            val res = returnResponse(
                File(idFolder, name),
                request, chain
            )
            res
        } else chain.proceed(request)
    }

    private fun returnResponse(
        cachedFile: File,
        request: Request,
        chain: Interceptor.Chain,
        doCheckEndTag: Boolean = false
    ): Response {
        return if (cachedFile.exists()) {
            // Serve cached file
            Log.d("TAG--", "returnResponseFrom---Cache: ${cachedFile.name}")
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
                    response.headers["Cf-Cache-Status"] ?: "NaN".toString()
                        .removePrefix("[").removeSuffix("]")
                } ${
                    if (request.url.toString().contains("himu.in"))
                        request.url.toString()
                            .substring(request.url.toString().indexOf(".in/") + 4)
                    else request.url.toString()
                        .replace("https://storage.googleapis.com/flick-pub/", "")
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
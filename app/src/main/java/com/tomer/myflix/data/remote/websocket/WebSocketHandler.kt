package com.tomer.myflix.data.remote.websocket

import android.util.Log
import com.tomer.myflix.common.getRandomName
import com.tomer.myflix.common.linkWebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

class WebSocketHandle @Inject constructor(

) {

    //region HANDEL FLOWS
//    val flowMsgs = MutableSharedFlow<MsgsFlowState>()
    val flowConnection = MutableSharedFlow<Boolean>()

    //endregion HANDEL FLOWS

    //region GLOBALS

    private var webSocket: WebSocket? = null
    private var closedByActivityEnd = false
    private val scope = CoroutineScope(Dispatchers.IO)
    private var deviceName: String = "Phone ${getRandomName()}"

    private val webSocketListener = object : WebSocketListener() {
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            tryReconnectAfter2Sec()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            tryReconnectAfter2Sec()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("TAG--", "onMessage: $text")
            scope.launch {
                try {
                    //TODO
                    if (text == "PONG") {
                        lastReceivedPong = System.currentTimeMillis()
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.e("TAG--", "onMessage: ", e)
                }
            }
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            CoroutineScope(Dispatchers.IO).launch {
                flowConnection.emit(true)
                backOffMultiplier = 0
                Log.d("TAG--", "onOpen: ")
            }
        }
    }

    private var backOffMultiplier = 0
    private fun tryReconnectAfter2Sec() {
        if (closedByActivityEnd) {
            webSocket = null
            return
        }
        retryJob = CoroutineScope(Dispatchers.IO).launch {
            backOffMultiplier++
            Log.d("TAG--", "tryReconnectAfter2Sec: ")
            webSocket = null
            delay(2000L * backOffMultiplier)
            openConnection(deviceName)
        }
    }

    private var pingingJob: Job = createNewPingingJob()
    private var retryJob: Job? = null


    private var lastReceivedPong = System.currentTimeMillis()
    private fun createPongCheckJob(): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            delay(30_000L)
            if ((System.currentTimeMillis() - lastReceivedPong) > 30_000L)
                webSocket?.close(1000, "PONG NOT RECEIVED")
        }
    }

    private fun createNewPingingJob(): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(150_000)
                webSocket?.let {
                    it.send("PING")
                    createPongCheckJob()
                }
            }
        }
    }

    //endregion GLOBALS


    //region COMMU

    fun sendMessage(text: String) {
        if (webSocket == null) {
            scope.launch {
                openConnection(deviceName)
            }
            return
        }
        webSocket?.send(text)
    }

    suspend fun openConnection(name: String) {
        this.deviceName = name
        closedByActivityEnd = false
        if (webSocket != null) return
        withContext(Dispatchers.IO) {
            webSocket = try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(linkWebSocket)
                    .addHeader("X-NAME", deviceName)
                    .addHeader("X-TYPE", "MOBILE")
                    .build()
                pingingJob.cancel()
                pingingJob = createNewPingingJob()
                client.newWebSocket(request, webSocketListener)
            } catch (e: Exception) {
                pingingJob.cancel()
                null
            }
        }
    }

    @Throws(Exception::class)
    fun closeConnection() {
        closedByActivityEnd = true
        try {
            webSocket!!.close(1001, "Activity closed")
        } catch (_: Exception) {
        }
        pingingJob.cancel()
        retryJob?.cancel()
    }

    //endregion COMMU
}

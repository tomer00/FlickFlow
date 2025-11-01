package com.tomer.myflix.data.remote.retro.utils

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.math.pow

class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var attemptCount = 0
        var exception: Exception? = null

        while (attemptCount < maxRetries) {
            try {
                attemptCount++
                response = chain.proceed(request)
                if (response.isSuccessful) {
                    return response
                } else if (!shouldRetry(response)) {
                    return response
                }
            } catch (e: SocketTimeoutException) {
                exception = e
            } catch (e: IOException) {
                exception = e
            }
            response?.close()
            // Wait before retrying
            if (attemptCount < maxRetries) {
                try {
                    Thread.sleep(calculateBackoffDelay(attemptCount))
                } catch (e: InterruptedException) {
                    // Ignore, don't care about interrupting sleep
                }
            }
        }

        if (exception != null) {
            exception.printStackTrace()
            throw exception
        }
        // Return the latest failed response if all retries failed
        return response ?: chain.proceed(request).newBuilder().code(500)
            .body("Retry Failed".toResponseBody()).build()
    }

    private fun shouldRetry(response: Response): Boolean {
        // You can customize this logic to determine if a retry is appropriate
        // based on the HTTP status code or other response details.
        return response.code in 500..599 // Retry for 5xx errors (server errors)
    }

    private fun calculateBackoffDelay(attempt: Int): Long {
        // Implement exponential backoff (e.g., 1s, 2s, 4s, ...)
        return (1000L * 2.0.pow(attempt.toDouble())).toLong()
    }
}
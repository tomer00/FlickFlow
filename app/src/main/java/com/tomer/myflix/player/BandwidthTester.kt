package com.tomer.myflix.player

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import java.net.URLConnection
import kotlin.system.measureTimeMillis

class BandwidthTester {

    /**
     * Tests the available bandwidth and returns an integer representing the suggested video quality.
     *
     * This function performs a simple bandwidth test by downloading a small file and measuring the time it takes.
     * Based on the download speed, it suggests a suitable video quality.
     *
     * @param testFileUrl The URL of a small file to use for the bandwidth test.
     * @param fileSizeInBytes The size of the test file in bytes.
     * @return An integer representing the suggested video quality (360, 480, 720, 1080, or 2160).
     */
    suspend fun checkPlayableQuality(
        testFileUrl: String = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        fileSizeInBytes: Long = 1024 * 1024 * 4 // 1MB as default
    ): Int = withContext(Dispatchers.IO) {
        val downloadTimeMillis = measureDownloadTime(testFileUrl, fileSizeInBytes)
        val bandwidthKbps = calculateBandwidth(fileSizeInBytes, downloadTimeMillis)
        suggestQuality(bandwidthKbps)
    }

    /**
     * Measures the time it takes to download a file from the given URL.
     *
     * @param fileUrl The URL of the file to download.
     * @param fileSizeInBytes The expected size of the file in bytes.
     * @return The time it took to download the file in milliseconds.
     * @throws IOException If there is an error during the download.
     */
    private fun measureDownloadTime(fileUrl: String, fileSizeInBytes: Long): Long {
        var connection: URLConnection? = null
        return try {
            val url = URL(fileUrl)
            connection = url.openConnection()
            connection.connect()

            val startTime = System.currentTimeMillis()
            val inputStream = connection.getInputStream()
            val buffer = ByteArray(4096)
            var bytesRead: Int
            var totalBytesRead: Long = 0
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                totalBytesRead += bytesRead
                if (totalBytesRead >= fileSizeInBytes) {
                    break
                }
            }
            System.currentTimeMillis() - startTime
        } catch (e: IOException) {
            throw IOException("Error during bandwidth test: ${e.message}", e)
        } finally {
            if (connection != null) {
                try {
                    connection.getInputStream().close()
                } catch (_: IOException) {
                }
            }
        }
    }

    /**
     * Calculates the bandwidth in kilobits per second (Kbps).
     *
     * @param fileSizeInBytes The size of the downloaded file in bytes.
     * @param downloadTimeMillis The time it took to download the file in milliseconds.
     * @return The calculated bandwidth in Kbps.
     */
    private fun calculateBandwidth(fileSizeInBytes: Long, downloadTimeMillis: Long): Double {
        if (downloadTimeMillis == 0L) {
            return Double.POSITIVE_INFINITY // Avoid division by zero
        }
        val fileSizeInKilobits = fileSizeInBytes * 8 / 1000.0
        return fileSizeInKilobits / (downloadTimeMillis / 1000.0)
    }

    /**
     * Suggests a video quality based on the available bandwidth.
     *
     * @param bandwidthKbps The available bandwidth in Kbps.
     * @return An integer representing the suggested video quality (360, 480, 720, 1080, or 2160).
     */
    private fun suggestQuality(bandwidthKbps: Double): Int {
        Log.d("TAG--", "suggestQuality: $bandwidthKbps")
        return when {
            bandwidthKbps >= 15000 -> 2160 // 4K
            bandwidthKbps >= 5000 -> 1080 // Full HD
            bandwidthKbps >= 2500 -> 720  // HD
            bandwidthKbps >= 1000 -> 480  // SD
            else -> 360 // Low quality
        }.also { Log.d("TAG--", "suggestQuality: $it") }
    }
}
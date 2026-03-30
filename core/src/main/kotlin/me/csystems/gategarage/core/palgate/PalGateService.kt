package me.csystems.gategarage.core.palgate

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

class PalGateService(
    private val sessionToken: ByteArray,
    private val phoneNumber: Long,
    private val tokenType: Int,
    private val deviceId: String,
    debugLogging: Boolean = false,
    private val baseUrl: String = "https://api1.pal-es.com"
) {
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", "okhttp/4.9.3")
                    .build()
            )
        }
        .apply {
            if (debugLogging) addInterceptor(
                HttpLoggingInterceptor(::println).apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
        }
        .build()

    fun openGate(): Result<Unit> = runCatching {
        val (baseId, outputNum) = splitDeviceId(deviceId)
        val token = PalGateTokenGenerator.generate(sessionToken, phoneNumber, tokenType)
        val url = "$baseUrl/v1/bt/device/$baseId/open-gate?openBy=100&outputNum=$outputNum"

        client.newCall(
            Request.Builder().url(url).header("X-Bt-Token", token).build()
        ).execute().use { response ->
            if (!response.isSuccessful)
                throw Exception("API error ${response.code}: ${response.message}")
        }
    }

    private fun splitDeviceId(id: String): Pair<String, Int> {
        val colon = id.indexOf(':')
        return if (colon >= 0) id.substring(0, colon) to id.substring(colon + 1).toInt()
        else id to 1
    }
}

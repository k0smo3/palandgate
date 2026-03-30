package me.csystems.gategarage.app

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

data class Credentials(
    val sessionToken: ByteArray,
    val phoneNumber: Long,
    val tokenType: Int,
    val deviceId: String
)

class CredentialsStore(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "gate_credentials",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(tokenHex: String, phoneNumber: String, tokenType: Int, gateDeviceId: String) {
        prefs.edit()
            .putString(KEY_TOKEN, tokenHex.uppercase())
            .putString(KEY_PHONE, phoneNumber)
            .putInt(KEY_TOKEN_TYPE, tokenType)
            .putString(KEY_DEVICE_ID, gateDeviceId.trim())
            .apply()
    }

    fun load(): Credentials? {
        val tokenHex  = prefs.getString(KEY_TOKEN, null)     ?: return null
        val phoneStr  = prefs.getString(KEY_PHONE, null)     ?: return null
        val tokenType = prefs.getInt(KEY_TOKEN_TYPE, -1).takeIf { it in 0..2 } ?: return null
        val deviceId  = prefs.getString(KEY_DEVICE_ID, null) ?: return null

        if (tokenHex.length != 32) return null
        val phone = phoneStr.toLongOrNull() ?: return null

        return Credentials(
            sessionToken = tokenHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray(),
            phoneNumber  = phone,
            tokenType    = tokenType,
            deviceId     = deviceId
        )
    }

    fun loadRaw() = RawCredentials(
        sessionToken = prefs.getString(KEY_TOKEN, "") ?: "",
        phoneNumber  = prefs.getString(KEY_PHONE, "")  ?: "",
        tokenType    = prefs.getInt(KEY_TOKEN_TYPE, 1),
        gateDeviceId = prefs.getString(KEY_DEVICE_ID, "") ?: ""
    )

    data class RawCredentials(
        val sessionToken: String,
        val phoneNumber: String,
        val tokenType: Int,
        val gateDeviceId: String
    )

    companion object {
        private const val KEY_TOKEN      = "sessionToken"
        private const val KEY_PHONE      = "phoneNumber"
        private const val KEY_TOKEN_TYPE = "tokenType"
        private const val KEY_DEVICE_ID  = "deviceId"
    }
}

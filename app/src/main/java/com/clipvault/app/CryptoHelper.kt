package com.clipvault.app

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object CryptoHelper {
    private const val PREF_NAME = "clipvault_secure_prefs"
    private const val VAULT_KEY = "encrypted_clips_payload"

    private fun getPreferences(context: Context) = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_GCM,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        null
    }

    fun saveVaultData(context: Context, jsonPayload: String) {
        val prefs = getPreferences(context) ?: return
        prefs.edit().putString(VAULT_KEY, jsonPayload).apply()
    }

    fun loadVaultData(context: Context): String {
        val prefs = getPreferences(context) ?: return "[]"
        return prefs.getString(VAULT_KEY, "[]") ?: "[]"
    }
}

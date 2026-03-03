package com.module.notelycompose.ai.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

private const val PREFS_FILE = "ai_secure_prefs"
private const val KEY_API_KEY = "openai_api_key"
private const val KEY_ORG_ID = "openai_org_id"

class EncryptedKeyStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getApiKey(): String? = prefs.getString(KEY_API_KEY, null)?.takeIf { it.isNotBlank() }

    fun setApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key).apply()
    }

    fun getOrgId(): String? = prefs.getString(KEY_ORG_ID, null)?.takeIf { it.isNotBlank() }

    fun setOrgId(orgId: String) {
        prefs.edit().putString(KEY_ORG_ID, orgId).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}

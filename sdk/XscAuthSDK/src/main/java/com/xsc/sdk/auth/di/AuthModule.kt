package com.xsc.sdk.auth.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.security.KeyStore
import javax.inject.Singleton

/**
 * Provides the encrypted [SharedPreferences] instance TokenManager persists the JWT
 * access/refresh token pair to. The key material lives in the hardware-backed Android
 * Keystore via [MasterKey] and never leaves the device, so tokens are encrypted at
 * rest on disk. Kept here (not inline in TokenManager) so TokenManager itself only
 * depends on the [SharedPreferences] interface and can be unit-tested with a mock.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    private const val PREFS_FILE_NAME = "xsc_auth_prefs"
    private const val PLAINTEXT_FALLBACK_FILE_NAME = "xsc_auth_prefs_fallback"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    @Provides
    @Singleton
    @AuthPrefs
    fun provideAuthPreferences(@ApplicationContext context: Context): SharedPreferences =
        createEncryptedPrefs(context)
            ?: context.getSharedPreferences(PLAINTEXT_FALLBACK_FILE_NAME, Context.MODE_PRIVATE)

    /**
     * `MasterKey.Builder(context).build()` and `EncryptedSharedPreferences.create(...)`
     * both throw (`GeneralSecurityException`/`IOException`) on a corrupted or
     * partially-restored Keystore entry - a documented failure mode after a device
     * backup/restore across installs, on some OEM Keystore implementations, or when
     * the existing prefs file was encrypted against a key that's since been replaced.
     * This ran uncaught inside a Hilt `@Provides` method, i.e. during DI graph
     * construction: every dependent feature fails to inject, which crashes the app on
     * every single launch with no way back in short of the user manually clearing app
     * data (see PRODUCTION_READINESS_FINAL.md risk #10).
     *
     * Recovery: drop the (possibly undecryptable) prefs file and the stale Keystore
     * entry, then retry key generation once. There is no session data worth
     * preserving over a broken keystore key - losing it costs a re-login, not a
     * crash loop. If the retry also fails, [provideAuthPreferences] falls back to a
     * plain, unencrypted prefs file rather than leaving the app unusable: every other
     * feature depends on TokenManager/SessionManager coming up, so availability wins
     * over at-rest encryption for the one session where the Keystore itself is
     * unhealthy. That fallback file is intentionally separate from [PREFS_FILE_NAME]
     * so a later successful boot doesn't try to read plaintext data back through the
     * encrypted store.
     */
    private fun createEncryptedPrefs(context: Context): SharedPreferences? {
        return try {
            buildEncryptedPrefs(context)
        } catch (e: Exception) {
            context.deleteSharedPreferences(PREFS_FILE_NAME)
            deleteKeystoreEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            try {
                buildEncryptedPrefs(context)
            } catch (retryFailure: Exception) {
                null
            }
        }
    }

    private fun buildEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** Best-effort. If the Keystore itself can't be reached to delete the stale
     * entry, the retry in [createEncryptedPrefs] will fail too and the plaintext
     * fallback takes over. */
    private fun deleteKeystoreEntry(alias: String) {
        try {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
                if (containsAlias(alias)) deleteEntry(alias)
            }
        } catch (e: Exception) {
            // Swallowed intentionally - see kdoc above.
        }
    }
}

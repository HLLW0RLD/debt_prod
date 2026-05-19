package com.example.debt.data.local

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.MasterKey
import com.example.debt.App
import com.example.debt.domain.utils.LogUtils.errorLog
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object TokenCipher {

    private const val KEY_ALIAS = "token_cipher_key"
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val IV_SIZE_BYTES = 12
    private const val TAG_SIZE_BITS = 128
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private val keyStore by lazy {
        KeyStore.getInstance(ANDROID_KEY_STORE).apply {
            load(null)
        }
    }

    private fun getSecretKey(): SecretKey {
        // Проверяем, существует ли уже ключ
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null)
            if (entry is KeyStore.SecretKeyEntry) {
                return entry.secretKey
            }
        }

        // Создаем новый ключ
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEY_STORE
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    /**
     * Шифрование данных
     */
    fun encrypt(data: String): String {
        if (data.isEmpty()) return ""

        return try {
            val secretKey = getSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val ciphertext = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv
            val ivAndCiphertext = iv + ciphertext

            Base64.encodeToString(ivAndCiphertext, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            errorLog(e)
            ""
        }
    }

    /**
     * Расшифрование данных
     */
    fun decrypt(encryptedData: String): String {
        if (encryptedData.isEmpty()) return ""

        return try {
            val secretKey = getSecretKey()
            val ivAndCiphertext = Base64.decode(encryptedData, Base64.NO_WRAP)

            val iv = ivAndCiphertext.copyOfRange(0, IV_SIZE_BYTES)
            val ciphertext = ivAndCiphertext.copyOfRange(IV_SIZE_BYTES, ivAndCiphertext.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_SIZE_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val plaintext = cipher.doFinal(ciphertext)
            String(plaintext, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            errorLog(e)
            ""
        }
    }
}
package com.mahdisamavat.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.mahdisamavat.core.common.error.AppError
import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.logger.Logger
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlin.apply

class KeyStoreManager(
    private val logger: Logger
) {
    
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TAG = "KeyStoreManager"
        
        const val DATABASE_KEY_ALIAS = "database_encryption_key"
    }
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }
    
    fun getOrCreateDatabaseKey(): Result<SecretKey> {
        return try {
            logger.d(TAG, "Getting or creating database encryption key")
            val key = if (keyStore.containsAlias(DATABASE_KEY_ALIAS)) {
                logger.d(TAG, "Database key exists, retrieving")
                getKey(DATABASE_KEY_ALIAS)
            } else {
                logger.i(TAG, "Database key doesn't exist, generating new key")
                generateKey(DATABASE_KEY_ALIAS)
            }
            
            when (key) {
                is Result.Success -> {
                    logger.i(TAG, "Database key obtained successfully")
                    key
                }
                is Result.Failure -> {
                    logger.e(TAG, "Failed to obtain database key: ${key.error.message}")
                    key
                }
                else -> Result.failure(AppError.Security("Unexpected result type", null, false, "KeyStore"))
            }
        } catch (e: Exception) {
            logger.e(TAG, "Exception while getting/creating database key", e)
            Result.failure(AppError.Security("Failed to get/create database key: ${e.message}", e, true, "KeyStore"))
        }
    }

    
    private fun generateKey(alias: String): Result<SecretKey> {
        return try {
            logger.d(TAG, "Generating new key with alias: $alias")
            
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .setRandomizedEncryptionRequired(true)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            val key = keyGenerator.generateKey()
            
            logger.i(TAG, "Key generated successfully: $alias")
            Result.success(key)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to generate key: $alias", e)
            Result.failure(AppError.Security("Failed to generate key: ${e.message}", e, true, "KeyStore"))
        }
    }
    
    private fun getKey(alias: String): Result<SecretKey> {
        return try {
            logger.d(TAG, "Retrieving key with alias: $alias")
            
            if (!keyStore.containsAlias(alias)) {
                logger.w(TAG, "Key not found: $alias")
                return Result.failure(AppError.Security("Key not found: $alias", null, false, "KeyStore"))
            }
            
            val entry = keyStore.getEntry(alias, null)
            if (entry !is KeyStore.SecretKeyEntry) {
                logger.e(TAG, "Invalid key type for alias: $alias")
                return Result.failure(AppError.Security("Invalid key type for alias: $alias", null, false, "KeyStore"))
            }
            
            val key = entry.secretKey
            logger.d(TAG, "Key retrieved successfully: $alias")
            Result.success(key)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to retrieve key: $alias", e)
            Result.failure(AppError.Security("Failed to retrieve key: ${e.message}", e, true, "KeyStore"))
        }
    }
    


}

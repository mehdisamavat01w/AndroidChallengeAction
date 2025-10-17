package com.mahdisamavat.location.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.core.security.KeyStoreManager
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

object DatabaseBuilder {

    private const val TAG = "DatabaseBuilder"


    fun build(
        context: Context,
        keyStoreManager: KeyStoreManager,
        logger: Logger
    ): AppDatabase {
        logger.i(TAG, "Building encrypted database")

        val keyResult = keyStoreManager.getOrCreateDatabaseKey()

        if (keyResult !is Result.Success) {
            logger.e(TAG, "Failed to get database key from KeyStore")
            throw IllegalStateException("Cannot initialize database without encryption key")
        }

        val key = keyResult.data
        logger.i(TAG, "Database encryption key obtained from KeyStore")

        val passphrase = getOrCreateDatabasePassphrase(context, key, logger)
        logger.d(TAG, "Database passphrase obtained (${passphrase.size} bytes)")

        val factory = SupportFactory(passphrase)
        logger.i(TAG, "SQLCipher factory created")

        val database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    logger.i(TAG, "Database created successfully")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    logger.d(TAG, "Database opened")
                }
            })
            .fallbackToDestructiveMigration(true)
            .build()

        logger.i(TAG, "Encrypted database built successfully")
        logger.i(TAG, "Database path: ${context.getDatabasePath(AppDatabase.DATABASE_NAME)}")

        return database
    }

    fun getDatabaseSize(context: Context): Long {
        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        return if (dbFile.exists()) dbFile.length() else 0
    }

    private fun getOrCreateDatabasePassphrase(
        context: Context,
        keystoreKey: javax.crypto.SecretKey,
        logger: Logger
    ): ByteArray {
        val prefs = context.getSharedPreferences("database_security", Context.MODE_PRIVATE)
        val encryptedPassphraseHex = prefs.getString("encrypted_passphrase", null)
        val ivHex = prefs.getString("passphrase_iv", null)

        return if (encryptedPassphraseHex != null && ivHex != null) {
            logger.d(TAG, "Decrypting existing database passphrase")
            try {
                val encryptedPassphrase = hexToBytes(encryptedPassphraseHex)
                val iv = hexToBytes(ivHex)
                decryptPassphrase(keystoreKey, encryptedPassphrase, iv)
            } catch (e: Exception) {
                logger.w(TAG, "Failed to decrypt passphrase, generating new one", e)
                generateAndStorePassphrase(context, keystoreKey, prefs, logger)
            }
        } else {
            logger.d(TAG, "Generating new database passphrase")
            generateAndStorePassphrase(context, keystoreKey, prefs, logger)
        }
    }

    private fun generateAndStorePassphrase(
        context: Context,
        keystoreKey: javax.crypto.SecretKey,
        prefs: android.content.SharedPreferences,
        logger: Logger
    ): ByteArray {
        val passphrase = ByteArray(32)
        SecureRandom().nextBytes(passphrase)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keystoreKey)
        val iv = cipher.iv
        val encryptedPassphrase = cipher.doFinal(passphrase)

        prefs.edit()
            .putString("encrypted_passphrase", bytesToHex(encryptedPassphrase))
            .putString("passphrase_iv", bytesToHex(iv))
            .apply()

        logger.i(TAG, "Database passphrase generated and encrypted with KeyStore key")
        return passphrase
    }

    private fun decryptPassphrase(
        keystoreKey: javax.crypto.SecretKey,
        encryptedPassphrase: ByteArray,
        iv: ByteArray
    ): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, keystoreKey, spec)
        return cipher.doFinal(encryptedPassphrase)
    }


    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }


    private fun hexToBytes(hex: String): ByteArray {
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}

@file:Suppress("UNUSED")

package com.ub.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * @author https://medium.com/androiddevelopers/using-biometricprompt-with-cryptoobject-how-and-why-aace500ccdb7
 */
@RequiresApi(Build.VERSION_CODES.M)
interface CryptographyManager {

    /**
     * This method first gets or generates an instance of [SecretKey] and then initializes the [Cipher]
     * with the key. The secret key uses [Cipher.ENCRYPT_MODE] is used.
     *
     * @return [Cipher] instance for usage in BiometricPrompt::authenticate
     */
    @kotlin.jvm.Throws(KeyPermanentlyInvalidatedException::class)
    fun getInitializedCipherForEncryption(keyName: String): Cipher

    /**
     * This method first gets or generates an instance of [SecretKey] and then initializes the [Cipher]
     * with the key. The secret key uses [Cipher.DECRYPT_MODE] is used.
     *
     * @return [Cipher] instance for usage in BiometricPrompt::authenticate
     */
    @kotlin.jvm.Throws(KeyPermanentlyInvalidatedException::class)
    fun getInitializedCipherForDecryption(keyName: String, initializationVector: ByteArray): Cipher

    /**
     * The [Cipher] created with [getInitializedCipherForEncryption] is used here
     *
     * @param cipher instance must be received from BiometricPrompt.AuthenticationResult
     */
    fun encryptData(plaintext: String, cipher: Cipher): EncryptedData

    /**
     * The [Cipher] created with [getInitializedCipherForDecryption] is used here
     *
     * @param cipher instance must be received from BiometricPrompt.AuthenticationResult
     */
    fun decryptData(ciphertext: ByteArray, cipher: Cipher): String
}

@RequiresApi(Build.VERSION_CODES.M)
fun CryptographyManager(): CryptographyManager = CryptographyManagerImpl()

@RequiresApi(Build.VERSION_CODES.M)
class CryptographyManagerImpl(
    private val keystore: String = "AndroidKeyStore",
    private val encryptionBlockMode: String = KeyProperties.BLOCK_MODE_GCM,
    private val encryptionPadding: String = KeyProperties.ENCRYPTION_PADDING_NONE,
    private val encryptionAlgorithm: String = KeyProperties.KEY_ALGORITHM_AES,
    private val keySize: Int = 256
) : CryptographyManager {

    override fun getInitializedCipherForEncryption(keyName: String): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(keyName)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    override fun getInitializedCipherForDecryption(keyName: String, initializationVector: ByteArray): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(keyName)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, initializationVector))
        return cipher
    }

    override fun encryptData(plaintext: String, cipher: Cipher): EncryptedData {
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
        return EncryptedData(ciphertext, cipher.iv)
    }

    override fun decryptData(ciphertext: ByteArray, cipher: Cipher): String {
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charset.forName("UTF-8"))
    }

    private fun getCipher(): Cipher {
        val transformation = "$encryptionAlgorithm/$encryptionBlockMode/$encryptionPadding"
        return Cipher.getInstance(transformation)
    }

    private fun getOrCreateSecretKey(keyName: String): SecretKey {
        // If Secretkey was previously created for that keyName, then grab and return it.
        val keyStore = KeyStore.getInstance(keystore)
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        keyStore.getKey(keyName, null)?.let { return it as SecretKey }

        // if you reach here, then a new SecretKey must be generated for that keyName
        val keyGenParams = KeyGenParameterSpec.Builder(keyName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(encryptionBlockMode)
            .setEncryptionPaddings(encryptionPadding)
            .setKeySize(keySize)
            .setUserAuthenticationRequired(true)
            .build()

        val keyGenerator = KeyGenerator.getInstance(encryptionAlgorithm, keystore)
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }
}

data class EncryptedData(
    val ciphertext: ByteArray,
    val initializationVector: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedData) return false

        if (!ciphertext.contentEquals(other.ciphertext)) return false
        if (!initializationVector.contentEquals(other.initializationVector)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ciphertext.contentHashCode()
        result = 31 * result + initializationVector.contentHashCode()
        return result
    }
}
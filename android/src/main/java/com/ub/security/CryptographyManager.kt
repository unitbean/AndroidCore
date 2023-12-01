@file:Suppress("UNUSED")

package com.ub.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.KeyStore
import java.security.KeyStoreException
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.security.auth.x500.X500Principal
import kotlin.math.abs

/**
 * [May also be useful](https://habr.com/ru/companies/vk/articles/776728)
 *
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
    fun encryptData(value: ByteArray, cipher: Cipher): EncryptedData

    /**
     * The [Cipher] created with [getInitializedCipherForDecryption] is used here
     *
     * @param cipher instance must be received from BiometricPrompt.AuthenticationResult
     */
    fun decryptData(ciphertext: ByteArray, cipher: Cipher): ByteArray
}

@RequiresApi(Build.VERSION_CODES.M)
fun CryptographyManager(): CryptographyManager = CryptographyManagerImpl()

@RequiresApi(Build.VERSION_CODES.M)
class CryptographyManagerImpl : CryptographyManager {

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

    override fun encryptData(value: ByteArray, cipher: Cipher): EncryptedData {
        val ciphertext = cipher.doFinal(value)
        return EncryptedData(ciphertext, cipher.iv)
    }

    override fun decryptData(ciphertext: ByteArray, cipher: Cipher): ByteArray {
        return cipher.doFinal(ciphertext)
    }

    private fun getCipher(): Cipher {
        val transformation = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
        return Cipher.getInstance(transformation)
    }

    private fun getOrCreateSecretKey(keyName: String): SecretKey {
        // If Secretkey was previously created for that keyName, then grab and return it.
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        keyStore.getKey(keyName, null)?.let { return it as SecretKey }

        // if you reach here, then a new SecretKey must be generated for that keyName
        val keyGenParams = KeyGenParameterSpec.Builder(keyName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .setCertificateSubject(X500Principal("CN=$keyName"))
            .setCertificateSerialNumber(BigInteger.valueOf(abs(keyName.hashCode()).toLong()))
            .build()

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
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
        return initializationVector.contentEquals(other.initializationVector)
    }

    override fun hashCode(): Int {
        var result = ciphertext.contentHashCode()
        result = 31 * result + initializationVector.contentHashCode()
        return result
    }
}

@kotlin.jvm.Throws(KeyStoreException::class)
fun removeKeyFromKeystore(keyName: String, keyStoreName: String = "AndroidKeyStore") {
    val keyStore = KeyStore.getInstance(keyStoreName)
    keyStore.load(null)
    keyStore.deleteEntry(keyName)
}

fun UUID.asByteArray(): ByteArray = ByteBuffer.wrap(ByteArray(16)).apply {
    putLong(mostSignificantBits)
    putLong(leastSignificantBits)
}.array()
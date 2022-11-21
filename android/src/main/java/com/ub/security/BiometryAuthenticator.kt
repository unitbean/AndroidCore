package com.ub.security

import android.os.Build
import android.os.OperationCanceledException
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.Executor

class BiometryAuthenticator {

    private val biometricPrompt: BiometricPrompt
    private val cryptographyManager: CryptographyManager? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        CryptographyManager()
    } else {
        null
    }
    private var isDecrypt: Boolean? = null
    private var encryptionAwait: CompletableDeferred<EncryptedData?>? = null
    private var decryptionAwait: CompletableDeferred<ByteArray?>? = null
    private var dataToProceed: ByteArray? = null

    private val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
//            if (errorCode != BiometricPrompt.ERROR_USER_CANCELED
//                && errorCode != BiometricPrompt.ERROR_CANCELED
//                && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
//            ) {
//
//            }
            isDecrypt = null
            dataToProceed = null
            val exception = CancellationException("Biometry error: $errString", null)
            encryptionAwait?.cancel(exception)
            decryptionAwait?.cancel(exception)
            encryptionAwait = null
            decryptionAwait = null
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            when (isDecrypt) {
                false -> {
                    val encryptedData = result.cryptoObject?.cipher?.let { cipher ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            cryptographyManager?.encryptData(
                                dataToProceed ?: throw NullPointerException("Data to save cannot be null"),
                                cipher
                            )
                        } else null
                    }
                    dataToProceed = null
                    encryptionAwait?.complete(encryptedData)
                    encryptionAwait = null
                    isDecrypt = null
                }
                true -> {
                    val decryptedData = result.cryptoObject?.cipher?.let { cipher ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            cryptographyManager?.decryptData(dataToProceed ?: throw NullPointerException("Data to restore cannot be null"), cipher)
                        } else null
                    }
                    dataToProceed = null
                    decryptionAwait?.complete(decryptedData)
                    decryptionAwait = null
                    isDecrypt = null
                }
                else -> throw IllegalStateException("Unknown type of operation")
            }
        }
    }

    constructor(activity: FragmentActivity) {
        biometricPrompt = BiometricPrompt(activity, this.callback)
    }

    constructor(fragment: Fragment) {
        biometricPrompt = BiometricPrompt(fragment, this.callback)
    }

    constructor(activity: FragmentActivity, executor: Executor) {
        biometricPrompt = BiometricPrompt(activity, executor, this.callback)
    }

    constructor(fragment: Fragment, executor: Executor) {
        biometricPrompt = BiometricPrompt(fragment, executor, this.callback)
    }

    suspend fun authAndSave(
        valueToSave: ByteArray,
        keyName: String,
        title: String,
        negativeText: String,
        subtitle: String? = null,
    ): EncryptedData? {
        this.isDecrypt = false
        this.dataToProceed = valueToSave
        encryptionAwait?.cancel(CancellationException("Encryption process has been canceled", OperationCanceledException()))
        this.encryptionAwait = CompletableDeferred()
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setNegativeButtonText(negativeText)
            .setSubtitle(subtitle)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cipher = cryptographyManager!!.getInitializedCipherForEncryption(keyName = keyName)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } else {
            biometricPrompt.authenticate(promptInfo)
        }

        return encryptionAwait!!.await()
    }

    suspend fun authAndRestore(
        keyName: String,
        encryptedValue: ByteArray,
        iv: ByteArray,
        title: String,
        negativeText: String,
        subtitle: String? = null,
    ): ByteArray? {
        this.isDecrypt = true
        this.dataToProceed = encryptedValue
        decryptionAwait?.cancel(CancellationException("Decryption process has been canceled", OperationCanceledException()))
        this.decryptionAwait = CompletableDeferred()
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setNegativeButtonText(negativeText)
            .setSubtitle(subtitle)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cipher = cryptographyManager!!.getInitializedCipherForDecryption(
                keyName = keyName,
                initializationVector = iv
            )
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } else {
            biometricPrompt.authenticate(promptInfo)
        }
        return decryptionAwait!!.await()
    }

    fun cancel() {
        this.biometricPrompt.cancelAuthentication()
    }
}
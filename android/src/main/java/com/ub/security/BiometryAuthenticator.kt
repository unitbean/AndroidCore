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
            isDecrypt = null
            dataToProceed = null
            val exception = CancellationException(
                null,
                BiomteryAuthenticatorException(
                    errorCode = errorCode,
                    errString = errString
                )
            )
            encryptionAwait?.cancel(exception)
            decryptionAwait?.cancel(exception)
            encryptionAwait = null
            decryptionAwait = null
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            when (isDecrypt) {
                false -> try {
                    val encryptedData = result.cryptoObject?.cipher?.let { cipher ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            cryptographyManager?.encryptData(
                                dataToProceed ?: throw NullPointerException("Data to save cannot be null"),
                                cipher
                            )
                        } else null
                    }
                    encryptionAwait?.complete(encryptedData)
                } catch (e: Exception) {
                    encryptionAwait?.completeExceptionally(e)
                } finally {
                    dataToProceed = null
                    encryptionAwait = null
                    isDecrypt = null
                }
                true -> try {
                    val decryptedData = result.cryptoObject?.cipher?.let { cipher ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            cryptographyManager?.decryptData(dataToProceed ?: throw NullPointerException("Data to restore cannot be null"), cipher)
                        } else null
                    }
                    decryptionAwait?.complete(decryptedData)
                } catch (e: Exception) {
                    decryptionAwait?.completeExceptionally(e)
                } finally {
                    dataToProceed = null
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
    ): Result<EncryptedData?> {
        this.isDecrypt = false
        this.dataToProceed = valueToSave
        val result = try {
            encryptionAwait?.cancel(CancellationException("Encryption process has been canceled", OperationCanceledException()))
            this.encryptionAwait = CompletableDeferred()
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setNegativeButtonText(negativeText)
                .setSubtitle(subtitle)
                .build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val cipher =
                    cryptographyManager!!.getInitializedCipherForEncryption(keyName = keyName)
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            } else {
                biometricPrompt.authenticate(promptInfo)
            }
            encryptionAwait?.await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(result)
    }

    suspend fun authAndRestore(
        keyName: String,
        encryptedValue: ByteArray,
        iv: ByteArray,
        title: String,
        negativeText: String,
        subtitle: String? = null,
    ): Result<ByteArray?> {
        this.isDecrypt = true
        this.dataToProceed = encryptedValue
        val result = try {
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
            decryptionAwait?.await()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(result)
    }

    fun cancel() {
        this.biometricPrompt.cancelAuthentication()
    }
}

data class BiomteryAuthenticatorException(
    val errorCode: Int,
    val errString: CharSequence
) : Throwable(message = errString.toString())
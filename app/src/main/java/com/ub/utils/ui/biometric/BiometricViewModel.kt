package com.ub.utils.ui.biometric

import android.app.Application
import android.os.Build
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ub.security.EncryptedData
import com.ub.security.removeKeyFromKeystore
import com.ub.utils.withUseCaseScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import me.tatarka.inject.annotations.Inject
import timber.log.Timber
import kotlin.text.Charsets.UTF_8

@Inject
class BiometricViewModel(
    application: Application,
    private val dataStore: DataStore<Preferences>,
) : AndroidViewModel(application) {

    val encryptedValueFlow = dataStore.data.map { preferences ->
        val savedEncryptedString = preferences[stringPreferencesKey(ENCRYPTED)] ?: return@map null
        val splittedValue = savedEncryptedString.split(';')
        val initializationVector = Base64.decode(splittedValue[0].toByteArray(UTF_8), Base64.DEFAULT)
        val ciphertext = Base64.decode(splittedValue[1].toByteArray(UTF_8), Base64.DEFAULT)
        EncryptedData(
            ciphertext = ciphertext,
            initializationVector = initializationVector
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow = _errorFlow.asStateFlow()

    private val _toDecryptFlow = MutableSharedFlow<EncryptedData>()
    val toDecryptFlow = _toDecryptFlow.asSharedFlow()

    private val _biometryAvailabilityFlow = MutableStateFlow(false)
    val biometryAvailabilityFlow = _biometryAvailabilityFlow.asStateFlow()

    init {
        withUseCaseScope {
            val biometricManager = BiometricManager.from(application)
            if (biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS) {
                _biometryAvailabilityFlow.emit(true)
            } else {
                dataStore.updateData { preferences ->
                    preferences.toMutablePreferences().apply {
                        remove(stringPreferencesKey(ENCRYPTED))
                    }
                }
            }
        }
    }

    fun saveEncryptedValue(encryptedResult: EncryptedData) {
        withUseCaseScope(
            onError = { error ->
                Timber.e(error, "Biometry encrypt: %s", error.message)
                _errorFlow.emit(error.message)
            }
        ) {
            dataStore.updateData { preferences ->
                preferences.toMutablePreferences().apply {
                    val encryptedValue = Base64.encodeToString(encryptedResult.ciphertext, Base64.DEFAULT)
                    val ivString = Base64.encodeToString(encryptedResult.initializationVector, Base64.DEFAULT)
                    val encryptedString = "$ivString;$encryptedValue"
                    set(stringPreferencesKey(ENCRYPTED), encryptedString)
                }
            }
        }
    }

    fun doDecrypt() {
        withUseCaseScope(
            onError = { error ->
                Timber.e(error, "Biometry decrypt: %s", error.message)
                _errorFlow.emit(error.message)
            }
        ) {
            encryptedValueFlow.value?.let { encryptedValue ->
                _toDecryptFlow.emit(encryptedValue)
            }
        }
    }

    fun onError(error: Throwable) {
        withUseCaseScope {
            Timber.e(error, "Biometry error: %s", error.message)
            _errorFlow.update { error.message ?: error.cause?.message ?: error.toString() }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && error is KeyPermanentlyInvalidatedException) {
                removeKeyFromKeystore(keyName = "androidCore")
                dataStore.updateData { preferences ->
                    preferences.toMutablePreferences().apply {
                        remove(stringPreferencesKey(ENCRYPTED))
                    }
                }
            }
        }
    }

    companion object {
        private const val ENCRYPTED = "encrypted"
    }
}
package com.ub.utils.ui.biometric

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
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

@SuppressLint("StaticFieldLeak")
class BiometricViewModel(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
): ViewModel() {

    val encryptedValueFlow = dataStore.data.map { preferences ->
        EncryptedData(
            ciphertext = preferences[stringPreferencesKey(ENCRYPTED_VALUE)]?.let { Base64.decode(it, Base64.DEFAULT) } ?: return@map null,
            initializationVector = preferences[stringPreferencesKey(IV)]?.let { Base64.decode(it, Base64.DEFAULT) } ?: return@map null
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
            val biometricManager = BiometricManager.from(context)
            if (biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS) {
                _biometryAvailabilityFlow.emit(true)
            } else {
                dataStore.updateData { preferences ->
                    preferences.toMutablePreferences().apply {
                        remove(stringPreferencesKey(ENCRYPTED_VALUE))
                        remove(stringPreferencesKey(IV))
                    }
                }
            }
        }
    }

    fun saveEncryptedValue(encryptedResult: EncryptedData) {
        withUseCaseScope(
            onError = { error -> _errorFlow.emit(error.message) }
        ) {
            dataStore.updateData { preferences ->
                preferences.toMutablePreferences().apply {
                    val encryptedString = Base64.encodeToString(encryptedResult.ciphertext, Base64.DEFAULT)
                    set(stringPreferencesKey(ENCRYPTED_VALUE), encryptedString)
                    val ivString = Base64.encodeToString(encryptedResult.initializationVector, Base64.DEFAULT)
                    set(stringPreferencesKey(IV), ivString)
                }
            }
        }
    }

    fun doDecrypt() {
        withUseCaseScope(
            onError = { error -> _errorFlow.emit(error.message) }
        ) {
            encryptedValueFlow.value?.let { encryptedValue ->
                _toDecryptFlow.emit(encryptedValue)
            }
        }
    }

    fun onError(error: Exception) {
        withUseCaseScope {
            _errorFlow.update { error.message }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && error is KeyPermanentlyInvalidatedException) {
                removeKeyFromKeystore(keyName = "androidCore")
                dataStore.updateData { preferences ->
                    preferences.toMutablePreferences().apply {
                        remove(stringPreferencesKey(ENCRYPTED_VALUE))
                        remove(stringPreferencesKey(IV))
                    }
                }
            }
        }
    }

    companion object {
        private const val ENCRYPTED_VALUE = "encrypted_value"
        private const val IV = "initialization_vector"
    }
}
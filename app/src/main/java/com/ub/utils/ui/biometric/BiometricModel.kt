package com.ub.utils.ui.biometric

import androidx.compose.runtime.Immutable

@Immutable
internal data class BiometricState(
    val error: String = "",
    val rawInput: String = "",
    val encryptedValue: String = ""
)

@Immutable
sealed class BiometricAction {
    data object Encrypt : BiometricAction()
    data object Decrypt : BiometricAction()
}
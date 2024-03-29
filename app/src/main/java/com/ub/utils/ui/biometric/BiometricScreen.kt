package com.ub.utils.ui.biometric

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ub.utils.R

@Composable
internal fun BiometricScreen(
    state: BiometricState,
    onEncryptedTextChange: (String) -> Unit,
    onDecryptAction: (BiometricAction) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = state.error,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(all = 16.dp)
                .fillMaxWidth()
        )
        OutlinedTextField(
            value = state.rawInput,
            label = {
                Text(text = stringResource(id = R.string.text_to_encrypt))
            },
            onValueChange = onEncryptedTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Row {
            Button(
                enabled = state.rawInput.isNotEmpty(),
                onClick = { onDecryptAction.invoke(BiometricAction.Encrypt) },
                modifier = Modifier
                    .weight(1F)
                    .padding(start = 16.dp, end = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.encrypt_action))
            }
            Button(
                enabled = state.encryptedValue.isNotEmpty(),
                onClick = { onDecryptAction.invoke(BiometricAction.Decrypt) },
                modifier = Modifier
                    .weight(1F)
                    .padding(start = 8.dp, end = 16.dp)
            ) {
                Text(text = stringResource(id = R.string.decrypt_action))
            }
        }
        Text(
            text = state.encryptedValue.ifEmpty {
                stringResource(id = R.string.encrypted_text_will_be_here)
            },
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(all = 16.dp)
                .fillMaxWidth()
        )
    }
}
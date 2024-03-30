package com.ub.utils.ui.biometric

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ub.utils.ui.theme.CoreTheme

@Preview(
    device = "id:pixel",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
@Composable
private fun BiometricPreviewLight() {
    CoreTheme {
        BiometricScreen(
            state = BiometricState(
                error = "Error"
            ),
            onEncryptedTextChange = {},
            onDecryptAction = {}
        )
    }
}


@Preview(
    device = "id:pixel",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun BiometricPreviewNight() {
    CoreTheme {
        BiometricScreen(
            state = BiometricState(
                error = "Error"
            ),
            onEncryptedTextChange = {},
            onDecryptAction = {}
        )
    }
}
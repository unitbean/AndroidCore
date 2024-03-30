package com.ub.utils.ui.camera

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ub.utils.ui.theme.CoreTheme

@Composable
@Preview(
    device = "id:pixel",
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true
)
private fun CameraPreviewLight() {
    CameraPreview()
}

@Composable
@Preview(
    device = "id:pixel",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true
)
private fun CameraPreviewNight() {
    CameraPreview()
}

@Composable
private fun CameraPreview() {
    CoreTheme {
        CameraScreen(
            onEvent = {}
        )
    }
}
package com.ub.utils.ui.map

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ub.utils.ui.theme.CoreTheme

@Preview(
    device = "id:pixel",
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true
)
@Composable
private fun MapPreviewLight() {
    MapPreview()
}

@Preview(
    device = "id:pixel",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true
)
@Composable
private fun MapPreviewNight() {
    MapPreview()
}

@Composable
private fun MapPreview() {
    CoreTheme {
        MapScreen()
    }
}
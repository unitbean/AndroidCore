package com.ub.utils.ui.main

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ub.utils.NetworkSpec
import com.ub.utils.ui.theme.CoreTheme
import kotlinx.coroutines.flow.emptyFlow

@Composable
@Preview(
    device = "id:pixel",
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true
)
private fun MainPreviewLight() {
    MainPreview()
}

@Composable
@Preview(
    device = "id:pixel",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true
)
private fun MainPreviewNight() {
    MainPreview()
}

@Composable
private fun MainPreview() {
    CoreTheme {
        MainScreen(
            state = MainState(
                networkSpec = NetworkSpec.Active(isVpn = true)
            ),
            onEvent = {},
            error = emptyFlow()
        )
    }
}
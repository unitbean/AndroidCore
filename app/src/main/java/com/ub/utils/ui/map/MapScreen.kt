package com.ub.utils.ui.map

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import com.ub.utils.R

@Composable
internal fun MapScreen() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        var displayedLocation by remember {
            mutableStateOf("")
        }
        var isMapDisplayed by remember {
            mutableStateOf(false)
        }
        val context = LocalContext.current
        val fragmentContainer = remember {
            FragmentContainerView(context).apply {
                id = View.generateViewId()
            }
        }
        LaunchedEffect(key1 = isMapDisplayed) {
            if (isMapDisplayed) {

            } else {
                displayedLocation = ""
            }
        }
        Text(
            text = if (!LocalInspectionMode.current) {
                displayedLocation
            } else "foo bar baz",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 46.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        )
        AndroidView(
            factory = { fragmentContainer }
        )
        Button(
            onClick = { isMapDisplayed = !isMapDisplayed },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(all = 16.dp)
        ) {
            Text(
                text = stringResource(
                    id = if (isMapDisplayed) {
                        R.string.map_hide
                    } else {
                        R.string.map_show
                    }
                )
            )
        }
    }
}
package com.ub.utils.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import com.ub.utils.NetworkSpec
import com.ub.utils.R
import com.ub.utils.VpnAware

@Composable
internal fun MainScreen(
    state: MainState,
    onEvent: (MainEvent) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (statuses, snow, image, buttons) = createRefs()
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .constrainAs(statuses) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                }
        ) {
            if ((state.networkSpec as? VpnAware)?.isVpn == true) {
                Image(
                    painter = painterResource(id = R.drawable.ic_vector_vpn_key),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground)
                )
            }
            val connectivityIcon = when (state.networkSpec) {
                NetworkSpec.Connecting -> painterResource(id = R.drawable.outline_hourglass_empty_24)
                is NetworkSpec.Active -> painterResource(id = R.drawable.baseline_signal_cellular_alt_24)
                NetworkSpec.Disabled -> painterResource(id = R.drawable.baseline_error_outline_24)
                is NetworkSpec.Captive -> painterResource(id = R.drawable.baseline_login_24)
                else -> painterResource(id = R.drawable.baseline_device_unknown_24)
            }
            Image(
                painter = connectivityIcon,
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground)
            )
        }
        AndroidView(
            factory = {
                SnowImageView(it)
            },
            modifier = Modifier
                .constrainAs(snow) {
                    top.linkTo(statuses.top)
                    bottom.linkTo(image.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .size(size = 200.dp)
                .run {
                    if (LocalInspectionMode.current) {
                        border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(size = 8.dp)
                        )
                    } else this
                }
        )
        if (LocalInspectionMode.current) {
            Image(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                contentScale = ContentScale.Inside,
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(size = 8.dp)
                    )
                    .constrainAs(image) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .size(size = 100.dp)
            )
        } else {
            AsyncImage(
                model = state.displayingImage,
                contentDescription = null,
                modifier = Modifier
                    .constrainAs(image) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .size(size = 100.dp)
            )
        }
        Column(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .constrainAs(buttons) {
                    bottom.linkTo(parent.bottom)
                }
        ) {
            Button(
                onClick = { onEvent.invoke(MainEvent.ClearCache) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.clear_cache))
            }
            Button(
                onClick = { onEvent.invoke(MainEvent.PickImage) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.pick_image))
            }
            Button(
                onClick = { onEvent.invoke(MainEvent.NextPush) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.next_push))
            }
        }
    }
}
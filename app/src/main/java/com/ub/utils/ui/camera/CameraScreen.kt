package com.ub.utils.ui.camera

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ub.camera.CameraExternalStorage
import com.ub.camera.CameraOutputStream
import com.ub.camera.CameraSession
import com.ub.camera.setupExtensions
import com.ub.utils.R
import com.ub.utils.createUriReadyForWrite
import kotlinx.coroutines.launch

@Composable
internal fun CameraScreen(
    onEvent: (CameraEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val scope = rememberCoroutineScope()
        val previewView = remember { PreviewView(context) }
        val cameraSession = remember {
            CameraSession(
                lifecycleOwner = lifecycleOwner,
                previewView = previewView
            )
        }

        LaunchedEffect(cameraSession) {
            previewView.setupExtensions(
                session = cameraSession,
                pinchToZoomEnabled = true,
                tapToFocusEnabled = true
            )
        }

        val cameraState by remember {
            cameraSession.startSession()
        }.collectAsState(initial = com.ub.camera.CameraState())
        var cameraIndex by remember {
            mutableIntStateOf(0)
        }
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Row {
            IconButton(
                onClick = {
                    cameraState.availableCameras.let { cameras ->
                        if (cameras.size.minus(1) == cameraIndex) {
                            cameraIndex = 0
                        } else {
                            cameraIndex += 1
                        }
                        val camera = cameras[cameraIndex]
                        cameraSession.selectCamera(camera)
                    }
                },
                colors = IconButtonDefaults.iconButtonColors().copy(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                modifier = Modifier
                    .padding(all = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_cameraswitch_24),
                    contentDescription = null
                )
            }
            Text(
                text = cameraState.zoom.toString(),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1F)
            )
            IconButton(
                onClick = {
                    scope.launch {
                        cameraSession.toggleFlashlight()
                    }
                },
                colors = IconButtonDefaults.iconButtonColors().copy(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                modifier = Modifier
                    .padding(all = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_flashlight_on_24),
                    contentDescription = null
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(vertical = 8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        val uri = cameraSession.takePhoto(CameraExternalStorage()) ?: return@launch
                        onEvent.invoke(CameraEvent.MakePhotoToInternal(uri))
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.capture_files_action))
            }
            Button(
                onClick = {
                    scope.launch {
                        val uriForWrite = context.createUriReadyForWrite(
                            nameWithExtension = "temp_photo.jpg",
                            authority = "${context.packageName}.core.fileprovider"
                        )
                        val outStream = context.contentResolver.openOutputStream(uriForWrite)
                        val uri = cameraSession.takePhoto(CameraOutputStream(outStream ?: return@launch)) ?: return@launch
                        onEvent.invoke(CameraEvent.MakePhotoToExternal(uri))
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.capture_external_action))
            }
        }
    }
}
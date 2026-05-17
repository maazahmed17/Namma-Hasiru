package com.example.nammahasiru.ui.newplant

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

@Composable
fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    onImageCaptureReady: (ImageCapture) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val executor = remember { ContextCompat.getMainExecutor(context) }

    AndroidView(
        factory = { previewView },
        modifier = modifier,
    )

    DisposableEffect(lifecycleOwner, context) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            bindCameraUseCases(
                context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = previewView,
                cameraProviderFuture = cameraProviderFuture,
                executor = executor,
                onImageCaptureReady = onImageCaptureReady,
            )
        }
        cameraProviderFuture.addListener(listener, executor)

        onDispose {
            runCatching {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                } else {
                    cameraProviderFuture.cancel(true)
                }
            }.onFailure { Log.w("CameraPreview", "Unable to unbind camera", it) }
        }
    }
}

private fun bindCameraUseCases(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    cameraProviderFuture: com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider>,
    executor: java.util.concurrent.Executor,
    onImageCaptureReady: (ImageCapture) -> Unit,
) {
    val cameraProvider = try {
        cameraProviderFuture.get()
    } catch (e: Exception) {
        Log.e("CameraPreview", "Camera provider failed", e)
        return
    }

    val preview = Preview.Builder().build().also {
        it.setSurfaceProvider(previewView.getSurfaceProvider())
    }

    val imageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()

    onImageCaptureReady(imageCapture)

    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
        )
    } catch (e: Exception) {
        Log.e("CameraPreview", "Use case binding failed", e)
    }
}

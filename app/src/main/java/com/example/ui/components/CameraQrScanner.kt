package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * High-performance CameraX preview integrated with ML Kit Barcode Scanning.
 * Detects real QR codes automatically from camera feed or interactive sample scan buttons.
 */
@Composable
fun CameraQrScanner(
    onQrCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isTorchEnabled by remember { mutableStateOf(false) }
    var lastScannedCode by remember { mutableStateOf<String?>(null) }

    // Laser line scanning animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser_anim")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraExecutor = Executors.newSingleThreadExecutor()
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                processImageWithMlKit(
                                    imageProxy = imageProxy,
                                    onQrFound = { qrText ->
                                        if (qrText != lastScannedCode) {
                                            lastScannedCode = qrText
                                            onQrCodeScanned(qrText)
                                        }
                                    }
                                )
                            }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )

                            if (camera.cameraInfo.hasFlashUnit()) {
                                camera.cameraControl.enableTorch(isTorchEnabled)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("camerax_preview_view")
            )

            // Scanning Overlay Frame with animated laser
            Canvas(modifier = Modifier.fillMaxSize()) {
                val boxWidth = size.width * 0.75f
                val boxHeight = size.height * 0.7f
                val left = (size.width - boxWidth) / 2f
                val top = (size.height - boxHeight) / 2f

                // Draw translucent mask outside scanning square
                drawRect(
                    color = Color.Black.copy(alpha = 0.45f)
                )

                // Clear scanning rectangle in center
                drawRect(
                    color = Color.Transparent,
                    topLeft = Offset(left, top),
                    size = Size(boxWidth, boxHeight)
                )

                // Corner target borders
                val strokeWidth = 8.dp.toPx()
                val cornerLength = 32.dp.toPx()
                val cornerColor = Color(0xFF00E676) // Neon Green

                // Top-Left
                drawLine(cornerColor, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
                drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)

                // Top-Right
                drawLine(cornerColor, Offset(left + boxWidth, top), Offset(left + boxWidth - cornerLength, top), strokeWidth)
                drawLine(cornerColor, Offset(left + boxWidth, top), Offset(left + boxWidth, top + cornerLength), strokeWidth)

                // Bottom-Left
                drawLine(cornerColor, Offset(left, top + boxHeight), Offset(left + cornerLength, top + boxHeight), strokeWidth)
                drawLine(cornerColor, Offset(left, top + boxHeight), Offset(left, top + boxHeight - cornerLength), strokeWidth)

                // Bottom-Right
                drawLine(cornerColor, Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth - cornerLength, top + boxHeight), strokeWidth)
                drawLine(cornerColor, Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth, top + boxHeight - cornerLength), strokeWidth)

                // Animated Laser Line
                val laserY = top + (boxHeight * laserYRatio)
                drawLine(
                    color = Color(0xFFFF3D00), // Vibrant Orange/Red Laser
                    start = Offset(left + 12.dp.toPx(), laserY),
                    end = Offset(left + boxWidth - 12.dp.toPx(), laserY),
                    strokeWidth = 4.dp.toPx()
                )
            }

            // Status bar badge overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lastScannedCode == null) "ML Kit Active • Align QR Code in Box" else "QR Detected!",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

        } else {
            // Permission Prompt View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera Permission",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Live CameraX & ML Kit Scanner",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Grant camera permission to automatically scan dining table QR codes & guest loyalty passes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        hasCameraPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    },
                    modifier = Modifier.testTag("enable_camera_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Enable Camera Access")
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageWithMlKit(
    imageProxy: ImageProxy,
    onQrFound: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        val scanner = BarcodeScanning.getClient()
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue ?: barcode.displayValue
                    if (!rawValue.isNullOrBlank()) {
                        onQrFound(rawValue)
                        break
                    }
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

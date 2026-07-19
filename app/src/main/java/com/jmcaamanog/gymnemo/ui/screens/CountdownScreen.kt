package com.jmcaamanog.gymnemo.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text

@Composable
fun CountdownScreen(
    onCountdownFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 6000, easing = LinearEasing)
        )
        onCountdownFinished()
    }

    val count = when {
        progress.value < 0.166f -> 5
        progress.value < 0.333f -> 4
        progress.value < 0.5f -> 3
        progress.value < 0.666f -> 2
        progress.value < 0.833f -> 1
        else -> 0 // ¡GO!
    }

    // Efecto de latido/pulso para el número que cambia
    LaunchedEffect(count) {
        scale.snapTo(0.8f)
        scale.animateTo(
            targetValue = 1.4f,
            animationSpec = tween(durationMillis = 400)
        )
    }

    ScreenScaffold(timeText = {}) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Aro de progreso de fondo y activo (Verde Neón)
            Canvas(modifier = Modifier.size(130.dp)) {
                // Fondo gris apagado
                drawArc(
                    color = Color.Gray.copy(alpha = 0.15f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 5.dp.toPx())
                )
                // Arco de progreso verde neón
                val sweep = 360f * (progress.value / 0.833f).coerceAtMost(1f)
                drawArc(
                    color = Color(0xFF39FF14),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            if (count > 0) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF39FF14), // Verde Neón
                    modifier = Modifier.graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value
                    )
                )
            } else {
                Text(
                    text = "¡GO!",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 32.sp,
                    modifier = Modifier.graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value
                    )
                )
            }
        }
    }
}


package com.jmcaamanog.gymnemo.ui.screens

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jmcaamanog.gymnemo.viewmodel.WorkoutViewModel
import androidx.compose.animation.core.animateFloat

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun ActiveWorkoutScreen(
    viewModel: WorkoutViewModel,
    isAmbientMode: Boolean = false,
    onStopClicked: () -> Unit
) {
    val state by viewModel.workoutState.collectAsState()
    val haptic = LocalHapticFeedback.current

    val minutes = state.durationSeconds / 60
    val seconds = state.durationSeconds % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    // Animación de latido para el corazón (desactivada en modo ambiente)
    val infiniteTransition = rememberInfiniteTransition(label = "latido")
    val animatedHeartScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escalaCorazon"
    )
    val heartScale = if (isAmbientMode) 1.0f else animatedHeartScale
    val heartColor = if (isAmbientMode) Color.Gray else Color(0xFFFF2E56)
    val stopButtonColor = if (isAmbientMode) Color.DarkGray else Color(0xFFFF2E56)

    // Alerta de Ritmo Cardíaco Límite (85% de 220 - edad)
    val maxHR = 220 - state.birthYear
    val limitHR = (maxHR * 0.85).toInt()
    val hrAlertActive = !isAmbientMode && state.heartRate > limitHR

    val alertBgColor by infiniteTransition.animateColor(
        initialValue = Color.Black,
        targetValue = Color(0xFF6B0000),
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alertColor"
    )
    val backgroundColor = if (hrAlertActive) alertBgColor else Color.Black

    // Resplandor Neon por Frecuencia Cardíaca
    val neonGlowColor = when {
        state.heartRate >= 140 -> Color(0xFFFF007F) // Magenta
        state.heartRate >= 120 -> Color(0xFFFFEB3B) // Yellow
        else -> Color(0xFF00E5FF) // Cyan
    }
    val neonGlowPulse by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.6f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )
    val borderOpacity = if (isAmbientMode) 0f else neonGlowPulse

    // Ritmo/Tempo dinámico (guía visual de 4 segundos)
    val phaseTime = (state.durationSeconds % 4L).toInt()
    val tempoPhaseText = when (phaseTime) {
        0, 1 -> "ECC: BAJAR (2s)"
        2 -> "ISO: PAUSA (1s)"
        else -> "CON: SUBIR (1s)"
    }
    val tempoColor = when (phaseTime) {
        0, 1 -> Color(0xFF00E5FF) // Cyan
        2 -> Color(0xFFFFEB3B) // Yellow
        else -> Color(0xFFFF007F) // Magenta
    }

    LaunchedEffect(hrAlertActive) {
        if (hrAlertActive) {
            while (true) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(1000)
            }
        }
    }

    ScreenScaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .border(
                    width = 4.dp,
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, neonGlowColor.copy(alpha = borderOpacity)),
                        radius = 280f
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Spacer(modifier = Modifier.height(18.dp))

                // 1. Temporizador en la parte superior
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 2. Nombre del ejercicio en el centro (parte más ancha del círculo)
                Text(
                    text = state.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)
                )

                if (!isAmbientMode) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = tempoPhaseText,
                        style = MaterialTheme.typography.labelSmall,
                        color = tempoColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 8.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 3. Ritmo Cardíaco (BPM)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Pulsaciones",
                        tint = heartColor,
                        modifier = Modifier
                            .size(14.dp)
                            .graphicsLayer(scaleX = heartScale, scaleY = heartScale)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${state.heartRate} BPM",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 4. Botón de Stop al borde inferior (a unos px del borde)
                Button(
                    onClick = {
                        if (!isAmbientMode) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStopClicked()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = stopButtonColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(46.dp)
                        .padding(bottom = 2.dp)
                ) {
                    // Círculo sólido sin texto
                }
            }
        }
    }
}

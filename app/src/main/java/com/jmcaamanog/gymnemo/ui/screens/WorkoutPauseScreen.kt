package com.jmcaamanog.gymnemo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import kotlin.random.Random
import androidx.compose.runtime.produceState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jmcaamanog.gymnemo.R
import com.jmcaamanog.gymnemo.ui.components.RadialThreeButtons
import com.jmcaamanog.gymnemo.viewmodel.WorkoutViewModel
import kotlinx.coroutines.delay

@Composable
fun WorkoutPauseScreen(
    viewModel: WorkoutViewModel,
    isAmbientMode: Boolean = false,
    onRepeatClick: () -> Unit,
    onRepeatLongClick: () -> Unit,
    onNewSetClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    val state by viewModel.workoutState.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Temporizador de pausa positivo
    var pauseSeconds by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            pauseSeconds++
        }
    }

    val minutes = pauseSeconds / 60
    val seconds = pauseSeconds % 60
    val pauseTimeString = String.format("%02d:%02d", minutes, seconds)

    // Buscar las series registradas del ejercicio actual en esta sesión
    val exerciseSets = remember(state.loggedSets, state.exerciseName) {
        state.loggedSets.filter { it.exerciseName == state.exerciseName }
    }

    // Calcular el tiempo de descanso recomendado según repeticiones de la última serie
    val targetRest = remember(exerciseSets) {
        val lastSet = exerciseSets.lastOrNull()
        if (lastSet != null) {
            when {
                lastSet.reps < 6 -> 120  // Fuerza (Pesado) -> 2 min
                lastSet.reps in 6..12 -> 90 // Hipertrofia -> 1.5 min
                else -> 60 // Resistencia -> 1 min
            }
        } else {
            90 // Default 90s
        }
    }

    // Vibración corta al alcanzar el objetivo de descanso
    val restTargetReached = pauseSeconds == targetRest.toLong()
    LaunchedEffect(restTargetReached) {
        if (restTargetReached) {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            delay(300)
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        }
    }

    val isPrState = produceState(initialValue = false) {
        value = viewModel.isLastSetPersonalRecord()
    }

    val particles = remember {
        List(30) {
            ConfetiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.5f,
                speedY = Random.nextFloat() * 0.04f + 0.015f,
                speedX = Random.nextFloat() * 0.02f - 0.01f,
                color = listOf(Color(0xFF00E5FF), Color(0xFFFF007F), Color(0xFFFFEB3B), Color(0xFF39FF14)).random(),
                size = Random.nextFloat() * 6f + 3f
            )
        }
    }

    var confetiFrame by remember { mutableStateOf(0) }
    LaunchedEffect(isPrState.value) {
        if (isPrState.value) {
            while (true) {
                delay(20)
                particles.forEach { p ->
                    p.y += p.speedY
                    p.x += p.speedX
                    if (p.y > 1f) {
                        p.y = -0.1f
                        p.x = Random.nextFloat()
                    }
                }
                confetiFrame++
            }
        }
    }

    val timerColor = if (pauseSeconds >= targetRest) Color(0xFF39FF14) else Color(0xFF00E5FF)

    // Detención e hipoxia (oxígeno por debajo del 92%)
    val hypoxiaActive = state.spo2 < 92
    val hypoxiaScale by animateFloatAsState(
        targetValue = if (hypoxiaActive) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "hypoxiaScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pauseWave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )
    val liquidLevel = ((98f - state.spo2.toFloat()) / 8f).coerceIn(0f, 1f)

    LaunchedEffect(hypoxiaActive) {
        if (hypoxiaActive) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(600)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    ScreenScaffold {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // Arco minimalista exterior de descanso (AOD)
            val sweepAngle = 360f * (1f - (pauseSeconds.toFloat() / targetRest.toFloat()).coerceIn(0f, 1f))
            if (sweepAngle > 0.1f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val arcColor = if (isAmbientMode) Color.DarkGray else timerColor
                    drawArc(
                        color = arcColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = if (isAmbientMode) 2.dp.toPx() else 4.dp.toPx())
                    )
                }
            }

            if (!isAmbientMode) {
                RadialThreeButtons(
                topContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Repetir Serie",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF39FF14) // Verde Neón
                        )
                        Text(
                            text = "REPETIR",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                onTopClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRepeatClick()
                },
                onTopLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRepeatLongClick()
                },
                bottomLeftContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_ejercicio_brazo),
                            contentDescription = "Nueva Serie",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF00E5FF) // Cyan
                        )
                        Text(
                            text = "NUEVA",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                onBottomLeftClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNewSetClick()
                },
                bottomRightContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Terminar Ejercicio",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFFFF2E56) // Rojo
                        )
                        Text(
                            text = "TERMINAR",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                onBottomRightClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFinishClick()
                }
            )
            } // Fin de if (!isAmbientMode)

            // Temporizador de descanso elegante en el centro de la pantalla con etiqueta de descanso objetivo e historial integrado
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "META: ${targetRest}s",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = pauseTimeString,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = timerColor,
                            textAlign = TextAlign.Center
                        )
                        if (exerciseSets.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = exerciseSets.takeLast(2).joinToString(" | ") { "${it.reps}x${it.weightKg.toInt()}" },
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 7.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Superposición circular de alerta de hipoxia (con ola de líquido azul celeste Canvas que asciende/desciende)
            if (hypoxiaScale > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                        .graphicsLayer(
                            scaleX = hypoxiaScale,
                            scaleY = hypoxiaScale,
                            clip = true,
                            shape = CircleShape
                        )
                        .background(Color.Black)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val path = Path().apply {
                            val waveHeight = 12f
                            val liquidY = canvasHeight * (1f - liquidLevel)
                            moveTo(0f, canvasHeight)
                            lineTo(0f, liquidY)
                            for (x in 0..canvasWidth.toInt() step 6) {
                                val y = liquidY + waveHeight * kotlin.math.sin(x * 0.035f + wavePhase).toFloat()
                                lineTo(x.toFloat(), y)
                            }
                            lineTo(canvasWidth, canvasHeight)
                            close()
                        }
                        drawPath(path, Color(0xFF00E5FF))
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🚨 HIPOXIA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "${state.spo2}% O₂",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "Respira profundo\nEspera recuperación",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Animación de partículas de Confeti (PR en la serie activa)
            if (isPrState.value && !isAmbientMode) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    // Force Compose redraw on frame updates
                    val trigger = confetiFrame
                    particles.forEach { p ->
                        drawCircle(
                            color = p.color,
                            radius = p.size,
                            center = Offset(p.x * w, p.y * h)
                        )
                    }
                }
            }
        }
    }
}

private class ConfetiParticle(
    var x: Float,
    var y: Float,
    val speedY: Float,
    val speedX: Float,
    val color: Color,
    val size: Float
)

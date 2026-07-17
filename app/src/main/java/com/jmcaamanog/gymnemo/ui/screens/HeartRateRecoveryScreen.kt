package com.jmcaamanog.gymnemo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jmcaamanog.gymnemo.viewmodel.WorkoutViewModel
import kotlinx.coroutines.delay

enum class RecoveryState {
    TESTING,
    FINISHED
}

@Composable
fun HeartRateRecoveryScreen(
    viewModel: WorkoutViewModel,
    onFinished: (Int) -> Unit
) {
    val state by viewModel.workoutState.collectAsState()
    val haptic = LocalHapticFeedback.current

    var screenState by remember { mutableStateOf(RecoveryState.TESTING) }
    var secondsLeft by remember { mutableIntStateOf(60) }
    
    // Capturamos el pulso de inicio al abrir la pantalla
    val startBpm = remember { state.heartRate }
    var endBpm by remember { mutableIntStateOf(state.heartRate) }

    LaunchedEffect(screenState) {
        if (screenState == RecoveryState.TESTING) {
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
            // Al terminar los 60s
            endBpm = state.heartRate
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            screenState = RecoveryState.FINISHED
        }
    }

    val drop = (startBpm - endBpm).coerceAtLeast(0)

    ScreenScaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (screenState == RecoveryState.TESTING) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "RECUPERACIÓN",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${secondsLeft}s",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E5FF) // Cyan
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${state.heartRate} BPM",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botón para saltar el test
                    Button(
                        onClick = {
                            endBpm = state.heartRate
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            screenState = RecoveryState.FINISHED
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "SALTAR",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Resultados del test de recuperación (HRR)
                val (evaluationText, evaluationColor) = when {
                    drop > 18 -> "EXCELENTE" to Color(0xFF39FF14) // Verde Neón
                    drop in 12..18 -> "BUENA" to Color(0xFF00E5FF) // Cyan
                    else -> "MEJORABLE" to Color(0xFFFF2E56) // Rojo
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "TEST HRR",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "-$drop BPM",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = evaluationColor
                    )

                    Text(
                        text = evaluationText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = evaluationColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Inicio: $startBpm | Fin: $endBpm",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón de confirmación para guardar y salir
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            onFinished(drop)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF39FF14), // Verde Neón
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Confirmar",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

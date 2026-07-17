package com.jmcaamanog.gymnemo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jmcaamanog.gymnemo.viewmodel.SettingsViewModel
import com.jmcaamanog.gymnemo.viewmodel.WorkoutViewModel

@Composable
fun DashboardScreen(
    settingsViewModel: SettingsViewModel,
    workoutViewModel: WorkoutViewModel
) {
    val settingsState by settingsViewModel.uiState.collectAsState()

    val brazoKcalTrained by workoutViewModel.getKcalTrainedToday("brazo").collectAsState(initial = 0)
    val piernaKcalTrained by workoutViewModel.getKcalTrainedToday("pierna").collectAsState(initial = 0)
    val torsoKcalTrained by workoutViewModel.getKcalTrainedToday("torso").collectAsState(initial = 0)

    val brazoGoal = settingsState.brazoKcal.coerceAtLeast(1)
    val piernaGoal = settingsState.piernaKcal.coerceAtLeast(1)
    val torsoGoal = settingsState.torsoKcal.coerceAtLeast(1)

    val totalKcalTrained = brazoKcalTrained + piernaKcalTrained + torsoKcalTrained
    val totalGoal = brazoGoal + piernaGoal + torsoGoal

    val brazoProgress = (brazoKcalTrained.toFloat() / brazoGoal).coerceIn(0f, 1f)
    val piernaProgress = (piernaKcalTrained.toFloat() / piernaGoal).coerceIn(0f, 1f)
    val torsoProgress = (torsoKcalTrained.toFloat() / torsoGoal).coerceIn(0f, 1f)

    ScreenScaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Dibujar los anillos de progreso
            Canvas(modifier = Modifier.fillMaxSize(0.85f)) {
                val strokeWidth = 8.dp.toPx()

                // Anillo exterior: Brazo (Cyan)
                drawArc(
                    color = Color.DarkGray.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
                drawArc(
                    color = Color(0xFF00E5FF),
                    startAngle = -90f,
                    sweepAngle = brazoProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Anillo medio: Pierna (Magenta)
                val middleRadius = size.width / 2 - strokeWidth - 6.dp.toPx()
                drawArc(
                    color = Color.DarkGray.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth),
                    topLeft = center - androidx.compose.ui.geometry.Offset(middleRadius, middleRadius),
                    size = androidx.compose.ui.geometry.Size(middleRadius * 2, middleRadius * 2)
                )
                drawArc(
                    color = Color(0xFFFF007F),
                    startAngle = -90f,
                    sweepAngle = piernaProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = center - androidx.compose.ui.geometry.Offset(middleRadius, middleRadius),
                    size = androidx.compose.ui.geometry.Size(middleRadius * 2, middleRadius * 2)
                )

                // Anillo interior: Torso (Yellow)
                val innerRadius = middleRadius - strokeWidth - 6.dp.toPx()
                drawArc(
                    color = Color.DarkGray.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth),
                    topLeft = center - androidx.compose.ui.geometry.Offset(innerRadius, innerRadius),
                    size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2)
                )
                drawArc(
                    color = Color(0xFFFFEB3B),
                    startAngle = -90f,
                    sweepAngle = torsoProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = center - androidx.compose.ui.geometry.Offset(innerRadius, innerRadius),
                    size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2)
                )
            }

            // Datos de texto en el centro
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$totalKcalTrained Kcal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Meta: $totalGoal",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

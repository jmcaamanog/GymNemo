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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.jmcaamanog.gymnemo.viewmodel.WorkoutViewModel

@Composable
fun WorkoutCompleteScreen(
    viewModel: WorkoutViewModel,
    onDone: () -> Unit
) {
    val brazoKcal by viewModel.getKcalTrainedToday("brazo").collectAsState(initial = 0)
    val piernaKcal by viewModel.getKcalTrainedToday("pierna").collectAsState(initial = 0)
    val torsoKcal by viewModel.getKcalTrainedToday("torso").collectAsState(initial = 0)

    val totalKcal = brazoKcal + piernaKcal + torsoKcal

    ScreenScaffold(timeText = {}) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¡COMPLETADO!",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF39FF14), // Verde Neón
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Fecha
                val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
                Text(
                    text = dateFormat.format(Date(viewModel.lastSessionStart.takeIf { it > 0 } ?: System.currentTimeMillis())),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Horas (Inicio / Fin)
                val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                val startStr = timeFormat.format(Date(viewModel.lastSessionStart.takeIf { it > 0 } ?: System.currentTimeMillis()))
                val endStr = timeFormat.format(Date(viewModel.lastSessionEnd.takeIf { it > 0 } ?: System.currentTimeMillis()))
                Text(
                    text = "$startStr / $endStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Tiempo total
                val durationMin = viewModel.lastSessionDuration / 60
                val durationSec = viewModel.lastSessionDuration % 60
                val durationStr = String.format("%02d:%02d", durationMin, durationSec)
                Text(
                    text = durationStr,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Kcal
                Text(
                    text = "${viewModel.lastSessionKcal} Kcal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF39FF14),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completar",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

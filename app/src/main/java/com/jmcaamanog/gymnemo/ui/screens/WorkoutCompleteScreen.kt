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
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
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

    ScreenScaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¡ENTRENO COMPLETADO!",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF39FF14), // Verde Neón
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Hoy has quemado",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "$totalKcal Kcal",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF39FF14),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completar día",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

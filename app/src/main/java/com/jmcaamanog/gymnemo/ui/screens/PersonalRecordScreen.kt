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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import com.jmcaamanog.gymnemo.data.db.PersonalRecordTuple
import com.jmcaamanog.gymnemo.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PersonalRecordScreen(
    exerciseName: String,
    viewModel: WorkoutViewModel,
    onBackClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    // Carga asíncrona del PR desde la base de datos local
    val prState by produceState<PersonalRecordTuple?>(initialValue = null, exerciseName) {
        value = viewModel.getPersonalRecord(exerciseName)
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    ScreenScaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                // Nombre del ejercicio
                Text(
                    text = exerciseName.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Icono de Diana estilizado
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = "Récord Personal",
                    modifier = Modifier.size(24.dp),
                    tint = if (prState != null) Color(0xFF00E5FF) else Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                val record = prState
                if (record != null) {
                    // Si hay récord guardado
                    Text(
                        text = "${record.weightKg.toInt()} kg",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 26.sp
                    )
                    Text(
                        text = "${record.reps} reps",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp
                    )
                    Text(
                        text = dateFormat.format(Date(record.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontSize = 8.sp
                    )
                } else {
                    // Si no hay datos aún
                    Text(
                        text = "Sin récords aún",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "¡A entrenar!",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Botón Atrás
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                        onBackClick()
                    },
                    modifier = Modifier.size(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray.copy(alpha = 0.6f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Atrás",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

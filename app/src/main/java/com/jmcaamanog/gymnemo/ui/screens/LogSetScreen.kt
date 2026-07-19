package com.jmcaamanog.gymnemo.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Picker
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberPickerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check

@Composable
fun LogSetScreen(
    onConfirm: (reps: Int, weight: Float) -> Unit
) {
    val repsState = rememberPickerState(
        initialNumberOfOptions = 30,
        initiallySelectedIndex = 11, // 12 reps
        shouldRepeatOptions = false
    )

    val weightState = rememberPickerState(
        initialNumberOfOptions = 201, // 0..200 kg
        initiallySelectedIndex = 20,  // 20 kg
        shouldRepeatOptions = false
    )

    ScreenScaffold(timeText = {}) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Mitad Superior: Repeticiones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REPS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(42.dp)
                    )
                    Picker(
                        state = repsState,
                        contentDescription = { "Reps" },
                        modifier = Modifier.size(width = 80.dp, height = 55.dp)
                    ) { index ->
                        val isSelected = index == repsState.selectedOptionIndex
                        Text(
                            text = (index + 1).toString(),
                            style = if (isSelected) MaterialTheme.typography.displaySmall else MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF00E5FF) else Color.Gray.copy(alpha = 0.4f),
                            fontSize = if (isSelected) 30.sp else 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Mitad Inferior: Peso
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PESO",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(42.dp)
                    )
                    Picker(
                        state = weightState,
                        contentDescription = { "Peso" },
                        modifier = Modifier.size(width = 80.dp, height = 55.dp)
                    ) { index ->
                        val isSelected = index == weightState.selectedOptionIndex
                        Text(
                            text = "$index kg",
                            style = if (isSelected) MaterialTheme.typography.displaySmall else MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFFF007F) else Color.Gray.copy(alpha = 0.4f),
                            fontSize = if (isSelected) 26.sp else 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        val reps = repsState.selectedOptionIndex + 1
                        val weight = weightState.selectedOptionIndex.toFloat()
                        onConfirm(reps, weight)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF39FF14), // Verde Neón
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Guardar",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

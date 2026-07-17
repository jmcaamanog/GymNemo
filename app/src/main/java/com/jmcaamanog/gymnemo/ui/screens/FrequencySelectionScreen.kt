package com.jmcaamanog.gymnemo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jmcaamanog.gymnemo.ui.components.SilentButton

@Composable
fun FrequencySelectionScreen(
    currentDays: String,
    onDaysChanged: (String) -> Unit
) {
    val daysList = listOf("L", "M", "X", "J", "V", "S", "D")
    val selectedIndices = currentDays.split(",").filter { it.isNotEmpty() }.map { it.toInt() }.toSet()

    ScreenScaffold {
        Column(modifier = Modifier.fillMaxSize()) {
            // Mitad Superior: Calendario (Días de la semana)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    daysList.forEachIndexed { index, day ->
                        val isSelected = selectedIndices.contains(index)
                        SilentButton(
                            onClick = {
                                val newList = if (isSelected) {
                                    selectedIndices - index
                                } else {
                                    selectedIndices + index
                                }
                                onDaysChanged(newList.sorted().joinToString(","))
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }
            }

            // Mitad Inferior: Reloj
            SilentButton(
                onClick = { /* TODO: Reloj funcional si se requiere */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Reloj",
                    modifier = Modifier.fillMaxSize(0.4f),
                    tint = Color.White
                )
            }
        }
    }
}

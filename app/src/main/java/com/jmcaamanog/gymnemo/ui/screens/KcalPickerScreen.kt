package com.jmcaamanog.gymnemo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Picker
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberPickerState
import com.jmcaamanog.gymnemo.R

@Composable
fun KcalPickerScreen(
    initialValue: Int,
    onValueSelected: (Int) -> Unit
) {
    val step = 500
    val minValue = 500
    val maxValue = 10000
    val optionsCount = (maxValue - minValue) / step + 1
    
    val state = rememberPickerState(
        initialNumberOfOptions = optionsCount,
        initiallySelectedIndex = ((initialValue - minValue) / step).coerceIn(0, optionsCount - 1),
        shouldRepeatOptions = false
    )

    ScreenScaffold {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "KCAL",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                
                Icon(
                    painter = painterResource(R.drawable.ic_subir),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )

                Picker(
                    state = state,
                    contentDescription = { "Kcal Selector" },
                    modifier = Modifier.size(width = 120.dp, height = 100.dp)
                ) { index ->
                    val value = minValue + (index * step)
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Icon(
                    painter = painterResource(R.drawable.ic_bajar),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )

                Button(
                    onClick = { onValueSelected(minValue + (state.selectedOptionIndex * step)) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Confirmar")
                }
            }
        }
    }
}

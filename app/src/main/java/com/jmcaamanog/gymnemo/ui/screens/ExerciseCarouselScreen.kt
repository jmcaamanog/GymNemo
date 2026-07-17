package com.jmcaamanog.gymnemo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun ExerciseCarouselScreen(
    bodyPart: String,
    onExerciseSelected: (String) -> Unit
) {
    val exercises = when (bodyPart.lowercase()) {
        "brazo" -> listOf(
            "Curl Bíceps Mancuerna",
            "Extensión Tríceps Polea",
            "Curl Martillo",
            "Press Francés Barra",
            "Curl Concentrado",
            "Copa Tríceps Mancuerna",
            "Fondos Tríceps"
        )
        "pierna" -> listOf(
            "Sentadilla con Barra",
            "Prensa de Pierna",
            "Extensión de Cuádriceps",
            "Curl Femoral Tumbado",
            "Zancadas Mancuerna",
            "Peso Muerto Rumano",
            "Elevación de Gemelos"
        )
        else -> listOf(
            "Press de Banca Plano",
            "Dominadas Pronas",
            "Remo con Barra",
            "Press Militar Mancuerna",
            "Aperturas de Pecho",
            "Pullover Mancuerna",
            "Cruce de Poleas"
        )
    }

    val listState = rememberScalingLazyListState()

    ScreenScaffold {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ScalingLazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = bodyPart.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                items(exercises, key = { it }) { exercise ->
                    Text(
                        text = exercise,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clickable { onExerciseSelected(exercise) }
                            .graphicsLayer {
                                val layoutInfo = listState.layoutInfo
                                val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.key == exercise }
                                if (itemInfo != null) {
                                    val center = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2f
                                    val itemCenter = itemInfo.offset + (itemInfo.size / 2f)
                                    val distanceFromCenter = (itemCenter - center) / center
                                    rotationX = (distanceFromCenter * -30f).coerceIn(-45f, 45f)
                                    cameraDistance = 8f * density
                                }
                            }
                    )
                }
            }
        }
    }
}

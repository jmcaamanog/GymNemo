package com.jmcaamanog.gymnemo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.speech.RecognizerIntent
import android.content.Intent
import com.jmcaamanog.gymnemo.R
import com.jmcaamanog.gymnemo.viewmodel.WorkoutViewModel

@Composable
fun ExerciseCarouselScreen(
    bodyPart: String,
    viewModel: WorkoutViewModel,
    onExerciseSelected: (String) -> Unit
) {
    val customMap by viewModel.customExercises.collectAsState()
    val customList = customMap[bodyPart.lowercase()] ?: emptyList()

    val exercises = remember(customList) {
        val base = when (bodyPart.lowercase()) {
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
                "CARRERA",
                "CARRERA GYM",
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
        base + customList
    }

    val listState = rememberScalingLazyListState()

    ScreenScaffold {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ScalingLazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    val iconRes = when (bodyPart.lowercase()) {
                        "brazo" -> R.drawable.ic_ejercicio_brazo
                        "pierna" -> R.drawable.ic_ejercicio_pierna
                        else -> R.drawable.ic_ejercicio_torso
                    }
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = bodyPart,
                        modifier = Modifier.size(30.dp).padding(bottom = 8.dp),
                        tint = Color(0xFF00E5FF)
                    )
                }
                items(exercises, key = { it }) { exercise ->
                    val isTrained by produceState(initialValue = false, exercise) {
                        value = viewModel.wasTrainedThisWeek(exercise)
                    }

                    Text(
                        text = exercise,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isTrained) Color(0xFF39FF14) else Color.White,
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
                item {
                    val voiceLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                        if (!spokenText.isNullOrEmpty()) {
                            viewModel.addCustomExercise(bodyPart, spokenText)
                        }
                    }
                    Text(
                        text = "[+] Añadir Ejercicio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clickable {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Di el nombre del ejercicio")
                                }
                                try {
                                    voiceLauncher.launch(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                    )
                }
            }
        }
    }
}

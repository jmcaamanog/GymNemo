package com.jmcaamanog.gymnemo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jmcaamanog.gymnemo.R
import com.jmcaamanog.gymnemo.ui.components.RadialThreeButtons

@Composable
fun NumberPickerScreen(
    label: String, // p.ej. "KG", "CM", "AÑO", "KCAL", "REPS"
    initialValue: Int,
    range: IntRange,
    step: Int = 1,
    repsFor1Rm: Int? = null,
    suggestOverload: Boolean = false,
    onValueSelected: (Int) -> Unit
) {
    var currentValue by remember { mutableIntStateOf(initialValue.coerceIn(range)) }
    val haptic = LocalHapticFeedback.current

    var scaleTarget by remember { mutableStateOf(1f) }
    val pickerScale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "morphScale"
    )

    LaunchedEffect(currentValue) {
        scaleTarget = 1.12f
        delay(85)
        scaleTarget = 1f
    }

    ScreenScaffold {
        RadialThreeButtons(
            topContent = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .graphicsLayer(scaleX = pickerScale, scaleY = pickerScale)
                ) {
                    if (repsFor1Rm != null) {
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                                .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "R: $repsFor1Rm",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$currentValue kg",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        val oneRepMax = currentValue * (1 + repsFor1Rm / 30.0)
                        Text(
                            text = "1RM: ${oneRepMax.toInt()} kg",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                        if (suggestOverload) {
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "+2.5 KG SUGERIDO",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFF007F),
                                fontWeight = FontWeight.Black,
                                fontSize = 8.sp
                            )
                        }
                    } else {
                        Text(
                            text = currentValue.toString(),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 32.sp
                        )
                        Text(
                            text = label.lowercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }
                }
            },
            onTopClick = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onValueSelected(currentValue)
            },
            bottomLeftContent = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_subir),
                    contentDescription = "Incrementar",
                    modifier = Modifier.fillMaxSize(0.5f),
                    tint = Color.White
                )
            },
            onBottomLeftClick = {
                haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                if (currentValue + step <= range.last) {
                    currentValue += step
                }
            },
            bottomRightContent = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bajar),
                    contentDescription = "Decrementar",
                    modifier = Modifier.fillMaxSize(0.5f),
                    tint = Color.White
                )
            },
            onBottomRightClick = {
                haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                if (currentValue - step >= range.first) {
                    currentValue -= step
                }
            }
        )
    }
}

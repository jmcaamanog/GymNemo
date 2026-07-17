package com.jmcaamanog.gymnemo.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text

@Composable
fun CountdownScreen(
    onCountdownFinished: () -> Unit
) {
    var count by remember { mutableIntStateOf(5) }
    val scale = remember { Animatable(0.5f) }

    LaunchedEffect(count) {
        if (count > 0) {
            scale.snapTo(0.5f)
            scale.animateTo(
                targetValue = 1.5f,
                animationSpec = tween(durationMillis = 800)
            )
            count -= 1
        } else {
            onCountdownFinished()
        }
    }

    ScreenScaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (count > 0) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF39FF14), // Verde Neón
                    modifier = Modifier.graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value
                    )
                )
            } else {
                Text(
                    text = "¡GO!",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

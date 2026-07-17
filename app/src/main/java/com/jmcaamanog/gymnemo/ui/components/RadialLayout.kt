package com.jmcaamanog.gymnemo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadialThreeButtons(
    topContent: @Composable BoxScope.() -> Unit,
    onTopClick: () -> Unit,
    bottomLeftContent: @Composable BoxScope.() -> Unit,
    onBottomLeftClick: () -> Unit,
    bottomRightContent: @Composable BoxScope.() -> Unit,
    onBottomRightClick: () -> Unit,
    modifier: Modifier = Modifier,
    onTopLongClick: (() -> Unit)? = null,
    onBottomLeftLongClick: (() -> Unit)? = null,
    onBottomRightLongClick: (() -> Unit)? = null
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val radius = screenWidth / 2
    // Distancia del centro al icono para que esté equilibrado visualmente
    val iconDistance = radius * 0.55f 

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        // Líneas divisorias (Estilo "Y")
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val r = size.width / 2
            val color = Color.Gray.copy(alpha = 0.5f)
            val strokeWidth = 1.dp.toPx()

            // Ángulos divisorios: 90 (abajo), 210 (arriba-izq), 330 (arriba-der)
            listOf(90f, 210f, 330f).forEach { angle ->
                val rad = Math.toRadians(angle.toDouble())
                val end = Offset(
                    center.x + r * cos(rad).toFloat(),
                    center.y + r * sin(rad).toFloat()
                )
                drawLine(color, center, end, strokeWidth)
            }
        }

        // El centro de la pantalla es (radius, radius)
        
        // Sector Superior (Centrado a 270 grados - arriba vertical)
        val topX = iconDistance * cos(Math.toRadians(270.0)).toFloat()
        val topY = iconDistance * sin(Math.toRadians(270.0)).toFloat()
        SilentButton(
            onClick = onTopClick,
            onLongClick = onTopLongClick,
            modifier = Modifier
                .size(radius) // Área de click grande
                .align(Alignment.Center)
                .offset(x = topX, y = topY),
            content = topContent
        )

        // Sector Abajo Izquierda (Centrado a 150 grados)
        val blX = iconDistance * cos(Math.toRadians(150.0)).toFloat()
        val blY = iconDistance * sin(Math.toRadians(150.0)).toFloat()
        SilentButton(
            onClick = onBottomLeftClick,
            onLongClick = onBottomLeftLongClick,
            modifier = Modifier
                .size(radius)
                .align(Alignment.Center)
                .offset(x = blX, y = blY),
            content = bottomLeftContent
        )

        // Sector Abajo Derecha (Centrado a 30 grados)
        val brX = iconDistance * cos(Math.toRadians(30.0)).toFloat()
        val brY = iconDistance * sin(Math.toRadians(30.0)).toFloat()
        SilentButton(
            onClick = onBottomRightClick,
            onLongClick = onBottomRightLongClick,
            modifier = Modifier
                .size(radius)
                .align(Alignment.Center)
                .offset(x = brX, y = brY),
            content = bottomRightContent
        )
    }
}

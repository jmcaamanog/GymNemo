package com.jmcaamanog.gymnemo.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ScreenScaffold
import com.jmcaamanog.gymnemo.R
import com.jmcaamanog.gymnemo.ui.components.SilentButton

@Composable
fun BodySettingsScreen(
    onNavigateFrequency: () -> Unit,
    onNavigateKcal: () -> Unit
) {
    ScreenScaffold {
        Box(modifier = Modifier.fillMaxSize()) {
            // Mitad Superior: Reloj de Arena
            SilentButton(
                onClick = onNavigateFrequency,
                modifier = Modifier
                    .fillMaxSize(0.5f)
                    .align(Alignment.TopCenter)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_reloj_arena),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(0.5f),
                    tint = Color.White
                )
            }

            // Mitad Inferior: Kcal
            SilentButton(
                onClick = onNavigateKcal,
                modifier = Modifier
                    .fillMaxSize(0.5f)
                    .align(Alignment.BottomCenter)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_kcal),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(0.6f),
                    tint = Color.White
                )
            }
        }
    }
}

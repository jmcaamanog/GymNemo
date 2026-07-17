package com.jmcaamanog.gymnemo.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jmcaamanog.gymnemo.R
import com.jmcaamanog.gymnemo.ui.components.RadialThreeButtons
import com.jmcaamanog.gymnemo.viewmodel.SettingsViewModel

@Composable
fun ProfileScreen(
    viewModel: SettingsViewModel,
    onNavigateHeight: () -> Unit,
    onNavigateYear: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    ScreenScaffold {
        RadialThreeButtons(
            topContent = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_altura),
                    contentDescription = "Altura",
                    modifier = Modifier.fillMaxSize(0.6f),
                    tint = Color.White
                )
            },
            onTopClick = onNavigateHeight,
            bottomLeftContent = {
                Text(
                    text = "19XX",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            onBottomLeftClick = onNavigateYear,
            bottomRightContent = {
                Icon(
                    painter = painterResource(
                        id = if (uiState.isMale) R.drawable.ic_sexo_hombre else R.drawable.ic_sexo_mujer
                    ),
                    contentDescription = "Sexo",
                    modifier = Modifier.fillMaxSize(0.6f),
                    tint = Color.White
                )
            },
            onBottomRightClick = { viewModel.setGender(!uiState.isMale) }
        )
    }
}

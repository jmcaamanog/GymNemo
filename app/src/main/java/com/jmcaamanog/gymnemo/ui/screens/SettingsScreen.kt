package com.jmcaamanog.gymnemo.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import com.jmcaamanog.gymnemo.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onSyncClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(
        scrollState = listState
    ) { contentPadding ->
        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = contentPadding
        ) {
            item {
                ListHeader {
                    Text("PERFIL (INMUTABLE)")
                }
            }
            item {
                TitleCard(
                    onClick = { /* Bloqueado según guion */ },
                    title = { Text("Sexo") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (uiState.isMale) "Hombre" else "Mujer",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            item {
                TitleCard(
                    onClick = { /* Bloqueado según guion */ },
                    title = { Text("Altura") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${uiState.heightCm} cm",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            item {
                TitleCard(
                    onClick = { /* Bloqueado según guion */ },
                    title = { Text("Año Nacimiento") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.birthYear.toString(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                ListHeader {
                    Text("ENTRENO (MUTABLE)")
                }
            }
            item {
                TitleCard(
                    onClick = { /* TODO: Open Weight Picker */ },
                    title = { Text("Peso Actual") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${uiState.currentWeightKg} kg",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            item {
                TitleCard(
                    onClick = { /* TODO: Open Rest Picker */ },
                    title = { Text("Descanso Base") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val minutes = uiState.baseRestSeconds / 60
                    val seconds = uiState.baseRestSeconds % 60
                    Text(
                        text = "%02d:%02d".format(minutes, seconds),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                ListHeader {
                    Text("SINCRONIZAR")
                }
            }
            item {
                TitleCard(
                    onClick = onSyncClick,
                    title = { Text("Sincronizar Datos") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Forzar envío a móvil",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

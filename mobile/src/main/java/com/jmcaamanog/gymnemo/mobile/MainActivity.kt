package com.jmcaamanog.gymnemo.mobile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.jmcaamanog.gymnemo.mobile.data.db.WorkoutDb
import com.jmcaamanog.gymnemo.mobile.data.db.WorkoutSessionEntity
import com.jmcaamanog.gymnemo.mobile.data.db.WorkoutSetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = WorkoutDb.getDatabase(applicationContext)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00E5FF),
                    secondary = Color(0xFFFF007F),
                    tertiary = Color(0xFFFFEB3B),
                    background = Color(0xFF0C0C0C),
                    surface = Color(0xFF161616)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MobileAppScaffold(db)
                }
            }
        }
    }
}

@Composable
fun MobileAppScaffold(db: WorkoutDb) {
    var selectedTab by remember { mutableStateOf("historial") }
    val sessions by db.workoutDao().getAllSessions().collectAsState(initial = emptyList())
    val allSets by db.workoutDao().getAllSets().collectAsState(initial = emptyList())
    val exercises by db.workoutDao().getDistinctExercises().collectAsState(initial = emptyList())

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF101010)
            ) {
                NavigationBarItem(
                    selected = selectedTab == "historial",
                    onClick = { selectedTab = "historial" },
                    label = { Text("Historial") },
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00E5FF),
                        selectedTextColor = Color(0xFF00E5FF),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF1E1E1E)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "evolucion",
                    onClick = { selectedTab = "evolucion" },
                    label = { Text("Evolución") },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Evolución") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF007F),
                        selectedTextColor = Color(0xFFFF007F),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF1E1E1E)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "objetivos",
                    onClick = { selectedTab = "objetivos" },
                    label = { Text("Objetivos") },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Objetivos") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFFEB3B),
                        selectedTextColor = Color(0xFFFFEB3B),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF1E1E1E)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "respaldo",
                    onClick = { selectedTab = "respaldo" },
                    label = { Text("Respaldos") },
                    icon = { Icon(Icons.Default.Backup, contentDescription = "Respaldos") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00E5FF),
                        selectedTextColor = Color(0xFF00E5FF),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF1E1E1E)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                "historial" -> HistorialTab(sessions, allSets)
                "evolucion" -> EvolucionTab(exercises, allSets)
                "objetivos" -> ObjetivosTab(sessions)
                "respaldo" -> RespaldoTab(db)
            }
        }
    }
}

// ==========================================
// TABS IMPLEMENTATION
// ==========================================

@Composable
fun HistorialTab(sessions: List<WorkoutSessionEntity>, allSets: List<WorkoutSetEntity>) {
    val totalKcal = sessions.sumOf { it.totalKcal }
    val totalTimeMin = sessions.sumOf { it.durationSeconds } / 60

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Resumen Global",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        // Ficha de Resumen Global
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161616))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Entrenamientos", fontSize = 12.sp, color = Color.Gray)
                        Text("${sessions.size}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                    Column {
                        Text("Calorías Totales", fontSize = 12.sp, color = Color.Gray)
                        Text("$totalKcal kcal", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF007F))
                    }
                    Column {
                        Text("Tiempo Activo", fontSize = 12.sp, color = Color.Gray)
                        Text("${totalTimeMin}m", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF00E5FF))
                    }
                }
            }
        }

        item {
            Text(
                "Historial de Entrenamientos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (sessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay sesiones registradas aún.\n¡Realiza un entreno desde el reloj!",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
        } else {
            items(sessions) { session ->
                val sessionSets = allSets.filter { it.sessionId == session.sessionId }
                SessionCard(session = session, sets = sessionSets)
            }
        }
    }
}

@Composable
fun SessionCard(session: WorkoutSessionEntity, sets: List<WorkoutSetEntity>) {
    var expanded by remember { mutableStateOf(false) }
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    val tagColor = when (session.bodyPart.lowercase()) {
        "brazo" -> Color(0xFF00E5FF)
        "pierna" -> Color(0xFFFF007F)
        else -> Color(0xFFFFEB3B)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = session.bodyPart.uppercase(),
                        color = tagColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Duración: ${session.durationSeconds / 60}m ${session.durationSeconds % 60}s",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = format.format(Date(session.timestamp)),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${session.totalKcal} Kcal",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "${session.averageHeartRate} BPM prom",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expandir",
                        tint = Color.Gray
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = Color.DarkGray, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Detalles de Telemetría Adicionales
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Pulsaciones Max", fontSize = 10.sp, color = Color.Gray)
                            Text("${session.maxHeartRate} BPM", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Oxígeno Mín (SpO2)", fontSize = 10.sp, color = Color.Gray)
                            val spo2Color = if (session.minSpO2 < 92) Color(0xFF00E5FF) else Color.White
                            Text("${session.minSpO2}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = spo2Color)
                        }
                        Column {
                            Text("Recup. Cardíaca (HRR)", fontSize = 10.sp, color = Color.Gray)
                            val recoveryCatalog = when {
                                session.heartRateRecoveryDrop >= 20 -> "EXCELENTE"
                                session.heartRateRecoveryDrop >= 15 -> "BUENA"
                                else -> "MEJORABLE"
                            }
                            Text("-${session.heartRateRecoveryDrop} BPM ($recoveryCatalog)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF39FF14))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Series Realizadas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tagColor)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (sets.isEmpty()) {
                        Text("No se registraron series individuales en este entrenamiento.", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        sets.forEachIndexed { index, set ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${index + 1}. ${set.exerciseName}",
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${set.reps} reps x ${set.weightKg.toInt()} kg",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Meta desc: ${set.restSeconds}s",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    val context = LocalContext.current
                    Button(
                        onClick = {
                            val gpxFile = exportToGpxFile(context, session)
                            if (gpxFile != null) {
                                val uri = FileProvider.getUriForFile(context, "com.jmcaamanog.gymnemo.mobile.fileprovider", gpxFile)
                                shareFile(context, uri, "application/gpx+xml", "Compartir entrenamiento GPX (Strava)")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Compartir GPX (Strava)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EvolucionTab(exercises: List<String>, allSets: List<WorkoutSetEntity>) {
    var selectedExercise by remember { mutableStateOf(exercises.firstOrNull() ?: "") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Evolución de Fuerza",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        if (exercises.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay suficientes entrenamientos guardados para mostrar progreso.", color = Color.Gray)
                }
            }
        } else {
            // Dropdown Selector de Ejercicio
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF007F))
                    ) {
                        Text(if (selectedExercise.isEmpty()) "Seleccionar Ejercicio" else selectedExercise.uppercase())
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        exercises.forEach { exercise ->
                            DropdownMenuItem(
                                text = { Text(exercise.uppercase()) },
                                onClick = {
                                    selectedExercise = exercise
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Filtrar las series asociadas a este ejercicio y obtener peso máximo de cada sesión
            val prPoints = remember(selectedExercise, allSets) {
                allSets.filter { it.exerciseName.equals(selectedExercise, ignoreCase = true) }
                    .groupBy { it.sessionId }
                    .map { (sessionId, sets) ->
                        val maxWeight = sets.maxOfOrNull { it.weightKg } ?: 0f
                        val maxReps = sets.filter { it.weightKg == maxWeight }.maxOfOrNull { it.reps } ?: 0
                        // Obtener fecha estimada a partir de sessionId
                        Pair(sessionId, maxWeight)
                    }
                    .sortedBy { it.first } // Ordenar por ID de sesión cronológico
            }

            // Gráfica de peso máximo
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Histórico de Peso Máximo (KG)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (prPoints.size < 2) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Registra más sesiones de este ejercicio para ver la gráfica.", color = Color.Gray, fontSize = 11.sp)
                            }
                        } else {
                            // Canvas personalizado para dibujar la gráfica
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val padding = 20f

                                val minWeight = prPoints.minOf { it.second }
                                val maxWeight = prPoints.maxOf { it.second }
                                val weightRange = if (maxWeight == minWeight) 10f else (maxWeight - minWeight)

                                val pointsCoordinates = prPoints.mapIndexed { index, pair ->
                                    val x = padding + index * (canvasWidth - 2 * padding) / (prPoints.size - 1)
                                    val y = canvasHeight - padding - (pair.second - minWeight) * (canvasHeight - 2 * padding) / weightRange
                                    Offset(x, y)
                                }

                                // Dibujar líneas de rejilla del fondo
                                drawLine(Color.DarkGray, Offset(padding, padding), Offset(canvasWidth - padding, padding), strokeWidth = 0.5f)
                                drawLine(Color.DarkGray, Offset(padding, canvasHeight - padding), Offset(canvasWidth - padding, canvasHeight - padding), strokeWidth = 0.5f)

                                // Trazar la curva lineal
                                val path = Path().apply {
                                    moveTo(pointsCoordinates.first().x, pointsCoordinates.first().y)
                                    for (i in 1 until pointsCoordinates.size) {
                                        lineTo(pointsCoordinates[i].x, pointsCoordinates[i].y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = Color(0xFFFF007F),
                                    style = Stroke(width = 3.dp.toPx())
                                )

                                // Dibujar puntos e indicadores de peso sobre los puntos
                                pointsCoordinates.forEachIndexed { index, offset ->
                                    drawCircle(
                                        color = Color.White,
                                        radius = 4.dp.toPx(),
                                        center = offset
                                    )
                                    drawCircle(
                                        color = Color(0xFFFF007F),
                                        radius = 2.dp.toPx(),
                                        center = offset
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tarjeta de Récord Absoluto (PR)
            val absolutePR = remember(selectedExercise, allSets) {
                allSets.filter { it.exerciseName.equals(selectedExercise, ignoreCase = true) }
                    .maxByOrNull { it.weightKg }
            }

            if (absolutePR != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFFFF007F).copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "PR",
                                    tint = Color(0xFFFF007F),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Récord Absoluto (PR)", fontSize = 11.sp, color = Color.Gray)
                                Text("${absolutePR.weightKg.toInt()} kg x ${absolutePR.reps} reps", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text("Guardado en tu historial", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ObjetivosTab(sessions: List<WorkoutSessionEntity>) {
    // Calcular el estado de esta semana
    val currentCalendar = Calendar.getInstance()
    val currentWeekOfYear = currentCalendar.get(Calendar.WEEK_OF_YEAR)
    val currentYear = currentCalendar.get(Calendar.YEAR)

    val thisWeekSessions = remember(sessions) {
        sessions.filter { session ->
            val cal = Calendar.getInstance().apply { timeInMillis = session.timestamp }
            cal.get(Calendar.WEEK_OF_YEAR) == currentWeekOfYear && cal.get(Calendar.YEAR) == currentYear
        }
    }

    val weekDaysTrained = thisWeekSessions.map {
        val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
        cal.get(Calendar.DAY_OF_WEEK)
    }.distinct().size

    val weekKcal = thisWeekSessions.sumOf { it.totalKcal }
    val weekMinutes = thisWeekSessions.sumOf { it.durationSeconds } / 60

    // Metas semanales establecidas (Por defecto)
    val targetDays = 3f
    val targetKcal = 900f
    val targetMinutes = 120f

    // Calcular frecuencia cardíaca promedio de la semana para el latido
    val avgHeartRate = if (thisWeekSessions.isNotEmpty()) {
        val validHRs = thisWeekSessions.map { it.averageHeartRate }.filter { it > 0 }
        if (validHRs.isNotEmpty()) validHRs.average().toInt() else 72
    } else {
        72
    }

    // Animación de pulso/latido orgánico en los anillos
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulsePeriod = (60000 / avgHeartRate).coerceIn(400, 2000)
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = pulsePeriod / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Entrenos por grupo muscular
    val hasBrazo = thisWeekSessions.any { it.bodyPart.lowercase() == "brazo" }
    val hasTorso = thisWeekSessions.any { it.bodyPart.lowercase() == "torso" }
    val hasPierna = thisWeekSessions.any { it.bodyPart.lowercase() == "pierna" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Balance Semanal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        // Anillos de Actividad Latientes
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Canvas(modifier = Modifier.size(110.dp)) {
                        val w = size.width
                        val h = size.height
                        val centerOffset = Offset(w / 2f, h / 2f)

                        val daysProgress = (weekDaysTrained.toFloat() / targetDays).coerceIn(0f, 1f)
                        val kcalProgress = (weekKcal.toFloat() / targetKcal).coerceIn(0f, 1f)
                        val minProgress = (weekMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f)

                        // Anillo Exterior (Días) - Amarillo
                        drawCircle(Color.DarkGray.copy(alpha = 0.25f), radius = 48.dp.toPx(), center = centerOffset, style = Stroke(8.dp.toPx()))
                        drawArc(
                            color = Color(0xFFFFEB3B),
                            startAngle = -90f,
                            sweepAngle = 360f * daysProgress,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
                            topLeft = Offset(centerOffset.x - 48.dp.toPx(), centerOffset.y - 48.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(96.dp.toPx(), 96.dp.toPx())
                        )

                        // Anillo Central (Kcal) - Magenta
                        drawCircle(Color.DarkGray.copy(alpha = 0.25f), radius = 36.dp.toPx(), center = centerOffset, style = Stroke(8.dp.toPx()))
                        drawArc(
                            color = Color(0xFFFF007F),
                            startAngle = -90f,
                            sweepAngle = 360f * kcalProgress,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
                            topLeft = Offset(centerOffset.x - 36.dp.toPx(), centerOffset.y - 36.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(72.dp.toPx(), 72.dp.toPx())
                        )

                        // Anillo Interior (Minutos) - Cyan
                        drawCircle(Color.DarkGray.copy(alpha = 0.25f), radius = 24.dp.toPx(), center = centerOffset, style = Stroke(8.dp.toPx()))
                        drawArc(
                            color = Color(0xFF00E5FF),
                            startAngle = -90f,
                            sweepAngle = 360f * minProgress,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
                            topLeft = Offset(centerOffset.x - 24.dp.toPx(), centerOffset.y - 24.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(48.dp.toPx(), 48.dp.toPx())
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFFFFEB3B), RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Días: ${weekDaysTrained}/${targetDays.toInt()}", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF007F), RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kcal: ${weekKcal}/${targetKcal.toInt()}", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF00E5FF), RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Min: ${weekMinutes}/${targetMinutes.toInt()}", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pulso: $avgHeartRate BPM",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Mapa de Calor Anatómico (Heatmap)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Mapa de Calor Anatómico",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Zonas entrenadas esta semana (Iluminadas en Neón)",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Canvas(modifier = Modifier.size(width = 160.dp, height = 190.dp)) {
                        val w = size.width
                        val h = size.height
                        val centerX = w / 2f

                        // Zonas y Colores
                        val cabezaColor = Color.DarkGray
                        val torsoColor = if (hasTorso) Color(0xFFFF007F) else Color(0xFF333333)
                        val brazoColor = if (hasBrazo) Color(0xFF00E5FF) else Color(0xFF333333)
                        val piernaColor = if (hasPierna) Color(0xFFFFEB3B) else Color(0xFF333333)

                        // 1. Cabeza
                        drawCircle(color = cabezaColor, radius = 9.dp.toPx(), center = Offset(centerX, 18.dp.toPx()))

                        // 2. Cuello
                        drawRect(color = cabezaColor, topLeft = Offset(centerX - 3.dp.toPx(), 27.dp.toPx()), size = androidx.compose.ui.geometry.Size(6.dp.toPx(), 6.dp.toPx()))

                        // 3. Torso / Pecho
                        drawRect(
                            color = torsoColor,
                            topLeft = Offset(centerX - 16.dp.toPx(), 33.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(32.dp.toPx(), 58.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawRect(
                            color = torsoColor.copy(alpha = if (hasTorso) 0.25f else 0.08f),
                            topLeft = Offset(centerX - 16.dp.toPx(), 33.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(32.dp.toPx(), 58.dp.toPx())
                        )

                        // 4. Brazos
                        // Brazo Izquierdo
                        drawLine(
                            color = brazoColor,
                            start = Offset(centerX - 18.dp.toPx(), 35.dp.toPx()),
                            end = Offset(centerX - 32.dp.toPx(), 85.dp.toPx()),
                            strokeWidth = 6.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        // Brazo Derecho
                        drawLine(
                            color = brazoColor,
                            start = Offset(centerX + 18.dp.toPx(), 35.dp.toPx()),
                            end = Offset(centerX + 32.dp.toPx(), 85.dp.toPx()),
                            strokeWidth = 6.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )

                        // 5. Piernas
                        // Pierna Izquierda
                        drawLine(
                            color = piernaColor,
                            start = Offset(centerX - 8.dp.toPx(), 94.dp.toPx()),
                            end = Offset(centerX - 12.dp.toPx(), 170.dp.toPx()),
                            strokeWidth = 7.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        // Pierna Derecha
                        drawLine(
                            color = piernaColor,
                            start = Offset(centerX + 8.dp.toPx(), 94.dp.toPx()),
                            end = Offset(centerX + 12.dp.toPx(), 170.dp.toPx()),
                            strokeWidth = 7.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }

                    // Leyenda del mapa de calor
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF00E5FF), RoundedCornerShape(2.dp)).align(Alignment.CenterVertically))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Brazo", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(16.dp))

                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF007F), RoundedCornerShape(2.dp)).align(Alignment.CenterVertically))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Torso", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(16.dp))

                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFFFEB3B), RoundedCornerShape(2.dp)).align(Alignment.CenterVertically))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pierna", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GoalProgressBar(title: String, current: Float, target: Float, unit: String, accentColor: Color) {
    val progress = (current / target).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                Text(
                    text = "${current.toInt()} / ${target.toInt()} $unit",
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = progress,
                color = accentColor,
                trackColor = Color.DarkGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${(progress * 100).toInt()}% alcanzado",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun RespaldoTab(db: WorkoutDb) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Importador JSON launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val jsonStr = inputStream?.bufferedReader()?.use { it.readText() }
                    if (!jsonStr.isNullOrEmpty()) {
                        val jsonArr = JSONArray(jsonStr)
                        for (i in 0 until jsonArr.length()) {
                            val sessionObj = jsonArr.getJSONObject(i)
                            val sessionId = db.workoutDao().insertSession(
                                WorkoutSessionEntity(
                                    timestamp = sessionObj.getLong("timestamp"),
                                    endTimestamp = sessionObj.optLong("endTimestamp", sessionObj.getLong("timestamp")),
                                    durationSeconds = sessionObj.getLong("durationSeconds"),
                                    totalKcal = sessionObj.getInt("totalKcal"),
                                    averageHeartRate = sessionObj.getInt("averageHeartRate"),
                                    maxHeartRate = sessionObj.optInt("maxHeartRate", sessionObj.getInt("averageHeartRate")),
                                    minSpO2 = sessionObj.optInt("minSpO2", 98),
                                    bodyPart = sessionObj.getString("bodyPart"),
                                    heartRateRecoveryDrop = sessionObj.optInt("heartRateRecoveryDrop", 0)
                                )
                            )
                            val setsArr = sessionObj.getJSONArray("sets")
                            for (j in 0 until setsArr.length()) {
                                val setObj = setsArr.getJSONObject(j)
                                db.workoutDao().insertSet(
                                    WorkoutSetEntity(
                                        sessionId = sessionId,
                                        exerciseName = setObj.getString("exerciseName"),
                                        weightKg = setObj.getDouble("weightKg").toFloat(),
                                        reps = setObj.getInt("reps"),
                                        restSeconds = setObj.optInt("restSeconds", 90)
                                    )
                                )
                            }
                        }
                        launch(Dispatchers.Main) {
                            Toast.makeText(context, "Historial importado correctamente", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Error al importar: formato incorrecto", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Copias de Seguridad",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        // Exportar CSV
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161616))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Exportar Historial a CSV", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Genera un archivo compatible con Excel o Google Sheets.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                val sessionsList = db.workoutDao().getAllSessions().first()
                                val allSetsList = db.workoutDao().getAllSets().first()
                                val csvFile = exportToCsvFile(context, sessionsList, allSetsList)
                                if (csvFile != null) {
                                    val uri = FileProvider.getUriForFile(context, "com.jmcaamanog.gymnemo.mobile.fileprovider", csvFile)
                                    shareFile(context, uri, "text/csv", "Compartir entrenamientos CSV")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Exportar a CSV", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Respaldos JSON (Exportar e importar)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161616))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Copia de Seguridad JSON", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Permite salvaguardar tus datos y restaurarlos en otro dispositivo.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val sessionsList = db.workoutDao().getAllSessions().first()
                                    val allSetsList = db.workoutDao().getAllSets().first()
                                    val jsonFile = exportToJsonFile(context, sessionsList, allSetsList)
                                    if (jsonFile != null) {
                                        val uri = FileProvider.getUriForFile(context, "com.jmcaamanog.gymnemo.mobile.fileprovider", jsonFile)
                                        shareFile(context, uri, "application/json", "Copia de Seguridad GymNemo")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F), contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Exportar JSON", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { importLauncher.launch("application/json") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Importar JSON", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Respaldos Automáticos
        item {
            val sharedPrefs = remember { context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE) }
            var autoBackupEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("auto_backup", false)) }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161616))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Copia Automática al Sincronizar", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Exporta una copia local JSON automáticamente al sincronizar tus series.", fontSize = 12.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = autoBackupEnabled,
                            onCheckedChange = { checked ->
                                autoBackupEnabled = checked
                                sharedPrefs.edit().putBoolean("auto_backup", checked).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E5FF),
                                checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// FILE UTILITIES FOR EXPORTS
// ==========================================

fun exportToCsvFile(context: Context, sessions: List<WorkoutSessionEntity>, allSets: List<WorkoutSetEntity>): File? {
    try {
        val cacheFile = File(context.cacheDir, "gymnemo_entrenamientos.csv")
        val stream = FileOutputStream(cacheFile)
        val writer = stream.bufferedWriter()

        // Escribir cabecera
        writer.write("Fecha,Hora Inicio,Hora Fin,Zona Muscular,Ejercicio,Serie,Peso (KG),Reps,Descanso (s),Calorias,HR Promedio,HR Max,Oxigeno Min,Recuperacion (HRR)\n")

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        sessions.forEach { session ->
            val sessionSets = allSets.filter { it.sessionId == session.sessionId }
            val dateStr = dateFormat.format(Date(session.timestamp))
            val startStr = timeFormat.format(Date(session.timestamp))
            val endStr = timeFormat.format(Date(session.endTimestamp))

            sessionSets.forEachIndexed { index, set ->
                writer.write(
                    "$dateStr,$startStr,$endStr,${session.bodyPart},${set.exerciseName},${index + 1},${set.weightKg},${set.reps},${set.restSeconds},${session.totalKcal},${session.averageHeartRate},${session.maxHeartRate},${session.minSpO2}%,-${session.heartRateRecoveryDrop} BPM\n"
                )
            }
        }
        writer.flush()
        writer.close()
        return cacheFile
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun exportToJsonFile(context: Context, sessions: List<WorkoutSessionEntity>, allSets: List<WorkoutSetEntity>): File? {
    try {
        val cacheFile = File(context.cacheDir, "gymnemo_backup.json")
        val stream = FileOutputStream(cacheFile)
        val writer = stream.bufferedWriter()

        val jsonArray = JSONArray()
        sessions.forEach { session ->
            val sessionSets = allSets.filter { it.sessionId == session.sessionId }
            val sessionObj = JSONObject().apply {
                put("timestamp", session.timestamp)
                put("endTimestamp", session.endTimestamp)
                put("durationSeconds", session.durationSeconds)
                put("totalKcal", session.totalKcal)
                put("averageHeartRate", session.averageHeartRate)
                put("maxHeartRate", session.maxHeartRate)
                put("minSpO2", session.minSpO2)
                put("bodyPart", session.bodyPart)
                put("heartRateRecoveryDrop", session.heartRateRecoveryDrop)

                val setsArray = JSONArray()
                sessionSets.forEach { set ->
                    val setObj = JSONObject().apply {
                        put("exerciseName", set.exerciseName)
                        put("weightKg", set.weightKg.toDouble())
                        put("reps", set.reps)
                        put("restSeconds", set.restSeconds)
                    }
                    setsArray.put(setObj)
                }
                put("sets", setsArray)
            }
            jsonArray.put(sessionObj)
        }
        writer.write(jsonArray.toString(2))
        writer.flush()
        writer.close()
        return cacheFile
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun shareFile(context: Context, uri: Uri, mimeType: String, title: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, title))
}

fun exportToGpxFile(context: Context, session: WorkoutSessionEntity): File? {
    try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val startTimeStr = dateFormat.format(Date(session.timestamp))
        val cacheFile = File(context.cacheDir, "gymnemo_entrenamiento_${session.sessionId}.gpx")
        val writer = cacheFile.bufferedWriter()

        writer.write("""
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1" creator="GymNemo" xmlns="http://www.topografix.com/GPX/1/1"
                 xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1">
              <metadata>
                <time>$startTimeStr</time>
              </metadata>
              <trk>
                <name>GymNemo: ${session.bodyPart.uppercase()}</name>
                <type>9</type> 
                <trkseg>
        """.trimIndent() + "\n")

        val duration = session.durationSeconds
        val avgHR = session.averageHeartRate
        val steps = (duration / 10).coerceAtLeast(1)
        for (i in 0..steps) {
            val pointTime = session.timestamp + (i * 10 * 1000)
            val timeStr = dateFormat.format(Date(pointTime))
            writer.write("""
                  <trkpt lat="0.0" lon="0.0">
                    <time>$timeStr</time>
                    <extensions>
                      <gpxtpx:TrackPointExtension>
                        <gpxtpx:hr>$avgHR</gpxtpx:hr>
                      </gpxtpx:TrackPointExtension>
                    </extensions>
                  </trkpt>
            """.trimIndent() + "\n")
        }

        writer.write("""
                </trkseg>
              </trk>
            </gpx>
        """.trimIndent() + "\n")

        writer.flush()
        writer.close()
        return cacheFile
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

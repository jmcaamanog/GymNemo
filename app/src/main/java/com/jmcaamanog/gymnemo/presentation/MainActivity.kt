package com.jmcaamanog.gymnemo.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import androidx.wear.ambient.AmbientModeSupport
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.jmcaamanog.gymnemo.R
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.widget.ImageView
import com.jmcaamanog.gymnemo.data.datastore.UserPreferencesRepository
import com.jmcaamanog.gymnemo.data.db.GymNemoDatabase
import com.jmcaamanog.gymnemo.data.repository.WorkoutRepository
import com.jmcaamanog.gymnemo.presentation.theme.GymNemoTheme
import com.jmcaamanog.gymnemo.ui.components.RadialThreeButtons
import com.jmcaamanog.gymnemo.ui.screens.BodySettingsScreen
import com.jmcaamanog.gymnemo.ui.screens.FrequencySelectionScreen
import com.jmcaamanog.gymnemo.ui.screens.KcalPickerScreen
import com.jmcaamanog.gymnemo.ui.screens.NumberPickerScreen
import com.jmcaamanog.gymnemo.ui.screens.ProfileScreen
import com.jmcaamanog.gymnemo.ui.screens.ExerciseCarouselScreen
import com.jmcaamanog.gymnemo.ui.screens.CountdownScreen
import com.jmcaamanog.gymnemo.ui.screens.ActiveWorkoutScreen
import com.jmcaamanog.gymnemo.ui.screens.PersonalRecordScreen
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Job
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrackChanges
import com.jmcaamanog.gymnemo.ui.screens.LogSetScreen
import com.jmcaamanog.gymnemo.ui.screens.DashboardScreen
import com.jmcaamanog.gymnemo.ui.screens.WorkoutPauseScreen
import com.jmcaamanog.gymnemo.ui.screens.WorkoutCompleteScreen
import com.jmcaamanog.gymnemo.viewmodel.SettingsViewModel
import com.jmcaamanog.gymnemo.viewmodel.WorkoutViewModel
import com.jmcaamanog.gymnemo.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import android.content.pm.PackageManager

class MainActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider {
    private var isAmbientMode by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Attach Ambient Mode
        AmbientModeSupport.attach(this)

        // Request GPS Location Permissions
        val permissions = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, 100)
        }

        val db = GymNemoDatabase.getDatabase(applicationContext)
        val prefRepository = UserPreferencesRepository(applicationContext)
        val workoutRepository = WorkoutRepository(applicationContext, db.workoutDao(), prefRepository)
        val factory = ViewModelFactory(workoutRepository)

        val startScreen = intent.getStringExtra("startScreen") ?: "main"
        setContent {
            GymNemoApp(factory, prefRepository, isAmbientMode, startScreen)
        }
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = object : AmbientModeSupport.AmbientCallback() {
        override fun onEnterAmbient(ambientDetails: Bundle?) {
            isAmbientMode = true
        }

        override fun onExitAmbient() {
            isAmbientMode = false
        }

        override fun onUpdateAmbient() {
            // Actualizar la pantalla cada minuto si es necesario
        }
    }
}

@Composable
fun GymNemoApp(factory: ViewModelFactory, prefRepository: UserPreferencesRepository, isAmbientMode: Boolean, startScreen: String = "main") {
    val navController = rememberSwipeDismissableNavController()
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val workoutViewModel: WorkoutViewModel = viewModel(factory = factory)
    val uiState by settingsViewModel.uiState.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val dataClient = com.google.android.gms.wearable.Wearable.getDataClient(context)
        val dataListener = com.google.android.gms.wearable.DataClient.OnDataChangedListener { dataEvents ->
            for (event in dataEvents) {
                if (event.type == com.google.android.gms.wearable.DataEvent.TYPE_CHANGED) {
                    val path = event.dataItem.uri.path
                    if (path == "/custom_exercises") {
                        val dataMap = com.google.android.gms.wearable.DataMapItem.fromDataItem(event.dataItem).dataMap
                        val brazo = dataMap.getStringArrayList("brazo") ?: emptyList<String>()
                        val pierna = dataMap.getStringArrayList("pierna") ?: emptyList<String>()
                        val torso = dataMap.getStringArrayList("torso") ?: emptyList<String>()

                        val prefs = context.getSharedPreferences("custom_exercises_prefs", android.content.Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            putStringSet("brazo", brazo.toSet())
                            putStringSet("pierna", pierna.toSet())
                            putStringSet("torso", torso.toSet())
                            apply()
                        }
                        workoutViewModel.loadCustomExercises()
                    }
                }
            }
        }
        dataClient.addListener(dataListener)
    }

    GymNemoTheme {
        AppScaffold {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "splash"
            ) {
                composable("splash") {
                    var stage by remember { mutableStateOf(0) }
                    LaunchedEffect(Unit) {
                        delay(3000)
                        stage = 1
                        delay(4000)
                        navController.navigate(startScreen) {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        if (stage == 0) {
                            Icon(
                                painter = painterResource(R.drawable.icono_app),
                                contentDescription = "Logo",
                                modifier = Modifier.size(80.dp),
                                tint = Color.Unspecified
                            )
                        } else {
                            AndroidView(
                                factory = { ctx ->
                                    ImageView(ctx).apply {
                                        scaleType = ImageView.ScaleType.CENTER_CROP
                                        try {
                                            val source = ImageDecoder.createSource(ctx.assets, "yo_animado.gif")
                                            val drawable = ImageDecoder.decodeDrawable(source)
                                            setImageDrawable(drawable)
                                            if (drawable is AnimatedImageDrawable) {
                                                drawable.start()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                composable("main") {
                    val recoveryState by prefRepository.activeWorkoutStateFlow.collectAsState(
                        initial = com.jmcaamanog.gymnemo.data.datastore.ActiveWorkoutRecoveryState(false, "", "", 0, 0f)
                    )

                    var showRecoveryDialog by remember { mutableStateOf(false) }

                    LaunchedEffect(recoveryState) {
                        if (recoveryState.inProgress) {
                            showRecoveryDialog = true
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        RadialThreeButtons(
                            topContent = { Icon(painterResource(R.drawable.ic_dashboard), null, Modifier.fillMaxSize(0.6f), tint = Color.White) },
                            onTopClick = { navController.navigate("dashboard_menu") },
                            bottomLeftContent = { Icon(painterResource(R.drawable.ic_pesa_gym), null, Modifier.fillMaxSize(0.5f), tint = Color.White) },
                            onBottomLeftClick = { navController.navigate("train_category") },
                            bottomRightContent = { Icon(painterResource(R.drawable.ic_ajustes), null, Modifier.fillMaxSize(0.5f), tint = Color.White) },
                            onBottomRightClick = { navController.navigate("settings") }
                        )

                        if (showRecoveryDialog) {
                            androidx.wear.compose.material3.AlertDialog(
                                visible = showRecoveryDialog,
                                onDismissRequest = { 
                                    showRecoveryDialog = false
                                    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                                    scope.launch {
                                        prefRepository.saveActiveWorkoutState(false)
                                    }
                                },
                                title = { androidx.wear.compose.material3.Text("¿Reanudar entreno?", textAlign = TextAlign.Center) },
                                text = { androidx.wear.compose.material3.Text("Se detectó una sesión de ${recoveryState.bodyPart.uppercase()}.", fontSize = 11.sp, textAlign = TextAlign.Center) },
                                confirmButton = {
                                    androidx.wear.compose.material3.Button(
                                        onClick = {
                                            showRecoveryDialog = false
                                            workoutViewModel.startWorkout(
                                                bodyPart = recoveryState.bodyPart,
                                                exerciseName = recoveryState.exerciseName,
                                                initialDuration = recoveryState.durationSeconds.toLong(),
                                                initialKcal = recoveryState.accumulatedKcal.toDouble()
                                            )
                                            navController.navigate("active_workout")
                                        }
                                    ) {
                                        androidx.wear.compose.material3.Text("SÍ")
                                    }
                                },
                                dismissButton = {
                                    androidx.wear.compose.material3.Button(
                                        onClick = {
                                            showRecoveryDialog = false
                                            val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                                            scope.launch {
                                                prefRepository.saveActiveWorkoutState(false)
                                            }
                                        }
                                    ) {
                                        androidx.wear.compose.material3.Text("NO")
                                    }
                                }
                            )
                        }
                    }
                }
                composable("dashboard_menu") {
                    RadialThreeButtons(
                        topContent = { Icon(painterResource(R.drawable.ic_dashboard), null, Modifier.fillMaxSize(0.6f), tint = Color.White) },
                        onTopClick = { navController.navigate("dashboard") },
                        bottomRightContent = { Icon(Icons.Default.TrackChanges, null, Modifier.fillMaxSize(0.5f), tint = Color.White) },
                        onBottomRightClick = { navController.navigate("pr_category") },
                        bottomLeftContent = { Icon(Icons.Default.ArrowBack, null, Modifier.fillMaxSize(0.5f), tint = Color.White) },
                        onBottomLeftClick = { navController.popBackStack() }
                    )
                }
                composable("pr_category") {
                    RadialThreeButtons(
                        topContent = { Icon(painterResource(R.drawable.ic_ejercicio_brazo), null, Modifier.fillMaxSize(0.5f), tint = Color.White) },
                        onTopClick = { navController.navigate("pr_carousel/brazo") },
                        bottomLeftContent = { Icon(painterResource(R.drawable.ic_ejercicio_pierna), null, Modifier.fillMaxSize(0.5f), tint = Color.White) },
                        onBottomLeftClick = { navController.navigate("pr_carousel/pierna") },
                        bottomRightContent = { Icon(painterResource(R.drawable.ic_ejercicio_torso), null, Modifier.fillMaxSize(0.5f), tint = Color.White) },
                        onBottomRightClick = { navController.navigate("pr_carousel/torso") }
                    )
                }
                composable("pr_carousel/{part}") { backStackEntry ->
                    val part = backStackEntry.arguments?.getString("part") ?: ""
                    ExerciseCarouselScreen(
                        bodyPart = part,
                        viewModel = workoutViewModel,
                        onExerciseSelected = { exercise ->
                            navController.navigate("pr_detail/$exercise")
                        }
                    )
                }
                composable("pr_detail/{exerciseName}") { backStackEntry ->
                    val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: ""
                    PersonalRecordScreen(
                        exerciseName = exerciseName,
                        viewModel = workoutViewModel,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
                composable("dashboard") {
                    DashboardScreen(
                        settingsViewModel = settingsViewModel,
                        workoutViewModel = workoutViewModel
                    )
                }
                composable("train_category") {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                        RadialThreeButtons(
                            topContent = { Icon(painterResource(R.drawable.ic_ejercicio_brazo), null, Modifier.fillMaxSize(0.5f), tint = Color.White) },
                            onTopClick = { navController.navigate("carousel/brazo") },
                            bottomLeftContent = { Icon(painterResource(R.drawable.ic_ejercicio_pierna), null, Modifier.fillMaxSize(0.5f), tint = Color.White) },
                            onBottomLeftClick = { navController.navigate("carousel/pierna") },
                            bottomRightContent = { Icon(painterResource(R.drawable.ic_ejercicio_torso), null, Modifier.fillMaxSize(0.5f), tint = Color.White) },
                            onBottomRightClick = { navController.navigate("carousel/torso") }
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF007F))
                                .clickable {
                                    navController.navigate("workout_complete")
                                }
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.wear.compose.material3.Text(
                                text = "END",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
                composable("carousel/{part}") { backStackEntry ->
                    val part = backStackEntry.arguments?.getString("part") ?: ""
                    ExerciseCarouselScreen(
                        bodyPart = part,
                        viewModel = workoutViewModel,
                        onExerciseSelected = { exercise ->
                            workoutViewModel.startWorkout(part, exercise)
                            navController.navigate("countdown")
                        }
                    )
                }
                composable("countdown") {
                    CountdownScreen(
                        onCountdownFinished = {
                            navController.navigate("active_workout")
                        }
                    )
                }
                composable("active_workout") {
                    ActiveWorkoutScreen(
                        viewModel = workoutViewModel,
                        isAmbientMode = isAmbientMode,
                        onStopClicked = {
                            workoutViewModel.pauseWorkout()
                            navController.navigate("pause_screen")
                        }
                    )
                }
                composable("pause_screen") {
                    WorkoutPauseScreen(
                        viewModel = workoutViewModel,
                        isAmbientMode = isAmbientMode,
                        onRepeatClick = {
                            navController.navigate("log_reps/repeat")
                        },
                        onRepeatLongClick = {
                            workoutViewModel.resumeWorkout()
                            navController.navigate("countdown")
                        },
                        onNewSetClick = {
                            workoutViewModel.resumeWorkout()
                            navController.navigate("countdown")
                        },
                        onFinishClick = {
                            navController.navigate("log_reps/finish")
                        }
                    )
                }
                composable("log_reps/{action}") { backStackEntry ->
                    val action = backStackEntry.arguments?.getString("action") ?: "repeat"
                    NumberPickerScreen(
                        label = "REPS",
                        initialValue = 12,
                        range = 1..30,
                        step = 1,
                        onValueSelected = { reps ->
                            navController.navigate("log_weight/$reps/$action")
                        }
                    )
                }
                composable("log_weight/{reps}/{action}") { backStackEntry ->
                    val reps = backStackEntry.arguments?.getString("reps")?.toInt() ?: 12
                    val action = backStackEntry.arguments?.getString("action") ?: "repeat"
                    val currentPart = workoutViewModel.workoutState.value.bodyPart
                    val exerciseName = workoutViewModel.workoutState.value.exerciseName

                    val lastWeightState = produceState(initialValue = 20f, exerciseName) {
                        value = workoutViewModel.getLastWeightForExercise(exerciseName)
                    }

                    val suggestOverloadState = produceState(initialValue = false, exerciseName) {
                        value = workoutViewModel.shouldSuggestOverload(exerciseName)
                    }

                    NumberPickerScreen(
                        label = "KG",
                        initialValue = lastWeightState.value.toInt(),
                        range = 0..200,
                        step = 1,
                        repsFor1Rm = reps,
                        suggestOverload = suggestOverloadState.value,
                        onValueSelected = { weight ->
                            workoutViewModel.logSet(weight.toFloat(), reps)
                            when (action) {
                                "repeat" -> {
                                    navController.popBackStack("pause_screen", false)
                                }
                                "change" -> {
                                    workoutViewModel.stopAndSaveWorkout {
                                        navController.popBackStack("carousel/$currentPart", false)
                                    }
                                }
                                "finish" -> {
                                    workoutViewModel.stopAndSaveWorkout {
                                        navController.navigate("workout_complete") {
                                            popUpTo("main") { inclusive = false }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
                composable("workout_complete") {
                    WorkoutCompleteScreen(
                        viewModel = workoutViewModel,
                        onDone = {
                            navController.popBackStack("main", false)
                        }
                    )
                }
                composable("settings") {
                    var progress by remember { mutableStateOf(0f) }
                    val coroutineScope = rememberCoroutineScope()
                    val haptic = LocalHapticFeedback.current
                    var startTime by remember { mutableStateOf(0L) }

                    RadialThreeButtons(
                        topContent = { Icon(painterResource(R.drawable.ic_calendario), null, Modifier.fillMaxSize(0.5f), tint = Color.White) },
                        onTopClick = { navController.navigate("objectives") },
                        bottomLeftContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                startTime = System.currentTimeMillis()
                                                progress = 0f
                                                val job = coroutineScope.launch {
                                                    val duration = 7000f // 7 segundos
                                                    val steps = 70
                                                    val stepTime = 100L
                                                    for (i in 1..steps) {
                                                        delay(stepTime)
                                                        progress = i.toFloat() / steps
                                                    }
                                                }
                                                try {
                                                    awaitRelease()
                                                } finally {
                                                    job.cancel()
                                                    val holdTime = System.currentTimeMillis() - startTime
                                                    if (progress >= 1.0f) {
                                                        progress = 0f
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        navController.navigate("easter_egg")
                                                    } else {
                                                        progress = 0f
                                                        if (holdTime < 500) {
                                                            navController.navigate("profile")
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }
                                    .drawWithContent {
                                        drawContent()
                                        if (progress > 0f) {
                                            // Dibujar el arco de carga celeste neón alrededor del icono
                                            drawArc(
                                                color = Color(0xFF00E5FF),
                                                startAngle = -90f,
                                                sweepAngle = 360f * progress,
                                                useCenter = false,
                                                style = Stroke(width = 3.dp.toPx())
                                            )
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_cuerpo_persona),
                                    contentDescription = "Perfil",
                                    modifier = Modifier.fillMaxSize(0.6f),
                                    tint = Color.White
                                )
                            }
                        },
                        onBottomLeftClick = {
                            // Handled by pointerInput inside bottomLeftContent
                        },
                        bottomRightContent = { Icon(painterResource(R.drawable.ic_peso_bascula), null, Modifier.fillMaxSize(0.6f), tint = Color.White) },
                        onBottomRightClick = { navController.navigate("picker_weight") }
                    )
                }
                composable("objectives") {
                    RadialThreeButtons(
                        topContent = { Icon(painterResource(R.drawable.ic_ejercicio_brazo), null, Modifier.fillMaxSize(0.5f), tint = Color.White) },
                        onTopClick = { navController.navigate("body_settings/brazo") },
                        bottomLeftContent = { Icon(painterResource(R.drawable.ic_ejercicio_pierna), null, Modifier.fillMaxSize(0.5f), tint = Color.White) },
                        onBottomLeftClick = { navController.navigate("body_settings/pierna") },
                        bottomRightContent = { Icon(painterResource(R.drawable.ic_ejercicio_torso), null, Modifier.fillMaxSize(0.5f), tint = Color.White) },
                        onBottomRightClick = { navController.navigate("body_settings/torso") }
                    )
                }
                composable("body_settings/{part}") { backStackEntry ->
                    val part = backStackEntry.arguments?.getString("part") ?: ""
                    BodySettingsScreen(
                        onNavigateFrequency = { navController.navigate("frequency/$part") },
                        onNavigateKcal = { navController.navigate("kcal/$part") }
                    )
                }
                composable("frequency/{part}") { backStackEntry ->
                    val part = backStackEntry.arguments?.getString("part") ?: ""
                    val currentDays = when(part) {
                        "brazo" -> uiState.brazoDays
                        "pierna" -> uiState.piernaDays
                        "torso" -> uiState.torsoDays
                        else -> ""
                    }
                    FrequencySelectionScreen(
                        currentDays = currentDays,
                        onDaysChanged = { settingsViewModel.updateObjectiveDays(part, it) }
                    )
                }
                composable("kcal/{part}") { backStackEntry ->
                    val part = backStackEntry.arguments?.getString("part") ?: ""
                    val currentKcal = when(part) {
                        "brazo" -> uiState.brazoKcal
                        "pierna" -> uiState.piernaKcal
                        "torso" -> uiState.torsoKcal
                        else -> 2000
                    }
                    NumberPickerScreen(
                        label = "KCAL",
                        initialValue = currentKcal,
                        range = 100..5000,
                        step = 100,
                        onValueSelected = {
                            settingsViewModel.updateObjectiveKcal(part, it)
                            navController.popBackStack()
                        }
                    )
                }
                composable("profile") {
                    ProfileScreen(
                        viewModel = settingsViewModel,
                        onNavigateHeight = { navController.navigate("picker_height") },
                        onNavigateYear = { navController.navigate("picker_year") }
                    )
                }
                composable("picker_weight") {
                    NumberPickerScreen(
                        label = "PESO (KG)",
                        initialValue = uiState.currentWeightKg.toInt(),
                        range = 30..200,
                        onValueSelected = {
                            settingsViewModel.setWeight(it.toFloat())
                            navController.popBackStack()
                        }
                    )
                }
                composable("picker_height") {
                    NumberPickerScreen(
                        label = "ALTURA (CM)",
                        initialValue = uiState.heightCm,
                        range = 100..250,
                        onValueSelected = {
                            settingsViewModel.setHeight(it)
                            navController.popBackStack()
                        }
                    )
                }
                composable("picker_year") {
                    NumberPickerScreen(
                        label = "AÑO",
                        initialValue = uiState.birthYear,
                        range = 1940..2020,
                        onValueSelected = {
                            settingsViewModel.setBirthYear(it)
                            navController.popBackStack()
                        }
                    )
                }
                composable("easter_egg") {
                    val context = LocalContext.current
                    var updateStatus by remember { mutableStateOf("Buscar Actualización") }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                ImageView(ctx).apply {
                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                    try {
                                        val source = ImageDecoder.createSource(ctx.assets, "yo_animado.gif")
                                        val drawable = ImageDecoder.decodeDrawable(source)
                                        setImageDrawable(drawable)
                                        if (drawable is AnimatedImageDrawable) {
                                            drawable.start()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.3f)
                        )
                        androidx.compose.foundation.layout.Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text("GymNemo Watch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF), fontSize = 11.sp)
                            Text("v1.0.0", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
                            Text("Autor: JMCG", style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 9.sp)
                            Text("¡Cuidado con la hipoxia y dale caña al hierro! 🏋️‍♂️💀", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF007F), fontSize = 8.sp, textAlign = TextAlign.Center)
                            Text("Copyright © 2026 jmcaamanog", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 7.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.wear.compose.material3.Button(
                                onClick = {
                                    updateStatus = "Comprobando..."
                                    checkAppUpdate(context) { version, url ->
                                        updateStatus = "Nueva v$version en GitHub"
                                    }
                                },
                                modifier = Modifier.height(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                            ) {
                                Text(updateStatus, fontSize = 8.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun checkAppUpdate(context: android.content.Context, onUpdateAvailable: (String, String) -> Unit) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            val url = java.net.URL("https://api.github.com/repos/jmcaamanog/GymNemo/releases/latest")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()
            if (connection.responseCode == 200) {
                val text = connection.inputStream.bufferedReader().use { it.readText() }
                val json = org.json.JSONObject(text)
                val remoteVersion = json.getString("tag_name").replace("v", "")
                val assets = json.getJSONArray("assets")
                val downloadUrl = if (assets.length() > 0) assets.getJSONObject(0).getString("browser_download_url") else ""
                val localVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName
                if (remoteVersion != localVersion) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onUpdateAvailable(remoteVersion, downloadUrl)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

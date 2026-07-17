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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.jmcaamanog.gymnemo.R
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrackChanges
import com.jmcaamanog.gymnemo.ui.screens.LogSetScreen
import com.jmcaamanog.gymnemo.ui.screens.DashboardScreen
import com.jmcaamanog.gymnemo.ui.screens.WorkoutPauseScreen
import com.jmcaamanog.gymnemo.ui.screens.WorkoutCompleteScreen
import com.jmcaamanog.gymnemo.ui.screens.HeartRateRecoveryScreen
import com.jmcaamanog.gymnemo.viewmodel.SettingsViewModel
import com.jmcaamanog.gymnemo.viewmodel.WorkoutViewModel
import com.jmcaamanog.gymnemo.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider {
    private var isAmbientMode by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Attach Ambient Mode
        AmbientModeSupport.attach(this)

        val db = GymNemoDatabase.getDatabase(applicationContext)
        val prefRepository = UserPreferencesRepository(applicationContext)
        val workoutRepository = WorkoutRepository(applicationContext, db.workoutDao(), prefRepository)
        val factory = ViewModelFactory(workoutRepository)

        setContent {
            GymNemoApp(factory, prefRepository, isAmbientMode)
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
fun GymNemoApp(factory: ViewModelFactory, prefRepository: UserPreferencesRepository, isAmbientMode: Boolean) {
    val navController = rememberSwipeDismissableNavController()
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val workoutViewModel: WorkoutViewModel = viewModel(factory = factory)
    val uiState by settingsViewModel.uiState.collectAsState()

    GymNemoTheme {
        AppScaffold {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "main"
            ) {
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
                                    navController.navigate("hr_recovery")
                                }
                            }
                        }
                    )
                }
                composable("hr_recovery") {
                    HeartRateRecoveryScreen(
                        viewModel = workoutViewModel,
                        onFinished = { drop ->
                            workoutViewModel.stopAndSaveWorkout(heartRateRecoveryDrop = drop) {
                                navController.popBackStack("train_category", false)
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
                    RadialThreeButtons(
                        topContent = { Icon(painterResource(R.drawable.ic_calendario), null, Modifier.fillMaxSize(0.5f), tint = Color.White) },
                        onTopClick = { navController.navigate("objectives") },
                        bottomLeftContent = { Icon(painterResource(R.drawable.ic_cuerpo_persona), null, Modifier.fillMaxSize(0.6f), tint = Color.White) },
                        onBottomLeftClick = { navController.navigate("profile") },
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
            }
        }
    }
}

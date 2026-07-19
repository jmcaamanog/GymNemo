package com.jmcaamanog.gymnemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmcaamanog.gymnemo.data.datastore.UserPreferences
import com.jmcaamanog.gymnemo.data.db.PersonalRecordTuple
import com.jmcaamanog.gymnemo.data.db.WorkoutSession
import com.jmcaamanog.gymnemo.data.db.WorkoutSet
import com.jmcaamanog.gymnemo.data.repository.WorkoutRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random

data class ActiveWorkoutState(
    val isActive: Boolean = false,
    val exerciseName: String = "",
    val bodyPart: String = "",
    val durationSeconds: Long = 0,
    val isPaused: Boolean = false,
    val accumulatedKcal: Double = 0.0,
    val loggedSets: List<WorkoutSet> = emptyList(),
    val birthYear: Int = 1995
)

class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {

    private val _workoutState = MutableStateFlow(ActiveWorkoutState())
    val workoutState: StateFlow<ActiveWorkoutState> = _workoutState.asStateFlow()

    private val _customExercises = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val customExercises: StateFlow<Map<String, List<String>>> = _customExercises.asStateFlow()

    init {
        loadCustomExercises()
    }

    fun loadCustomExercises() {
        val prefs = repository.context.getSharedPreferences("custom_exercises_prefs", android.content.Context.MODE_PRIVATE)
        val map = mutableMapOf<String, List<String>>()
        listOf("brazo", "pierna", "torso").forEach { part ->
            val set = prefs.getStringSet(part, emptySet()) ?: emptySet()
            map[part] = set.toList()
        }
        _customExercises.value = map
    }

    fun addCustomExercise(bodyPart: String, name: String) {
        val prefs = repository.context.getSharedPreferences("custom_exercises_prefs", android.content.Context.MODE_PRIVATE)
        val partKey = bodyPart.lowercase()
        val currentSet = prefs.getStringSet(partKey, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(name)
        prefs.edit().putStringSet(partKey, currentSet).apply()
        loadCustomExercises()
    }

    private var timerJob: Job? = null

    // Obtenemos los rings de progreso de calorías para hoy
    fun getKcalTrainedToday(bodyPart: String): kotlinx.coroutines.flow.Flow<Int> {
        val startOfToday = getStartOfTodayTimestamp()
        return repository.getKcalForPartSince(bodyPart, startOfToday)
    }

    fun getDaysTrainedToday(bodyPart: String): kotlinx.coroutines.flow.Flow<Int> {
        val startOfToday = getStartOfTodayTimestamp()
        return repository.getDaysTrainedSince(bodyPart, startOfToday)
    }

    fun startWorkout(bodyPart: String, exerciseName: String, initialDuration: Long = 0L, initialKcal: Double = 0.0) {
        timerJob?.cancel()

        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            _workoutState.update {
                ActiveWorkoutState(
                    isActive = true,
                    exerciseName = exerciseName,
                    bodyPart = bodyPart,
                    durationSeconds = initialDuration,
                    accumulatedKcal = initialKcal,
                    loggedSets = emptyList(),
                    birthYear = prefs.birthYear
                )
            }

            // Iniciar cronómetro de entrenamiento
            timerJob = launch {
                val age = Calendar.getInstance().get(Calendar.YEAR) - prefs.birthYear
                val isMale = prefs.isMale
                val weight = prefs.currentWeightKg

                while (true) {
                    delay(1000)
                    _workoutState.update { state ->
                        val newDuration = state.durationSeconds + 1

                        // Fallback METs para calorías
                        val met = if (state.bodyPart.lowercase() == "pierna") 6.0 else 5.0
                        val kcalBurnedPerSecond = (met * 3.5 * weight) / (200.0 * 60.0)

                        val updatedKcal = (state.accumulatedKcal + kcalBurnedPerSecond).coerceAtLeast(0.0)

                        if (newDuration % 5 == 0L) {
                            viewModelScope.launch {
                                repository.preferencesRepository.saveActiveWorkoutState(
                                    inProgress = true,
                                    bodyPart = state.bodyPart,
                                    exerciseName = state.exerciseName,
                                    durationSeconds = newDuration.toInt(),
                                    accumulatedKcal = updatedKcal.toFloat()
                                )
                            }
                        }

                        state.copy(
                            durationSeconds = newDuration,
                            accumulatedKcal = updatedKcal
                        )
                    }
                }
            }
        }
    }

    var restStartTime: Long = 0L
    
    // Variables temporales para mostrar estadísticas en la pantalla de completado
    var lastSessionDuration: Long = 0L
    var lastSessionStart: Long = 0L
    var lastSessionEnd: Long = 0L
    var lastSessionKcal: Int = 0

    fun pauseWorkout() {
        restStartTime = System.currentTimeMillis()
        _workoutState.update { it.copy(isPaused = true) }
    }

    fun resumeWorkout() {
        _workoutState.update { it.copy(isPaused = false) }
    }

    fun updateGpsTrack(pointsJson: String, avgSpeed: Float) {
        // Por ahora solo actualizamos el estado si es necesario, 
        // o guardamos para la sesión final
    }

    fun logSet(weight: Float, reps: Int, restSeconds: Int = 90) {
        val state = _workoutState.value
        val newSet = WorkoutSet(
            sessionId = 0, // Se actualizará al guardar
            exerciseName = state.exerciseName,
            weightKg = weight,
            reps = reps,
            restSeconds = restSeconds
        )
        _workoutState.update { it.copy(loggedSets = it.loggedSets + newSet) }
    }

    fun stopAndSaveWorkout(onComplete: () -> Unit) {
        timerJob?.cancel()

        viewModelScope.launch {
            val state = _workoutState.value
            if (state.isActive) {
                val endTimestamp = System.currentTimeMillis()
                val startTimestamp = endTimestamp - (state.durationSeconds * 1000)

                // Guardar estadísticas para la pantalla de completado
                lastSessionDuration = state.durationSeconds
                lastSessionStart = startTimestamp
                lastSessionEnd = endTimestamp
                lastSessionKcal = state.accumulatedKcal.toInt()

                val finalSession = WorkoutSession(
                    timestamp = startTimestamp,
                    endTimestamp = endTimestamp,
                    durationSeconds = state.durationSeconds,
                    totalKcal = state.accumulatedKcal.toInt(),
                    bodyPart = state.bodyPart
                )

                repository.saveSession(finalSession, state.loggedSets)
                repository.preferencesRepository.saveActiveWorkoutState(inProgress = false)
            }

            _workoutState.update { ActiveWorkoutState() } // Resetear estado
            onComplete()
        }
    }

    suspend fun syncUnsyncedSessions(): Int {
        return repository.syncUnsyncedSessions()
    }

    suspend fun getLastWeightForExercise(exerciseName: String): Float {
        val currentSet = _workoutState.value.loggedSets.lastOrNull { it.exerciseName == exerciseName }
        if (currentSet != null) {
            return currentSet.weightKg
        }
        return repository.getLastWeightForExercise(exerciseName)
    }

    suspend fun getLastSetsForExercise(exerciseName: String): List<WorkoutSet> {
        return repository.getLastSetsForExercise(exerciseName)
    }

    suspend fun shouldSuggestOverload(exerciseName: String): Boolean {
        return try {
            val lastSets = repository.getLastSetsForExercise(exerciseName)
            // Si el usuario levantó un peso con éxito >= 10 reps en la última sesión, sugerimos sobrecarga
            lastSets.any { it.reps >= 10 }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isLastSetPersonalRecord(): Boolean {
        val lastSet = _workoutState.value.loggedSets.lastOrNull() ?: return false
        val pr = repository.getPersonalRecordForExercise(lastSet.exerciseName) ?: return true
        return lastSet.weightKg > pr.weightKg || (lastSet.weightKg == pr.weightKg && lastSet.reps > pr.reps)
    }

    suspend fun wasTrainedThisWeek(exerciseName: String): Boolean {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfWeek = calendar.timeInMillis
            repository.hasTrainedExerciseSince(exerciseName, startOfWeek)
        } catch (e: Exception) {
            false
        }
    }

    private fun getStartOfTodayTimestamp(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    suspend fun getPersonalRecord(exerciseName: String): PersonalRecordTuple? {
        return repository.getPersonalRecordForExercise(exerciseName)
    }
}

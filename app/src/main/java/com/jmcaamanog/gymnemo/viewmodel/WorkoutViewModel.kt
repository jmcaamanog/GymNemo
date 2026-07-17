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
    val heartRate: Int = 0,
    val maxHeartRate: Int = 0,
    val spo2: Int = 98,
    val minSpO2: Int = 98,
    val isPaused: Boolean = false,
    val accumulatedKcal: Double = 0.0,
    val loggedSets: List<WorkoutSet> = emptyList(),
    val averageHeartRate: Int = 0,
    val birthYear: Int = 1995,
    val currentTempo: String = "3-0-1-0"
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
    private var heartRateJob: Job? = null

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
        heartRateJob?.cancel()

        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            _workoutState.update {
                ActiveWorkoutState(
                    isActive = true,
                    exerciseName = exerciseName,
                    bodyPart = bodyPart,
                    durationSeconds = initialDuration,
                    heartRate = 75,
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
                        val currentHR = state.heartRate

                        // Fórmula Keytel por segundo
                        val kcalBurnedPerSecond = if (currentHR > 0) {
                            if (isMale) {
                                ((-55.0969 + (0.6309 * currentHR) + (0.1988 * weight) + (0.2017 * age)) / (4.184 * 60.0))
                            } else {
                                ((-20.4022 + (0.4472 * currentHR) - (0.1263 * weight) + (0.074 * age)) / (4.184 * 60.0))
                            }
                        } else {
                            // Fallback METs
                            val met = if (state.bodyPart.lowercase() == "pierna") 6.0 else 5.0
                            (met * 3.5 * weight) / (200.0 * 60.0)
                        }

                        val updatedKcal = (state.accumulatedKcal + kcalBurnedPerSecond).coerceAtLeast(0.0)

                        // Simulación de SpO2:
                        var newSpo2 = state.spo2
                        if (state.isPaused) {
                            // Se recupera 1% cada 2 segundos en pausa
                            if (state.spo2 < 98 && newDuration % 2 == 0L) {
                                newSpo2 = state.spo2 + 1
                            }
                        } else {
                            // En ejercicio se mantiene estable entre 95-98%
                            if (newDuration % 10 == 0L) {
                                newSpo2 = Random.nextInt(95, 99)
                            }
                        }

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
                            accumulatedKcal = updatedKcal,
                            spo2 = newSpo2,
                            minSpO2 = minOf(state.minSpO2, newSpo2)
                        )
                    }
                }
            }

            // Simular sensor de ritmo cardíaco (rango realista de esfuerzo de entrenamiento 110-160 BPM)
            heartRateJob = launch {
                while (true) {
                    delay(2000)
                    _workoutState.update { state ->
                        val hrDelta = Random.nextInt(-5, 6)
                        val nextHR = if (state.isPaused) {
                            // En pausa, baja el pulso gradualmente hacia 80 BPM
                            (state.heartRate - 4).coerceIn(80, 140)
                        } else {
                            // Entrenando, fluctúa arriba hacia esfuerzo
                            (state.heartRate + hrDelta).coerceIn(110, 165)
                        }
                        state.copy(
                            heartRate = nextHR,
                            maxHeartRate = maxOf(state.maxHeartRate, nextHR)
                        )
                    }
                }
            }
        }
    }

    fun pauseWorkout() {
        _workoutState.update { it.copy(isPaused = true, spo2 = 90) }
    }

    fun resumeWorkout() {
        _workoutState.update { it.copy(isPaused = false, spo2 = 98) }
    }

    fun logSet(weight: Float, reps: Int, restSeconds: Int = 90) {
        val state = _workoutState.value
        val newSet = WorkoutSet(
            sessionId = 0, // Se actualizará al guardar
            exerciseName = state.exerciseName,
            weightKg = weight,
            reps = reps,
            restSeconds = restSeconds,
            tempo = state.currentTempo
        )
        _workoutState.update { it.copy(loggedSets = it.loggedSets + newSet) }
    }

    fun setCurrentTempo(tempo: String) {
        _workoutState.update { it.copy(currentTempo = tempo) }
    }

    fun stopAndSaveWorkout(heartRateRecoveryDrop: Int = 0, onComplete: () -> Unit) {
        timerJob?.cancel()
        heartRateJob?.cancel()

        viewModelScope.launch {
            val state = _workoutState.value
            if (state.isActive) {
                val averageHR = if (state.loggedSets.isNotEmpty()) {
                    Random.nextInt(120, 140) // Pulsaciones promedio estimadas
                } else {
                    130
                }

                val endTimestamp = System.currentTimeMillis()
                val startTimestamp = endTimestamp - (state.durationSeconds * 1000)

                val finalSession = WorkoutSession(
                    timestamp = startTimestamp,
                    endTimestamp = endTimestamp,
                    durationSeconds = state.durationSeconds,
                    totalKcal = state.accumulatedKcal.toInt(),
                    averageHeartRate = averageHR,
                    maxHeartRate = state.maxHeartRate,
                    minSpO2 = state.minSpO2,
                    bodyPart = state.bodyPart,
                    heartRateRecoveryDrop = heartRateRecoveryDrop
                )

                repository.saveSession(finalSession, state.loggedSets)
                repository.preferencesRepository.saveActiveWorkoutState(inProgress = false)
            }

            _workoutState.update { ActiveWorkoutState() } // Resetear estado
            onComplete()
        }
    }

    suspend fun getLastWeightForExercise(exerciseName: String): Float {
        val currentSet = _workoutState.value.loggedSets.lastOrNull { it.exerciseName == exerciseName }
        if (currentSet != null) {
            return currentSet.weightKg
        }
        return repository.getLastWeightForExercise(exerciseName)
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

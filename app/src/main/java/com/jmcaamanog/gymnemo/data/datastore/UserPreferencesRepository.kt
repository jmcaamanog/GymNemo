package com.jmcaamanog.gymnemo.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserPreferences(
    val isMale: Boolean = true,
    val heightCm: Int = 175,
    val birthYear: Int = 1990,
    val currentWeightKg: Float = 75f,
    val baseRestSeconds: Int = 90,
    // Objetivos (Días de la semana como String "1,3,5" -> L,X,V)
    val brazoDays: String = "",
    val brazoKcal: Int = 2000,
    val piernaDays: String = "",
    val piernaKcal: Int = 2000,
    val torsoDays: String = "",
    val torsoKcal: Int = 2000
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_MALE = booleanPreferencesKey("is_male")
        val HEIGHT_CM = intPreferencesKey("height_cm")
        val BIRTH_YEAR = intPreferencesKey("birth_year")
        val CURRENT_WEIGHT_KG = floatPreferencesKey("current_weight_kg")
        val BASE_REST_SECONDS = intPreferencesKey("base_rest_seconds")
        
        val BRAZO_DAYS = stringPreferencesKey("brazo_days")
        val BRAZO_KCAL = intPreferencesKey("brazo_kcal")
        val PIERNA_DAYS = stringPreferencesKey("pierna_days")
        val PIERNA_KCAL = intPreferencesKey("pierna_kcal")
        val TORSO_DAYS = stringPreferencesKey("torso_days")
        val TORSO_KCAL = intPreferencesKey("torso_kcal")

        val ACTIVE_WORKOUT_IN_PROGRESS = booleanPreferencesKey("active_workout_in_progress")
        val ACTIVE_WORKOUT_BODY_PART = stringPreferencesKey("active_workout_body_part")
        val ACTIVE_WORKOUT_EXERCISE_NAME = stringPreferencesKey("active_workout_exercise_name")
        val ACTIVE_WORKOUT_DURATION_SECONDS = intPreferencesKey("active_workout_duration_seconds")
        val ACTIVE_WORKOUT_ACCUMULATED_KCAL = floatPreferencesKey("active_workout_accumulated_kcal")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                isMale = preferences[PreferencesKeys.IS_MALE] ?: true,
                heightCm = preferences[PreferencesKeys.HEIGHT_CM] ?: 175,
                birthYear = preferences[PreferencesKeys.BIRTH_YEAR] ?: 1990,
                currentWeightKg = preferences[PreferencesKeys.CURRENT_WEIGHT_KG] ?: 75f,
                baseRestSeconds = preferences[PreferencesKeys.BASE_REST_SECONDS] ?: 90,
                brazoDays = preferences[PreferencesKeys.BRAZO_DAYS] ?: "",
                brazoKcal = preferences[PreferencesKeys.BRAZO_KCAL] ?: 2000,
                piernaDays = preferences[PreferencesKeys.PIERNA_DAYS] ?: "",
                piernaKcal = preferences[PreferencesKeys.PIERNA_KCAL] ?: 2000,
                torsoDays = preferences[PreferencesKeys.TORSO_DAYS] ?: "",
                torsoKcal = preferences[PreferencesKeys.TORSO_KCAL] ?: 2000
            )
        }

    val activeWorkoutStateFlow: Flow<ActiveWorkoutRecoveryState> = context.dataStore.data
        .map { preferences ->
            ActiveWorkoutRecoveryState(
                inProgress = preferences[PreferencesKeys.ACTIVE_WORKOUT_IN_PROGRESS] ?: false,
                bodyPart = preferences[PreferencesKeys.ACTIVE_WORKOUT_BODY_PART] ?: "",
                exerciseName = preferences[PreferencesKeys.ACTIVE_WORKOUT_EXERCISE_NAME] ?: "",
                durationSeconds = preferences[PreferencesKeys.ACTIVE_WORKOUT_DURATION_SECONDS] ?: 0,
                accumulatedKcal = preferences[PreferencesKeys.ACTIVE_WORKOUT_ACCUMULATED_KCAL] ?: 0f
            )
        }

    suspend fun saveActiveWorkoutState(
        inProgress: Boolean,
        bodyPart: String = "",
        exerciseName: String = "",
        durationSeconds: Int = 0,
        accumulatedKcal: Float = 0f
    ) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ACTIVE_WORKOUT_IN_PROGRESS] = inProgress
            prefs[PreferencesKeys.ACTIVE_WORKOUT_BODY_PART] = bodyPart
            prefs[PreferencesKeys.ACTIVE_WORKOUT_EXERCISE_NAME] = exerciseName
            prefs[PreferencesKeys.ACTIVE_WORKOUT_DURATION_SECONDS] = durationSeconds
            prefs[PreferencesKeys.ACTIVE_WORKOUT_ACCUMULATED_KCAL] = accumulatedKcal
        }
    }

    suspend fun updateBrazoDays(days: String) = context.dataStore.edit { it[PreferencesKeys.BRAZO_DAYS] = days }
    suspend fun updateBrazoKcal(kcal: Int) = context.dataStore.edit { it[PreferencesKeys.BRAZO_KCAL] = kcal }
    suspend fun updatePiernaDays(days: String) = context.dataStore.edit { it[PreferencesKeys.PIERNA_DAYS] = days }
    suspend fun updatePiernaKcal(kcal: Int) = context.dataStore.edit { it[PreferencesKeys.PIERNA_KCAL] = kcal }
    suspend fun updateTorsoDays(days: String) = context.dataStore.edit { it[PreferencesKeys.TORSO_DAYS] = days }
    suspend fun updateTorsoKcal(kcal: Int) = context.dataStore.edit { it[PreferencesKeys.TORSO_KCAL] = kcal }

    suspend fun updateIsMale(isMale: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_MALE] = isMale }
    }

    suspend fun updateHeight(heightCm: Int) {
        context.dataStore.edit { it[PreferencesKeys.HEIGHT_CM] = heightCm }
    }

    suspend fun updateBirthYear(year: Int) {
        context.dataStore.edit { it[PreferencesKeys.BIRTH_YEAR] = year }
    }

    suspend fun updateWeight(weightKg: Float) {
        context.dataStore.edit { it[PreferencesKeys.CURRENT_WEIGHT_KG] = weightKg }
    }

    suspend fun updateBaseRest(seconds: Int) {
        context.dataStore.edit { it[PreferencesKeys.BASE_REST_SECONDS] = seconds }
    }
}

data class ActiveWorkoutRecoveryState(
    val inProgress: Boolean,
    val bodyPart: String,
    val exerciseName: String,
    val durationSeconds: Int,
    val accumulatedKcal: Float
)

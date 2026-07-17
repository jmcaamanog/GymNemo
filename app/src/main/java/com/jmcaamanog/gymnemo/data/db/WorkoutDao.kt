package com.jmcaamanog.gymnemo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSet)

    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId")
    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSet>>

    @Query("SELECT SUM(totalKcal) FROM workout_sessions WHERE bodyPart = :bodyPart AND timestamp >= :startTimestamp")
    fun getKcalForPartSince(bodyPart: String, startTimestamp: Long): Flow<Int?>

    @Query("SELECT COUNT(DISTINCT sessionId) FROM workout_sessions WHERE bodyPart = :bodyPart AND timestamp >= :startTimestamp")
    fun getDaysTrainedSince(bodyPart: String, startTimestamp: Long): Flow<Int?>

    @Query("SELECT weightKg FROM workout_sets WHERE exerciseName = :exerciseName ORDER BY setId DESC LIMIT 1")
    suspend fun getLastWeightForExercise(exerciseName: String): Float?

    @Query("SELECT * FROM workout_sets WHERE exerciseName = :exerciseName ORDER BY setId DESC LIMIT 5")
    suspend fun getLastSetsForExercise(exerciseName: String): List<WorkoutSet>

    @Query("""
        SELECT ws.weightKg, ws.reps, s.timestamp 
        FROM workout_sets ws
        INNER JOIN workout_sessions s ON ws.sessionId = s.sessionId
        WHERE ws.exerciseName = :exerciseName
        ORDER BY ws.weightKg DESC, ws.reps DESC
        LIMIT 1
    """)
    suspend fun getPersonalRecordForExercise(exerciseName: String): PersonalRecordTuple?

    @Query("SELECT * FROM workout_sessions WHERE synced = 0")
    suspend fun getUnsyncedSessions(): List<WorkoutSession>

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun getSetsForSessionDirect(sessionId: Long): List<WorkoutSet>

    @Query("UPDATE workout_sessions SET synced = :synced WHERE sessionId = :sessionId")
    suspend fun updateSessionSynced(sessionId: Long, synced: Boolean)

    @Query("SELECT * FROM workout_sessions WHERE bodyPart = :bodyPart ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSessionForPart(bodyPart: String): WorkoutSession?
}

data class PersonalRecordTuple(
    val weightKg: Float,
    val reps: Int,
    val timestamp: Long
)

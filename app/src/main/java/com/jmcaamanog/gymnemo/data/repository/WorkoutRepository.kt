package com.jmcaamanog.gymnemo.data.repository

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.jmcaamanog.gymnemo.data.datastore.UserPreferencesRepository
import com.jmcaamanog.gymnemo.data.db.PersonalRecordTuple
import com.jmcaamanog.gymnemo.data.db.WorkoutDao
import com.jmcaamanog.gymnemo.data.db.WorkoutSession
import com.jmcaamanog.gymnemo.data.db.WorkoutSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepository(
    val context: Context,
    private val workoutDao: WorkoutDao,
    val preferencesRepository: UserPreferencesRepository
) {
    val userPreferencesFlow = preferencesRepository.userPreferencesFlow

    suspend fun saveSession(session: WorkoutSession, sets: List<WorkoutSet>) {
        val sessionId = workoutDao.insertSession(session)
        sets.forEach { set ->
            workoutDao.insertSet(set.copy(sessionId = sessionId))
        }

        // Intentar sincronizar inmediatamente
        val success = sendSessionToMobile(session, sets)
        if (success) {
            workoutDao.updateSessionSynced(sessionId, true)
        }
    }



    private suspend fun sendSessionToMobile(session: WorkoutSession, sets: List<WorkoutSet>): Boolean {
        return try {
            val dataClient = Wearable.getDataClient(context)
            val putDataMapReq = PutDataMapRequest.create("/workout/new_session/${System.currentTimeMillis()}").apply {
                dataMap.putLong("timestamp", session.timestamp)
                dataMap.putLong("endTimestamp", session.endTimestamp)
                dataMap.putLong("duration", session.durationSeconds)
                dataMap.putInt("totalKcal", session.totalKcal)
                dataMap.putString("bodyPart", session.bodyPart)

                // Formato simple de series: "nombre:peso:reps;nombre:peso:reps"
                val setsStr = sets.joinToString(";") { "${it.exerciseName}:${it.weightKg}:${it.reps}" }
                dataMap.putString("setsJson", setsStr)
            }
            val putDataReq = putDataMapReq.asPutDataRequest().setUrgent()
            dataClient.putDataItem(putDataReq)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getKcalForPartSince(bodyPart: String, startTimestamp: Long): Flow<Int> {
        return workoutDao.getKcalForPartSince(bodyPart.lowercase(), startTimestamp).map { it ?: 0 }
    }

    fun getDaysTrainedSince(bodyPart: String, startTimestamp: Long): Flow<Int> {
        return workoutDao.getDaysTrainedSince(bodyPart.lowercase(), startTimestamp).map { it ?: 0 }
    }

    fun getAllSessions(): Flow<List<WorkoutSession>> {
        return workoutDao.getAllSessions()
    }

    suspend fun getLastWeightForExercise(exerciseName: String): Float {
        return workoutDao.getLastWeightForExercise(exerciseName) ?: 20f
    }

    suspend fun getLastSetsForExercise(exerciseName: String): List<WorkoutSet> {
        return workoutDao.getLastSetsForExercise(exerciseName)
    }

    suspend fun getPersonalRecordForExercise(exerciseName: String): PersonalRecordTuple? {
        return workoutDao.getPersonalRecordForExercise(exerciseName)
    }

    suspend fun hasTrainedExerciseSince(exerciseName: String, since: Long): Boolean {
        return workoutDao.hasTrainedExerciseSince(exerciseName, since)
    }

    suspend fun syncUnsyncedSessions(): Int {
        val unsynced = workoutDao.getUnsyncedSessions()
        var count = 0
        unsynced.forEach { session ->
            val sets = workoutDao.getSetsForSessionDirect(session.sessionId)
            val success = sendSessionToMobile(session, sets)
            if (success) {
                workoutDao.updateSessionSynced(session.sessionId, true)
                count++
            }
        }
        return count
    }
}

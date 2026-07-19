package com.jmcaamanog.gymnemo.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val sessionId: Long = 0,
    val timestamp: Long,
    val endTimestamp: Long = 0,
    val durationSeconds: Long,
    val totalKcal: Int,
    val minSpO2: Int = 98,
    val bodyPart: String, // "brazo", "pierna", "torso"
    val synced: Boolean = false
)

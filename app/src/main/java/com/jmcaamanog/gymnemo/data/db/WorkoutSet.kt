package com.jmcaamanog.gymnemo.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sets",
    foreignKeys = [ForeignKey(
        entity = WorkoutSession::class,
        parentColumns = ["sessionId"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class WorkoutSet(
    @PrimaryKey(autoGenerate = true) val setId: Long = 0,
    val sessionId: Long,
    val exerciseName: String,
    val weightKg: Float,
    val reps: Int,
    val restSeconds: Int,
    val tempo: String = "3-0-1-0"
)

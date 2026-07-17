package com.jmcaamanog.gymnemo.mobile.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "mobile_workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val sessionId: Long = 0,
    val timestamp: Long,
    val endTimestamp: Long = 0,
    val durationSeconds: Long,
    val totalKcal: Int,
    val averageHeartRate: Int,
    val maxHeartRate: Int = 0,
    val minSpO2: Int = 98,
    val bodyPart: String,
    val heartRateRecoveryDrop: Int = 0
)

@Entity(
    tableName = "mobile_workout_sets",
    foreignKeys = [ForeignKey(
        entity = WorkoutSessionEntity::class,
        parentColumns = ["sessionId"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true) val setId: Long = 0,
    val sessionId: Long,
    val exerciseName: String,
    val weightKg: Float,
    val reps: Int,
    val restSeconds: Int,
    val tempo: String = "3-0-1-0"
)

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSetEntity)

    @Query("SELECT * FROM mobile_workout_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM mobile_workout_sets WHERE sessionId = :sessionId")
    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM mobile_workout_sets")
    fun getAllSets(): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM mobile_workout_sessions ORDER BY timestamp DESC")
    suspend fun getAllSessionsDirect(): List<WorkoutSessionEntity>

    @Query("SELECT * FROM mobile_workout_sets")
    suspend fun getAllSetsDirect(): List<WorkoutSetEntity>

    @Query("SELECT DISTINCT exerciseName FROM mobile_workout_sets")
    fun getDistinctExercises(): Flow<List<String>>
}

@Database(entities = [WorkoutSessionEntity::class, WorkoutSetEntity::class], version = 4, exportSchema = false)
abstract class WorkoutDb : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDb? = null

        fun getDatabase(context: Context): WorkoutDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDb::class.java,
                    "mobile_workout_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

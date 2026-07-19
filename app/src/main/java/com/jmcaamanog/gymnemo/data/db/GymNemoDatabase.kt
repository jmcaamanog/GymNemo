package com.jmcaamanog.gymnemo.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WorkoutSession::class, WorkoutSet::class], version = 6, exportSchema = false)
abstract class GymNemoDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: GymNemoDatabase? = null

        fun getDatabase(context: Context): GymNemoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymNemoDatabase::class.java,
                    "gymnemo_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

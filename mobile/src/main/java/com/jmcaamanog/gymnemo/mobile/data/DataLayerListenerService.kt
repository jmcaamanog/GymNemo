package com.jmcaamanog.gymnemo.mobile.data

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.jmcaamanog.gymnemo.mobile.data.db.WorkoutDb
import com.jmcaamanog.gymnemo.mobile.data.db.WorkoutSessionEntity
import com.jmcaamanog.gymnemo.mobile.data.db.WorkoutSetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataLayerListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        val db = WorkoutDb.getDatabase(applicationContext)
        
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val uri = event.dataItem.uri
                if (uri.path?.startsWith("/workout/new_session") == true) {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val timestamp = dataMap.getLong("timestamp", System.currentTimeMillis())
                    val endTimestamp = dataMap.getLong("endTimestamp", System.currentTimeMillis())
                    val duration = dataMap.getLong("duration", 0L)
                    val totalKcal = dataMap.getInt("totalKcal", 0)
                    val minSpO2 = dataMap.getInt("minSpO2", 98)
                    val bodyPart = dataMap.getString("bodyPart", "brazo")
                    val setsString = dataMap.getString("setsJson", "")

                    CoroutineScope(Dispatchers.IO).launch {
                        val sessionId = db.workoutDao().insertSession(
                            WorkoutSessionEntity(
                                timestamp = timestamp,
                                endTimestamp = endTimestamp,
                                durationSeconds = duration,
                                totalKcal = totalKcal,
                                minSpO2 = minSpO2,
                                bodyPart = bodyPart
                            )
                        )

                        if (setsString.isNotEmpty()) {
                            // Separador simple: "ejercicio:peso:reps:tempo;ejercicio:peso:reps:tempo"
                            setsString.split(";").filter { it.isNotEmpty() }.forEach { setStr ->
                                val parts = setStr.split(":")
                                if (parts.size >= 3) {
                                    val exercise = parts[0]
                                    val weight = parts[1].toFloatOrNull() ?: 0f
                                    val reps = parts[2].toIntOrNull() ?: 0
                                    val tempoVal = if (parts.size >= 4) parts[3] else "3-0-1-0"
                                    db.workoutDao().insertSet(
                                        WorkoutSetEntity(
                                            sessionId = sessionId,
                                            exerciseName = exercise,
                                            weightKg = weight,
                                            reps = reps,
                                            restSeconds = 90,
                                            tempo = tempoVal
                                        )
                                    )
                                }
                            }
                        }

                        // Copia de seguridad automática si está activada
                        val sharedPrefs = applicationContext.getSharedPreferences("backup_prefs", MODE_PRIVATE)
                        if (sharedPrefs.getBoolean("auto_backup", false)) {
                            try {
                                val sessionsList = db.workoutDao().getAllSessionsDirect()
                                val allSetsList = db.workoutDao().getAllSetsDirect()
                                val backupDir = applicationContext.getExternalFilesDir(null)
                                if (backupDir != null) {
                                    val backupFile = java.io.File(backupDir, "gymnemo_auto_backup.json")
                                    val jsonArray = org.json.JSONArray()
                                    sessionsList.forEach { s ->
                                        val sSets = allSetsList.filter { it.sessionId == s.sessionId }
                                        val sObj = org.json.JSONObject().apply {
                                            put("timestamp", s.timestamp)
                                            put("endTimestamp", s.endTimestamp)
                                            put("durationSeconds", s.durationSeconds)
                                            put("totalKcal", s.totalKcal)
                                            put("minSpO2", s.minSpO2)
                                            put("bodyPart", s.bodyPart)

                                            val setsArr = org.json.JSONArray()
                                            sSets.forEach { set ->
                                                val setObj = org.json.JSONObject().apply {
                                                    put("exerciseName", set.exerciseName)
                                                    put("weightKg", set.weightKg.toDouble())
                                                    put("reps", set.reps)
                                                    put("restSeconds", set.restSeconds)
                                                    put("tempo", set.tempo)
                                                }
                                                setsArr.put(setObj)
                                            }
                                            put("sets", setsArr)
                                        }
                                        jsonArray.put(sObj)
                                    }
                                    backupFile.writeText(jsonArray.toString(2))
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }
}

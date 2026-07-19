package com.jmcaamanog.gymnemo.mobile.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
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
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                    val bodyPart = dataMap.getString("bodyPart", "brazo")
                    val setsString = dataMap.getString("setsJson", "")

                    CoroutineScope(Dispatchers.IO).launch {
                        val sessionId = db.workoutDao().insertSession(
                            WorkoutSessionEntity(
                                timestamp = timestamp,
                                endTimestamp = endTimestamp,
                                durationSeconds = duration,
                                totalKcal = totalKcal,
                                bodyPart = bodyPart
                            )
                        )

                        val setsList = mutableListOf<WorkoutSetEntity>()
                        if (setsString.isNotEmpty()) {
                            // Separador simple: "ejercicio:peso:reps;ejercicio:peso:reps"
                            setsString.split(";").filter { it.isNotEmpty() }.forEach { setStr ->
                                val parts = setStr.split(":")
                                if (parts.size >= 3) {
                                    val exercise = parts[0]
                                    val weight = parts[1].toFloatOrNull() ?: 0f
                                    val reps = parts[2].toIntOrNull() ?: 0
                                    val setEntity = WorkoutSetEntity(
                                        sessionId = sessionId,
                                        exerciseName = exercise,
                                        weightKg = weight,
                                        reps = reps,
                                        restSeconds = 90
                                    )
                                    db.workoutDao().insertSet(setEntity)
                                    setsList.add(setEntity)
                                }
                            }
                        }

                        // Enviar a Google Sheets si está configurado
                        val sessionEntity = WorkoutSessionEntity(
                            sessionId = sessionId,
                            timestamp = timestamp,
                            endTimestamp = endTimestamp,
                            durationSeconds = duration,
                            totalKcal = totalKcal,
                            bodyPart = bodyPart
                        )
                        sendWorkoutToGoogleSheets(applicationContext, sessionEntity, setsList)

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
                                            put("bodyPart", s.bodyPart)
 
                                            val setsArr = org.json.JSONArray()
                                            sSets.forEach { set ->
                                                val setObj = org.json.JSONObject().apply {
                                                    put("exerciseName", set.exerciseName)
                                                    put("weightKg", set.weightKg.toDouble())
                                                    put("reps", set.reps)
                                                    put("restSeconds", set.restSeconds)
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

fun sendWorkoutToGoogleSheets(context: Context, session: WorkoutSessionEntity, sets: List<WorkoutSetEntity>) {
    val prefs = context.getSharedPreferences("google_sheets_prefs", Context.MODE_PRIVATE)
    val defaultUrl = "https://script.google.com/macros/s/AKfycbwASXMQJjYm5ge6L9w34mU5fr5fByC-Nrf9l1Iwtj-C8YcENW78a_4xBrpPV02N8ZdJ/exec"
    val urlStr = prefs.getString("sheets_url", defaultUrl) ?: defaultUrl
    if (urlStr.isBlank()) return

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateStr = dateFormat.format(Date(session.timestamp))
            val startStr = timeFormat.format(Date(session.timestamp))
            val endStr = timeFormat.format(Date(session.endTimestamp))

            val json = JSONObject().apply {
                put("timestamp", session.timestamp)
                put("date", dateStr)
                put("startTime", startStr)
                put("endTime", endStr)
                put("durationSeconds", session.durationSeconds)
                put("totalKcal", session.totalKcal)
                put("bodyPart", session.bodyPart)

                val setsArray = JSONArray()
                sets.forEach { set ->
                    setsArray.put(JSONObject().apply {
                        put("exerciseName", set.exerciseName)
                        put("weightKg", set.weightKg.toDouble())
                        put("reps", set.reps)
                        put("restSeconds", set.restSeconds)
                    })
                }
                put("sets", setsArray)
            }

            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 15000
            conn.readTimeout = 15000

            conn.outputStream.use { os ->
                os.write(json.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299 || responseCode == 302) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "¡Entreno exportado a Google Sheets!", Toast.LENGTH_SHORT).show()
                }
            }
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Error al enviar a Google Sheets: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

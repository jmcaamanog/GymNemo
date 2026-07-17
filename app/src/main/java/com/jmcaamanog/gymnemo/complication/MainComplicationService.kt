package com.jmcaamanog.gymnemo.complication

import android.app.PendingIntent
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.jmcaamanog.gymnemo.data.db.GymNemoDatabase
import com.jmcaamanog.gymnemo.presentation.MainActivity
import kotlinx.coroutines.flow.first
import java.util.Calendar

class MainComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type != ComplicationType.SHORT_TEXT) {
            return null
        }
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("150kcal").build(),
            contentDescription = PlainComplicationText.Builder("GymNemo Kcal").build()
        ).build()
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.SHORT_TEXT) {
            return null
        }

        val database = GymNemoDatabase.getDatabase(applicationContext)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = calendar.timeInMillis

        val sessions = try {
            database.workoutDao().getAllSessions().first()
        } catch (e: Exception) {
            emptyList()
        }
        val todaySessions = sessions.filter { it.timestamp >= todayStart }
        val totalKcal = todaySessions.sumOf { it.totalKcal }

        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("${totalKcal}kcal").build(),
            contentDescription = PlainComplicationText.Builder("Calorías hoy").build()
        )
            .setTapAction(pendingIntent)
            .build()
    }
}
package com.jmcaamanog.gymnemo.mobile

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class GymNemoWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val views = RemoteViews(context.packageName, R.layout.gym_nemo_widget)

    // Intent for Historial
    val intentHistorial = Intent(context, MainActivity::class.java).apply {
        putExtra("startTab", "historial")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingHistorial = PendingIntent.getActivity(
        context, 
        1, 
        intentHistorial, 
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.btn_widget_historial, pendingHistorial)

    // Intent for Objetivos
    val intentObjetivos = Intent(context, MainActivity::class.java).apply {
        putExtra("startTab", "objetivos")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingObjetivos = PendingIntent.getActivity(
        context, 
        2, 
        intentObjetivos, 
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.btn_widget_objetivos, pendingObjetivos)

    // Intent for Ajustes
    val intentAjustes = Intent(context, MainActivity::class.java).apply {
        putExtra("startTab", "ajustes")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingAjustes = PendingIntent.getActivity(
        context, 
        3, 
        intentAjustes, 
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.btn_widget_ajustes, pendingAjustes)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}

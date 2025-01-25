package com.bjit.alarmmanager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val snoozeDuration = 5 * 60 * 1000 // 5 minutes
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeIntent = Intent(context, MyAlarm::class.java)
        snoozeIntent.putExtra("audioUri", intent.getStringExtra("audioUri"))
        val pendingIntent = PendingIntent.getBroadcast(context, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.set(
            AlarmManager.RTC,
            System.currentTimeMillis() + snoozeDuration.toLong(),
            pendingIntent
        )
        Toast.makeText(context, "Alarm snoozed for ${snoozeDuration / 60000} minutes", Toast.LENGTH_SHORT).show()
    }
}
package com.bjit.alarmmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyAlarm : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val audioUri = intent.getStringExtra("audioUri")
        val alarmTime = System.currentTimeMillis().toString() // Example alarm time

        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("audioUri", audioUri)
            putExtra("alarmTime", alarmTime)
        }
        context.startActivity(alarmIntent)
    }
}
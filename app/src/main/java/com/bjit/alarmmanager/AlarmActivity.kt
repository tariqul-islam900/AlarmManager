// AlarmActivity.kt
package com.bjit.alarmmanager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AlarmActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        val alarmTime = intent.getStringExtra("alarmTime")?.toLongOrNull()
        val audioUri = intent.getStringExtra("audioUri")

        val textViewAlarmTime: TextView = findViewById(R.id.textViewAlarmTime)
        val buttonSnooze5: Button = findViewById(R.id.buttonSnooze5)
        val buttonSnooze10: Button = findViewById(R.id.buttonSnooze10)
        val buttonSnooze15: Button = findViewById(R.id.buttonSnooze15)
        val buttonClose: Button = findViewById(R.id.buttonClose)

        alarmTime?.let {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
            textViewAlarmTime.text = "Alarm Time: ${dateFormat.format(Date(it))}"
        }

        audioUri?.let {
            val uri = Uri.parse(it)
            mediaPlayer = MediaPlayer.create(this, uri)
            mediaPlayer?.start()
        }

        buttonSnooze5.setOnClickListener { snoozeAlarm(5 * 60 * 1000) }
        buttonSnooze10.setOnClickListener { snoozeAlarm(10 * 60 * 1000) }
        buttonSnooze15.setOnClickListener { snoozeAlarm(15 * 60 * 1000) }
        buttonClose.setOnClickListener { closeAlarm() }
    }

    private fun snoozeAlarm(snoozeDuration: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeIntent = Intent(this, MyAlarm::class.java)
        snoozeIntent.putExtra("audioUri", intent.getStringExtra("audioUri"))
        val pendingIntent = PendingIntent.getBroadcast(this, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.set(
            AlarmManager.RTC,
            System.currentTimeMillis() + snoozeDuration.toLong(),
            pendingIntent
        )
        Toast.makeText(this, "Alarm snoozed for ${snoozeDuration / 60000} minutes", Toast.LENGTH_SHORT).show()
        mediaPlayer?.stop()
        finish()
    }

    private fun closeAlarm() {
        mediaPlayer?.stop()
        val alarmTime = intent.getStringExtra("alarmTime")?.toLongOrNull()
        alarmTime?.let {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("removeAlarmTime", it)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}
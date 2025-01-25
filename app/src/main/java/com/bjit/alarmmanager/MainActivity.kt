// MainActivity.kt
package com.bjit.alarmmanager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerViewAlarms: RecyclerView
    private lateinit var alarmAdapter: AlarmAdapter
    private val alarmList = ArrayList<Long>()
    private var selectedAudioUri: Uri? = null
    private lateinit var dialogView: View
    private val sharedPreferences by lazy { getSharedPreferences("alarms", Context.MODE_PRIVATE) }

    companion object {
        private const val REQUEST_CODE_SELECT_AUDIO = 1
    }

    private val selectAudioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                val fileSize = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
                if (fileSize > 1 * 1024 * 1024) {
                    Toast.makeText(this, "File size exceeds 1MB", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                val mimeType = contentResolver.getType(uri)
                if (mimeType != "audio/mpeg") {
                    Toast.makeText(this, "Only MP3 files are allowed", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                selectedAudioUri = uri
                val textViewSelectedAudio: TextView = dialogView.findViewById(R.id.textViewSelectedAudio)
                val buttonRemoveAudio: Button = dialogView.findViewById(R.id.buttonRemoveAudio)
                textViewSelectedAudio.text = uri.lastPathSegment
                textViewSelectedAudio.visibility = View.VISIBLE
                buttonRemoveAudio.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerViewAlarms = findViewById(R.id.recyclerViewAlarms)
        val imageViewAddAlarm: ImageView = findViewById(R.id.imageViewAddAlarm)

        alarmAdapter = AlarmAdapter(alarmList, this)
        recyclerViewAlarms.layoutManager = LinearLayoutManager(this)
        recyclerViewAlarms.adapter = alarmAdapter

        loadAlarms()

        imageViewAddAlarm.setOnClickListener {
            showSetAlarmDialog()
        }

        intent.getLongExtra("removeAlarmTime", -1L).takeIf { it != -1L }?.let { alarmTime ->
            alarmList.remove(alarmTime)
            saveAlarms()
            alarmAdapter.notifyDataSetChanged()
        }
    }

    private fun showSetAlarmDialog() {
        dialogView = layoutInflater.inflate(R.layout.dialog_set_alarm, null)
        val datePicker: DatePicker = dialogView.findViewById(R.id.datePicker)
        val timePicker: TimePicker = dialogView.findViewById(R.id.timePicker)
        val buttonSelectAudio: Button = dialogView.findViewById(R.id.buttonSelectAudio)
        val textViewSelectedAudio: TextView = dialogView.findViewById(R.id.textViewSelectedAudio)
        val buttonRemoveAudio: Button = dialogView.findViewById(R.id.buttonRemoveAudio)
        val buttonSetAlarm: Button = dialogView.findViewById(R.id.buttonSetAlarm)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Set Alarm")
            .create()

        datePicker.setOnDateChangedListener { _, year, month, dayOfMonth ->
            datePicker.visibility = View.GONE
            timePicker.visibility = View.VISIBLE
            buttonSelectAudio.visibility = View.VISIBLE
            buttonSetAlarm.visibility = View.VISIBLE

            buttonSetAlarm.setOnClickListener {
                val hour = if (Build.VERSION.SDK_INT >= 23) timePicker.hour else timePicker.currentHour
                val minute = if (Build.VERSION.SDK_INT >= 23) timePicker.minute else timePicker.currentMinute
                val calendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, hour, minute, 0)
                }
                setAlarm(calendar.timeInMillis)
                alertDialog.dismiss()
            }
        }

        buttonSelectAudio.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "audio/*"
            }
            selectAudioLauncher.launch(intent)
        }

        buttonRemoveAudio.setOnClickListener {
            selectedAudioUri = null
            textViewSelectedAudio.visibility = View.GONE
            buttonRemoveAudio.visibility = View.GONE
        }

        alertDialog.show()
    }

    private fun setAlarm(timeInMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MyAlarm::class.java)
        selectedAudioUri?.let {
            intent.putExtra("audioUri", it.toString())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarmList.size,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    this,
                    "Cannot schedule exact alarms. Please grant the permission.",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
            alarmList.add(timeInMillis)
            saveAlarms()
            alarmAdapter.notifyItemInserted(alarmList.size - 1)
            Toast.makeText(this, "Alarm is set", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permission denied to schedule exact alarms.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun saveAlarms() {
        val editor = sharedPreferences.edit()
        editor.putStringSet("alarmList", alarmList.map { it.toString() }.toSet())
        editor.apply()
    }

    private fun loadAlarms() {
        val savedAlarms =
            sharedPreferences.getStringSet("alarmList", emptySet())?.map { it.toLong() }
                ?: emptyList()
        val currentTime = System.currentTimeMillis()
        val upcomingAlarms = savedAlarms.filter { it > currentTime }
        alarmList.clear()
        alarmList.addAll(upcomingAlarms)
        saveAlarms() // Save only upcoming alarms
        alarmAdapter.notifyDataSetChanged()
    }
}
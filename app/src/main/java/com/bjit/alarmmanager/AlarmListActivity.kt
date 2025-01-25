package com.bjit.alarmmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AlarmListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_list)

        val alarmList = intent.getSerializableExtra("alarmList") as ArrayList<Long>
        alarmList.sortDescending()
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewAlarms)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = AlarmAdapter(alarmList, this)
    }
}
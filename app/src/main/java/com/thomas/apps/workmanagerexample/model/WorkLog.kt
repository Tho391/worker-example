package com.thomas.apps.workmanagerexample.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WorkLog(
    val time: Long,
    val runFail: Boolean,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)
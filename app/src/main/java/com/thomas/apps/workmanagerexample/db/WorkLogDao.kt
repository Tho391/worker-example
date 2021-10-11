package com.thomas.apps.workmanagerexample.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thomas.apps.workmanagerexample.model.WorkLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkLogDao {

    @Query("select * from WorkLog order by time desc")
    fun getWorkLogs(): Flow<List<WorkLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(log: WorkLog): Long
}
package com.thomas.apps.workmanagerexample.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.thomas.apps.workmanagerexample.model.WorkLog

@Database(
    entities = [WorkLog::class],
    version = 1,
    exportSchema = false
)
abstract class WorkDatabase : RoomDatabase() {

    abstract val workLogDao: WorkLogDao

    companion object {
        private const val DATABASE_NAME = "notes.db"

        @Volatile
        private var instance: WorkDatabase? = null

        fun getInstance(context: Context): WorkDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): WorkDatabase {
            return Room
                .databaseBuilder(context, WorkDatabase::class.java, DATABASE_NAME)
                .build()
        }
    }
}
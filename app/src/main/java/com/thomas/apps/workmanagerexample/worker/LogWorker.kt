package com.thomas.apps.workmanagerexample.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.thomas.apps.workmanagerexample.DataStoreUtils
import com.thomas.apps.workmanagerexample.DataStoreUtils.getRunFail
import com.thomas.apps.workmanagerexample.NotificationUtils.showNotification
import com.thomas.apps.workmanagerexample.db.WorkDatabase
import com.thomas.apps.workmanagerexample.model.WorkLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import java.time.Instant
import kotlin.math.roundToInt

class LogWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val PROGRESS = "Progress"

        //delay
        private const val DELAY_DURATION = 1000L

        //5 second
        private const val WORK_DURATION = 5
    }

    override suspend fun doWork(): Result {

        setProgress(workDataOf(PROGRESS to 0))

        val repeatTimes = WORK_DURATION * 10
        repeat(repeatTimes) {
            delay(DELAY_DURATION / 10)

            val percent: Int = (it.toDouble() / repeatTimes * 100).roundToInt()
            setProgress(workDataOf(PROGRESS to percent))
        }

        val runFail =
            applicationContext.getRunFail().firstOrNull() ?: DataStoreUtils.DEFAULT_RUN_FAIL
        Timber.i("runFail $runFail")
        val db = WorkDatabase.getInstance(applicationContext)

        val log = WorkLog(Instant.now().toEpochMilli(), runFail)
        val id = db.workLogDao.insertOrReplace(log)

        val updatedLog = log.copy(id = id)
        applicationContext.showNotification(updatedLog)

        return if (runFail) {
            Result.retry()
        } else {
            Result.success()
        }
    }


}
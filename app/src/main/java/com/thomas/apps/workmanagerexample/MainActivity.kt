package com.thomas.apps.workmanagerexample

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.thomas.apps.workmanagerexample.DataStoreUtils.getDelay
import com.thomas.apps.workmanagerexample.DataStoreUtils.getEnableNotification
import com.thomas.apps.workmanagerexample.DataStoreUtils.getInterval
import com.thomas.apps.workmanagerexample.DataStoreUtils.getPolicy
import com.thomas.apps.workmanagerexample.DataStoreUtils.getRunFail
import com.thomas.apps.workmanagerexample.DataStoreUtils.setDelay
import com.thomas.apps.workmanagerexample.DataStoreUtils.setEnableNotification
import com.thomas.apps.workmanagerexample.DataStoreUtils.setInterval
import com.thomas.apps.workmanagerexample.DataStoreUtils.setPolicy
import com.thomas.apps.workmanagerexample.DataStoreUtils.setRunFail
import com.thomas.apps.workmanagerexample.NotificationUtils.createLogNotificationChannel
import com.thomas.apps.workmanagerexample.databinding.ActivityMainBinding
import com.thomas.apps.workmanagerexample.db.WorkDatabase
import com.thomas.apps.workmanagerexample.worker.LogWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val logAdapter by lazy { WorkLogAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpDropdown()

        setUpButton()

        setUpRecyclerView()

        loadDefaultConfigToUI()

        loadDataFromDb()

        startWorker()
    }

    private fun loadDataFromDb() {
        val db = WorkDatabase.getInstance(this)
        db.workLogDao.getWorkLogs().onEach {
            logAdapter.submitList(it)
            binding.recyclerView.postDelayed({
                binding.recyclerView.smoothScrollToPosition(0)
            }, 300)
        }.launchIn(lifecycleScope)
    }

    private fun setUpRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = logAdapter
        }
    }

    private fun setUpButton() {
        with(binding) {
            buttonSave.setOnClickListener {
                val interval = textInputInterval.text?.toString()?.toLongOrNull()
                val delay = textInputDelay.text?.toString()?.toLongOrNull()
                val policy = textInputPolicy.text?.toString()
                val runFail = switchWorker.isChecked
                val enableNotification = switchNotification.isChecked

                if (isSettingValid(interval, delay, policy)) {
                    lifecycleScope.launch {
                        setInterval(interval!!)
                        setDelay(delay!!)
                        setPolicy(policy!!)
                        setRunFail(runFail)
                        setEnableNotification(enableNotification)
                        Toast.makeText(this@MainActivity, "Saved", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this@MainActivity, "Check Input", Toast.LENGTH_SHORT).show()
                }
            }

            buttonStartWorker.setOnClickListener { startWorker() }
        }

    }

    private fun isSettingValid(interval: Long?, delay: Long?, policy: String?): Boolean {
        val validInterval = interval != null && interval >= 15L
        val validDelay = delay != null && delay >= 10L
        val validPolicy = try {
            if (policy != null) BackoffPolicy.valueOf(policy) else null
        } catch (e: Exception) {
            null
        } != null
        return validInterval && validDelay && validPolicy
    }

    private fun loadDefaultConfigToUI() {
        lifecycleScope.launchWhenCreated {
            getInterval().collectLatest {
                binding.textInputInterval.setText(
                    it.toString(),
                    TextView.BufferType.EDITABLE
                )
            }
        }

        lifecycleScope.launchWhenCreated {
            getDelay().collectLatest {
                binding.textInputDelay.setText(
                    it.toString(),
                    TextView.BufferType.EDITABLE
                )
            }
        }

        lifecycleScope.launchWhenCreated {
            getPolicy().collectLatest {
                binding.textInputPolicy.setText(it, false)
            }
        }

        lifecycleScope.launchWhenCreated {
            getRunFail().collectLatest { binding.switchWorker.isChecked = it }
        }

        lifecycleScope.launchWhenCreated {
            getEnableNotification().collectLatest { binding.switchNotification.isChecked = it }
        }
    }

    private fun setUpDropdown() {
        val policies = listOf(
            BackoffPolicy.EXPONENTIAL.name,
            BackoffPolicy.LINEAR.name,
        )
        val adapter = ArrayAdapter<String>(this, R.layout.item_dropdown, policies)
        binding.textInputPolicy.setAdapter(adapter)
    }

    val tag = "Log worker"

    private fun startWorker() {
        val intervalFlow = getInterval().distinctUntilChanged()
        val delayFlow = getDelay().distinctUntilChanged()
        val policyFlow = getPolicy().distinctUntilChanged()
        val setting = combine(intervalFlow, delayFlow, policyFlow) { interval, delay, policy ->
            Timber.i("startWorker $interval - $delay - $policy")
            Triple(interval, delay, policy)
        }
        lifecycleScope.launch(Dispatchers.Main) {
            setting.collect { setting ->
                val interval = setting.first
                val delay = setting.second
                val policy = setting.third

                val backoffPolicy = try {
                    BackoffPolicy.valueOf(policy)
                } catch (e: Exception) {
                    Timber.e(e)
                    BackoffPolicy.EXPONENTIAL
                }
                val workManager = WorkManager.getInstance(this@MainActivity)

                val uniqueWorkName = "Log worker"
                val existingWorkPolicy = ExistingPeriodicWorkPolicy.REPLACE

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val logWorkRequest =
                    PeriodicWorkRequestBuilder<LogWorker>(interval, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .setBackoffCriteria(backoffPolicy, delay, TimeUnit.SECONDS)
                        .addTag(tag)
                        .build()

                workManager.enqueueUniquePeriodicWork(
                    uniqueWorkName,
                    existingWorkPolicy,
                    logWorkRequest
                )

                WorkManager.getInstance(applicationContext)
                    // requestId is the WorkRequest id
                    .getWorkInfosByTagLiveData(tag)
                    .observe(this@MainActivity) { workInfos ->
                        val workInfo = workInfos.firstOrNull()
                        val state = workInfo?.state?.name
                        val progress = workInfo?.progress?.getInt(LogWorker.PROGRESS, 0)
                        binding.textInputState.setText(
                            "$state - ${progress}% - run attempt count: ${workInfo?.runAttemptCount}",
                            TextView.BufferType.EDITABLE
                        )
                        //Timber.i("data: ${workInfo?.progress}")
                    }
            }
        }
    }
}
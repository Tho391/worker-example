package com.thomas.apps.workmanagerexample

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.BackoffPolicy
import kotlinx.coroutines.flow.map

object DataStoreUtils {

    // At the top level of your kotlin file:
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    const val DEFAULT_INTERVAL: Long = 15L
    const val DEFAULT_DELAY: Long = 15L
    val DEFAULT_POLICY = BackoffPolicy.EXPONENTIAL.name
    const val DEFAULT_RUN_FAIL = false
    const val DEFAULT_ENABLE_NOTIFICATION = false

    private val INTERVAL = longPreferencesKey("INTERVAL")
    private val DELAY = longPreferencesKey("DELAY")
    private val POLICY = stringPreferencesKey("POLICY")
    private val RUN_FAIL = booleanPreferencesKey("RUN_FAIL")
    private val ENABLE_NOTIFICATION = booleanPreferencesKey("ENABLE_NOTIFICATION")


    fun Context.getInterval() = dataStore.data.map { it[INTERVAL] ?: DEFAULT_INTERVAL }
    fun Context.getDelay() = dataStore.data.map { it[DELAY] ?: DEFAULT_DELAY }
    fun Context.getPolicy() = dataStore.data.map { it[POLICY] ?: DEFAULT_POLICY }
    fun Context.getRunFail() = dataStore.data.map { it[RUN_FAIL] ?: DEFAULT_RUN_FAIL }
    fun Context.getEnableNotification() =
        dataStore.data.map { it[ENABLE_NOTIFICATION] ?: DEFAULT_ENABLE_NOTIFICATION }

    suspend fun Context.setInterval(interval: Long) {
        dataStore.edit {
            it[INTERVAL] = interval
        }
    }

    suspend fun Context.setDelay(delay: Long) {
        dataStore.edit {
            it[DELAY] = delay
        }
    }

    suspend fun Context.setPolicy(policy: String) {
        dataStore.edit {
            it[POLICY] = policy
        }
    }

    suspend fun Context.setRunFail(runFail: Boolean) {
        dataStore.edit {
            it[RUN_FAIL] = runFail
        }
    }

    suspend fun Context.setEnableNotification(enable: Boolean) {
        dataStore.edit {
            it[ENABLE_NOTIFICATION] = enable
        }
    }
}
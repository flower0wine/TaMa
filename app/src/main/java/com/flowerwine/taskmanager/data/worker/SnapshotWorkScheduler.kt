package com.flowerwine.taskmanager.data.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SnapshotWorkScheduler {

    private const val WorkName = "device_snapshot_worker"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<SnapshotWorker>(6, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WorkName,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}

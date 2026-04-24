package com.flowerwine.taskmanager.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.flowerwine.taskmanager.TaskManagerApplication

class SnapshotWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val appContainer = (applicationContext as TaskManagerApplication).appContainer
        return runCatching {
            appContainer.deviceRepository.captureSnapshot()
            Result.success()
        }.getOrElse { Result.retry() }
    }
}

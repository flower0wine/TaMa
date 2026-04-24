package com.flowerwine.taskmanager

import android.app.Application
import com.flowerwine.taskmanager.core.di.AppContainer
import com.flowerwine.taskmanager.data.worker.SnapshotWorkScheduler

class TaskManagerApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
        SnapshotWorkScheduler.schedule(applicationContext)
    }
}

package com.flowerwine.taskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.flowerwine.taskmanager.navigation.TaskManagerApp
import com.flowerwine.taskmanager.ui.theme.TaskManagerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as TaskManagerApplication).appContainer

        setContent {
            TaskManagerTheme {
                TaskManagerApp(appContainer = appContainer)
            }
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.PlannerDatabase
import com.example.data.local.ScenarioRepository
import com.example.data.local.UserPreferencesRepository
import com.example.ui.PlannerApp
import com.example.ui.state.PlannerViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = PlannerDatabase.getInstance(applicationContext)
        val preferencesRepository = UserPreferencesRepository(applicationContext)
        val scenarioRepository = ScenarioRepository(
            scenarioDao = database.scenarioDao(),
            preferencesRepository = preferencesRepository
        )
        val viewModel = ViewModelProvider(
            this,
            PlannerViewModel.Factory(scenarioRepository, preferencesRepository)
        )[PlannerViewModel::class.java]

        setContent {
            PlannerApp(viewModel = viewModel)
        }
    }
}

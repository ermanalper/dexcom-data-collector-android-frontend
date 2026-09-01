package com.alptrosoft.dexcom_data_collector_android_frontend.presentation

import android.app.Application
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.ui.components.GlucoseChartScreen
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.GlucoseViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue

@HiltAndroidApp
class DexcomApp : Application()

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: GlucoseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.state.collectAsState()

            GlucoseChartScreen(state = state, onEvent = viewModel::onEvent)
        }
    }



}
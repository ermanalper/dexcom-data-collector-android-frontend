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
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.InsulinDoseViewModel

@HiltAndroidApp
class DexcomApp : Application()

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val glucoseViewModel: GlucoseViewModel by viewModels()
    private val insulinViewModel: InsulinDoseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val glucoseState by glucoseViewModel.state.collectAsState()
            val insulinState by insulinViewModel.state.collectAsState()

            GlucoseChartScreen(
                glucoseState = glucoseState,
                insulinState = insulinState,
                onGlucoseEvent = glucoseViewModel::onEvent,
                onInsulinEvent = insulinViewModel::onEvent
            )
        }
    }



}
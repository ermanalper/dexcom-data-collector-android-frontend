package com.alptrosoft.dexcom_data_collector_android_frontend.presentation

import android.app.Application
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.alptrosoft.dexcom_data_collector_android_frontend.R
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.GlucoseViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltAndroidApp
class DexcomApp : Application()

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: GlucoseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        viewModel

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        testEndpoints()
    }
    private fun testEndpoints() {
        lifecycleScope.launch {
            while(true) {
                viewModel.getLatestGlucose()
                delay(1000) // Non-blocking delay
                viewModel.getGlucoseHistory(5)
                delay(1000) // Non-blocking delay
            }
        }
    }

}
package com.alptrosoft.dexcom_data_collector_android_frontend.presentation

import android.app.Application
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.activity.compose.setContent
import androidx.compose.material.icons.filled.Notifications
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.ui.components.AlarmsScreen
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.ui.components.CompareChartsScreen
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.ui.components.GlucoseChartScreen
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.AlarmViewModel
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.GlucoseViewModel
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.InsulinDoseViewModel
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.MealViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DexcomApp : Application()

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val glucoseViewModel: GlucoseViewModel by viewModels()
    private val insulinViewModel: InsulinDoseViewModel by viewModels()
    private val mealViewModel: MealViewModel by viewModels()
    private val alarmViewModel: AlarmViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            MaterialTheme {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.ShowChart, contentDescription = "Grafik") },
                                label = { Text("Grafik") },
                                selected = currentRoute == "home",
                                onClick = {
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.BarChart, contentDescription = "Karşılaştır") },
                                label = { Text("Karşılaştır") },
                                selected = currentRoute == "compare",
                                onClick = {
                                    navController.navigate("compare") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Notifications, contentDescription = "Alarmlar") },
                                label = { Text("Alarmlar") },
                                selected = currentRoute == "alarms",
                                onClick = {
                                    navController.navigate("alarms") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )

                        }

                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            val glucoseState by glucoseViewModel.state.collectAsState()
                            val insulinState by insulinViewModel.state.collectAsState()
                            val mealState by mealViewModel.state.collectAsState()

                            GlucoseChartScreen(
                                glucoseState = glucoseState,
                                insulinState = insulinState,
                                mealState = mealState,
                                onGlucoseEvent = glucoseViewModel::onEvent,
                                onInsulinEvent = insulinViewModel::onEvent,
                                onMealEvent = mealViewModel::onEvent
                            )
                        }

                        composable("compare") {
                            CompareChartsScreen()
                        }

                        composable("alarms") {
                            val alarmState by alarmViewModel.state.collectAsState()
                            AlarmsScreen(
                                state = alarmState,
                                onEvent = alarmViewModel::onEvent
                            )
                        }
                    }
                }
            }
        }
    }
}
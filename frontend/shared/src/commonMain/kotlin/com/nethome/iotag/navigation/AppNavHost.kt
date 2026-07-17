package com.nethome.iotag.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nethome.iotag.data.repository.SensorRepository
import com.nethome.iotag.ui.screens.DashboardScreen
import com.nethome.iotag.ui.screens.EnergyScreen
import com.nethome.iotag.ui.screens.SettingsScreen
import com.nethome.iotag.ui.viewmodels.SettingsViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val sensorRepository = remember { SensorRepository() }

    NavHost(
        navController = navController,
        startDestination = DashboardRoute,
        modifier = modifier
    ) {
        composable<DashboardRoute> {
            DashboardScreen(Modifier)
        }
        composable<SettingsRoute> {
            val settingsViewModel = remember {
                SettingsViewModel(repository = sensorRepository)
            }
            SettingsScreen(viewModel = settingsViewModel)
        }
        composable<EnergyRoute> {
            EnergyScreen(sensorRepository)
        }
    }
}
package com.nethome.iotag.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nethome.iotag.ui.screens.DashboardScreen
import com.nethome.iotag.ui.screens.EnergyScreen
import com.nethome.iotag.ui.screens.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = DashboardRoute,
        modifier = modifier
    ) {
        composable<DashboardRoute> {
            DashboardScreen(Modifier)
        }
        composable<SettingsRoute> {
            SettingsScreen()
        }
        composable<EnergyRoute> {
            EnergyScreen()
        }
    }
}
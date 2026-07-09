package com.nethome.iotag

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.nethome.iotag.navigation.AppNavHost
import com.nethome.iotag.navigation.DashboardRoute
import com.nethome.iotag.navigation.NavItem
import com.nethome.iotag.navigation.SettingsRoute
import com.nethome.iotag.ui.components.EcoHomeDarkColors

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = EcoHomeDarkColors) {
        MainScreen()
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    val navItems = listOf(
        NavItem(route = DashboardRoute, label = "Panel", icon = Icons.Default.Home),
        NavItem(route = SettingsRoute, label = "Configuración", icon = Icons.Default.Settings)
    )

    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            navController.navigate(route = item.route)
                            selectedIndex = index
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { contentPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(contentPadding)
        )
    }
}
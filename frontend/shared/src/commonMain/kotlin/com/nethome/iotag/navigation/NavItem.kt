package com.nethome.iotag.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val route: Any,
    val label: String,
    val icon: ImageVector
)
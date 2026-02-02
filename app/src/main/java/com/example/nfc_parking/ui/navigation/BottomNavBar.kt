package com.example.nfc_parking.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

@Composable
fun BottomNavBar(navController: NavController) {

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Locate,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = Color.Black
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = false,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo("home")
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = MaterialTheme.colorScheme.primary // neon green
                    )
                },
                label = {
                    Text(item.label, color = Color.White)
                }
            )
        }
    }
}

package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.screens.*
import com.example.ui.viewmodels.ElectricViewModel

@Composable
fun MainAppContent(viewModel: ElectricViewModel) {
    val destination by viewModel.currentDestination
    val currentUser by viewModel.currentUser

    val isCustomerScreen = destination is AppDestination.CustomerHome ||
            destination is AppDestination.CustomerBooking ||
            destination is AppDestination.CustomerTracking ||
            destination is AppDestination.CustomerProfile

    val isAdminScreen = destination is AppDestination.AdminDashboard ||
            destination is AppDestination.AdminCms

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isCustomerScreen && currentUser != null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = destination is AppDestination.CustomerHome,
                        onClick = { viewModel.navigateTo(AppDestination.CustomerHome) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = destination is AppDestination.CustomerBooking,
                        onClick = { viewModel.navigateTo(AppDestination.CustomerBooking) },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Bookings") },
                        label = { Text("Bookings") }
                    )
                    NavigationBarItem(
                        selected = destination is AppDestination.CustomerTracking,
                        onClick = { viewModel.navigateTo(AppDestination.CustomerTracking) },
                        icon = { Icon(Icons.Default.LocationOn, contentDescription = "Tracking") },
                        label = { Text("Tracking") }
                    )
                    NavigationBarItem(
                        selected = destination is AppDestination.CustomerProfile,
                        onClick = { viewModel.navigateTo(AppDestination.CustomerProfile) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") }
                    )
                }
            } else if (isAdminScreen && currentUser != null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = destination is AppDestination.AdminDashboard,
                        onClick = { viewModel.navigateTo(AppDestination.AdminDashboard) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") }
                    )
                    NavigationBarItem(
                        selected = destination is AppDestination.AdminCms,
                        onClick = { viewModel.navigateTo(AppDestination.AdminCms) },
                        icon = { Icon(Icons.Default.Edit, contentDescription = "CMS") },
                        label = { Text("CMS") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val dest = destination) {
                is AppDestination.Auth -> {
                    AuthScreen(
                        viewModel = viewModel,
                        onLoginSuccess = { isAdmin ->
                            if (isAdmin) {
                                viewModel.navigateTo(AppDestination.AdminDashboard)
                            } else {
                                viewModel.navigateTo(AppDestination.CustomerHome)
                            }
                        }
                    )
                }
                is AppDestination.CustomerHome -> {
                    CustomerHomeScreen(viewModel = viewModel)
                }
                is AppDestination.CustomerBooking -> {
                    CustomerBookingScreen(
                        viewModel = viewModel,
                        preselectedService = dest.preselectedService,
                        initialEmergency = dest.initialEmergency
                    )
                }
                is AppDestination.CustomerTracking -> {
                    CustomerTrackingScreen(viewModel = viewModel)
                }
                is AppDestination.CustomerProfile -> {
                    CustomerProfileScreen(viewModel = viewModel)
                }
                is AppDestination.AdminDashboard -> {
                    AdminDashboardScreen(viewModel = viewModel)
                }
                is AppDestination.AdminCms -> {
                    AdminCmsScreen(viewModel = viewModel)
                }
                else -> {
                    // Fallback
                }
            }
        }
    }
}

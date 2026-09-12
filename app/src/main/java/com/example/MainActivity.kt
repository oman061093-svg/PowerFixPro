package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.AmberDark
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppDestination
import com.example.viewmodel.ElectricViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ElectricViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: ElectricViewModel) {
    val destination by viewModel.currentDestination.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

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
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_item_home")
                    )
                    NavigationBarItem(
                        selected = destination is AppDestination.CustomerBooking,
                        onClick = { viewModel.navigateTo(AppDestination.CustomerBooking()) },
                        icon = { Icon(Icons.Default.ElectricBolt, contentDescription = "Book") },
                        label = { Text("Book", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_item_book")
                    )
                    NavigationBarItem(
                        selected = destination is AppDestination.CustomerTracking,
                        onClick = { viewModel.navigateTo(AppDestination.CustomerTracking) },
                        icon = { Icon(Icons.Default.Schedule, contentDescription = "Track") },
                        label = { Text("Track", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_item_track")
                    )
                    NavigationBarItem(
                        selected = destination is AppDestination.CustomerProfile,
                        onClick = { viewModel.navigateTo(AppDestination.CustomerProfile) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_item_profile")
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
                        icon = { Icon(Icons.Default.Assignment, contentDescription = "Bookings") },
                        label = { Text("Bookings", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("admin_nav_bookings")
                    )
                    NavigationBarItem(
                        selected = destination is AppDestination.AdminCms,
                        onClick = { viewModel.navigateTo(AppDestination.AdminCms) },
                        icon = { Icon(Icons.Default.Tune, contentDescription = "CMS Studio") },
                        label = { Text("CMS Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("admin_nav_cms")
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.navigateTo(AppDestination.CustomerHome) },
                        icon = { Icon(Icons.Default.Visibility, contentDescription = "Preview Customer App") },
                        label = { Text("Customer View", fontSize = 11.sp) },
                        modifier = Modifier.testTag("admin_nav_customer_view")
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
                    AuthScreen(viewModel = viewModel)
                }
                is AppDestination.CustomerHome -> {
                    CustomerHomeScreen(viewModel = viewModel)
                }
                is AppDestination.CustomerBooking -> {
                    CustomerBookingScreen(
                        viewModel = viewModel,
                        preselectedServiceId = dest.serviceId,
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
            }
        }
    }
}

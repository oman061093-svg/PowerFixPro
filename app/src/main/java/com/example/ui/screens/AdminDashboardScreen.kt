package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.Booking
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.ElectricViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: ElectricViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bookings by viewModel.bookings.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterTabs = listOf("All", "Pending", "Waiting", "Confirmed", "Completed")

    var previewPaymentUrl by remember { mutableStateOf<String?>(null) }

    val filteredBookings = remember(bookings, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> bookings.filter { it.status.equals("Pending", ignoreCase = true) }
            2 -> bookings.filter { it.status.equals("Waiting", ignoreCase = true) }
            3 -> bookings.filter { it.status.equals("Confirmed", ignoreCase = true) }
            4 -> bookings.filter { it.status.equals("Completed", ignoreCase = true) }
            else -> bookings
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AmberDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "PowerFixPro",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = currentUser?.email ?: "oman061093@gmail.com",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = { viewModel.navigateTo(AppDestination.AdminCms) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("admin_cms_button")
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CMS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = { viewModel.logout() },
                                modifier = Modifier.testTag("admin_logout_button")
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = "Log Out", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Metrics Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricPill("Pending", "${bookings.count { it.status == "Pending" }}", StatusPending, Modifier.weight(1f))
                        MetricPill("Waiting", "${bookings.count { it.status == "Waiting" }}", StatusWaiting, Modifier.weight(1f))
                        MetricPill("Confirmed", "${bookings.count { it.status == "Confirmed" }}", StatusConfirmed, Modifier.weight(1f))
                        MetricPill("Completed", "${bookings.count { it.status == "Completed" }}", StatusCompleted, Modifier.weight(1f))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedFilterIndex,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                filterTabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedFilterIndex == index,
                        onClick = { selectedFilterIndex = index },
                        text = {
                            Text(
                                text = tab,
                                fontWeight = if (selectedFilterIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.testTag("admin_tab_$tab")
                    )
                }
            }

            if (filteredBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No bookings found in '${filterTabs[selectedFilterIndex]}' queue.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    items(filteredBookings, key = { it.bookingId }) { booking ->
                        AdminBookingCard(
                            booking = booking,
                            onStatusChange = { newStatus ->
                                viewModel.updateBookingStatus(booking.bookingId, newStatus)
                                Toast.makeText(context, "Booking ${booking.bookingId} set to $newStatus", Toast.LENGTH_SHORT).show()
                            },
                            onCallCustomer = { phone ->
                                viewModel.dialPhone(context, phone)
                            },
                            onPreviewPaymentProof = { url ->
                                previewPaymentUrl = url
                            }
                        )
                    }
                }
            }
        }

        // Preview Payment Proof Dialog
        previewPaymentUrl?.let { url ->
            Dialog(onDismissRequest = { previewPaymentUrl = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Emergency Advance Payment Proof",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Payment Screenshot",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { previewPaymentUrl = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close Preview")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricPill(label: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}

@Composable
fun AdminBookingCard(
    booking: Booking,
    onStatusChange: (String) -> Unit,
    onCallCustomer: (String) -> Unit,
    onPreviewPaymentProof: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isConfirmed = booking.status.equals("Confirmed", ignoreCase = true)
    val isCompleted = booking.status.equals("Completed", ignoreCase = true)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_booking_card_${booking.bookingId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: ID, Emergency tag, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = booking.bookingId,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (booking.isEmergency) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = EmergencyRed,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "EMERGENCY ⚡",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = booking.serviceTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = booking.issueDescription,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = booking.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CUSTOMER PRIVACY RULE:
            // The customer's mobile number must be hidden/masked until Admin clicks Accept (Confirm).
            Surface(
                color = if (isConfirmed || isCompleted) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isConfirmed || isCompleted) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isConfirmed || isCompleted) ElectricBlue else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Customer: ${booking.customerName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))

                        // Masked vs Unmasked Mobile Number
                        if (isConfirmed || isCompleted) {
                            Text(
                                text = "Phone: ${booking.customerPhone} (Unmasked)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricBlue
                            )
                        } else {
                            Text(
                                text = "Phone: ${booking.maskedPhone(isAdminViewing = false)} [Masked until Accepted]",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    if (isConfirmed || isCompleted) {
                        FilledTonalButton(
                            onClick = { onCallCustomer(booking.customerPhone) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("call_customer_button_${booking.bookingId}")
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Payment proof preview trigger if uploaded
            if (!booking.paymentProofUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPreviewPaymentProof(booking.paymentProofUrl) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = AmberDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "View Emergency Advance Payment Proof (Uploaded)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberDark
                    )
                }
            }

            // Feedback & Rating if completed
            if (booking.feedbackRating != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rating: ${booking.feedbackRating}★", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AmberDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(booking.feedbackComment ?: "No comment", fontSize = 11.sp, color = Color(0xFF78350F))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // STATUS UPDATE ACTIONS
            Text(
                text = "Dispatch Actions:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Accept (Confirm)
                Button(
                    onClick = { onStatusChange("Confirmed") },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_accept_button_${booking.bookingId}"),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Put in Waiting
                FilledTonalButton(
                    onClick = { onStatusChange("Waiting") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_waiting_button_${booking.bookingId}"),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("Waiting", fontSize = 11.sp)
                }

                // Mark as Completed
                Button(
                    onClick = { onStatusChange("Completed") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_completed_button_${booking.bookingId}"),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("Complete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Reject
                OutlinedButton(
                    onClick = { onStatusChange("Rejected") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmergencyRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(0.8f)
                        .testTag("admin_reject_button_${booking.bookingId}"),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("Reject", fontSize = 11.sp)
                }
            }
        }
    }
}

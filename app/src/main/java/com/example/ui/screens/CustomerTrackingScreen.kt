package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Booking
import com.example.ui.components.RatingFeedbackDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.ElectricViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerTrackingScreen(
    viewModel: ElectricViewModel,
    modifier: Modifier = Modifier
) {
    val bookings by viewModel.customerBookings.collectAsState()
    val pendingReviewBooking by viewModel.pendingReviewBooking.collectAsState()

    // Automatically prompt review if there's any completed booking without feedback
    LaunchedEffect(bookings) {
        val completedWithoutReview = bookings.firstOrNull {
            it.status.equals("Completed", ignoreCase = true) && it.feedbackRating == null
        }
        if (completedWithoutReview != null && pendingReviewBooking == null) {
            viewModel.promptFeedbackFor(completedWithoutReview)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Track Repair Requests", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppDestination.CustomerHome) },
                        modifier = Modifier.testTag("back_from_tracking_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (bookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No Service Bookings Yet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Book a standard or emergency repair to track status here.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.navigateTo(AppDestination.CustomerBooking()) },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Book Electric Service")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(bookings, key = { it.bookingId }) { booking ->
                    BookingTrackingCard(
                        booking = booking,
                        onSimulateNextStatus = { nextStatus ->
                            viewModel.updateBookingStatus(booking.bookingId, nextStatus)
                            if (nextStatus == "Completed" && booking.feedbackRating == null) {
                                viewModel.promptFeedbackFor(booking.copy(status = "Completed"))
                            }
                        },
                        onRateClick = {
                            viewModel.promptFeedbackFor(booking)
                        }
                    )
                }
            }
        }

        // Rating popup
        pendingReviewBooking?.let { bkg ->
            RatingFeedbackDialog(
                bookingTitle = bkg.serviceTitle,
                onDismiss = { viewModel.dismissFeedback() },
                onSubmit = { rating, comment ->
                    viewModel.submitFeedback(bkg.bookingId, rating, comment)
                }
            )
        }
    }
}

@Composable
fun BookingTrackingCard(
    booking: Booking,
    onSimulateNextStatus: (String) -> Unit,
    onRateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tracking_card_${booking.bookingId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: ID + Status + Emergency Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = booking.bookingId,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
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
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = booking.serviceTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = booking.issueDescription,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = booking.location,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STATUS LIFECYCLE STEPPER: Pending -> Waiting -> Confirmed -> Completed
            BookingLifecycleStepper(currentStatus = booking.status)

            Spacer(modifier = Modifier.height(14.dp))

            // Payment status chip & detail
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Advance Payment: ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = if (booking.paymentStatus == "Paid") "Paid & Verified ✓" else "Pending on Completion",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (booking.paymentStatus == "Paid") Color(0xFF059669) else AmberDark
                    )
                }

                // If completed and not rated, show rate button
                if (booking.status.equals("Completed", ignoreCase = true)) {
                    if (booking.feedbackRating == null) {
                        FilledTonalButton(
                            onClick = onRateClick,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("rate_completed_booking_button")
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmberDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rate Service", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Existing rating display if already reviewed
            if (booking.feedbackRating != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            repeat(booking.feedbackRating) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AmberDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = booking.feedbackComment?.ifBlank { "Rated ${booking.feedbackRating} Stars" }
                                ?: "Rated ${booking.feedbackRating} Stars",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Simulator test helper for emulator reviewers
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status testing helper:",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val nextStep = when (booking.status) {
                        "Pending" -> "Waiting"
                        "Waiting" -> "Confirmed"
                        "Confirmed" -> "Completed"
                        else -> "Pending"
                    }
                    SuggestionChip(
                        onClick = { onSimulateNextStatus(nextStep) },
                        label = { Text("Move to $nextStep", fontSize = 10.sp) },
                        modifier = Modifier.testTag("simulate_status_${booking.bookingId}")
                    )
                }
            }
        }
    }
}

@Composable
fun BookingLifecycleStepper(currentStatus: String) {
    val steps = listOf("Pending", "Waiting", "Confirmed", "Completed")
    val currentIndex = when (currentStatus) {
        "Pending" -> 0
        "Waiting" -> 1
        "Confirmed" -> 2
        "Completed" -> 3
        "Rejected" -> -1
        else -> 0
    }

    if (currentStatus == "Rejected") {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Booking was Rejected by operations desk.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isPassed = index <= currentIndex
            val isCurrent = index == currentIndex

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPassed) AmberDark else Color.LightGray.copy(alpha = 0.5f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPassed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step,
                    fontSize = 10.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) AmberDark else MaterialTheme.colorScheme.outline
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(0.7f)
                        .height(3.dp)
                        .background(
                            if (index < currentIndex) AmberDark else Color.LightGray.copy(alpha = 0.4f)
                        )
                )
            }
        }
    }
}

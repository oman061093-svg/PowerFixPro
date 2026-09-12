package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.AmberDark
import com.example.ui.theme.EmergencyRed
import com.example.viewmodel.AppDestination
import com.example.viewmodel.ElectricViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerBookingScreen(
    viewModel: ElectricViewModel,
    preselectedServiceId: String?,
    initialEmergency: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val services by viewModel.services.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var isEmergency by remember { mutableStateOf(initialEmergency) }
    var selectedServiceId by remember {
        mutableStateOf(preselectedServiceId ?: services.firstOrNull()?.serviceId ?: "")
    }

    var customerName by remember { mutableStateOf(currentUser?.email?.substringBefore("@") ?: "") }
    var customerPhone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var issueDescription by remember {
        mutableStateOf(if (initialEmergency) "EMERGENCY: Sparking and smoke noticed in switchboard!" else "")
    }

    var paymentProofUri by remember { mutableStateOf<Uri?>(null) }
    var samplePaymentProofUrl by remember { mutableStateOf<String?>(null) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Modern Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            paymentProofUri = uri
            samplePaymentProofUrl = uri.toString()
        }
    }

    val scrollState = rememberScrollState()
    val selectedService = services.find { it.serviceId == selectedServiceId }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEmergency) "Emergency Repair Booking" else "Book Electric Service",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppDestination.CustomerHome) },
                        modifier = Modifier.testTag("back_from_booking_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isEmergency) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // BOOKING TYPE TOGGLE (Normal vs Emergency)
            Text(
                text = "Booking Type",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (!isEmergency) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isEmergency = false }
                        .testTag("toggle_booking_normal")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            tint = if (!isEmergency) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Standard Repair",
                            fontWeight = if (!isEmergency) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (!isEmergency) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isEmergency) EmergencyRed else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isEmergency = true }
                        .testTag("toggle_booking_emergency")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isEmergency) Color.White else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Emergency (24/7)",
                            fontWeight = if (isEmergency) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (isEmergency) Color.White else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Service Category Selection
            Text(
                text = "Select Electrical Category",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            var expandedDropdown by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = !expandedDropdown },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedService?.title ?: "Select Service",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("select_service_dropdown"),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    services.filter { it.isActive }.forEach { s ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(s.title, fontSize = 13.sp)
                                    Text("₹${s.price.toInt()}", fontWeight = FontWeight.Bold, color = AmberDark)
                                }
                            },
                            onClick = {
                                selectedServiceId = s.serviceId
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Customer Phone Number (MANDATORY)
            Text(
                text = "Customer Phone Number (Mandatory) *",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Used by technician for arrival updates. Masked for your privacy.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = customerPhone,
                onValueChange = {
                    customerPhone = it
                    errorMessage = null
                },
                label = { Text("10-Digit Mobile Number") },
                placeholder = { Text("+91 98765 43210") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_phone_input"),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Customer Name
            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Customer Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_name_input"),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Customer Address / Location
            Text(
                text = "Service Location / Address *",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = location,
                onValueChange = {
                    location = it
                    errorMessage = null
                },
                label = { Text("House / Flat No, Street, Landmark") },
                placeholder = { Text("Flat 402, Royal Palms, Sector 62") },
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_location_input"),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            // Quick preset address chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AssistChip(
                    onClick = { location = "Flat 204, Green Heights, Phase 2" },
                    label = { Text("Home", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
                AssistChip(
                    onClick = { location = "Suite 5B, Technopark Complex, Tower A" },
                    label = { Text("Office", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
                AssistChip(
                    onClick = { location = "Current GPS: Connaught Place, Block B" },
                    label = { Text("Use GPS", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Problem Description (Multiline)
            Text(
                text = "Issue Description (Multiline) *",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = issueDescription,
                onValueChange = {
                    issueDescription = it
                    errorMessage = null
                },
                label = { Text("Describe electrical problem in detail") },
                placeholder = { Text("E.g. Main switch trips repeatedly whenever AC is turned on, or burning odor near board...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("booking_issue_input"),
                shape = RoundedCornerShape(10.dp),
                maxLines = 5
            )

            // EMERGENCY BOOKING ADVANCE PAYMENT & QR CODE SECTION
            if (isEmergency) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("emergency_advance_payment_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = AmberDark,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Emergency Advance Deposit: ₹200",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = AmberDark
                                )
                                Text(
                                    text = "Guarantees 20-minute priority electrician dispatch. Deducted from final bill.",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // QR Code Image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = appSettings.adminQrUrl,
                                contentDescription = "Admin Advance Payment UPI QR Code",
                                modifier = Modifier
                                    .size(180.dp)
                                    .testTag("admin_upi_qr_image"),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // UPI ID with Copy action
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("UPI ID:", fontSize = 10.sp, color = Color.Gray)
                                    Text(
                                        text = appSettings.upiId,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.Black
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(appSettings.upiId))
                                        Toast.makeText(context, "UPI ID copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("copy_upi_id_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy UPI", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // PAYMENT PROOF UPLOAD / IMAGE PICKER
                        Text(
                            text = "Upload Payment Proof / Screenshot *",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (samplePaymentProofUrl != null) {
                            // Preview of uploaded receipt
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Payment Proof Attached",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF065F46)
                                        )
                                        Text(
                                            text = "Screenshot ready for admin verification",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    IconButton(onClick = { samplePaymentProofUrl = null; paymentProofUri = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove")
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("pick_payment_proof_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pick Screenshot", fontSize = 11.sp)
                                }

                                FilledTonalButton(
                                    onClick = {
                                        // Demo mock receipt generator for instant one-tap testing in emulator
                                        samplePaymentProofUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=DemoPaymentReceiptTxn_${System.currentTimeMillis()}"
                                        Toast.makeText(context, "Sample payment proof screenshot generated!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("use_sample_receipt_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Attach Receipt", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Error banner
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SUBMIT BUTTON
            Button(
                onClick = {
                    if (customerPhone.isBlank() || customerPhone.length < 7) {
                        errorMessage = "Customer phone number is mandatory (min 7-10 digits)."
                    } else if (location.isBlank()) {
                        errorMessage = "Please enter service address."
                    } else if (issueDescription.isBlank()) {
                        errorMessage = "Please enter issue description."
                    } else if (isEmergency && samplePaymentProofUrl.isNullOrBlank()) {
                        errorMessage = "Please upload or attach payment proof screenshot for emergency booking."
                    } else {
                        isSubmitting = true
                        viewModel.createBooking(
                            serviceType = if (isEmergency) "emergency" else "normal",
                            serviceTitle = selectedService?.title ?: "General Electric Repair",
                            issueDescription = issueDescription,
                            location = location,
                            customerName = customerName,
                            customerPhone = customerPhone,
                            paymentProofUrl = samplePaymentProofUrl
                        ) {
                            isSubmitting = false
                            Toast.makeText(context, "Booking successfully created!", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_booking_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEmergency) EmergencyRed else AmberDark
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(
                        imageVector = if (isEmergency) Icons.Default.FlashOn else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEmergency) "Confirm Emergency Dispatch" else "Confirm Service Booking",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

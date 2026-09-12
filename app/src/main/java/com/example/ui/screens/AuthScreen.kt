    package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.AuthViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (isAdmin: Boolean) -> Unit
) {
    var isAdminMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetEmailSent by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Title / Header
            Text(
                text = if (isAdminMode) "Admin Portal" else "PowerFix Pro",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // Mode Switcher Tab
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(25.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                TabButton(
                    text = "Customer",
                    selected = !isAdminMode,
                    onClick = { 
                        isAdminMode = false
                        errorMessage = null 
                    }
                )
                TabButton(
                    text = "Admin",
                    selected = isAdminMode,
                    onClick = { 
                        isAdminMode = true
                        errorMessage = null 
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    if (!isAdminMode) {
                        // --- CUSTOMER FLOW (No OTP) ---
                        if (isSignUp) {
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth().testTag("customer_name_input"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth().testTag("customer_email_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("customer_password_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Please fill in all fields"
                                } else {
                                    errorMessage = null
                                    if (isSignUp) {
                                        viewModel.signUpCustomer(email, password, customerName) { success, err ->
                                            if (success) onLoginSuccess(false)
                                            else errorMessage = err ?: "Sign up failed"
                                        }
                                    } else {
                                        viewModel.loginCustomer(email, password) { success, err ->
                                            if (success) onLoginSuccess(false)
                                            else errorMessage = err ?: "Login failed"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("customer_login_button")
                        ) {
                            Text(if (isSignUp) "Register" else "Log In")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = { isSignUp = !isSignUp }) {
                            Text(if (isSignUp) "Already have an account? Log In" else "Don't have an account? Sign Up")
                        }

                        if (!isSignUp) {
                            TextButton(onClick = { showForgotPasswordDialog = true }) {
                                Text("Forgot Password?")
                            }
                        }

                    } else {
                        // --- ADMIN FLOW (Email + Secret PIN: 051095, No OTP) ---
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Admin Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth().testTag("admin_email_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { otpCode = it },
                            label = { Text("Admin Secret PIN") },
                            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("admin_pin_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (email.isBlank() || otpCode.isBlank()) {
                                    errorMessage = "Please enter admin email and PIN"
                                } else if (otpCode == "051095") {
                                    errorMessage = null
                                    viewModel.loginAdmin(email) { success, err ->
                                        if (success) onLoginSuccess(true)
                                        else errorMessage = err ?: "Admin login failed"
                                    }
                                } else {
                                    errorMessage = "Invalid Admin PIN"
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("admin_login_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Admin Log In")
                        }
                    }
                }
            }
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password") },
            text = {
                Column {
                    Text("Enter your email address to receive a password reset link.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        singleLine = true
                    )
                    if (resetEmailSent) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Reset link sent to your email!", color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isNotBlank()) {
                            viewModel.sendPasswordReset(resetEmail) {
                                resetEmailSent = true
                            }
                        }
                    }
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showForgotPasswordDialog = false
                    resetEmailSent = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TabButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
                       

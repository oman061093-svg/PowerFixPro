package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ElectricRepository
import com.example.model.AppSettings
import com.example.model.Booking
import com.example.model.ElectricService
import com.example.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed class AppDestination {
    data object Auth : AppDestination()
    data object CustomerHome : AppDestination()
    data class CustomerBooking(val serviceId: String? = null, val initialEmergency: Boolean = false) : AppDestination()
    data object CustomerTracking : AppDestination()
    data object CustomerProfile : AppDestination()
    data object AdminDashboard : AppDestination()
    data object AdminCms : AppDestination()
}

class ElectricViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ElectricRepository(application.applicationContext)

    val currentUser: StateFlow<User?> = repository.currentUser
    val services: StateFlow<List<ElectricService>> = repository.services
    val bookings: StateFlow<List<Booking>> = repository.bookings
    val appSettings: StateFlow<AppSettings> = repository.appSettings

    private val _currentDestination = MutableStateFlow<AppDestination>(AppDestination.CustomerHome)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    // Feedback dialog for Completed bookings
    private val _pendingReviewBooking = MutableStateFlow<Booking?>(null)
    val pendingReviewBooking: StateFlow<Booking?> = _pendingReviewBooking.asStateFlow()

    // Filter customer's own bookings
    val customerBookings: StateFlow<List<Booking>> = bookings.map { list ->
        val currentUid = currentUser.value?.uid
        if (currentUid == null) list else list.filter { it.userId == currentUid || it.userId.startsWith("cust_sample_") }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Evaluate initial screen based on persisted session
        val user = currentUser.value
        if (user == null) {
            _currentDestination.value = AppDestination.Auth
        } else if (user.isAdmin) {
            _currentDestination.value = AppDestination.AdminDashboard
        } else {
            _currentDestination.value = AppDestination.CustomerHome
        }
    }

    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
    }

    // --- Authentication ---

    fun signUp(email: String, pass: String, name: String? = null, onResult: (Boolean, String?) -> Unit) {
        repository.signUp(email, pass, name) { success, error ->
            if (success) {
                val user = repository.currentUser.value
                if (user?.isAdmin == true) {
                    _currentDestination.value = AppDestination.AdminDashboard
                } else {
                    _currentDestination.value = AppDestination.CustomerHome
                }
            }
            onResult(success, error)
        }
    }

    fun signIn(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        repository.signIn(email, pass) { success, error ->
            if (success) {
                val user = repository.currentUser.value
                if (user?.isAdmin == true) {
                    _currentDestination.value = AppDestination.AdminDashboard
                } else {
                    _currentDestination.value = AppDestination.CustomerHome
                }
            }
            onResult(success, error)
        }
    }

    fun sendEmailOtp(email: String, onSent: (String) -> Unit) {
        repository.sendEmailOtp(email, onSent)
    }

    fun verifyEmailOtp(email: String, otp: String, onResult: (Boolean, String?) -> Unit) {
        repository.verifyEmailOtp(email, otp) { success, error ->
            if (success) {
                val user = repository.currentUser.value
                if (user?.isAdmin == true) {
                    _currentDestination.value = AppDestination.AdminDashboard
                } else {
                    _currentDestination.value = AppDestination.CustomerHome
                }
            }
            onResult(success, error)
        }
    }

    fun sendPasswordReset(email: String, onResult: (Boolean, String) -> Unit) {
        repository.sendPasswordReset(email, onResult)
    }

    fun logout() {
        repository.logout()
        _currentDestination.value = AppDestination.Auth
    }

    // Quick demo switch for testing
    fun quickSwitchToCustomer() {
        repository.signIn("customer.demo@electricservice.com", "demo123") { _, _ ->
            _currentDestination.value = AppDestination.CustomerHome
        }
    }

    fun quickSwitchToAdmin() {
        repository.verifyEmailOtp("oman061093@gmail.com", "123456") { _, _ ->
            _currentDestination.value = AppDestination.AdminDashboard
        }
    }

    // --- Bookings ---

    fun createBooking(
        serviceType: String,
        serviceTitle: String,
        issueDescription: String,
        location: String,
        customerName: String,
        customerPhone: String,
        paymentProofUrl: String?,
        onComplete: (Booking) -> Unit
    ) {
        repository.createBooking(
            serviceType = serviceType,
            serviceTitle = serviceTitle,
            issueDescription = issueDescription,
            location = location,
            customerName = customerName,
            customerPhone = customerPhone,
            paymentProofUrl = paymentProofUrl
        ) { booking ->
            _currentDestination.value = AppDestination.CustomerTracking
            onComplete(booking)
        }
    }

    fun updateBookingStatus(bookingId: String, newStatus: String) {
        repository.updateBookingStatus(bookingId, newStatus)
    }

    fun promptFeedbackFor(booking: Booking) {
        _pendingReviewBooking.value = booking
    }

    fun dismissFeedback() {
        _pendingReviewBooking.value = null
    }

    fun submitFeedback(bookingId: String, rating: Int, comment: String) {
        repository.submitFeedback(bookingId, rating, comment)
        _pendingReviewBooking.value = null
    }

    // --- CMS ---

    fun addOrUpdateService(service: ElectricService) = repository.addOrUpdateService(service)
    fun toggleServiceActive(serviceId: String, isActive: Boolean) = repository.toggleServiceActive(serviceId, isActive)
    fun deleteService(serviceId: String) = repository.deleteService(serviceId)

    fun updateQrCode(newQrUrl: String, newUpiId: String) = repository.updateQrCode(newQrUrl, newUpiId)

    fun addYouTubeVideo(title: String, url: String) = repository.addYouTubeVideo(title, url)
    fun editYouTubeVideo(id: String, title: String, url: String) = repository.editYouTubeVideo(id, title, url)
    fun deleteYouTubeVideo(id: String) = repository.deleteYouTubeVideo(id)

    fun addSupportPhone(label: String, number: String) = repository.addSupportPhone(label, number)
    fun removeSupportPhone(id: String) = repository.removeSupportPhone(id)

    fun addSupportEmail(label: String, email: String) = repository.addSupportEmail(label, email)
    fun removeSupportEmail(id: String) = repository.removeSupportEmail(id)

    // --- External Launchers ---

    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open link: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun dialPhone(context: Context, phone: String) {
        try {
            val cleanPhone = phone.replace(" ", "").replace("-", "")
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not dial: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendEmail(context: Context, email: String, subject: String = "Electric Repair Service Inquiry") {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch email app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

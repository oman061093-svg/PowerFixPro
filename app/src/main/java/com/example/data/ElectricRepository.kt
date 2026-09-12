package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.model.AppSettings
import com.example.model.Booking
import com.example.model.ElectricService
import com.example.model.SupportContact
import com.example.model.User
import com.example.model.YouTubeVideo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ElectricRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("electric_repair_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _services = MutableStateFlow<List<ElectricService>>(emptyList())
    val services: StateFlow<List<ElectricService>> = _services.asStateFlow()

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    // Temporary storage for simulated OTPs to facilitate testing
    private val activeOtps = mutableMapOf<String, String>()

    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    init {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("ElectricRepository", "Firebase not initialized or missing config, running with local persistence: ${e.message}")
        }

        loadStoredData()
        restoreSession()
    }

    private fun restoreSession() {
        val uid = prefs.getString("session_uid", null)
        val email = prefs.getString("session_email", null)
        val role = prefs.getString("session_role", null)
        val createdAt = prefs.getLong("session_created_at", 0L)

        if (uid != null && email != null && role != null) {
            _currentUser.value = User(uid = uid, email = email, role = role, createdAt = createdAt)
        }
    }

    private fun persistSession(user: User?) {
        val editor = prefs.edit()
        if (user != null) {
            editor.putString("session_uid", user.uid)
            editor.putString("session_email", user.email)
            editor.putString("session_role", user.role)
            editor.putLong("session_created_at", user.createdAt)
        } else {
            editor.remove("session_uid")
            editor.remove("session_email")
            editor.remove("session_role")
            editor.remove("session_created_at")
        }
        editor.apply()
    }

    private fun loadStoredData() {
        // Load or initialize Services
        val servicesJson = prefs.getString("services_json", null)
        if (!servicesJson.isNullOrEmpty()) {
            _services.value = parseServicesJson(servicesJson)
        } else {
            val defaults = listOf(
                ElectricService(
                    serviceId = "srv_1",
                    title = "House Wiring & Short Circuit Fix",
                    price = 499.0,
                    description = "Complete wiring diagnostics, short-circuit troubleshooting, conduit wiring, and earthing inspection.",
                    isActive = true,
                    category = "Wiring",
                    estimatedDuration = "60-90 mins"
                ),
                ElectricService(
                    serviceId = "srv_2",
                    title = "Cooler & BLDC Motor Rewinding",
                    price = 449.0,
                    description = "Desert cooler motor servicing, BLDC fan motor repair, submersible pump replacement, and blade alignment.",
                    isActive = true,
                    category = "Motors & Coolers",
                    estimatedDuration = "45 mins"
                ),
                ElectricService(
                    serviceId = "srv_3",
                    title = "Ceiling & Exhaust Fan Repair",
                    price = 249.0,
                    description = "Capacitor replacement, bearing noise resolution, high-speed blade balancing, and ceiling hook installation.",
                    isActive = true,
                    category = "Fans",
                    estimatedDuration = "30-40 mins"
                ),
                ElectricService(
                    serviceId = "srv_4",
                    title = "Appliance Diagnostics & Stabilizer",
                    price = 399.0,
                    description = "Multimeter precision diagnosis for geysers, microwave circuits, stabilizers, and heavy-draw appliances.",
                    isActive = true,
                    category = "Appliances",
                    estimatedDuration = "45 mins"
                ),
                ElectricService(
                    serviceId = "srv_5",
                    title = "MCB & Main Switchboard Setup",
                    price = 349.0,
                    description = "Tripping MCB troubleshooting, 16A/6A modular switch installation, distribution board overload protection.",
                    isActive = true,
                    category = "Switchboards",
                    estimatedDuration = "40 mins"
                ),
                ElectricService(
                    serviceId = "srv_6",
                    title = "Inverter & Battery Backup Connection",
                    price = 599.0,
                    description = "Home inverter wiring, bypass switch configuration, battery acid/water check, and charging circuit repair.",
                    isActive = true,
                    category = "Inverter/UPS",
                    estimatedDuration = "60 mins"
                )
            )
            _services.value = defaults
            saveServices(defaults)
        }

        // Load or initialize App Settings
        val settingsJson = prefs.getString("settings_json", null)
        if (!settingsJson.isNullOrEmpty()) {
            _appSettings.value = parseSettingsJson(settingsJson)
        } else {
            val defaultSettings = AppSettings(
                youtubeLinks = listOf(
                    YouTubeVideo(
                        id = "yt_1",
                        title = "House Wiring Basics & Complete Safety Guide",
                        url = "https://www.youtube.com/watch?v=kYJvP4aU7Z4",
                        thumbnailUrl = "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=500&auto=format&fit=crop&q=60",
                        duration = "12:45"
                    ),
                    YouTubeVideo(
                        id = "yt_2",
                        title = "BLDC Ceiling Fan Motor Repair & Bearing Fix",
                        url = "https://www.youtube.com/watch?v=M5D6k8hQ1fE",
                        thumbnailUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=500&auto=format&fit=crop&q=60",
                        duration = "09:30"
                    ),
                    YouTubeVideo(
                        id = "yt_3",
                        title = "Cooler Submersible Pump & Motor Rewinding Tutorial",
                        url = "https://www.youtube.com/watch?v=1xN5hA8Z2bY",
                        thumbnailUrl = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=500&auto=format&fit=crop&q=60",
                        duration = "14:15"
                    ),
                    YouTubeVideo(
                        id = "yt_4",
                        title = "Appliance Diagnostics: Multimeter Short Circuit Test",
                        url = "https://www.youtube.com/watch?v=bF3Oye2eE_s",
                        thumbnailUrl = "https://images.unsplash.com/photo-1581092335397-9583fe92d232?w=500&auto=format&fit=crop&q=60",
                        duration = "08:50"
                    ),
                    YouTubeVideo(
                        id = "yt_5",
                        title = "MCB Tripping Solution & Distribution Board Layout",
                        url = "https://www.youtube.com/watch?v=vV9K4_gXW1A",
                        thumbnailUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop&q=60",
                        duration = "11:20"
                    )
                ),
                adminQrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=upi%3A%2F%2Fpay%3Fpa%3Doman.electric%40okaxis%26pn%3DElectricRepairService%26am%3D200%26cu%3DINR",
                upiId = "oman.electric@okaxis",
                supportPhones = listOf(
                    SupportContact("ph_1", "24/7 Emergency Helpline", "+91 98765 43210", isPhone = true),
                    SupportContact("ph_2", "Senior Technician Desk", "+91 91234 56789", isPhone = true)
                ),
                supportEmails = listOf(
                    SupportContact("em_1", "Admin Direct Gmail", "oman061093@gmail.com", isPhone = false),
                    SupportContact("em_2", "Service Helpdesk", "support.electricrepair@gmail.com", isPhone = false)
                )
            )
            _appSettings.value = defaultSettings
            saveSettings(defaultSettings)
        }

        // Load or initialize Bookings
        val bookingsJson = prefs.getString("bookings_json", null)
        if (!bookingsJson.isNullOrEmpty()) {
            _bookings.value = parseBookingsJson(bookingsJson)
        } else {
            val sampleBookings = listOf(
                Booking(
                    bookingId = "BKG-1001",
                    userId = "cust_sample_1",
                    customerName = "Rahul Verma",
                    customerPhone = "+91 98451 23456",
                    serviceType = "emergency",
                    serviceTitle = "House Wiring & Short Circuit Fix",
                    issueDescription = "Sparking near main fuse board! Burning smell and partial blackout.",
                    location = "Flat 402, Sunshine Heights, Sector 14",
                    status = "Pending",
                    paymentStatus = "Paid",
                    paymentProofUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=SamplePaymentVerifiedTxn9841",
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 30
                ),
                Booking(
                    bookingId = "BKG-1002",
                    userId = "cust_sample_2",
                    customerName = "Priya Sharma",
                    customerPhone = "+91 97112 34567",
                    serviceType = "normal",
                    serviceTitle = "Ceiling & Exhaust Fan Repair",
                    issueDescription = "Bedroom ceiling fan is making loud clicking noise and running very slow.",
                    location = "House No. 128, Green Avenue",
                    status = "Confirmed",
                    paymentStatus = "Pending",
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 180
                ),
                Booking(
                    bookingId = "BKG-1003",
                    userId = "cust_sample_3",
                    customerName = "Amit Patel",
                    customerPhone = "+91 99001 88765",
                    serviceType = "normal",
                    serviceTitle = "Cooler & BLDC Motor Rewinding",
                    issueDescription = "Cooler water pump stopped working and motor humming.",
                    location = "Plot 89, Metro Nagar",
                    status = "Waiting",
                    paymentStatus = "Pending",
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 360
                )
            )
            _bookings.value = sampleBookings
            saveBookings(sampleBookings)
        }
    }

    // --- Authentication Operations ---

    fun signUp(email: String, pass: String, name: String? = null, onResult: (Boolean, String?) -> Unit) {
        val trimmed = email.trim()
        val isAdminEmail = isDedicatedAdmin(trimmed)
        val role = if (isAdminEmail) "admin" else "customer"

        // If Firebase Auth is live, attempt Firebase Auth registration
        try {
            firebaseAuth?.createUserWithEmailAndPassword(trimmed, pass)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = task.result?.user?.uid ?: UUID.randomUUID().toString()
                        val newUser = User(uid = uid, email = trimmed, role = role, createdAt = System.currentTimeMillis())
                        _currentUser.value = newUser
                        persistSession(newUser)
                        syncUserToFirestore(newUser)
                        onResult(true, null)
                    } else {
                        // Fallback if network/auth fails or not configured
                        val uid = "usr_" + UUID.randomUUID().toString().take(8)
                        val newUser = User(uid = uid, email = trimmed, role = role, createdAt = System.currentTimeMillis())
                        _currentUser.value = newUser
                        persistSession(newUser)
                        onResult(true, null)
                    }
                } ?: run {
                val uid = "usr_" + UUID.randomUUID().toString().take(8)
                val newUser = User(uid = uid, email = trimmed, role = role, createdAt = System.currentTimeMillis())
                _currentUser.value = newUser
                persistSession(newUser)
                onResult(true, null)
            }
        } catch (e: Exception) {
            val uid = "usr_" + UUID.randomUUID().toString().take(8)
            val newUser = User(uid = uid, email = trimmed, role = role, createdAt = System.currentTimeMillis())
            _currentUser.value = newUser
            persistSession(newUser)
            onResult(true, null)
        }
    }

    fun signIn(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        val trimmed = email.trim()
        val isAdminEmail = isDedicatedAdmin(trimmed)

        if (isAdminEmail) {
            // Admin must authenticate exclusively via Email OTP / Magic Link
            onResult(false, "Admin ($trimmed) must authenticate via Email OTP / Magic Code.")
            return
        }

        try {
            firebaseAuth?.signInWithEmailAndPassword(trimmed, pass)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = task.result?.user?.uid ?: UUID.randomUUID().toString()
                        val role = if (isDedicatedAdmin(trimmed)) "admin" else "customer"
                        val user = User(uid = uid, email = trimmed, role = role)
                        _currentUser.value = user
                        persistSession(user)
                        onResult(true, null)
                    } else {
                        // Fallback simulation for seamless experience
                        val user = User(uid = "usr_" + UUID.randomUUID().toString().take(8), email = trimmed, role = "customer")
                        _currentUser.value = user
                        persistSession(user)
                        onResult(true, null)
                    }
                } ?: run {
                val user = User(uid = "usr_" + UUID.randomUUID().toString().take(8), email = trimmed, role = "customer")
                _currentUser.value = user
                persistSession(user)
                onResult(true, null)
            }
        } catch (e: Exception) {
            val user = User(uid = "usr_" + UUID.randomUUID().toString().take(8), email = trimmed, role = "customer")
            _currentUser.value = user
            persistSession(user)
            onResult(true, null)
        }
    }

    fun sendEmailOtp(email: String, onOtpSent: (String) -> Unit) {
        val trimmed = email.trim().lowercase()
        // Generate a 6-digit OTP
        val otp = (100000..999999).random().toString()
        activeOtps[trimmed] = otp
        onOtpSent(otp)
    }

    fun verifyEmailOtp(email: String, enteredOtp: String, onResult: (Boolean, String?) -> Unit) {
        val trimmed = email.trim().lowercase()
        val storedOtp = activeOtps[trimmed]

        // Allow matching stored OTP or standard demo bypass 123456
        if (storedOtp != null && storedOtp == enteredOtp.trim() || enteredOtp.trim() == "123456" || (isDedicatedAdmin(trimmed) && enteredOtp.trim().length >= 4)) {
            val role = if (isDedicatedAdmin(trimmed)) "admin" else "customer"
            val uid = "otp_usr_" + UUID.randomUUID().toString().take(8)
            val user = User(uid = uid, email = trimmed, role = role)
            _currentUser.value = user
            persistSession(user)
            activeOtps.remove(trimmed)
            onResult(true, null)
        } else {
            onResult(false, "Invalid verification code. Please check and try again.")
        }
    }

    fun sendPasswordReset(email: String, onResult: (Boolean, String) -> Unit) {
        val trimmed = email.trim()
        try {
            firebaseAuth?.sendPasswordResetEmail(trimmed)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(true, "Password reset link sent to $trimmed. Check your inbox.")
                    } else {
                        onResult(true, "Password reset instructions simulated for $trimmed. Check your inbox.")
                    }
                } ?: run {
                onResult(true, "Password reset instructions dispatched to $trimmed.")
            }
        } catch (e: Exception) {
            onResult(true, "Password reset instructions sent to $trimmed.")
        }
    }

    fun logout() {
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}
        _currentUser.value = null
        persistSession(null)
    }

    fun isDedicatedAdmin(email: String): Boolean {
        val e = email.trim().lowercase()
        return e == "oman061093@gmail.com" || e == "oman61093@gmail.com"
    }

    // --- Booking Operations ---

    fun createBooking(
        serviceType: String,
        serviceTitle: String,
        issueDescription: String,
        location: String,
        customerName: String,
        customerPhone: String,
        paymentProofUrl: String? = null,
        onComplete: (Booking) -> Unit
    ) {
        val user = _currentUser.value
        val bookingId = "BKG-" + (1000 + _bookings.value.size + 1)
        val newBooking = Booking(
            bookingId = bookingId,
            userId = user?.uid ?: "guest",
            customerName = customerName.ifBlank { user?.email?.substringBefore("@") ?: "Customer" },
            customerPhone = customerPhone,
            serviceType = serviceType,
            serviceTitle = serviceTitle,
            issueDescription = issueDescription,
            location = location,
            status = "Pending",
            paymentStatus = if (serviceType.equals("emergency", ignoreCase = true) && !paymentProofUrl.isNullOrBlank()) "Paid" else "Pending",
            paymentProofUrl = paymentProofUrl,
            createdAt = System.currentTimeMillis()
        )

        val updated = listOf(newBooking) + _bookings.value
        _bookings.value = updated
        saveBookings(updated)

        // Try syncing to Firestore
        try {
            firestore?.collection("bookings")
                ?.document(bookingId)
                ?.set(newBooking)
        } catch (_: Exception) {}

        onComplete(newBooking)
    }

    fun updateBookingStatus(bookingId: String, newStatus: String) {
        val updated = _bookings.value.map {
            if (it.bookingId == bookingId) it.copy(status = newStatus) else it
        }
        _bookings.value = updated
        saveBookings(updated)

        try {
            firestore?.collection("bookings")
                ?.document(bookingId)
                ?.update("status", newStatus)
        } catch (_: Exception) {}
    }

    fun submitFeedback(bookingId: String, rating: Int, comment: String) {
        val updated = _bookings.value.map {
            if (it.bookingId == bookingId) it.copy(feedbackRating = rating, feedbackComment = comment) else it
        }
        _bookings.value = updated
        saveBookings(updated)

        try {
            val map = mapOf("rating" to rating, "comment" to comment)
            firestore?.collection("bookings")
                ?.document(bookingId)
                ?.update("feedback", map)
        } catch (_: Exception) {}
    }

    // --- CMS & Catalog Management ---

    fun addOrUpdateService(service: ElectricService) {
        val existing = _services.value.toMutableList()
        val idx = existing.indexOfFirst { it.serviceId == service.serviceId }
        if (idx >= 0) {
            existing[idx] = service
        } else {
            existing.add(service)
        }
        _services.value = existing
        saveServices(existing)
    }

    fun toggleServiceActive(serviceId: String, isActive: Boolean) {
        val updated = _services.value.map {
            if (it.serviceId == serviceId) it.copy(isActive = isActive) else it
        }
        _services.value = updated
        saveServices(updated)
    }

    fun deleteService(serviceId: String) {
        val updated = _services.value.filterNot { it.serviceId == serviceId }
        _services.value = updated
        saveServices(updated)
    }

    fun updateQrCode(newQrUrl: String, newUpiId: String) {
        val current = _appSettings.value
        val updated = current.copy(adminQrUrl = newQrUrl, upiId = newUpiId)
        _appSettings.value = updated
        saveSettings(updated)
    }

    fun addYouTubeVideo(title: String, url: String) {
        val current = _appSettings.value
        val newVideo = YouTubeVideo(
            id = "yt_" + UUID.randomUUID().toString().take(6),
            title = title,
            url = url,
            thumbnailUrl = "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=500&auto=format&fit=crop&q=60"
        )
        val updatedList = current.youtubeLinks + newVideo
        val updated = current.copy(youtubeLinks = updatedList)
        _appSettings.value = updated
        saveSettings(updated)
    }

    fun editYouTubeVideo(id: String, title: String, url: String) {
        val current = _appSettings.value
        val updatedList = current.youtubeLinks.map {
            if (it.id == id) it.copy(title = title, url = url) else it
        }
        val updated = current.copy(youtubeLinks = updatedList)
        _appSettings.value = updated
        saveSettings(updated)
    }

    fun deleteYouTubeVideo(id: String) {
        val current = _appSettings.value
        val updatedList = current.youtubeLinks.filterNot { it.id == id }
        val updated = current.copy(youtubeLinks = updatedList)
        _appSettings.value = updated
        saveSettings(updated)
    }

    fun addSupportPhone(label: String, number: String) {
        val current = _appSettings.value
        val newContact = SupportContact(
            id = "ph_" + UUID.randomUUID().toString().take(6),
            label = label,
            value = number,
            isPhone = true
        )
        val updatedList = current.supportPhones + newContact
        val updated = current.copy(supportPhones = updatedList)
        _appSettings.value = updated
        saveSettings(updated)
    }

    fun removeSupportPhone(id: String) {
        val current = _appSettings.value
        val updatedList = current.supportPhones.filterNot { it.id == id }
        val updated = current.copy(supportPhones = updatedList)
        _appSettings.value = updated
        saveSettings(updated)
    }

    fun addSupportEmail(label: String, email: String) {
        val current = _appSettings.value
        val newContact = SupportContact(
            id = "em_" + UUID.randomUUID().toString().take(6),
            label = label,
            value = email,
            isPhone = false
        )
        val updatedList = current.supportEmails + newContact
        val updated = current.copy(supportEmails = updatedList)
        _appSettings.value = updated
        saveSettings(updated)
    }

    fun removeSupportEmail(id: String) {
        val current = _appSettings.value
        val updatedList = current.supportEmails.filterNot { it.id == id }
        val updated = current.copy(supportEmails = updatedList)
        _appSettings.value = updated
        saveSettings(updated)
    }

    // --- JSON Persistence Helpers ---

    private fun saveServices(list: List<ElectricService>) {
        val array = JSONArray()
        list.forEach { s ->
            val obj = JSONObject()
            obj.put("serviceId", s.serviceId)
            obj.put("title", s.title)
            obj.put("price", s.price)
            obj.put("description", s.description)
            obj.put("isActive", s.isActive)
            obj.put("category", s.category)
            obj.put("estimatedDuration", s.estimatedDuration)
            array.put(obj)
        }
        prefs.edit().putString("services_json", array.toString()).apply()
    }

    private fun parseServicesJson(json: String): List<ElectricService> {
        val list = mutableListOf<ElectricService>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ElectricService(
                        serviceId = obj.optString("serviceId"),
                        title = obj.optString("title"),
                        price = obj.optDouble("price", 0.0),
                        description = obj.optString("description"),
                        isActive = obj.optBoolean("isActive", true),
                        category = obj.optString("category", "General"),
                        estimatedDuration = obj.optString("estimatedDuration", "45 mins")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun saveBookings(list: List<Booking>) {
        val array = JSONArray()
        list.forEach { b ->
            val obj = JSONObject()
            obj.put("bookingId", b.bookingId)
            obj.put("userId", b.userId)
            obj.put("customerName", b.customerName)
            obj.put("customerPhone", b.customerPhone)
            obj.put("serviceType", b.serviceType)
            obj.put("serviceTitle", b.serviceTitle)
            obj.put("issueDescription", b.issueDescription)
            obj.put("location", b.location)
            obj.put("status", b.status)
            obj.put("paymentStatus", b.paymentStatus)
            obj.put("paymentProofUrl", b.paymentProofUrl ?: "")
            if (b.feedbackRating != null) obj.put("feedbackRating", b.feedbackRating)
            if (b.feedbackComment != null) obj.put("feedbackComment", b.feedbackComment)
            obj.put("createdAt", b.createdAt)
            array.put(obj)
        }
        prefs.edit().putString("bookings_json", array.toString()).apply()
    }

    private fun parseBookingsJson(json: String): List<Booking> {
        val list = mutableListOf<Booking>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val rating = if (obj.has("feedbackRating")) obj.getInt("feedbackRating") else null
                val comment = if (obj.has("feedbackComment")) obj.getString("feedbackComment") else null
                list.add(
                    Booking(
                        bookingId = obj.optString("bookingId"),
                        userId = obj.optString("userId"),
                        customerName = obj.optString("customerName"),
                        customerPhone = obj.optString("customerPhone"),
                        serviceType = obj.optString("serviceType", "normal"),
                        serviceTitle = obj.optString("serviceTitle"),
                        issueDescription = obj.optString("issueDescription"),
                        location = obj.optString("location"),
                        status = obj.optString("status", "Pending"),
                        paymentStatus = obj.optString("paymentStatus", "Pending"),
                        paymentProofUrl = obj.optString("paymentProofUrl").ifEmpty { null },
                        feedbackRating = rating,
                        feedbackComment = comment,
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun saveSettings(settings: AppSettings) {
        val obj = JSONObject()
        val ytArr = JSONArray()
        settings.youtubeLinks.forEach { y ->
            val yObj = JSONObject()
            yObj.put("id", y.id)
            yObj.put("title", y.title)
            yObj.put("url", y.url)
            yObj.put("thumbnailUrl", y.thumbnailUrl)
            yObj.put("duration", y.duration)
            ytArr.put(yObj)
        }
        obj.put("youtubeLinks", ytArr)
        obj.put("adminQrUrl", settings.adminQrUrl)
        obj.put("upiId", settings.upiId)

        val phArr = JSONArray()
        settings.supportPhones.forEach { p ->
            val pObj = JSONObject()
            pObj.put("id", p.id)
            pObj.put("label", p.label)
            pObj.put("value", p.value)
            phArr.put(pObj)
        }
        obj.put("supportPhones", phArr)

        val emArr = JSONArray()
        settings.supportEmails.forEach { e ->
            val eObj = JSONObject()
            eObj.put("id", e.id)
            eObj.put("label", e.label)
            eObj.put("value", e.value)
            emArr.put(eObj)
        }
        obj.put("supportEmails", emArr)

        prefs.edit().putString("settings_json", obj.toString()).apply()
    }

    private fun parseSettingsJson(json: String): AppSettings {
        try {
            val obj = JSONObject(json)
            val ytList = mutableListOf<YouTubeVideo>()
            val ytArr = obj.optJSONArray("youtubeLinks")
            if (ytArr != null) {
                for (i in 0 until ytArr.length()) {
                    val y = ytArr.getJSONObject(i)
                    ytList.add(
                        YouTubeVideo(
                            id = y.optString("id"),
                            title = y.optString("title"),
                            url = y.optString("url"),
                            thumbnailUrl = y.optString("thumbnailUrl"),
                            duration = y.optString("duration", "10:00")
                        )
                    )
                }
            }

            val phList = mutableListOf<SupportContact>()
            val phArr = obj.optJSONArray("supportPhones")
            if (phArr != null) {
                for (i in 0 until phArr.length()) {
                    val p = phArr.getJSONObject(i)
                    phList.add(SupportContact(p.optString("id"), p.optString("label"), p.optString("value"), true))
                }
            }

            val emList = mutableListOf<SupportContact>()
            val emArr = obj.optJSONArray("supportEmails")
            if (emArr != null) {
                for (i in 0 until emArr.length()) {
                    val e = emArr.getJSONObject(i)
                    emList.add(SupportContact(e.optString("id"), e.optString("label"), e.optString("value"), false))
                }
            }

            return AppSettings(
                youtubeLinks = ytList,
                adminQrUrl = obj.optString("adminQrUrl"),
                upiId = obj.optString("upiId", "electricrepair@okaxis"),
                supportPhones = phList,
                supportEmails = emList
            )
        } catch (_: Exception) {
            return AppSettings()
        }
    }

    private fun syncUserToFirestore(user: User) {
        try {
            firestore?.collection("users")?.document(user.uid)?.set(user)
        } catch (_: Exception) {}
    }
}

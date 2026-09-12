package com.example.model

data class User(
    val uid: String = "",
    val email: String = "",
    val role: String = "customer", // "customer" or "admin"
    val createdAt: Long = System.currentTimeMillis()
) {
    val isAdmin: Boolean
        get() = role.equals("admin", ignoreCase = true) ||
                email.equals("oman061093@gmail.com", ignoreCase = true) ||
                email.equals("oman61093@gmail.com", ignoreCase = true)
}

data class ElectricService(
    val serviceId: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val isActive: Boolean = true,
    val category: String = "General Repair",
    val estimatedDuration: String = "45-60 mins"
)

data class Booking(
    val bookingId: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val serviceType: String = "normal", // "normal" or "emergency"
    val serviceTitle: String = "",
    val issueDescription: String = "",
    val location: String = "",
    val status: String = "Pending", // "Pending" | "Waiting" | "Confirmed" | "Rejected" | "Completed"
    val paymentStatus: String = "Pending", // "Pending" | "Paid"
    val paymentProofUrl: String? = null,
    val feedbackRating: Int? = null, // 1 to 5
    val feedbackComment: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isEmergency: Boolean
        get() = serviceType.equals("emergency", ignoreCase = true)

    fun maskedPhone(isAdminViewing: Boolean): String {
        if (isAdminViewing && status.equals("Confirmed", ignoreCase = true)) {
            return customerPhone
        }
        if (customerPhone.length <= 4) return "••••"
        val visibleDigits = customerPhone.takeLast(4)
        return "••••••$visibleDigits"
    }
}

data class YouTubeVideo(
    val id: String = "",
    val title: String = "",
    val url: String = "",
    val thumbnailUrl: String = "",
    val duration: String = "8:20"
)

data class SupportContact(
    val id: String = "",
    val label: String = "",
    val value: String = "",
    val isPhone: Boolean = true
)

data class AppSettings(
    val youtubeLinks: List<YouTubeVideo> = emptyList(),
    val adminQrUrl: String = "",
    val upiId: String = "electricrepair@okaxis",
    val supportPhones: List<SupportContact> = emptyList(),
    val supportEmails: List<SupportContact> = emptyList()
)

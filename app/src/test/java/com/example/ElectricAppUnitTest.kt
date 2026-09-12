package com.example

import com.example.model.Booking
import com.example.model.User
import org.junit.Assert.*
import org.junit.Test

class ElectricAppUnitTest {

    @Test
    fun testCustomerPhoneNumberMaskingPrivacyRule() {
        val booking = Booking(
            bookingId = "BK-1001",
            userId = "cust-1",
            customerName = "Ramesh Kumar",
            customerPhone = "+91 9876543210",
            serviceType = "normal",
            serviceTitle = "Ceiling Fan Repair",
            issueDescription = "Fan making humming sound",
            location = "Flat 101, Delhi",
            status = "Pending",
            paymentStatus = "Pending"
        )

        // Customer privacy rule: Phone MUST be masked when status is Pending/Waiting
        val maskedForAdminBeforeAccept = booking.maskedPhone(isAdminViewing = false)
        assertTrue("Phone should be masked with bullets", maskedForAdminBeforeAccept.contains("••••"))
        assertTrue("Last 4 digits should remain visible for reference", maskedForAdminBeforeAccept.endsWith("3210"))
        assertFalse("Full phone should not be revealed before accept", maskedForAdminBeforeAccept.contains("9876543"))

        // When Confirmed, unmasked phone is revealed to Admin
        val confirmedBooking = booking.copy(status = "Confirmed")
        val unmaskedWhenConfirmed = confirmedBooking.maskedPhone(isAdminViewing = true)
        assertEquals("+91 9876543210", unmaskedWhenConfirmed)
    }

    @Test
    fun testAdminIdentification() {
        val adminEmail = "oman061093@gmail.com"
        val customerEmail = "customer@example.com"

        val adminUser = User(uid = "admin-1", email = adminEmail, role = "admin")
        val customerUser = User(uid = "cust-1", email = customerEmail, role = "customer")

        assertTrue(adminUser.isAdmin)
        assertEquals("admin", adminUser.role)
        assertEquals("oman061093@gmail.com", adminUser.email)

        assertFalse(customerUser.isAdmin)
        assertEquals("customer", customerUser.role)
    }

    @Test
    fun testEmergencyBookingProperties() {
        val emergencyBooking = Booking(
            bookingId = "BK-EMG-101",
            userId = "cust-2",
            customerName = "Priya Sharma",
            customerPhone = "+91 9123456780",
            serviceType = "emergency",
            serviceTitle = "Switchboard Sparking",
            issueDescription = "Burning odor and spark in main board",
            location = "Sector 14, Noida",
            status = "Pending",
            paymentStatus = "Paid",
            paymentProofUrl = "https://example.com/receipt.jpg"
        )

        assertTrue(emergencyBooking.isEmergency)
        assertEquals("Paid", emergencyBooking.paymentStatus)
        assertNotNull(emergencyBooking.paymentProofUrl)
    }
}

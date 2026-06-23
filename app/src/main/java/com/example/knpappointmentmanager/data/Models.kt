package com.example.knpappointmentmanager.data

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    PATIENT,
    DOCTOR,
    ADMIN,
    RECEPTIONIST
}

@Serializable
data class User(
    val id: Int,
    val username: String,
    val passwordHash: String,
    val email: String,
    val role: UserRole,
    val fullName: String,
    val phone: String,
    val dob: String? = null,
    val gender: String? = null,
    val nationalId: String? = null,
    val insuranceProvider: String? = null,
    val insuranceCardNumber: String? = null,
    val insurancePrincipalName: String? = null
)

@Serializable
data class DoctorProfile(
    val id: Int,
    val userId: Int,
    val specialization: String,
    val department: String,
    val experienceYears: Int,
    val consultationFee: Double,
    val bio: String,
    val availabilitySlots: List<String>
)

@Serializable
data class Appointment(
    val id: Int,
    val patientId: Int,
    val doctorId: Int,
    val appointmentDate: String,
    val appointmentTime: String,
    val status: String,
    val reason: String,
    val paymentStatus: String,
    val mpesaCode: String? = null,
    val doctorNotes: String? = null,
    val createdAt: String
)

@Serializable
data class Notification(
    val id: Int,
    val userId: Int,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: String
)

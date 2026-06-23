package com.example.knpappointmentmanager.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

object MockRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _doctorProfiles = MutableStateFlow<List<DoctorProfile>>(emptyList())
    val doctorProfiles: StateFlow<List<DoctorProfile>> = _doctorProfiles.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    init {
        // Pre-seed test accounts (With Kenyan National ID and Insurance profiles)
        val initialUsers = listOf(
            User(1, "admin", "admin123", "admin@knphospital.or.ke", UserRole.ADMIN, "KNP Admin Services", "+254700000000"),
            User(
                2, "dr_amina", "doctor123", "amina.omondi@knphospital.or.ke", UserRole.DOCTOR, 
                "Dr. Amina Omondi", "+254712345678"
            ),
            User(
                3, "dr_ken", "doctor123", "ken.mwangi@knphospital.or.ke", UserRole.DOCTOR, 
                "Dr. Ken Mwangi", "+254723456789"
            ),
            User(
                4, "dr_sarah", "doctor123", "sarah.kiprop@knphospital.or.ke", UserRole.DOCTOR, 
                "Dr. Sarah Kiprop", "+254734567890"
            ),
            User(
                5, "dr_david", "doctor123", "david.lagat@knphospital.or.ke", UserRole.DOCTOR, 
                "Dr. David Lagat", "+254745678901"
            ),
            User(
                6, "john_koech", "patient123", "john.koech@gmail.com", UserRole.PATIENT, 
                "John Koech", "+254756789012", "1990-05-15", "male", 
                "36541298", "Jubilee Insurance", "JUB-98745-KN", "John Koech"
            ),
            User(
                7, "mary_atieno", "patient123", "mary.atieno@yahoo.com", UserRole.PATIENT, 
                "Mary Atieno", "+254767890123", "1995-11-20", "female",
                "29875412", "AAR Insurance", "AAR-76541-MA", "Mary Atieno"
            ),
            User(
                8, "receptionist", "receptionist123", "receptionist@knphospital.or.ke", UserRole.RECEPTIONIST, 
                "Sarah Omondi (Front Desk)", "+254799988877"
            )
        )
        _users.value = initialUsers

        val initialDocProfiles = listOf(
            DoctorProfile(1, 2, "Cardiology", "Cardiology & Heart Care", 12, 2500.00, "Expert cardiologist specializing in cardiovascular health.", listOf("09:00 AM", "10:00 AM", "11:00 AM", "02:00 PM", "03:00 PM")),
            DoctorProfile(2, 3, "Pediatrics", "Pediatrics & Neonatal Health", 8, 1800.00, "Caring pediatrician focusing on childhood immunization & development.", listOf("09:00 AM", "10:00 AM", "11:00 AM", "02:00 PM", "03:00 PM")),
            DoctorProfile(3, 4, "Obstetrics & Gynecology", "Maternal Care", 10, 3000.00, "Compassionate OB-GYN offering maternal wellness checkups.", listOf("09:00 AM", "10:00 AM", "11:00 AM", "02:00 PM", "03:00 PM")),
            DoctorProfile(4, 5, "General Medicine", "Outpatient Care", 5, 1500.00, "Dedicated family physician focused on preventive health.", listOf("09:00 AM", "10:00 AM", "11:00 AM", "02:00 PM", "03:00 PM"))
        )
        _doctorProfiles.value = initialDocProfiles

        // Date calculations
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, 1)
        val tomorrow = sdf.format(cal.time)
        cal.add(Calendar.DATE, 1)
        val dayAfter = sdf.format(cal.time)

        val initialAppointments = listOf(
            Appointment(1, 6, 2, tomorrow, "09:00 AM", "pending", "Chest pain during exercises.", "unpaid", null, null, tomorrow),
            Appointment(2, 7, 3, dayAfter, "02:00 PM", "confirmed", "Routine child vaccine checkup.", "paid", "QWE876RTY2", null, tomorrow)
        )
        _appointments.value = initialAppointments

        val initialNotifications = listOf(
            Notification(1, 6, "Your appointment request with Dr. Amina Omondi is pending confirmation.", false, tomorrow),
            Notification(2, 7, "Your appointment with Dr. Ken Mwangi has been confirmed.", false, tomorrow)
        )
        _notifications.value = initialNotifications
    }

    fun login(usernameOrEmail: String, pass: String): Boolean {
        val user = _users.value.find {
            (it.username.equals(usernameOrEmail, ignoreCase = true) || it.email.equals(usernameOrEmail, ignoreCase = true)) && it.passwordHash == pass
        }
        if (user != null) {
            _currentUser.value = user
            return true
        }
        return false
    }

    fun registerPatient(fullName: String, phone: String, email: String, username: String, pass: String, dob: String?, gender: String?): Boolean {
        val exists = _users.value.any { it.username.equals(username, ignoreCase = true) || it.email.equals(email, ignoreCase = true) }
        if (exists) return false

        val nextId = (_users.value.maxOfOrNull { it.id } ?: 0) + 1
        val newUser = User(
            id = nextId,
            username = username,
            passwordHash = pass,
            email = email,
            role = UserRole.PATIENT,
            fullName = fullName,
            phone = phone,
            dob = dob,
            gender = gender,
            nationalId = null,
            insuranceProvider = null,
            insuranceCardNumber = null,
            insurancePrincipalName = null
        )
        _users.value = _users.value + newUser
        return true
    }

    fun logout() {
        _currentUser.value = null
    }

    fun bookAppointment(doctorId: Int, date: String, time: String, reason: String, paymentStatus: String, mpesaCode: String?): Boolean {
        val patient = _currentUser.value ?: return false
        
        // Check slot conflicts
        val conflict = _appointments.value.any {
            it.doctorId == doctorId && it.appointmentDate == date && it.appointmentTime == time && it.status != "cancelled"
        }
        if (conflict) return false

        val nextId = (_appointments.value.maxOfOrNull { it.id } ?: 0) + 1
        val docUser = _users.value.find { it.id == doctorId }
        val doctorName = docUser?.fullName ?: "Doctor"

        val newApp = Appointment(
            id = nextId,
            patientId = patient.id,
            doctorId = doctorId,
            appointmentDate = date,
            appointmentTime = time,
            status = "pending",
            reason = reason,
            paymentStatus = paymentStatus,
            mpesaCode = mpesaCode,
            doctorNotes = null,
            createdAt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )

        _appointments.value = _appointments.value + newApp

        // Add notifications
        sendNotification(patient.id, "You booked a consultation with $doctorName for $date at $time. Status is pending.")
        sendNotification(doctorId, "New consultation booked by ${patient.fullName} for $date at $time.")
        return true
    }

    fun cancelAppointment(appointmentId: Int) {
        val user = _currentUser.value ?: return
        val list = _appointments.value.map {
            if (it.id == appointmentId) {
                val statusString = "cancelled"
                
                // Notify both
                val otherUserId = if (user.role == UserRole.PATIENT) it.doctorId else it.patientId
                val otherUserName = _users.value.find { u -> u.id == otherUserId }?.fullName ?: "User"
                
                sendNotification(otherUserId, "${user.fullName} cancelled the appointment scheduled on ${it.appointmentDate} at ${it.appointmentTime}.")
                sendNotification(user.id, "You cancelled the appointment on ${it.appointmentDate} at ${it.appointmentTime}.")
                
                it.copy(status = statusString)
            } else {
                it
            }
        }
        _appointments.value = list
    }

    fun rescheduleAppointment(appointmentId: Int, newDate: String, newTime: String) {
        val user = _currentUser.value ?: return
        val list = _appointments.value.map {
            if (it.id == appointmentId) {
                // Notify other user
                val otherUserId = if (user.role == UserRole.PATIENT) it.doctorId else it.patientId
                sendNotification(otherUserId, "${user.fullName} requested to reschedule appointment to $newDate at $newTime.")
                sendNotification(user.id, "You requested to reschedule appointment to $newDate at $newTime.")
                
                it.copy(appointmentDate = newDate, appointmentTime = newTime, status = "pending")
            } else {
                it
            }
        }
        _appointments.value = list
    }

    fun updatePatientProfile(
        fullName: String, 
        phone: String, 
        email: String, 
        dob: String?, 
        gender: String?,
        nationalId: String?,
        insuranceProvider: String?,
        insuranceCardNumber: String?,
        insurancePrincipalName: String?
    ) {
        val user = _currentUser.value ?: return
        val updated = user.copy(
            fullName = fullName, 
            phone = phone, 
            email = email, 
            dob = dob, 
            gender = gender,
            nationalId = nationalId,
            insuranceProvider = insuranceProvider,
            insuranceCardNumber = insuranceCardNumber,
            insurancePrincipalName = insurancePrincipalName
        )
        _currentUser.value = updated
        _users.value = _users.value.map { if (it.id == user.id) updated else it }
    }

    fun updateDoctorProfile(
        fullName: String,
        phone: String,
        email: String,
        specialization: String,
        department: String,
        experienceYears: Int,
        consultationFee: Double,
        bio: String,
        availabilitySlots: List<String>
    ) {
        val user = _currentUser.value ?: return
        val updated = user.copy(
            fullName = fullName,
            phone = phone,
            email = email
        )
        _currentUser.value = updated
        _users.value = _users.value.map { if (it.id == user.id) updated else it }

        _doctorProfiles.value = _doctorProfiles.value.map {
            if (it.userId == user.id) {
                it.copy(
                    specialization = specialization,
                    department = department,
                    experienceYears = experienceYears,
                    consultationFee = consultationFee,
                    bio = bio,
                    availabilitySlots = availabilitySlots
                )
            } else {
                it
            }
        }
    }

    fun doctorAction(appointmentId: Int, newStatus: String, notes: String? = null) {
        val doctor = _currentUser.value ?: return
        val list = _appointments.value.map {
            if (it.id == appointmentId) {
                sendNotification(it.patientId, "${doctor.fullName} updated your appointment status to '$newStatus'.")
                
                val currentMpesaStatus = if (newStatus == "completed" || it.mpesaCode != null) "verified" else it.paymentStatus
                
                it.copy(
                    status = newStatus, 
                    doctorNotes = notes ?: it.doctorNotes,
                    paymentStatus = currentMpesaStatus
                )
            } else {
                it
            }
        }
        _appointments.value = list
    }

    fun bookWalkIn(
        patientName: String,
        patientPhone: String,
        patientNationalId: String,
        insuranceProvider: String,
        insuranceCard: String?,
        doctorId: Int,
        date: String,
        time: String,
        reason: String
    ): Boolean {
        var patient = _users.value.find { 
            (it.nationalId != null && it.nationalId == patientNationalId) || it.phone == patientPhone 
        }
        
        if (patient == null) {
            val nextId = (_users.value.maxOfOrNull { it.id } ?: 0) + 1
            patient = User(
                id = nextId,
                username = "walkin_${patientPhone.takeLast(4)}_${System.currentTimeMillis() % 1000}",
                passwordHash = "walkin123",
                email = "${patientPhone}@walkin.knphospital.or.ke",
                role = UserRole.PATIENT,
                fullName = patientName,
                phone = patientPhone,
                nationalId = patientNationalId,
                insuranceProvider = insuranceProvider,
                insuranceCardNumber = insuranceCard?.ifBlank { null },
                insurancePrincipalName = patientName
            )
            _users.value = _users.value + patient
        }
        
        val conflict = _appointments.value.any {
            it.doctorId == doctorId && it.appointmentDate == date && it.appointmentTime == time && it.status != "cancelled"
        }
        if (conflict) return false

        val nextAppId = (_appointments.value.maxOfOrNull { it.id } ?: 0) + 1
        val docUser = _users.value.find { it.id == doctorId }
        val doctorName = docUser?.fullName ?: "Doctor"

        val newApp = Appointment(
            id = nextAppId,
            patientId = patient.id,
            doctorId = doctorId,
            appointmentDate = date,
            appointmentTime = time,
            status = "confirmed",
            reason = reason,
            paymentStatus = if (insuranceProvider != "None (Self Pay / Cash)") "verified" else "paid",
            mpesaCode = if (insuranceProvider == "None (Self Pay / Cash)") "CASH_TILL" else null,
            doctorNotes = null,
            createdAt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )

        _appointments.value = _appointments.value + newApp
        sendNotification(patient.id, "Front-desk registered a walk-in appointment for you with $doctorName on $date at $time.")
        sendNotification(doctorId, "Receptionist booked a walk-in patient (${patient.fullName}) for you on $date at $time.")
        return true
    }

    fun receptionistAction(appointmentId: Int, newStatus: String, newPaymentStatus: String) {
        val list = _appointments.value.map {
            if (it.id == appointmentId) {
                sendNotification(it.patientId, "Front desk updated your appointment status to '$newStatus' and payment to '$newPaymentStatus'.")
                it.copy(status = newStatus, paymentStatus = newPaymentStatus)
            } else {
                it
            }
        }
        _appointments.value = list
    }

    fun adminAddDoctor(
        username: String,
        pass: String,
        email: String,
        fullName: String,
        phone: String,
        specialization: String,
        department: String,
        experienceYears: Int,
        consultationFee: Double,
        bio: String,
        availabilitySlots: List<String>
    ): Boolean {
        val exists = _users.value.any { it.username.equals(username, ignoreCase = true) || it.email.equals(email, ignoreCase = true) }
        if (exists) return false

        val nextId = (_users.value.maxOfOrNull { it.id } ?: 0) + 1
        val newDocUser = User(
            id = nextId,
            username = username,
            passwordHash = pass,
            email = email,
            role = UserRole.DOCTOR,
            fullName = fullName,
            phone = phone
        )
        _users.value = _users.value + newDocUser

        val nextProfileId = (_doctorProfiles.value.maxOfOrNull { it.id } ?: 0) + 1
        val newProfile = DoctorProfile(
            id = nextProfileId,
            userId = nextId,
            specialization = specialization,
            department = department,
            experienceYears = experienceYears,
            consultationFee = consultationFee,
            bio = bio,
            availabilitySlots = availabilitySlots
        )
        _doctorProfiles.value = _doctorProfiles.value + newProfile
        return true
    }

    fun adminEditDoctor(
        doctorUserId: Int,
        fullName: String,
        phone: String,
        email: String,
        specialization: String,
        department: String,
        experienceYears: Int,
        consultationFee: Double,
        bio: String,
        availabilitySlots: List<String>
    ): Boolean {
        _users.value = _users.value.map {
            if (it.id == doctorUserId) {
                it.copy(fullName = fullName, phone = phone, email = email)
            } else {
                it
            }
        }
        _doctorProfiles.value = _doctorProfiles.value.map {
            if (it.userId == doctorUserId) {
                it.copy(
                    specialization = specialization,
                    department = department,
                    experienceYears = experienceYears,
                    consultationFee = consultationFee,
                    bio = bio,
                    availabilitySlots = availabilitySlots
                )
            } else {
                it
            }
        }
        return true
    }

    fun adminUpdateUserPassword(userId: Int, newPassword: String) {
        _users.value = _users.value.map {
            if (it.id == userId) {
                val updated = it.copy(passwordHash = newPassword)
                if (_currentUser.value?.id == userId) {
                    _currentUser.value = updated
                }
                updated
            } else {
                it
            }
        }
    }

    fun adminUpdateUsername(userId: Int, newUsername: String): Boolean {
        val exists = _users.value.any { it.id != userId && it.username.equals(newUsername, ignoreCase = true) }
        if (exists) return false

        _users.value = _users.value.map {
            if (it.id == userId) {
                val updated = it.copy(username = newUsername)
                if (_currentUser.value?.id == userId) {
                    _currentUser.value = updated
                }
                updated
            } else {
                it
            }
        }
        return true
    }

    fun adminDeleteDoctor(doctorUserId: Int) {
        _users.value = _users.value.filter { it.id != doctorUserId }
        _doctorProfiles.value = _doctorProfiles.value.filter { it.userId != doctorUserId }
        _appointments.value = _appointments.value.map {
            if (it.doctorId == doctorUserId && it.status != "completed" && it.status != "cancelled") {
                sendNotification(it.patientId, "Your scheduled appointment has been cancelled as the specialist is no longer available.")
                it.copy(status = "cancelled")
            } else {
                it
            }
        }
    }

    fun markNotificationsAsRead() {
        val user = _currentUser.value ?: return
        _notifications.value = _notifications.value.map {
            if (it.userId == user.id) it.copy(isRead = true) else it
        }
    }

    private fun sendNotification(userId: Int, message: String) {
        val nextId = (_notifications.value.maxOfOrNull { it.id } ?: 0) + 1
        val newNotif = Notification(
            id = nextId,
            userId = userId,
            message = message,
            isRead = false,
            createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )
        _notifications.value = _notifications.value + newNotif
    }
}

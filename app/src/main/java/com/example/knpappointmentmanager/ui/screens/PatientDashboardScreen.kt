package com.example.knpappointmentmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.knpappointmentmanager.data.MockRepository
import com.example.knpappointmentmanager.data.Appointment
import com.example.knpappointmentmanager.data.DoctorProfile
import com.example.knpappointmentmanager.data.User
import java.text.SimpleDateFormat
import java.util.*

enum class PatientTab {
    OVERVIEW, BOOK, APPOINTMENTS, MEDICAL_LOG, PROFILE
}

// Global text visibility color (Slate 600 - High Contrast / Theme Aware)
private val TextContrastColor: Color @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDashboardScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by MockRepository.currentUser.collectAsStateWithLifecycle()
    val appointments by MockRepository.appointments.collectAsStateWithLifecycle()
    val doctorProfiles by MockRepository.doctorProfiles.collectAsStateWithLifecycle()
    val usersList by MockRepository.users.collectAsStateWithLifecycle()
    val notifications by MockRepository.notifications.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(PatientTab.OVERVIEW) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    // Filter appointments for active patient
    val myAppointments = remember(appointments, user) {
        appointments.filter { it.patientId == user?.id }
            .sortedByDescending { it.appointmentDate + it.appointmentTime }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Portal", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                actions = {
                    IconButton(onClick = {
                        MockRepository.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home", fontSize = 11.sp) },
                    selected = currentTab == PatientTab.OVERVIEW,
                    onClick = { currentTab = PatientTab.OVERVIEW; statusMessage = null }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
                    label = { Text("Book", fontSize = 11.sp) },
                    selected = currentTab == PatientTab.BOOK,
                    onClick = { currentTab = PatientTab.BOOK; statusMessage = null }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Visits", fontSize = 11.sp) },
                    selected = currentTab == PatientTab.APPOINTMENTS,
                    onClick = { currentTab = PatientTab.APPOINTMENTS; statusMessage = null }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("Records", fontSize = 11.sp) },
                    selected = currentTab == PatientTab.MEDICAL_LOG,
                    onClick = { currentTab = PatientTab.MEDICAL_LOG; statusMessage = null }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Profile", fontSize = 11.sp) },
                    selected = currentTab == PatientTab.PROFILE,
                    onClick = { currentTab = PatientTab.PROFILE; statusMessage = null }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Status Banner
            statusMessage?.let { msg ->
                val cardColor = if (isError) Color(0xFFFEE2E2) else Color(0xFFD1FAE5)
                val textColor = if (isError) Color(0xFF991B1B) else Color(0xFF065F46)
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isError) Icons.Default.Warning else Icons.Default.Check,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = msg, color = textColor, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { statusMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Tabs Content Router
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (currentTab) {
                    PatientTab.OVERVIEW -> OverviewTab(
                        user = user,
                        appointments = myAppointments,
                        usersList = usersList,
                        notifications = notifications.filter { it.userId == user?.id }
                    )
                    PatientTab.BOOK -> BookingTab(
                        user = user,
                        doctorProfiles = doctorProfiles,
                        usersList = usersList,
                        onBookSuccess = { success, msg ->
                            isError = !success
                            statusMessage = msg
                            if (success) currentTab = PatientTab.APPOINTMENTS
                        }
                    )
                    PatientTab.APPOINTMENTS -> AppointmentsTab(
                        myAppointments = myAppointments,
                        usersList = usersList,
                        onCancelClick = { id ->
                            MockRepository.cancelAppointment(id)
                            isError = false
                            statusMessage = "Appointment cancelled successfully."
                        },
                        onRescheduleSubmit = { id, date, time ->
                            MockRepository.rescheduleAppointment(id, date, time)
                            isError = false
                            statusMessage = "Reschedule request submitted successfully."
                        }
                    )
                    PatientTab.MEDICAL_LOG -> MedicalHistoryTab(
                        myAppointments = myAppointments,
                        usersList = usersList
                    )
                    PatientTab.PROFILE -> ProfileTab(
                        user = user,
                        onSaveSuccess = { msg ->
                            isError = false
                            statusMessage = msg
                        }
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// SUB-TAB COMPOSABLES
// ----------------------------------------------------

@Composable
fun OverviewTab(
    user: User?,
    appointments: List<Appointment>,
    usersList: List<User>,
    notifications: List<com.example.knpappointmentmanager.data.Notification>
) {
    val total = appointments.size
    val pending = appointments.count { it.status == "pending" }
    val confirmed = appointments.count { it.status == "confirmed" }
    val completed = appointments.count { it.status == "completed" }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Habari, ${user?.fullName ?: "Patient"}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Manage your clinical bookings at KNP Hospital Upper Hill, Nairobi.", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
        }

        // Stats Grid Rows
        Text("Overview Summary", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard(title = "Total Bookings", value = total.toString(), color = Color(0xFF3B82F6), modifier = Modifier.weight(1f))
            StatCard(title = "Pending Review", value = pending.toString(), color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard(title = "Confirmed", value = confirmed.toString(), color = Color(0xFF10B981), modifier = Modifier.weight(1f))
            StatCard(title = "Completed", value = completed.toString(), color = Color(0xFFEAB308), modifier = Modifier.weight(1f))
        }

        // Next Appointment Alert
        val upcoming = appointments.firstOrNull { it.status == "pending" || it.status == "confirmed" }
        if (upcoming != null) {
            val docName = usersList.find { it.id == upcoming.doctorId }?.fullName ?: "Doctor"
            Text("Upcoming Consultation", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(docName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Date: ${upcoming.appointmentDate} | Time: ${upcoming.appointmentTime}", fontSize = 13.sp, color = TextContrastColor)
                    }
                    StatusBadge(status = upcoming.status)
                }
            }
        }
 
        // Notifications List
        Text("Recent Activity Notifications", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
        if (notifications.isEmpty()) {
            Text("No recent alerts.", color = TextContrastColor, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    notifications.take(5).forEach { notif ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Text(notif.message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(notif.createdAt, fontSize = 10.sp, color = TextContrastColor, modifier = Modifier.padding(top = 2.dp))
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingTab(
    user: User?,
    doctorProfiles: List<DoctorProfile>,
    usersList: List<User>,
    onBookSuccess: (Boolean, String) -> Unit
) {
    var selectedDocId by remember { mutableStateOf(-1) }
    var selectedTime by remember { mutableStateOf("") }
    var bookingDate by remember { mutableStateOf("") }
    var symptomsReason by remember { mutableStateOf("") }
    var paymentOption by remember { mutableStateOf("unpaid") }
    var mpesaCode by remember { mutableStateOf("") }

    val activeDocProfile = doctorProfiles.find { it.userId == selectedDocId }

    // DatePicker States
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        bookingDate = sdf.format(Date(selectedDateMillis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("1. Choose Medical Specialist", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
        
        doctorProfiles.forEach { profile ->
            val docUser = usersList.find { it.id == profile.userId }
            val isSelected = selectedDocId == profile.userId
            val cardColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
            val borderCol = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
 
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                    .clickable { 
                        selectedDocId = profile.userId
                        selectedTime = "" // reset time slot selection
                    }
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(docUser?.fullName ?: "Doctor", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(profile.specialization, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text(profile.bio, fontSize = 12.sp, color = TextContrastColor, maxLines = 2)
                    }
                    Text("KES ${profile.consultationFee.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("2. Set Date & Time Slot", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
        
        Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
            OutlinedTextField(
                value = bookingDate,
                onValueChange = { },
                label = { Text("Consultation Date *") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                }
            )
        }

        activeDocProfile?.let { prof ->
            Text("Available Time Slots", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                prof.availabilitySlots.forEach { slot ->
                    val isSlotSelected = selectedTime == slot
                    val btnBg = if (isSlotSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    val textCol = if (isSlotSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    Button(
                        onClick = { selectedTime = slot },
                        colors = ButtonDefaults.buttonColors(containerColor = btnBg),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(slot, color = textCol, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("3. Clinical Details", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
        
        OutlinedTextField(
            value = symptomsReason,
            onValueChange = { symptomsReason = it },
            label = { Text("Reason for visit / Symptoms") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        // Payment logic
        Text("Payment Details", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = paymentOption == "unpaid", onClick = { paymentOption = "unpaid" })
            Text("Pay cash/insurance at triage", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = paymentOption == "paid", onClick = { paymentOption = "paid" })
            Text("Pay upfront via M-Pesa", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        }

        if (paymentOption == "unpaid") {
            // Dynamic check of saved insurance details
            val isInsured = !user?.insuranceProvider.isNullOrBlank() && !user?.insuranceProvider.equals("None (Self Pay / Cash)", ignoreCase = true)
            if (isInsured) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4EE)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFB9F6CA), RoundedCornerShape(8.dp))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF065F46), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cover Active: ${user?.insuranceProvider} (Policy #${user?.insuranceCardNumber}). Co-pay validated at clinical desk.",
                            color = Color(0xFF065F46),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF92400E), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No insurance details saved on your profile. Please add them in the profile settings tab if you intend to pay via insurance provider.",
                            color = Color(0xFF92400E),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        if (paymentOption == "paid") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Pay KES ${activeDocProfile?.consultationFee?.toInt() ?: 0} to Till Number 554433", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    OutlinedTextField(
                        value = mpesaCode,
                        onValueChange = { mpesaCode = it.uppercase() },
                        label = { Text("Enter M-Pesa Transaction Code") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        singleLine = true
                    )
                }
            }
        }

        Button(
            onClick = {
                if (selectedDocId == -1 || bookingDate.isBlank() || selectedTime.isBlank() || symptomsReason.isBlank()) {
                    onBookSuccess(false, "Please complete all fields (Doctor, Date, Time, and Symptoms).")
                } else if (paymentOption == "paid" && mpesaCode.isBlank()) {
                    onBookSuccess(false, "Please enter your M-Pesa transaction code.")
                } else {
                    val codeVal = if (paymentOption == "paid") mpesaCode else null
                    val ok = MockRepository.bookAppointment(selectedDocId, bookingDate, selectedTime, symptomsReason, paymentOption, codeVal)
                    if (ok) {
                        onBookSuccess(true, "Appointment request submitted successfully!")
                    } else {
                        onBookSuccess(false, "This slot is already booked for this doctor. Choose another date/time.")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 10.dp)
        ) {
            Text("Confirm Appointment", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsTab(
    myAppointments: List<Appointment>,
    usersList: List<User>,
    onCancelClick: (Int) -> Unit,
    onRescheduleSubmit: (Int, String, String) -> Unit
) {
    var rescheduleTargetId by remember { mutableStateOf(-1) }
    var rescheduleDate by remember { mutableStateOf("") }
    var rescheduleTime by remember { mutableStateOf("09:00 AM") }

    // DatePicker States for Rescheduling
    val rescheduleDatePickerState = rememberDatePickerState()
    var showRescheduleDatePicker by remember { mutableStateOf(false) }

    if (showRescheduleDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showRescheduleDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDateMillis = rescheduleDatePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        rescheduleDate = sdf.format(Date(selectedDateMillis))
                    }
                    showRescheduleDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRescheduleDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = rescheduleDatePickerState)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (myAppointments.isEmpty()) {
            item {
                Text("No appointment records found.", color = TextContrastColor, modifier = Modifier.padding(16.dp))
            }
        } else {
            items(myAppointments) { app ->
                val docName = usersList.find { it.id == app.doctorId }?.fullName ?: "Doctor"
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(docName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("Reason: ${app.reason}", fontSize = 12.sp, color = TextContrastColor, maxLines = 1)
                            }
                            StatusBadge(status = app.status)
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Date: ${app.appointmentDate}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                Text("Time: ${app.appointmentTime}", fontSize = 13.sp, color = TextContrastColor)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Payment: ${app.paymentStatus.uppercase()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                if (app.mpesaCode != null) {
                                    Text("Ref: ${app.mpesaCode}", fontSize = 11.sp, color = TextContrastColor)
                                }
                            }
                        }

                        if (app.status == "pending" || app.status == "confirmed") {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { onCancelClick(app.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                Button(
                                    onClick = { 
                                        rescheduleTargetId = app.id
                                        rescheduleDate = app.appointmentDate
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Reschedule", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
 
    // Inline Rescheduling panel overlay if active
    if (rescheduleTargetId != -1) {
        AlertDialog(
            onDismissRequest = { rescheduleTargetId = -1 },
            title = { Text("Reschedule Appointment", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().clickable { showRescheduleDatePicker = true }) {
                        OutlinedTextField(
                            value = rescheduleDate,
                            onValueChange = { },
                            label = { Text("New Date *") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showRescheduleDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                }
                            }
                        )
                    }
                    Text("Select Time Slot", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    val slots = listOf("09:00 AM", "10:00 AM", "11:00 AM", "02:00 PM", "03:00 PM")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        slots.forEach { slot ->
                            val active = rescheduleTime == slot
                            val bg = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            val tc = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            Button(
                                onClick = { rescheduleTime = slot },
                                colors = ButtonDefaults.buttonColors(containerColor = bg),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(slot, color = tc, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (rescheduleDate.isNotBlank() && rescheduleTime.isNotBlank()) {
                        onRescheduleSubmit(rescheduleTargetId, rescheduleDate, rescheduleTime)
                        rescheduleTargetId = -1
                    }
                }) {
                    Text("Submit Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { rescheduleTargetId = -1 }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MedicalHistoryTab(
    myAppointments: List<Appointment>,
    usersList: List<User>
) {
    val completedVisits = myAppointments.filter { it.status == "completed" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (completedVisits.isEmpty()) {
            item {
                Text("No medical records or completed visits found.", color = TextContrastColor, modifier = Modifier.padding(16.dp))
            }
        } else {
            items(completedVisits) { app ->
                val docName = usersList.find { it.id == app.doctorId }?.fullName ?: "Doctor"
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(docName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Visited Date: ${app.appointmentDate}", fontSize = 12.sp, color = TextContrastColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Diagnosis Symptoms:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(app.reason, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Doctor Clinical Notes:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Text(
                                text = app.doctorNotes ?: "No notes provided.",
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTab(
    user: User?,
    onSaveSuccess: (String) -> Unit
) {
    var fullName by remember { mutableStateOf(user?.fullName ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var dob by remember { mutableStateOf(user?.dob ?: "") }
    var gender by remember { mutableStateOf(user?.gender ?: "") }
    
    // New Fields
    var nationalId by remember { mutableStateOf(user?.nationalId ?: "") }
    var insuranceProvider by remember { mutableStateOf(user?.insuranceProvider ?: "None (Self Pay / Cash)") }
    var insuranceCardNumber by remember { mutableStateOf(user?.insuranceCardNumber ?: "") }
    var insurancePrincipalName by remember { mutableStateOf(user?.insurancePrincipalName ?: "") }

    var isDropdownExpanded by remember { mutableStateOf(false) }
    val insurersList = listOf(
        "None (Self Pay / Cash)",
        "Social Health Authority (SHA / SHIF)",
        "AAR Insurance Kenya",
        "Africa Merchant Assurance Company (AMACO)",
        "AIG Kenya Insurance Company",
        "Allianz Insurance Company of Kenya",
        "APA Insurance",
        "Barloworld Equipment Insurance Company",
        "Britam General Insurance",
        "Cannon Assurance",
        "Capex Life Assurance",
        "CIC General Insurance",
        "Corporate Insurance Company",
        "Directline Assurance Company",
        "Equity Life Assurance Kenya",
        "Fidelity Shield Insurance",
        "First Assurance Company",
        "GA Insurance",
        "Geminia Insurance Company",
        "Heritage Insurance Kenya",
        "ICEA LION General Insurance",
        "Intra Africa Assurance Company",
        "Invesco Assurance Company",
        "Jubilee Health Insurance",
        "Kenindia Assurance Company",
        "Kenya Orient Insurance",
        "Kuscco Mutual Assurance",
        "Liberty Life Assurance Kenya",
        "Madison General Insurance",
        "Mayfair Insurance Company",
        "Metropolitan Cannon Life Assurance",
        "Monarch Insurance Company",
        "MUA Insurance (Kenya)",
        "Occidental Insurance Company",
        "Old Mutual Insurance (UAP)",
        "Pacis Insurance Company",
        "Pioneer General Insurance",
        "Prudential Life Assurance Kenya",
        "Sanlam General Insurance",
        "Takaful Insurance of Africa",
        "Trident Insurance Company",
        "Xplico Insurance Company"
    )

    // Profile Birth DatePicker State
    val profileDatePickerState = rememberDatePickerState()
    var showProfileDatePicker by remember { mutableStateOf(false) }

    if (showProfileDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showProfileDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDateMillis = profileDatePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        dob = sdf.format(Date(selectedDateMillis))
                    }
                    showProfileDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = profileDatePickerState)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Personal & Account Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Fixed DOB field to use Native DatePicker
        Box(modifier = Modifier.fillMaxWidth().clickable { showProfileDatePicker = true }) {
            OutlinedTextField(
                value = dob,
                onValueChange = { },
                label = { Text("Date of Birth") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showProfileDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                }
            )
        }

        OutlinedTextField(
            value = nationalId,
            onValueChange = { nationalId = it },
            label = { Text("National ID Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("Gender", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = gender == "male", onClick = { gender = "male" })
                Text("Male", color = MaterialTheme.colorScheme.onSurface)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = gender == "female", onClick = { gender = "female" })
                Text("Female", color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text("Insurance & Health Cover Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        // Dropdown for Kenyan insurers list
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = insuranceProvider,
                onValueChange = { },
                label = { Text("Insurance Provider") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth().clickable { isDropdownExpanded = true },
                trailingIcon = {
                    IconButton(onClick = { isDropdownExpanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )
            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                insurersList.forEach { insurer ->
                    DropdownMenuItem(
                        text = { Text(insurer) },
                        onClick = {
                            insuranceProvider = insurer
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        val hasInsuranceSelected = insuranceProvider != "None (Self Pay / Cash)"
        if (hasInsuranceSelected) {
            OutlinedTextField(
                value = insuranceCardNumber,
                onValueChange = { insuranceCardNumber = it },
                label = { Text("Insurance Policy / Card Number *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = insurancePrincipalName,
                onValueChange = { insurancePrincipalName = it },
                label = { Text("Principal Member Full Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Button(
            onClick = {
                if (fullName.isNotBlank() && phone.isNotBlank() && email.isNotBlank()) {
                    MockRepository.updatePatientProfile(
                        fullName = fullName,
                        phone = phone,
                        email = email,
                        dob = dob.ifBlank { null },
                        gender = gender,
                        nationalId = nationalId.ifBlank { null },
                        insuranceProvider = insuranceProvider,
                        insuranceCardNumber = if (hasInsuranceSelected) insuranceCardNumber.ifBlank { null } else null,
                        insurancePrincipalName = if (hasInsuranceSelected) insurancePrincipalName.ifBlank { null } else null
                    )
                    onSaveSuccess("Profile details and insurance cover updated successfully.")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Save Profile Changes", fontWeight = FontWeight.Bold)
        }
    }
}

// ----------------------------------------------------
// HELPER COMPOSABLES
// ----------------------------------------------------

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 12.sp, color = TextContrastColor, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val bg = when (status) {
        "pending" -> Color(0xFFFEF3C7)
        "confirmed" -> Color(0xFFDBEAFE)
        "cancelled" -> Color(0xFFFEE2E2)
        "completed" -> Color(0xFFD1FAE5)
        else -> Color.Gray
    }
    val fg = when (status) {
        "pending" -> Color(0xFFB45309)
        "confirmed" -> Color(0xFF1E40AF)
        "cancelled" -> Color(0xFF991B1B)
        "completed" -> Color(0xFF065F46)
        else -> Color.White
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(status.uppercase(), color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}

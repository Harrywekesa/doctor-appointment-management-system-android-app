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
import com.example.knpappointmentmanager.data.User
import com.example.knpappointmentmanager.data.UserRole
import java.text.SimpleDateFormat
import java.util.*

enum class ReceptionistTab {
    SCHEDULE, WALKIN
}

private val TextContrastColor: Color @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceptionistDashboardScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appointments by MockRepository.appointments.collectAsStateWithLifecycle()
    val usersList by MockRepository.users.collectAsStateWithLifecycle()
    val doctorProfiles by MockRepository.doctorProfiles.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(ReceptionistTab.SCHEDULE) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receptionist Portal", fontWeight = FontWeight.Bold, color = Color.White) },
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
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Active Bookings", fontSize = 11.sp) },
                    selected = currentTab == ReceptionistTab.SCHEDULE,
                    onClick = { currentTab = ReceptionistTab.SCHEDULE; statusMessage = null }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Create, contentDescription = null) },
                    label = { Text("Book Walk-In", fontSize = 11.sp) },
                    selected = currentTab == ReceptionistTab.WALKIN,
                    onClick = { currentTab = ReceptionistTab.WALKIN; statusMessage = null }
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
            // Status banner message
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

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (currentTab) {
                    ReceptionistTab.SCHEDULE -> {
                        ScheduleTabContent(
                            appointments = appointments,
                            usersList = usersList,
                            onActionMessage = { msg, err ->
                                statusMessage = msg
                                isError = err
                            }
                        )
                    }
                    ReceptionistTab.WALKIN -> {
                        WalkInTabContent(
                            usersList = usersList,
                            doctorProfiles = doctorProfiles,
                            onActionMessage = { msg, err ->
                                statusMessage = msg
                                isError = err
                                if (!err) {
                                    currentTab = ReceptionistTab.SCHEDULE
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleTabContent(
    appointments: List<Appointment>,
    usersList: List<User>,
    onActionMessage: (String, Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterStatus by remember { mutableStateOf("All") }

    val statusOptions = listOf("All", "pending", "confirmed", "completed", "cancelled")

    // Filter appointments
    val filteredAppointments = appointments.filter { app ->
        val patientUser = usersList.find { it.id == app.patientId }
        val matchesSearch = patientUser?.fullName?.contains(searchQuery, ignoreCase = true) == true ||
                patientUser?.phone?.contains(searchQuery) == true ||
                patientUser?.nationalId?.contains(searchQuery) == true

        val matchesStatus = selectedFilterStatus == "All" || app.status.equals(selectedFilterStatus, ignoreCase = true)
        matchesSearch && matchesStatus
    }.sortedByDescending { it.appointmentDate }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Active Outpatient Bookings",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by Patient Name / Phone / National ID") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            statusOptions.forEach { status ->
                val isSelected = selectedFilterStatus == status
                val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                val textCol = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                
                Button(
                    onClick = { selectedFilterStatus = status },
                    colors = ButtonDefaults.buttonColors(containerColor = chipBg),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = status.replaceFirstChar { it.uppercase() },
                        color = textCol,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Appointments List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredAppointments.isEmpty()) {
                item {
                    Text(
                        text = "No appointment schedules found matching your query.",
                        color = TextContrastColor,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(filteredAppointments) { app ->
                    val patient = usersList.find { it.id == app.patientId }
                    val doctor = usersList.find { it.id == app.doctorId }
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Row 1: Patient details and Status badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = patient?.fullName ?: "Walk-in Patient",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Phone: ${patient?.phone ?: "N/A"} | ID: ${patient?.nationalId ?: "N/A"}",
                                        fontSize = 12.sp,
                                        color = TextContrastColor
                                    )
                                }
                                StatusBadge(status = app.status)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                            // Row 2: Appointment details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Assigned Specialist: ${doctor?.fullName ?: "Doctor"}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Reason: ${app.reason}",
                                        fontSize = 12.sp,
                                        color = TextContrastColor,
                                        maxLines = 2
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = app.appointmentDate,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = app.appointmentTime,
                                        fontSize = 12.sp,
                                        color = TextContrastColor
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                            // Row 3: Payment details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Payment: ",
                                        fontSize = 12.sp,
                                        color = TextContrastColor
                                    )
                                    val payBg = when (app.paymentStatus) {
                                        "paid" -> Color(0xFFD1FAE5)
                                        "verified" -> Color(0xFFDBEAFE)
                                        else -> Color(0xFFFEF3C7)
                                    }
                                    val payFg = when (app.paymentStatus) {
                                        "paid" -> Color(0xFF065F46)
                                        "verified" -> Color(0xFF1E40AF)
                                        else -> Color(0xFFB45309)
                                    }
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = payBg),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = app.paymentStatus.uppercase(),
                                            color = payFg,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (app.mpesaCode != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Ref: ${app.mpesaCode}",
                                            fontSize = 11.sp,
                                            color = TextContrastColor,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                if (patient?.insuranceProvider != null && patient.insuranceProvider != "None (Self Pay / Cash)") {
                                    Text(
                                        text = "Cover: ${patient.insuranceProvider}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Row 4: Actions for receptionist
                            if (app.status == "pending" || app.status == "confirmed" || app.paymentStatus == "unpaid") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Confirm/Accept Booking Action
                                    if (app.status == "pending") {
                                        Button(
                                            onClick = {
                                                MockRepository.receptionistAction(app.id, "confirmed", app.paymentStatus)
                                                onActionMessage("Booking for ${patient?.fullName} has been confirmed.", false)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Confirm", fontSize = 11.sp)
                                        }
                                    }

                                    // Payment verification actions
                                    if (app.paymentStatus == "unpaid") {
                                        if (patient?.insuranceProvider != null && patient.insuranceProvider != "None (Self Pay / Cash)") {
                                            Button(
                                                onClick = {
                                                    MockRepository.receptionistAction(app.id, "confirmed", "verified")
                                                    onActionMessage("Insurance co-pay verified successfully.", false)
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Verify Insure", fontSize = 11.sp)
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    MockRepository.receptionistAction(app.id, "confirmed", "paid")
                                                    onActionMessage("Cash payment confirmed & checked in.", false)
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Confirm Cash", fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    // Cancel Action
                                    if (app.status != "cancelled") {
                                        Button(
                                            onClick = {
                                                MockRepository.receptionistAction(app.id, "cancelled", app.paymentStatus)
                                                onActionMessage("Appointment booking has been cancelled.", false)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                            modifier = Modifier.weight(0.8f)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Cancel", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkInTabContent(
    usersList: List<User>,
    doctorProfiles: List<com.example.knpappointmentmanager.data.DoctorProfile>,
    onActionMessage: (String, Boolean) -> Unit
) {
    var patientName by remember { mutableStateOf("") }
    var patientPhone by remember { mutableStateOf("") }
    var patientNationalId by remember { mutableStateOf("") }
    var insuranceProvider by remember { mutableStateOf("None (Self Pay / Cash)") }
    var insuranceCard by remember { mutableStateOf("") }
    var selectedDocId by remember { mutableStateOf(-1) }
    var bookingDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var symptomsReason by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var isDocDropdownExpanded by remember { mutableStateOf(false) }
    var isInsurerDropdownExpanded by remember { mutableStateOf(false) }

    val insurersList = listOf(
        "None (Self Pay / Cash)",
        "Social Health Authority (SHA / SHIF)",
        "AAR Insurance Kenya",
        "APA Insurance",
        "Britam General Insurance",
        "CIC General Insurance",
        "Jubilee Health Insurance",
        "Madison General Insurance",
        "Old Mutual Insurance (UAP)",
        "Sanlam General Insurance"
    )

    val activeDocProfile = doctorProfiles.find { it.userId == selectedDocId }
    val activeDocUser = usersList.find { it.id == selectedDocId }

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
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Register Walk-in Patient & Book Consultation",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )

        // 1. Patient Registration Section
        Text("1. Patient Personal & Insurance Profile", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        OutlinedTextField(
            value = patientName,
            onValueChange = { patientName = it },
            label = { Text("Patient Full Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = patientPhone,
                onValueChange = { patientPhone = it },
                label = { Text("Phone Number *") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = patientNationalId,
                onValueChange = { patientNationalId = it },
                label = { Text("National ID Number *") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        // Insurance Provider Selector
        Box(modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
                expanded = isInsurerDropdownExpanded,
                onExpandedChange = { isInsurerDropdownExpanded = !isInsurerDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = insuranceProvider,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Insurance Provider *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isInsurerDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isInsurerDropdownExpanded,
                    onDismissRequest = { isInsurerDropdownExpanded = false }
                ) {
                    insurersList.forEach { insurer ->
                        DropdownMenuItem(
                            text = { Text(insurer) },
                            onClick = {
                                insuranceProvider = insurer
                                isInsurerDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        if (insuranceProvider != "None (Self Pay / Cash)") {
            OutlinedTextField(
                value = insuranceCard,
                onValueChange = { insuranceCard = it },
                label = { Text("Insurance Card / Policy Number *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // 2. Doctor and Slot details
        Text("2. Consultation Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

        // Doctor Selection
        Box(modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
                expanded = isDocDropdownExpanded,
                onExpandedChange = { isDocDropdownExpanded = !isDocDropdownExpanded }
            ) {
                val docText = if (selectedDocId != -1) {
                    val name = usersList.find { it.id == selectedDocId }?.fullName ?: "Doctor"
                    val spec = doctorProfiles.find { it.userId == selectedDocId }?.specialization ?: ""
                    "$name ($spec)"
                } else "Select Medical Specialist *"

                OutlinedTextField(
                    value = docText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Assigned Doctor *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDocDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isDocDropdownExpanded,
                    onDismissRequest = { isDocDropdownExpanded = false }
                ) {
                    doctorProfiles.forEach { profile ->
                        val docUser = usersList.find { it.id == profile.userId }
                        if (docUser != null) {
                            DropdownMenuItem(
                                text = { Text("${docUser.fullName} - ${profile.specialization}") },
                                onClick = {
                                    selectedDocId = profile.userId
                                    selectedTime = "" // Reset slot
                                    isDocDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Date selection
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

        // Availability Slots
        activeDocProfile?.let { prof ->
            Text("Available Time Slots", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

        OutlinedTextField(
            value = symptomsReason,
            onValueChange = { symptomsReason = it },
            label = { Text("Reason for visit / Symptoms *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                if (patientName.isBlank() || patientPhone.isBlank() || patientNationalId.isBlank() ||
                    selectedDocId == -1 || bookingDate.isBlank() || selectedTime.isBlank() || symptomsReason.isBlank()) {
                    onActionMessage("Error: Please fill all required fields.", true)
                } else if (insuranceProvider != "None (Self Pay / Cash)" && insuranceCard.isBlank()) {
                    onActionMessage("Error: Please provide policy number for the selected insurance provider.", true)
                } else {
                    val success = MockRepository.bookWalkIn(
                        patientName = patientName,
                        patientPhone = patientPhone,
                        patientNationalId = patientNationalId,
                        insuranceProvider = insuranceProvider,
                        insuranceCard = insuranceCard,
                        doctorId = selectedDocId,
                        date = bookingDate,
                        time = selectedTime,
                        reason = symptomsReason
                    )
                    if (success) {
                        onActionMessage("Walk-in Patient $patientName booked successfully with ${activeDocUser?.fullName ?: "Doctor"}.", false)
                    } else {
                        onActionMessage("Error: This slot is already booked for this doctor. Choose another date/time.", true)
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Register Patient & Confirm Booking", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

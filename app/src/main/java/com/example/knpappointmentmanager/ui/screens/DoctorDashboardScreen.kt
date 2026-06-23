package com.example.knpappointmentmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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

enum class DoctorTab {
    AGENDA, STATS, PROFILE
}

// Global text visibility color (Slate 600 - High Contrast / Theme Aware)
private val TextContrastColor: Color @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val doctor by MockRepository.currentUser.collectAsStateWithLifecycle()
    val appointments by MockRepository.appointments.collectAsStateWithLifecycle()
    val usersList by MockRepository.users.collectAsStateWithLifecycle()
    val doctorProfiles by MockRepository.doctorProfiles.collectAsStateWithLifecycle()
    val notifications by MockRepository.notifications.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(DoctorTab.AGENDA) }
    var activeActionAppId by remember { mutableStateOf(-1) }
    var clinicalNotesText by remember { mutableStateOf("") }
    
    var showCompletionDialog by remember { mutableStateOf(false) }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    // Search and Filter state
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterStatus by remember { mutableStateOf("All") }

    // Filter appointments for this doctor
    val myAppointments = remember(appointments, doctor) {
        appointments.filter { it.doctorId == doctor?.id }
            .sortedBy { it.appointmentDate + it.appointmentTime }
    }

    val filteredAppointments = remember(myAppointments, searchQuery, selectedFilterStatus, usersList) {
        myAppointments.filter { app ->
            val patName = usersList.find { it.id == app.patientId }?.fullName ?: ""
            val matchesSearch = patName.contains(searchQuery, ignoreCase = true) || app.reason.contains(searchQuery, ignoreCase = true)
            val matchesStatus = when (selectedFilterStatus) {
                "All" -> true
                "Pending" -> app.status.equals("pending", ignoreCase = true)
                "Confirmed" -> app.status.equals("confirmed", ignoreCase = true)
                "Completed" -> app.status.equals("completed", ignoreCase = true)
                "Declined" -> app.status.equals("cancelled", ignoreCase = true)
                else -> true
            }
            matchesSearch && matchesStatus
        }
    }

    val docProfile = remember(doctorProfiles, doctor) {
        doctorProfiles.find { it.userId == doctor?.id }
    }

    val myNotifications = remember(notifications, doctor) {
        notifications.filter { it.userId == doctor?.id }
            .sortedByDescending { it.createdAt }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doctor Console", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                actions = {
                    var showNotifMenu by remember { mutableStateOf(false) }
                    val unreadCount = myNotifications.count { !it.isRead }
                    Box {
                        IconButton(onClick = { showNotifMenu = true }) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.tertiary,
                                            contentColor = MaterialTheme.colorScheme.onTertiary
                                        ) {
                                            Text(unreadCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                            }
                        }
                        
                        if (showNotifMenu) {
                            AlertDialog(
                                onDismissRequest = { showNotifMenu = false },
                                title = { Text("Doctor Notifications", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp)) {
                                        if (myNotifications.isEmpty()) {
                                            Text("No alerts received.", fontSize = 13.sp, color = TextContrastColor)
                                        } else {
                                            LazyColumn(
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f, fill = false)
                                            ) {
                                                items(myNotifications) { notif ->
                                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                                        Text(notif.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                                        Text(notif.createdAt, fontSize = 10.sp, color = TextContrastColor)
                                                    }
                                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    if (unreadCount > 0) {
                                        TextButton(onClick = {
                                            MockRepository.markNotificationsAsRead()
                                            showNotifMenu = false
                                        }) {
                                            Text("Mark as Read")
                                        }
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showNotifMenu = false }) {
                                        Text("Close")
                                    }
                                }
                            )
                        }
                    }

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
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("Agenda Tasks", fontSize = 11.sp) },
                    selected = currentTab == DoctorTab.AGENDA,
                    onClick = { currentTab = DoctorTab.AGENDA }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("My Insights", fontSize = 11.sp) },
                    selected = currentTab == DoctorTab.STATS,
                    onClick = { currentTab = DoctorTab.STATS }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile", fontSize = 11.sp) },
                    selected = currentTab == DoctorTab.PROFILE,
                    onClick = { currentTab = DoctorTab.PROFILE }
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
            
            // Header card welcome
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(doctor?.fullName ?: "Doctor", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        Text(docProfile?.specialization ?: "Specialist", fontSize = 13.sp, color = TextContrastColor)
                    }
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Active Agenda", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (currentTab) {
                    DoctorTab.AGENDA -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Search Box
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Search Patients or Reasons") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            // Horizontal Filter Chips
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val filters = listOf("All", "Pending", "Confirmed", "Completed", "Declined")
                                filters.forEach { filter ->
                                    val isSelected = selectedFilterStatus == filter
                                    val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    val chipTc = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    
                                    Button(
                                        onClick = { selectedFilterStatus = filter },
                                        colors = ButtonDefaults.buttonColors(containerColor = chipBg, contentColor = chipTc),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(filter, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(2.dp))

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (filteredAppointments.isEmpty()) {
                                    item {
                                        Text("No schedule bookings match search filters.", color = TextContrastColor, modifier = Modifier.padding(16.dp))
                                    }
                                } else {
                                    items(filteredAppointments) { app ->
                                        val patName = usersList.find { it.id == app.patientId }?.fullName ?: "Patient"
                                        
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(patName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                                        Text("Reason: ${app.reason}", fontSize = 12.sp, color = TextContrastColor)
                                                    }
                                                    StatusBadge(status = app.status)
                                                }
                                                
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                                
                                                Row(
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column {
                                                        Text("Date: ${app.appointmentDate}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                                        Text("Time: ${app.appointmentTime}", fontSize = 12.sp, color = TextContrastColor)
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text("Payment: ${app.paymentStatus.uppercase()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                        if (app.mpesaCode != null) {
                                                            Text("M-Pesa: ${app.mpesaCode}", fontSize = 11.sp, color = TextContrastColor)
                                                        }
                                                    }
                                                }

                                                // Action Buttons for doctor
                                                if (app.status == "pending" || app.status == "confirmed") {
                                                    Spacer(modifier = Modifier.height(14.dp))
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        if (app.status == "pending") {
                                                            Button(
                                                                onClick = { MockRepository.doctorAction(app.id, "confirmed") },
                                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                                modifier = Modifier.weight(1.5f),
                                                                contentPadding = PaddingValues(vertical = 4.dp)
                                                            ) {
                                                                Text("Confirm Visit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        } else {
                                                            Button(
                                                                onClick = { 
                                                                    activeActionAppId = app.id
                                                                    clinicalNotesText = ""
                                                                    showCompletionDialog = true
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                                modifier = Modifier.weight(1.5f),
                                                                contentPadding = PaddingValues(vertical = 4.dp)
                                                            ) {
                                                                Text("Complete Visit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }

                                                        Button(
                                                            onClick = { MockRepository.doctorAction(app.id, "cancelled") },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                                                            ),
                                                            modifier = Modifier.weight(1f),
                                                            contentPadding = PaddingValues(vertical = 4.dp)
                                                        ) {
                                                            Text("Decline", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }

                                                if (app.status == "completed") {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Text("Clinical Notes Summary:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                                    ) {
                                                        Text(app.doctorNotes ?: "No notes captured.", fontSize = 12.sp, modifier = Modifier.padding(10.dp), color = MaterialTheme.colorScheme.onSurface)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    DoctorTab.STATS -> {
                        val totalEarned = myAppointments.filter { it.status == "completed" || it.paymentStatus == "paid" || it.paymentStatus == "verified" }
                            .sumOf { (docProfile?.consultationFee ?: 0.0) }
                        
                        val totalConsultations = myAppointments.count { it.status == "completed" }
                        val activeConsultations = myAppointments.count { it.status == "confirmed" }
                        val pendingReview = myAppointments.count { it.status == "pending" }

                        Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Consultation Insights", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text("Estimated Earnings", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("KES ${totalEarned.toInt()}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Based on confirmed and completed sessions.", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                StatCard(title = "Sessions Completed", value = totalConsultations.toString(), color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                                StatCard(title = "Upcoming Sessions", value = activeConsultations.toString(), color = Color(0xFF3B82F6), modifier = Modifier.weight(1f))
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                StatCard(title = "Pending Review", value = pendingReview.toString(), color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                                StatCard(title = "Consultation Fee", value = "KES ${docProfile?.consultationFee?.toInt() ?: 0}", color = Color(0xFFEAB308), modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    DoctorTab.PROFILE -> {
                        DoctorProfileTab(
                            doctor = doctor,
                            docProfile = docProfile,
                            onSaveSuccess = { msg ->
                                isError = msg.startsWith("Error")
                                statusMessage = msg
                            }
                        )
                    }
                }
            }
        }
    }

    // Diagnostic log capture dialog
    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = { showCompletionDialog = false },
            title = { Text("Complete Session & Write Clinical Notes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Capture your diagnosis notes, clinical requests, or prescriptions for the patient record:", fontSize = 12.sp, color = TextContrastColor)
                    OutlinedTextField(
                        value = clinicalNotesText,
                        onValueChange = { clinicalNotesText = it },
                        label = { Text("Clinical Diagnostic Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (clinicalNotesText.isNotBlank()) {
                        MockRepository.doctorAction(activeActionAppId, "completed", clinicalNotesText)
                        showCompletionDialog = false
                    }
                }) {
                    Text("Complete Consultation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompletionDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DoctorProfileTab(
    doctor: User?,
    docProfile: com.example.knpappointmentmanager.data.DoctorProfile?,
    onSaveSuccess: (String) -> Unit
) {
    var fullName by remember { mutableStateOf(doctor?.fullName ?: "") }
    var phone by remember { mutableStateOf(doctor?.phone ?: "") }
    var email by remember { mutableStateOf(doctor?.email ?: "") }
    var specialization by remember { mutableStateOf(docProfile?.specialization ?: "") }
    var department by remember { mutableStateOf(docProfile?.department ?: "") }
    var experienceYears by remember { mutableStateOf(docProfile?.experienceYears?.toString() ?: "0") }
    var consultationFee by remember { mutableStateOf(docProfile?.consultationFee?.toString() ?: "0.0") }
    var bio by remember { mutableStateOf(docProfile?.bio ?: "") }
    
    val allSlots = listOf("09:00 AM", "10:00 AM", "11:00 AM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM")
    var selectedSlots by remember { mutableStateOf(docProfile?.availabilitySlots ?: emptyList()) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Professional & Contact Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

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

        OutlinedTextField(
            value = specialization,
            onValueChange = { specialization = it },
            label = { Text("Medical Specialization *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = department,
            onValueChange = { department = it },
            label = { Text("Department / Clinic *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = experienceYears,
                onValueChange = { experienceYears = it },
                label = { Text("Experience (Years) *") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = consultationFee,
                onValueChange = { consultationFee = it },
                label = { Text("Consultation Fee (KES) *") },
                modifier = Modifier.weight(1.2f),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Biography / Notes *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Text("Set Availability Slots", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allSlots.forEach { slot ->
                val isSelected = selectedSlots.contains(slot)
                val btnBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                val btnText = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                
                Button(
                    onClick = {
                        selectedSlots = if (isSelected) {
                            selectedSlots - slot
                        } else {
                            selectedSlots + slot
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = btnBg, contentColor = btnText),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(slot, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val exp = experienceYears.toIntOrNull() ?: 0
                val fee = consultationFee.toDoubleOrNull() ?: 0.0
                if (fullName.isBlank() || phone.isBlank() || email.isBlank() || specialization.isBlank() || department.isBlank() || bio.isBlank()) {
                    onSaveSuccess("Error: Please fill in all required settings fields.")
                } else if (selectedSlots.isEmpty()) {
                    onSaveSuccess("Error: Please select at least one active slot.")
                } else {
                    MockRepository.updateDoctorProfile(
                        fullName = fullName,
                        phone = phone,
                        email = email,
                        specialization = specialization,
                        department = department,
                        experienceYears = exp,
                        consultationFee = fee,
                        bio = bio,
                        availabilitySlots = selectedSlots
                    )
                    onSaveSuccess("Doctor profile settings saved successfully!")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Save Profile Settings", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

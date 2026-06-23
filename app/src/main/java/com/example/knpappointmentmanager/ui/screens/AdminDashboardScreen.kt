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
import com.example.knpappointmentmanager.data.DoctorProfile
import com.example.knpappointmentmanager.data.User
import com.example.knpappointmentmanager.data.UserRole
import com.example.knpappointmentmanager.data.Appointment
import java.text.SimpleDateFormat
import java.util.*

enum class AdminTab {
    DOCTORS, PATIENTS, APPOINTMENTS, REVENUE, STATS
}

private val TextContrastColor: Color @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appointments by MockRepository.appointments.collectAsStateWithLifecycle()
    val doctorProfiles by MockRepository.doctorProfiles.collectAsStateWithLifecycle()
    val usersList by MockRepository.users.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(AdminTab.DOCTORS) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    // Dialog state controllers
    var showAddDoctorDialog by remember { mutableStateOf(false) }
    var editingDoctorId by remember { mutableStateOf(-1) }
    var showDeleteConfirmId by remember { mutableStateOf(-1) }
    var reschedulingAppId by remember { mutableStateOf(-1) }
    var passwordResetUserId by remember { mutableStateOf(-1) }
    var usernameEditUserId by remember { mutableStateOf(-1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Console", fontWeight = FontWeight.Bold, color = Color.White) },
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
                    icon = { Icon(Icons.Default.Build, contentDescription = null) },
                    label = { Text("Specialists", fontSize = 10.sp) },
                    selected = currentTab == AdminTab.DOCTORS,
                    onClick = { currentTab = AdminTab.DOCTORS; statusMessage = null }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Patients", fontSize = 10.sp) },
                    selected = currentTab == AdminTab.PATIENTS,
                    onClick = { currentTab = AdminTab.PATIENTS; statusMessage = null }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("Schedules", fontSize = 10.sp) },
                    selected = currentTab == AdminTab.APPOINTMENTS,
                    onClick = { currentTab = AdminTab.APPOINTMENTS; statusMessage = null }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    label = { Text("Revenue", fontSize = 10.sp) },
                    selected = currentTab == AdminTab.REVENUE,
                    onClick = { currentTab = AdminTab.REVENUE; statusMessage = null }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("Insights", fontSize = 10.sp) },
                    selected = currentTab == AdminTab.STATS,
                    onClick = { currentTab = AdminTab.STATS; statusMessage = null }
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
                    AdminTab.DOCTORS -> {
                        DoctorsTabContent(
                            doctorProfiles = doctorProfiles,
                            usersList = usersList,
                            onAddDoctor = { showAddDoctorDialog = true },
                            onEditDoctor = { id -> editingDoctorId = id },
                            onDeleteDoctor = { id -> showDeleteConfirmId = id }
                        )
                    }
                    AdminTab.PATIENTS -> {
                        PatientsTabContent(
                            usersList = usersList,
                            onResetPassword = { id -> passwordResetUserId = id },
                            onChangeUsername = { id -> usernameEditUserId = id }
                        )
                    }
                    AdminTab.APPOINTMENTS -> {
                        AppointmentsTabContent(
                            appointments = appointments,
                            usersList = usersList,
                            onCancelApp = { id ->
                                MockRepository.cancelAppointment(id)
                                statusMessage = "Appointment Cancelled Successfully."
                                isError = false
                            },
                            onRescheduleApp = { id -> reschedulingAppId = id }
                        )
                    }
                    AdminTab.REVENUE -> {
                        RevenueTabContent(
                            appointments = appointments,
                            doctorProfiles = doctorProfiles,
                            usersList = usersList
                        )
                    }
                    AdminTab.STATS -> {
                        StatsTabContent(
                            appointments = appointments,
                            doctorProfiles = doctorProfiles,
                            usersList = usersList
                        )
                    }
                }
            }
        }
    }

    // Add Doctor Dialog Form
    if (showAddDoctorDialog) {
        AddDoctorDialog(
            onDismiss = { showAddDoctorDialog = false },
            onSaveSuccess = { msg, err ->
                statusMessage = msg
                isError = err
                showAddDoctorDialog = false
            }
        )
    }

    // Edit Doctor Dialog Form
    if (editingDoctorId != -1) {
        val currentDocUser = usersList.find { it.id == editingDoctorId }
        val currentDocProfile = doctorProfiles.find { it.userId == editingDoctorId }
        if (currentDocUser != null && currentDocProfile != null) {
            EditDoctorDialog(
                docUser = currentDocUser,
                docProfile = currentDocProfile,
                onDismiss = { editingDoctorId = -1 },
                onSaveSuccess = { msg, err ->
                    statusMessage = msg
                    isError = err
                    editingDoctorId = -1
                }
            )
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmId != -1) {
        val targetName = usersList.find { it.id == showDeleteConfirmId }?.fullName ?: "Specialist"
        AlertDialog(
            onDismissRequest = { showDeleteConfirmId = -1 },
            title = { Text("De-register Specialist?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Are you sure you want to remove $targetName from the hospital registry? Outstanding active appointments will be cancelled.", color = TextContrastColor) },
            confirmButton = {
                Button(
                    onClick = {
                        MockRepository.adminDeleteDoctor(showDeleteConfirmId)
                        isError = false
                        statusMessage = "$targetName has been successfully removed from the registry."
                        showDeleteConfirmId = -1
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = Color.White)
                ) {
                    Text("Remove Doctor")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmId = -1 }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reschedule Dialog
    if (reschedulingAppId != -1) {
        RescheduleDialog(
            appointmentId = reschedulingAppId,
            onDismiss = { reschedulingAppId = -1 },
            onSaveSuccess = { msg, err ->
                statusMessage = msg
                isError = err
                reschedulingAppId = -1
            }
        )
    }

    // Password Reset Dialog
    if (passwordResetUserId != -1) {
        val targetUser = usersList.find { it.id == passwordResetUserId }
        val targetName = targetUser?.fullName ?: "User"
        var newPassword by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { passwordResetUserId = -1 },
            title = { Text("Reset User Password", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Enter a new password for $targetName:", fontSize = 12.sp, color = TextContrastColor)
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPassword.isBlank()) {
                            statusMessage = "Error: Password cannot be blank."
                            isError = true
                        } else {
                            MockRepository.adminUpdateUserPassword(passwordResetUserId, newPassword)
                            statusMessage = "Password for $targetName has been successfully reset."
                            isError = false
                            passwordResetUserId = -1
                        }
                    }
                ) {
                    Text("Reset Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { passwordResetUserId = -1 }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Username Edit Dialog
    if (usernameEditUserId != -1) {
        val targetUser = usersList.find { it.id == usernameEditUserId }
        val targetName = targetUser?.fullName ?: "User"
        var newUsername by remember { mutableStateOf(targetUser?.username ?: "") }

        AlertDialog(
            onDismissRequest = { usernameEditUserId = -1 },
            title = { Text("Change Username", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Enter a new username for $targetName:", fontSize = 12.sp, color = TextContrastColor)
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("New Username *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUsername.isBlank()) {
                            statusMessage = "Error: Username cannot be blank."
                            isError = true
                        } else {
                            val ok = MockRepository.adminUpdateUsername(usernameEditUserId, newUsername)
                            if (ok) {
                                statusMessage = "Username for $targetName has been successfully changed to '$newUsername'."
                                isError = false
                                usernameEditUserId = -1
                            } else {
                                statusMessage = "Error: Username '$newUsername' is already taken."
                                isError = true
                            }
                        }
                    }
                ) {
                    Text("Save Username")
                }
            },
            dismissButton = {
                TextButton(onClick = { usernameEditUserId = -1 }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// SUB-TAB CONTENTS
// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

@Composable
fun DoctorsTabContent(
    doctorProfiles: List<DoctorProfile>,
    usersList: List<User>,
    onAddDoctor: () -> Unit,
    onEditDoctor: (Int) -> Unit,
    onDeleteDoctor: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hospital Specialists Registry", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Button(
                onClick = onAddDoctor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Doctor", fontSize = 12.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (doctorProfiles.isEmpty()) {
                item {
                    Text("No doctors registered yet.", color = TextContrastColor, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(doctorProfiles) { profile ->
                    val docUser = usersList.find { it.id == profile.userId }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(docUser?.fullName ?: "Doctor", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("${profile.specialization} (${profile.department})", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                }
                                IconButton(onClick = { onEditDoctor(profile.userId) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit profile", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onDeleteDoctor(profile.userId) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete specialist", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(profile.bio, fontSize = 12.sp, color = TextContrastColor)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Fee: KES ${profile.consultationFee.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Experience: ${profile.experienceYears} Years", fontSize = 12.sp, color = TextContrastColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PatientsTabContent(
    usersList: List<User>,
    onResetPassword: (Int) -> Unit,
    onChangeUsername: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val patients = usersList.filter {
        it.role == UserRole.PATIENT && (
            it.fullName.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery) ||
            it.nationalId?.contains(searchQuery) == true
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Registered Outpatients Directory", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Patients (Name / Phone / ID)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (patients.isEmpty()) {
                item {
                    Text("No matching patients registered in system.", color = TextContrastColor, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(patients) { patient ->
                    var isExpanded by remember { mutableStateOf(false) }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .clickable { isExpanded = !isExpanded }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(patient.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Phone: ${patient.phone} | Username: ${patient.username}", fontSize = 12.sp, color = TextContrastColor)
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (isExpanded) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Email: ${patient.email}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Birth Date: ${patient.dob ?: "Not set"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Gender: ${patient.gender?.replaceFirstChar { it.uppercase() } ?: "Not set"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("National ID: ${patient.nationalId ?: "Not set"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    val isInsured = !patient.insuranceProvider.isNullOrBlank() && patient.insuranceProvider != "None (Self Pay / Cash)"
                                    if (isInsured) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("Insurance Cover Active", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                                Text("Provider: ${patient.insuranceProvider}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                                Text("Policy #: ${patient.insuranceCardNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                                Text("Principal Name: ${patient.insurancePrincipalName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                    } else {
                                        Text("Insurance Details: None (Self Pay / Cash)", fontSize = 12.sp, color = TextContrastColor)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { onChangeUsername(patient.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit Username", fontSize = 11.sp)
                                        }
                                        Button(
                                            onClick = { onResetPassword(patient.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reset Password", fontSize = 11.sp)
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

@Composable
fun AppointmentsTabContent(
    appointments: List<Appointment>,
    usersList: List<User>,
    onCancelApp: (Int) -> Unit,
    onRescheduleApp: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterStatus by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "pending", "confirmed", "completed", "cancelled")

    val filteredApps = appointments.filter { app ->
        val patient = usersList.find { it.id == app.patientId }
        val doctor = usersList.find { it.id == app.doctorId }
        val matchesSearch = patient?.fullName?.contains(searchQuery, ignoreCase = true) == true ||
                doctor?.fullName?.contains(searchQuery, ignoreCase = true) == true ||
                app.reason.contains(searchQuery, ignoreCase = true)

        val matchesStatus = selectedFilterStatus == "All" || app.status.equals(selectedFilterStatus, ignoreCase = true)
        matchesSearch && matchesStatus
    }.sortedByDescending { it.appointmentDate }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Master Hospital Booking Queue", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by Patient, Doctor or Symptoms") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterOptions.forEach { status ->
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

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredApps.isEmpty()) {
                item {
                    Text("No appointment records match current filters.", color = TextContrastColor, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(filteredApps) { app ->
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(patient?.fullName ?: "Patient", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Doctor: ${doctor?.fullName ?: "Specialist"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                }
                                StatusBadge(status = app.status)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Time: ${app.appointmentDate} | ${app.appointmentTime}", fontSize = 12.sp, color = TextContrastColor)
                            Text("Reason: ${app.reason}", fontSize = 12.sp, color = TextContrastColor)
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Payment: ${app.paymentStatus.uppercase()} ${app.mpesaCode?.let { "(Ref: $it)" } ?: ""}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                                if (app.status == "pending" || app.status == "confirmed") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = { onRescheduleApp(app.id) }) {
                                            Text("Reschedule", fontSize = 11.sp)
                                        }
                                        TextButton(
                                            onClick = { onCancelApp(app.id) },
                                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) {
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

@Composable
fun RevenueTabContent(
    appointments: List<Appointment>,
    doctorProfiles: List<DoctorProfile>,
    usersList: List<User>
) {
    var searchQuery by remember { mutableStateOf("") }

    val completedPaidApps = appointments.filter {
        it.status == "completed" || it.paymentStatus == "paid" || it.paymentStatus == "verified"
    }
    
    val totalCollected = completedPaidApps.sumOf { app ->
        doctorProfiles.find { it.userId == app.doctorId }?.consultationFee ?: 0.0
    }

    val pendingPaidApps = appointments.filter {
        it.status != "cancelled" && it.paymentStatus == "unpaid"
    }

    val totalPending = pendingPaidApps.sumOf { app ->
        doctorProfiles.find { it.userId == app.doctorId }?.consultationFee ?: 0.0
    }

    val filteredPaidLog = appointments.filter { app ->
        val patient = usersList.find { it.id == app.patientId }
        val matchesSearch = patient?.fullName?.contains(searchQuery, ignoreCase = true) == true ||
                app.mpesaCode?.contains(searchQuery, ignoreCase = true) == true
        matchesSearch
    }.sortedByDescending { it.createdAt }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Hospital Revenue Ledger", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(title = "Total Collected", value = "KES ${totalCollected.toInt()}", color = Color(0xFF10B981), modifier = Modifier.weight(1f))
            StatCard(title = "Outstanding Co-Pay", value = "KES ${totalPending.toInt()}", color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search transactions by Patient Name / M-Pesa Code") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredPaidLog.isEmpty()) {
                item {
                    Text("No transaction logs match current query.", color = TextContrastColor, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(filteredPaidLog) { app ->
                    val patient = usersList.find { it.id == app.patientId }
                    val doctor = usersList.find { it.id == app.doctorId }
                    val fee = doctorProfiles.find { it.userId == app.doctorId }?.consultationFee ?: 0.0
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(patient?.fullName ?: "Outpatient", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("Specialist: ${doctor?.fullName ?: "Doctor"}", fontSize = 11.sp, color = TextContrastColor)
                                if (app.mpesaCode != null) {
                                    Text("Ref: ${app.mpesaCode}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                } else {
                                    Text("Method: Unpaid/Triage Co-pay", fontSize = 10.sp, color = Color.Red.copy(alpha = 0.8f))
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("KES ${fee.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                val badgeCol = if (app.paymentStatus == "paid" || app.paymentStatus == "verified") Color(0xFF10B981) else Color(0xFFF59E0B)
                                Text(app.paymentStatus.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeCol)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsTabContent(
    appointments: List<Appointment>,
    doctorProfiles: List<DoctorProfile>,
    usersList: List<User>
) {
    val totalAppCount = appointments.size
    val totalEarnings = appointments.filter { it.status == "completed" || it.paymentStatus == "paid" || it.paymentStatus == "verified" }
        .sumOf { app ->
            val docId = app.doctorId
            val fee = doctorProfiles.find { it.userId == docId }?.consultationFee ?: 0.0
            fee
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Hospital Insights Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(title = "Total Earnings", value = "KES ${totalEarnings.toInt()}", color = Color(0xFF10B981), modifier = Modifier.weight(1.2f))
            StatCard(title = "All Bookings", value = totalAppCount.toString(), color = Color(0xFF3B82F6), modifier = Modifier.weight(0.8f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(title = "Hospital Staff", value = usersList.count { it.role == UserRole.DOCTOR || it.role == UserRole.ADMIN || it.role == UserRole.RECEPTIONIST }.toString(), color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
            StatCard(title = "Registered Patients", value = usersList.count { it.role == UserRole.PATIENT }.toString(), color = Color(0xFFEAB308), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("Consultations by Specialists", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

        doctorProfiles.forEach { profile ->
            val docUser = usersList.find { it.id == profile.userId }
            val docApps = appointments.filter { it.doctorId == profile.userId }
            val completedCount = docApps.count { it.status == "completed" }
            val pendingCount = docApps.count { it.status == "pending" }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(docUser?.fullName ?: "Doctor", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(profile.specialization, fontSize = 11.sp, color = TextContrastColor)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("$completedCount Completed", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("$pendingCount Pending", fontSize = 11.sp, color = TextContrastColor)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// DIALOG FORMS
// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

@Composable
fun AddDoctorDialog(
    onDismiss: () -> Unit,
    onSaveSuccess: (String, Boolean) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("General Medicine") }
    var department by remember { mutableStateOf("Outpatient Care") }
    var experienceYears by remember { mutableStateOf("5") }
    var consultationFee by remember { mutableStateOf("1500") }
    var bio by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Medical Specialist", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username *") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password *") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = specialization, onValueChange = { specialization = it }, label = { Text("Specialization *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department / Clinic *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = experienceYears, onValueChange = { experienceYears = it }, label = { Text("Experience (Yrs)") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = consultationFee, onValueChange = { consultationFee = it }, label = { Text("Fee (KES)") }, modifier = Modifier.weight(1.2f), singleLine = true)
                }
                OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Biography *") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val feeVal = consultationFee.toDoubleOrNull() ?: 1000.0
                    val expVal = experienceYears.toIntOrNull() ?: 1
                    if (username.isBlank() || password.isBlank() || email.isBlank() || fullName.isBlank() || phone.isBlank() || bio.isBlank()) {
                        onSaveSuccess("Error: Please enter all required specialist fields.", true)
                    } else {
                        val success = MockRepository.adminAddDoctor(
                            username = username,
                            pass = password,
                            email = email,
                            fullName = fullName,
                            phone = phone,
                            specialization = specialization,
                            department = department,
                            experienceYears = expVal,
                            consultationFee = feeVal,
                            bio = bio,
                            availabilitySlots = listOf("09:00 AM", "10:00 AM", "11:00 AM", "02:00 PM", "03:00 PM")
                        )
                        if (success) {
                            onSaveSuccess("Specialist $fullName registered successfully.", false)
                        } else {
                            onSaveSuccess("Error: Username or email is already taken.", true)
                        }
                    }
                }
            ) {
                Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditDoctorDialog(
    docUser: User,
    docProfile: DoctorProfile,
    onDismiss: () -> Unit,
    onSaveSuccess: (String, Boolean) -> Unit
) {
    var fullName by remember { mutableStateOf(docUser.fullName) }
    var phone by remember { mutableStateOf(docUser.phone) }
    var email by remember { mutableStateOf(docUser.email) }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf(docUser.username) }
    var specialization by remember { mutableStateOf(docProfile.specialization) }
    var department by remember { mutableStateOf(docProfile.department) }
    var experienceYears by remember { mutableStateOf(docProfile.experienceYears.toString()) }
    var consultationFee by remember { mutableStateOf(docProfile.consultationFee.toInt().toString()) }
    var bio by remember { mutableStateOf(docProfile.bio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Medical Specialist", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = specialization, onValueChange = { specialization = it }, label = { Text("Specialization *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department / Clinic *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = experienceYears, onValueChange = { experienceYears = it }, label = { Text("Experience (Yrs)") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = consultationFee, onValueChange = { consultationFee = it }, label = { Text("Fee (KES)") }, modifier = Modifier.weight(1.2f), singleLine = true)
                }
                OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Biography *") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("New Password (blank to keep current)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val feeVal = consultationFee.toDoubleOrNull() ?: docProfile.consultationFee
                    val expVal = experienceYears.toIntOrNull() ?: docProfile.experienceYears
                    val usernameConflict = MockRepository.users.value.any { it.id != docUser.id && it.username.equals(username, ignoreCase = true) }
                    if (fullName.isBlank() || phone.isBlank() || email.isBlank() || bio.isBlank() || username.isBlank()) {
                        onSaveSuccess("Error: All fields are required.", true)
                    } else if (usernameConflict) {
                        onSaveSuccess("Error: Username '$username' is already taken.", true)
                    } else {
                        MockRepository.adminUpdateUsername(docUser.id, username)
                        MockRepository.adminEditDoctor(
                            doctorUserId = docUser.id,
                            fullName = fullName,
                            phone = phone,
                            email = email,
                            specialization = specialization,
                            department = department,
                            experienceYears = expVal,
                            consultationFee = feeVal,
                            bio = bio,
                            availabilitySlots = docProfile.availabilitySlots
                        )
                        if (password.isNotBlank()) {
                            MockRepository.adminUpdateUserPassword(docUser.id, password)
                        }
                        onSaveSuccess("Specialist details updated successfully.", false)
                    }
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RescheduleDialog(
    appointmentId: Int,
    onDismiss: () -> Unit,
    onSaveSuccess: (String, Boolean) -> Unit
) {
    var bookingDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    val timeslots = listOf("09:00 AM", "10:00 AM", "11:00 AM", "02:00 PM", "03:00 PM", "04:00 PM")

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reschedule Appointment", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select new date and time slot for the consultation override:", fontSize = 12.sp, color = TextContrastColor)
                
                Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                    OutlinedTextField(
                        value = bookingDate,
                        onValueChange = { },
                        label = { Text("New Date *") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                            }
                        }
                    )
                }

                Text("Available Slots", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timeslots.take(3).forEach { slot ->
                        val isSelected = selectedTime == slot
                        val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        val textCol = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        Button(
                            onClick = { selectedTime = slot },
                            colors = ButtonDefaults.buttonColors(containerColor = chipBg),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(slot, color = textCol, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timeslots.drop(3).forEach { slot ->
                        val isSelected = selectedTime == slot
                        val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        val textCol = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        Button(
                            onClick = { selectedTime = slot },
                            colors = ButtonDefaults.buttonColors(containerColor = chipBg),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(slot, color = textCol, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (bookingDate.isBlank() || selectedTime.isBlank()) {
                        onSaveSuccess("Error: Please select date and slot.", true)
                    } else {
                        MockRepository.rescheduleAppointment(appointmentId, bookingDate, selectedTime)
                        onSaveSuccess("Appointment rescheduled successfully.", false)
                    }
                }
            ) {
                Text("Confirm Override")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

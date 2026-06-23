package com.example.knpappointmentmanager

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Auth : NavKey
@Serializable data object PatientDashboard : NavKey
@Serializable data object DoctorDashboard : NavKey
@Serializable data object AdminDashboard : NavKey
@Serializable data object ReceptionistDashboard : NavKey

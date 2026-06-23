package com.example.knpappointmentmanager

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.knpappointmentmanager.data.UserRole
import com.example.knpappointmentmanager.ui.screens.AuthScreen
import com.example.knpappointmentmanager.ui.screens.PatientDashboardScreen
import com.example.knpappointmentmanager.ui.screens.DoctorDashboardScreen
import com.example.knpappointmentmanager.ui.screens.AdminDashboardScreen
import com.example.knpappointmentmanager.ui.screens.ReceptionistDashboardScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Auth)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Auth> {
                AuthScreen(
                    onLoginSuccess = { role ->
                        when (role) {
                            UserRole.PATIENT -> backStack.add(PatientDashboard)
                            UserRole.DOCTOR -> backStack.add(DoctorDashboard)
                            UserRole.ADMIN -> backStack.add(AdminDashboard)
                            UserRole.RECEPTIONIST -> backStack.add(ReceptionistDashboard)
                        }
                    }
                )
            }
            entry<PatientDashboard> {
                PatientDashboardScreen(
                    onLogout = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<DoctorDashboard> {
                DoctorDashboardScreen(
                    onLogout = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<AdminDashboard> {
                AdminDashboardScreen(
                    onLogout = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<ReceptionistDashboard> {
                ReceptionistDashboardScreen(
                    onLogout = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}

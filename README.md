# KNP Hospital Doctor Appointment Management System (Android App)

A modern, native Android application built with **Jetpack Compose** and **Material 3** for managing outpatient bookings, clinical specialist agendas, administrative controls, and front-desk reception triage at KNP Hospital, Upper Hill, Nairobi.

---

## 📱 Key Features

This application implements **four distinct user portals** configured with specific role-based workflows, dynamic HSL-derived branding, full dark/light theme contrast adaptations, and dynamic Material 3 controls.

### 1. Patient Portal
- **Consultation Booking:** Schedule appointments by selecting a doctor, date (using native Material 3 `DatePickerDialog`), and available time slots.
- **Triage Co-Pay Billing:** Choose between upfront mobile checkout via M-Pesa (Till `554433`) or pay at triage using insurance cover.
- **Kenyan Insurance Registry:** Pre-configured with all 40+ registered insurers operating in Kenya (including the Social Health Authority - SHA/SHIF) to save cover details.
- **Booking Management:** Reschedule (native date pickers) or cancel appointments dynamically.
- **Clinical Records:** Access electronic prescription notes and diagnostics from the specialist.

### 2. Doctor Console
- **Agenda Queue:** Scrollable view of daily consultations with search functions and filter chips (All, Pending, Confirmed, Completed, Cancelled).
- **Diagnostics Logger:** Triage patients, approve/decline slot requests, and capture diagnostic notes and prescriptions.
- **Profile Settings:** Update personal details, bios, consultation fees, and customize active availability hours grid.
- **Notification Hub:** Live badge alert center in `TopAppBar` showing unread appointments, cancellations, and reschedules.

### 3. Admin Dashboard
- **Specialist Management:** Register new specialists or de-register/edit details (Name, Email, Phone, username, password, Specialization, Fees, Experience, Bio) on the fly.
- **Patient Directory:** Directory of all registered outpatients with DOB, Gender, and Insurance policy details.
- **Master Schedules Override:** Global outpatient booking queue where admins can cancel or override slots on behalf of patients/doctors.
- **Revenue Ledger:** Track hospital financials showing **Total Collected** (paid/verified co-pays) and **Outstanding Co-Pay** (unpaid triage) alongside M-Pesa billing details.
- **Hospital Insights:** High-level dashboard statistics (Staff totals, Patient totals, Specialist activity breakdown).

### 4. Receptionist Portal (Front Desk)
- **Active Bookings Triage:** Searchable active outpatient queue to check in patients, verify cash receipts, or validate insurance co-pays.
- **Walk-in Scheduler:** Register walk-in patients on the spot, configure their insurance profile, assign them to a doctor, select a date via a native calendar picker, and confirm their booking.

---

## 🔐 Test Accounts (Quick Autofill Panel)

For testing purposes, the login screen includes a quick autofill card to sign in instantly with pre-seeded accounts:

| Portal Role | Username | Password | Default Test Features |
| :--- | :--- | :--- | :--- |
| **Patient Portal** | `john_koech` | `patient123` | Active Jubilee Insurance policy, books slot, mock M-Pesa till `554433` payments. |
| **Doctor Console** | `dr_amina` | `doctor123` | Cardiology specialist, updates consultation fee, captures clinical logs. |
| **Admin Console** | `admin` | `admin123` | Hospital statistics, edits doctor credentials/usernames, views patients. |
| **Receptionist Portal** | `receptionist` | `receptionist123` | Front-desk desk agent, registers walk-in patients, marks cash pay. |

---

## 🛠️ Build & Installation

### Prerequisites
- JDK 17
- Android SDK (configured via `local.properties`)
- USB debugging enabled on test device

### Build APK
Run the following Gradle command in the root project directory:
```powershell
.\gradlew.bat assembleDebug
```

### Install & Launch Debug Build
Deploy to your connected device and launch the launcher activity using ADB:
```powershell
.\gradlew.bat installDebug
adb shell am start -n com.example.knpappointmentmanager/com.example.knpappointmentmanager.MainActivity
```

---

## 🎨 Tech Stack & Architecture
- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material 3 components, DatePicker, FlowRow, ExposedDropdownMenuBox)
- **Navigation:** Navigation3 (`rememberNavBackStack`)
- **State Management:** Reactive flows using Kotlin `StateFlow` and `collectAsStateWithLifecycle`
- **Data Layer:** MockRepository hosting persistent local session states

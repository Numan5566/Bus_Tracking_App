package com.example.bus_tracking_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bus_tracking_app.data.SimulatedEmail
import com.example.bus_tracking_app.data.User
import com.example.bus_tracking_app.ui.components.CitySelector
import com.example.bus_tracking_app.ui.components.CustomTextField
import com.example.bus_tracking_app.ui.components.PasswordTextField
import com.example.bus_tracking_app.ui.components.SearchableUniversitySelector
import com.example.bus_tracking_app.ui.components.StudentCardPicker
import com.example.bus_tracking_app.utils.PreferencesManager
import com.example.bus_tracking_app.utils.SMTPClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RegisterScreen(
    prefs: PreferencesManager,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (SimulatedEmail?) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var regNumber by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("Lahore") }
    var selectedUniversity by remember { mutableStateOf("") }
    var studentCardUri by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var regError by remember { mutableStateOf<String?>(null) }
    var universityError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var registerErrorMsg by remember { mutableStateOf<String?>(null) }

    var isRegistering by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F2027),
            Color(0xFF203A43),
            Color(0xFF2C5364)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Join your university bus network today",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Student Registration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F4C81),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(bottom = 8.dp))

                    // Full Name
                    CustomTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = null
                        },
                        label = "Full Name",
                        leadingIcon = Icons.Default.Person,
                        isError = nameError != null,
                        errorMessage = nameError
                    )

                    // Email Address
                    CustomTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = "Academic Email Address",
                        leadingIcon = Icons.Default.Email,
                        isError = emailError != null,
                        errorMessage = emailError
                    )

                    // Registration Number
                    CustomTextField(
                        value = regNumber,
                        onValueChange = {
                            regNumber = it
                            regError = null
                        },
                        label = "Registration / Roll Number",
                        leadingIcon = Icons.Default.Badge,
                        isError = regError != null,
                        errorMessage = regError
                    )

                    // City Selection Dropdown (Lahore, Karachi, Islamabad, etc.)
                    CitySelector(
                        selectedCity = selectedCity,
                        onCitySelected = {
                            selectedCity = it
                            selectedUniversity = "" // Reset university on city change
                            universityError = null
                        }
                    )

                    // Dynamic Autocomplete University Selector
                    SearchableUniversitySelector(
                        selectedCity = selectedCity,
                        selectedUniversity = selectedUniversity,
                        onUniversitySelected = {
                            selectedUniversity = it
                            universityError = null
                        },
                        isError = universityError != null,
                        errorMessage = universityError
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Student ID Card Image Upload with Mocks
                    StudentCardPicker(
                        userName = name,
                        universityName = selectedUniversity,
                        selectedImageUri = studentCardUri,
                        onImageSelected = { studentCardUri = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Password
                    PasswordTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        label = "Create Password",
                        leadingIcon = Icons.Default.Lock,
                        passwordVisible = passwordVisible,
                        onPasswordVisibleChange = { passwordVisible = it },
                        isError = passwordError != null,
                        errorMessage = passwordError
                    )

                    // Confirm Password
                    PasswordTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmPasswordError = null
                        },
                        label = "Confirm Password",
                        leadingIcon = Icons.Default.Lock,
                        passwordVisible = confirmPasswordVisible,
                        onPasswordVisibleChange = { confirmPasswordVisible = it },
                        isError = confirmPasswordError != null,
                        errorMessage = confirmPasswordError
                    )

                    // Register Button
                    Button(
                        onClick = {
                            var hasError = false

                            if (name.isBlank()) {
                                nameError = "Name is required"
                                hasError = true
                            }
                            if (email.isBlank()) {
                                emailError = "Email is required"
                                hasError = true
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Enter a valid email"
                                hasError = true
                            }
                            if (regNumber.isBlank()) {
                                regError = "Registration number is required"
                                hasError = true
                            }
                            if (selectedUniversity.isBlank()) {
                                universityError = "Please select your university"
                                hasError = true
                            }
                            if (password.length < 6) {
                                passwordError = "Password must be at least 6 characters"
                                hasError = true
                            }
                            if (password != confirmPassword) {
                                confirmPasswordError = "Passwords do not match"
                                hasError = true
                            }

                            if (!hasError) {
                                isRegistering = true
                                registerErrorMsg = null

                                val newUser = User(
                                    email = email,
                                    name = name,
                                    regNumber = regNumber,
                                    city = selectedCity,
                                    university = selectedUniversity,
                                    studentCardUri = studentCardUri,
                                    passwordHash = password // Keep raw/hash simple for demo
                                )

                                val success = prefs.registerUser(newUser)
                                if (!success) {
                                    emailError = "Email is already registered"
                                    isRegistering = false
                                } else {
                                    // Trigger Welcome Registration Mail Action
                                    coroutineScope.launch {
                                        val deviceName = SMTPClient.getDeviceName()
                                        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                                        val subject = "🎉 Welcome to Bus Tracking App - Registration Successful!"
                                        val cardVerificationState = if (studentCardUri != null) "Verified (Digital ID Attached)" else "Pending Upload"
                                        val bodyHtml = """
                                            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                                                <h2 style="color: #0F4C81; text-align: center;">Welcome, Registration Approved!</h2>
                                                <p>Dear <b>$name</b>,</p>
                                                <p>Congratulations! Your academic account has been registered successfully with the <b>Bus Tracking Application</b>.</p>
                                                <div style="background-color: #e3f2fd; padding: 15px; border-radius: 6px; margin: 20px 0;">
                                                    <h3 style="margin-top: 0; color: #0f4c81;">Account Profile Details</h3>
                                                    <ul style="padding-left: 20px;">
                                                        <li><b>Name:</b> $name</li>
                                                        <li><b>Email:</b> $email</li>
                                                        <li><b>Reg Number:</b> $regNumber</li>
                                                        <li><b>Location:</b> $selectedCity, Pakistan</li>
                                                        <li><b>University:</b> $selectedUniversity</li>
                                                        <li><b>Student Card Verification:</b> <span style="color: #2ec4b6; font-weight: bold;">$cardVerificationState</span></li>
                                                    </ul>
                                                </div>
                                                <p>You can now log in securely using your mobile device ($deviceName) and track school buses in real-time!</p>
                                                <p style="color: #888; font-size: 12px; text-align: center; margin-top: 30px;">Bus Tracking Android Application &copy; 2026</p>
                                            </div>
                                        """.trimIndent()

                                        val smtpConfig = prefs.getSmtpConfig()
                                        var simulatedEmail: SimulatedEmail? = null

                                        if (smtpConfig.isEnabled) {
                                            val result = SMTPClient.sendEmail(smtpConfig, email, subject, bodyHtml)
                                            if (!result.isSuccess) {
                                                simulatedEmail = SimulatedEmail(email, subject, bodyHtml, timestamp, deviceName)
                                            }
                                        } else {
                                            simulatedEmail = SimulatedEmail(email, subject, bodyHtml, timestamp, deviceName)
                                        }

                                        isRegistering = false
                                        onRegisterSuccess(simulatedEmail)
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F4C81)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (isRegistering) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                "Submit Registration",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Display errors
                    registerErrorMsg?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Back to Login link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account? ",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Login here",
                            color = Color(0xFF0F4C81),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .clickable { onNavigateToLogin() }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

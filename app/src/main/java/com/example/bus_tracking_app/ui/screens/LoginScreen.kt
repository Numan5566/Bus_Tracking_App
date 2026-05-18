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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bus_tracking_app.data.SimulatedEmail
import com.example.bus_tracking_app.data.SmtpConfig
import com.example.bus_tracking_app.ui.components.CustomTextField
import com.example.bus_tracking_app.ui.components.PasswordTextField
import com.example.bus_tracking_app.ui.components.SmtpSettingsDialog
import com.example.bus_tracking_app.utils.PreferencesManager
import com.example.bus_tracking_app.utils.SMTPClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    prefs: PreferencesManager,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: (SimulatedEmail?) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var loginStatusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessStatus by remember { mutableStateOf(false) }

    var showSmtpSettings by remember { mutableStateOf(false) }
    var isLoggingIn by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Background Gradient Palette
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
            // SMTP Config Button at the top right
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
            ) {
                IconButton(
                    onClick = { showSmtpSettings = true },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "SMTP Configuration",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bus Tracker Visual Logo Header
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.size(100.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "🚍",
                        fontSize = 54.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bus Tracking System",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Text(
                text = "Track your university transport securely",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main Glassmorphic Input Card
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
                        text = "Member Login",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F4C81),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(bottom = 8.dp))

                    // Email Address
                    CustomTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = "Email Address",
                        leadingIcon = Icons.Default.Email,
                        isError = emailError != null,
                        errorMessage = emailError
                    )

                    // Password
                    PasswordTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        label = "Password",
                        leadingIcon = Icons.Default.Lock,
                        passwordVisible = passwordVisible,
                        onPasswordVisibleChange = { passwordVisible = it },
                        isError = passwordError != null,
                        errorMessage = passwordError
                    )

                    // Forget Password link row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = Color(0xFF0F4C81),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .clickable { onNavigateToForgotPassword() }
                                .padding(vertical = 4.dp)
                        )
                    }

                    // Login Action Button
                    Button(
                        onClick = {
                            var hasError = false
                            if (email.isBlank()) {
                                emailError = "Email is required"
                                hasError = true
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Enter a valid email"
                                hasError = true
                            }

                            if (password.isBlank()) {
                                passwordError = "Password is required"
                                hasError = true
                            }

                            if (!hasError) {
                                isLoggingIn = true
                                loginStatusMessage = null

                                val user = prefs.getUser(email)
                                if (user == null) {
                                    emailError = "Account does not exist"
                                    isLoggingIn = false
                                } else if (user.passwordHash != password) {
                                    passwordError = "Incorrect password"
                                    isLoggingIn = false
                                } else {
                                    // Trigger Login Mail Send Action
                                    coroutineScope.launch {
                                        val deviceName = SMTPClient.getDeviceName()
                                        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                                        val subject = "🚍 Security Alert: New Login Detected"
                                        val bodyHtml = """
                                            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                                                <h2 style="color: #0F4C81; text-align: center;">New Device Login Detected</h2>
                                                <p>Hello <b>${user.name}</b>,</p>
                                                <p>We detected a new login to your Bus Tracking Account on the following device:</p>
                                                <table style="width: 100%; border-collapse: collapse; margin: 20px 0;">
                                                    <tr style="background-color: #f8f9fa;">
                                                        <td style="padding: 10px; border: 1px solid #dee2e6; font-weight: bold;">Device Model</td>
                                                        <td style="padding: 10px; border: 1px solid #dee2e6; color: #ea4335;">$deviceName</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 10px; border: 1px solid #dee2e6; font-weight: bold;">Time</td>
                                                        <td style="padding: 10px; border: 1px solid #dee2e6;">$timestamp</td>
                                                    </tr>
                                                    <tr style="background-color: #f8f9fa;">
                                                        <td style="padding: 10px; border: 1px solid #dee2e6; font-weight: bold;">University</td>
                                                        <td style="padding: 10px; border: 1px solid #dee2e6;">${user.university}</td>
                                                    </tr>
                                                </table>
                                                <p>If this was you, you can safely ignore this email. If this was not you, please secure your account immediately by changing your password.</p>
                                                <p style="color: #888; font-size: 12px; text-align: center; margin-top: 30px;">Bus Tracking Android Application &copy; 2026</p>
                                            </div>
                                        """.trimIndent()

                                        val smtpConfig = prefs.getSmtpConfig()
                                        var simulatedEmail: SimulatedEmail? = null

                                        if (smtpConfig.isEnabled) {
                                            val result = SMTPClient.sendEmail(smtpConfig, email, subject, bodyHtml)
                                            if (result.isSuccess) {
                                                isSuccessStatus = true
                                                loginStatusMessage = "Login alert email successfully delivered to $email!"
                                            } else {
                                                isSuccessStatus = false
                                                loginStatusMessage = "Real email send failed: ${result.exceptionOrNull()?.message}. Mocking instead."
                                                simulatedEmail = SimulatedEmail(email, subject, bodyHtml, timestamp, deviceName)
                                            }
                                        } else {
                                            // Fallback simulated email
                                            simulatedEmail = SimulatedEmail(email, subject, bodyHtml, timestamp, deviceName)
                                        }

                                        isLoggingIn = false
                                        onLoginSuccess(simulatedEmail)
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
                        if (isLoggingIn) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                "Secure Login",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Display statuses/errors
                    loginStatusMessage?.let {
                        Text(
                            text = it,
                            color = if (isSuccessStatus) Color(0xFF2EC4B6) else Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Sign up navigation link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "New student? ",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Register Account",
                            color = Color(0xFF0F4C81),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .clickable { onNavigateToRegister() }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // SMTP Settings Overlay
        if (showSmtpSettings) {
            SmtpSettingsDialog(
                currentConfig = prefs.getSmtpConfig(),
                onSave = { newConfig ->
                    prefs.saveSmtpConfig(newConfig)
                    showSmtpSettings = false
                },
                onDismiss = { showSmtpSettings = false }
            )
        }
    }
}

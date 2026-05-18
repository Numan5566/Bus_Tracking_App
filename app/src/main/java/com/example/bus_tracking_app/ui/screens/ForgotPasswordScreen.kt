package com.example.bus_tracking_app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bus_tracking_app.data.SimulatedEmail
import com.example.bus_tracking_app.ui.components.CustomTextField
import com.example.bus_tracking_app.ui.components.PasswordTextField
import com.example.bus_tracking_app.utils.PreferencesManager
import com.example.bus_tracking_app.utils.SMTPClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class ResetPhase {
    ENTER_EMAIL,
    ENTER_CODE,
    NEW_PASSWORD
}

@Composable
fun ForgotPasswordScreen(
    prefs: PreferencesManager,
    onNavigateToLogin: () -> Unit,
    onCodeSent: (SimulatedEmail?) -> Unit,
    onPasswordChanged: (SimulatedEmail?) -> Unit,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(ResetPhase.ENTER_EMAIL) }
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Logic generated values
    var generatedCode by remember { mutableStateOf("") }

    // Error states
    var emailError by remember { mutableStateOf<String?>(null) }
    var codeError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var isProcessing by remember { mutableStateOf(false) }
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
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Recover access to your bus profile",
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
                    // Title based on Phase
                    val titleText = when (phase) {
                        ResetPhase.ENTER_EMAIL -> "Request Verification Code"
                        ResetPhase.ENTER_CODE -> "Verify Outbox Code"
                        ResetPhase.NEW_PASSWORD -> "Setup New Password"
                    }

                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F4C81),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(bottom = 8.dp))

                    // Phase 1: ENTER EMAIL
                    AnimatedVisibility(
                        visible = phase == ResetPhase.ENTER_EMAIL,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "Please enter your registered email address. We will generate and email a 6-digit OTP code to verify your identity.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

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

                                    if (!hasError) {
                                        isProcessing = true
                                        val user = prefs.getUser(email)
                                        if (user == null) {
                                            emailError = "This email is not registered"
                                            isProcessing = false
                                        } else {
                                            // Generate random OTP
                                            val otp = Random.nextInt(100000, 999999).toString()
                                            generatedCode = otp

                                            coroutineScope.launch {
                                                val deviceName = SMTPClient.getDeviceName()
                                                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                                                val subject = "🚍 Account Verification OTP Code"
                                                val bodyHtml = """
                                                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                                                        <h2 style="color: #0F4C81; text-align: center;">Reset Password Verification</h2>
                                                        <p>Hello <b>${user.name}</b>,</p>
                                                        <p>We received a request to reset your password for your Bus Tracking account on device <b>$deviceName</b>.</p>
                                                        <p>Please enter the following 6-digit secure Verification Code to update your password:</p>
                                                        <div style="text-align: center; margin: 30px 0;">
                                                            <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; padding: 12px 24px; background-color: #f1f3f4; border: 1px solid #dee2e6; border-radius: 6px; color: #0F4C81;">$otp</span>
                                                        </div>
                                                        <p style="color: #d93025; font-weight: bold;">This code is valid for 15 minutes and should not be shared with anyone.</p>
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

                                                isProcessing = false
                                                phase = ResetPhase.ENTER_CODE
                                                onCodeSent(simulatedEmail)
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
                                if (isProcessing) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Send Reset Code", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Phase 2: ENTER OTP CODE
                    AnimatedVisibility(
                        visible = phase == ResetPhase.ENTER_CODE,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "A 6-digit OTP verification code was sent to your email. Enter it below to unlock the password setup.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            CustomTextField(
                                value = code,
                                onValueChange = {
                                    code = it
                                    codeError = null
                                },
                                label = "6-Digit OTP Code",
                                leadingIcon = Icons.Default.Pin,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = codeError != null,
                                errorMessage = codeError
                            )

                            Button(
                                onClick = {
                                    if (code.isBlank()) {
                                        codeError = "Code is required"
                                    } else if (code != generatedCode) {
                                        codeError = "Incorrect OTP code. Try again."
                                    } else {
                                        phase = ResetPhase.NEW_PASSWORD
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F4C81)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text("Verify Code", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Phase 3: NEW PASSWORD SETUP
                    AnimatedVisibility(
                        visible = phase == ResetPhase.NEW_PASSWORD,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "Identity verified! Set your new password below and click save. An security confirmation will be delivered to your inbox.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            PasswordTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordError = null
                                },
                                label = "New Password",
                                leadingIcon = Icons.Default.Lock,
                                passwordVisible = passwordVisible,
                                onPasswordVisibleChange = { passwordVisible = it },
                                isError = passwordError != null,
                                errorMessage = passwordError
                            )

                            PasswordTextField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    confirmPasswordError = null
                                },
                                label = "Confirm New Password",
                                leadingIcon = Icons.Default.Lock,
                                passwordVisible = confirmPasswordVisible,
                                onPasswordVisibleChange = { confirmPasswordVisible = it },
                                isError = confirmPasswordError != null,
                                errorMessage = confirmPasswordError
                            )

                            Button(
                                onClick = {
                                    var hasError = false
                                    if (password.length < 6) {
                                        passwordError = "Password must be at least 6 characters"
                                        hasError = true
                                    }
                                    if (password != confirmPassword) {
                                        confirmPasswordError = "Passwords do not match"
                                        hasError = true
                                    }

                                    if (!hasError) {
                                        isProcessing = true
                                        val success = prefs.updatePassword(email, password)

                                        if (success) {
                                            coroutineScope.launch {
                                                val deviceName = SMTPClient.getDeviceName()
                                                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                                                val subject = "⚠️ Security Alert: Password Updated"
                                                val bodyHtml = """
                                                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                                                        <h2 style="color: #ea4335; text-align: center;">Security Alert: Password Changed</h2>
                                                        <p>Hello,</p>
                                                        <p>Your password for the academic Bus Tracking profile <b>$email</b> has been changed successfully.</p>
                                                        <div style="background-color: #fdf2f2; padding: 15px; border-radius: 6px; margin: 20px 0; border-left: 4px solid #ea4335;">
                                                            <p style="margin: 0; font-weight: bold; color: #ea4335;">Modification Details</p>
                                                            <ul style="padding-left: 20px; margin-top: 8px;">
                                                                <li><b>Action:</b> Password Reset Verification</li>
                                                                <li><b>Device Used:</b> $deviceName</li>
                                                                <li><b>Timestamp:</b> $timestamp</li>
                                                            </ul>
                                                        </div>
                                                        <p>If you made this change, you can safely ignore this mail. If you did not authorize this, please immediately lock your device and contact the administrator.</p>
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

                                                isProcessing = false
                                                onPasswordChanged(simulatedEmail)
                                            }
                                        } else {
                                            isProcessing = false
                                            passwordError = "Failed to update password. Try again."
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F4C81)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Save Password & Change", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Back to Login link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Remember password? ",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Login instead",
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
        }
    }
}

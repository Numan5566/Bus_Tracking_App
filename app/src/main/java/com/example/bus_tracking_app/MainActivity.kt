package com.example.bus_tracking_app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.bus_tracking_app.data.SimulatedEmail
import com.example.bus_tracking_app.data.User
import com.example.bus_tracking_app.ui.components.EmailSimulatorDialog
import com.example.bus_tracking_app.ui.screens.DashboardScreen
import com.example.bus_tracking_app.ui.screens.ForgotPasswordScreen
import com.example.bus_tracking_app.ui.screens.LoginScreen
import com.example.bus_tracking_app.ui.screens.RegisterScreen
import com.example.bus_tracking_app.ui.theme.Bus_Tracking_AppTheme
import com.example.bus_tracking_app.utils.PreferencesManager

enum class AppScreen {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD,
    DASHBOARD
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Bus_Tracking_AppTheme {
                val context = this
                val prefs = remember { PreferencesManager(context) }
                
                var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }
                var activeUser by remember { mutableStateOf<User?>(null) }
                var activeSimulatedEmail by remember { mutableStateOf<SimulatedEmail?>(null) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Crossfade(
                        targetState = currentScreen,
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            AppScreen.LOGIN -> {
                                LoginScreen(
                                    prefs = prefs,
                                    onNavigateToRegister = {
                                        currentScreen = AppScreen.REGISTER
                                    },
                                    onNavigateToForgotPassword = {
                                        currentScreen = AppScreen.FORGOT_PASSWORD
                                    },
                                    onLoginSuccess = { email ->
                                        // Login succeeds, find the registered profile to show in the Dashboard
                                        // Since email has already been validated, user must exist
                                        val emailStr = email?.recipient ?: prefs.getAllUsers().firstOrNull()?.email ?: ""
                                        activeUser = prefs.getUser(emailStr)
                                        
                                        if (email != null) {
                                            activeSimulatedEmail = email
                                        } else {
                                            Toast.makeText(context, "Login Alert Email Sent Successfully!", Toast.LENGTH_LONG).show()
                                        }
                                        currentScreen = AppScreen.DASHBOARD
                                    }
                                )
                            }
                            
                            AppScreen.REGISTER -> {
                                RegisterScreen(
                                    prefs = prefs,
                                    onNavigateToLogin = {
                                        currentScreen = AppScreen.LOGIN
                                    },
                                    onRegisterSuccess = { email ->
                                        if (email != null) {
                                            activeSimulatedEmail = email
                                        } else {
                                            Toast.makeText(context, "Welcome Email Sent Successfully!", Toast.LENGTH_LONG).show()
                                        }
                                        Toast.makeText(context, "Registration Complete! Please Login.", Toast.LENGTH_LONG).show()
                                        currentScreen = AppScreen.LOGIN
                                    }
                                )
                            }
                            
                            AppScreen.FORGOT_PASSWORD -> {
                                ForgotPasswordScreen(
                                    prefs = prefs,
                                    onNavigateToLogin = {
                                        currentScreen = AppScreen.LOGIN
                                    },
                                    onCodeSent = { email ->
                                        if (email != null) {
                                            activeSimulatedEmail = email
                                        } else {
                                            Toast.makeText(context, "OTP Code Delivered to Inbox!", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    onPasswordChanged = { email ->
                                        if (email != null) {
                                            activeSimulatedEmail = email
                                        } else {
                                            Toast.makeText(context, "Security Alert Email Sent!", Toast.LENGTH_LONG).show()
                                        }
                                        Toast.makeText(context, "Password Changed Successfully!", Toast.LENGTH_LONG).show()
                                        currentScreen = AppScreen.LOGIN
                                    }
                                )
                            }
                            
                            AppScreen.DASHBOARD -> {
                                val user = activeUser
                                if (user != null) {
                                    DashboardScreen(
                                        user = user,
                                        onSignOut = {
                                            activeUser = null
                                            currentScreen = AppScreen.LOGIN
                                            Toast.makeText(context, "Logged Out Securely", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    currentScreen = AppScreen.LOGIN
                                }
                            }
                        }
                    }
                }

                // Interactive Simulator Overlay
                EmailSimulatorDialog(
                    email = activeSimulatedEmail,
                    onDismiss = { activeSimulatedEmail = null }
                )
            }
        }
    }
}
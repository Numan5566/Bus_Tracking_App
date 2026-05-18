package com.example.bus_tracking_app.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bus_tracking_app.data.User
import com.example.bus_tracking_app.utils.SMTPClient

@Composable
fun DashboardScreen(
    user: User,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isMorningTab by remember { mutableStateOf(true) }

    // Pulse animation for the live transit indicator dot
    val infiniteTransition = rememberInfiniteTransition(label = "MapPulse")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseFraction"
    )
    val busMovement by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BusMovement"
    )

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
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Dashboard Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Real-Time Tracking 🚍",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = Color.White
                    )
                }
            }

            // Real-time animated map card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LIVE TRACKING MAP",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F4C81)
                            )
                            Text(
                                text = "Bus 4B - Main Station Path",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        }
                        Badge(containerColor = Color(0xFF2EC4B6)) {
                            Text("LIVE GPS", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Animated Canvas Route Map
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEDF2F4))
                            .border(1.dp, Color(0xFFDFE2E6), RoundedCornerShape(16.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw stops along the route
                            val startStop = Offset(w * 0.15f, h * 0.5f)
                            val midStop = Offset(w * 0.5f, h * 0.3f)
                            val endStop = Offset(w * 0.85f, h * 0.7f)

                            // Winding Bezier Path
                            val path = Path().apply {
                                moveTo(startStop.x, startStop.y)
                                cubicTo(
                                    w * 0.3f, h * 0.2f,
                                    w * 0.4f, h * 0.6f,
                                    midStop.x, midStop.y
                                )
                                cubicTo(
                                    w * 0.6f, h * 0.05f,
                                    w * 0.75f, h * 0.9f,
                                    endStop.x, endStop.y
                                )
                            }

                            // 1. Draw Path Base
                            drawPath(
                                path = path,
                                color = Color(0xFFBDC3C7),
                                style = Stroke(width = 8f)
                            )

                            // 2. Draw active covered path (Indigo glow)
                            drawPath(
                                path = path,
                                color = Color(0xFF0F4C81).copy(alpha = 0.4f),
                                style = Stroke(width = 12f)
                            )

                            // 3. Draw Station Points
                            drawCircle(color = Color(0xFF0F4C81), radius = 12f, center = startStop)
                            drawCircle(color = Color.White, radius = 6f, center = startStop)

                            drawCircle(color = Color(0xFF0F4C81), radius = 12f, center = midStop)
                            drawCircle(color = Color.White, radius = 6f, center = midStop)

                            drawCircle(color = Color(0xFFF4A261), radius = 16f, center = endStop)
                            drawCircle(color = Color.White, radius = 8f, center = endStop)

                            // 4. Draw Pulse Glow for Live Bus
                            val busX = w * (0.15f + (0.7f * busMovement))
                            // Simple curve mapping
                            val busY = h * (0.5f - 0.2f * kotlin.math.sin(busMovement * Math.PI.toFloat()))
                            val busPos = Offset(busX, busY)

                            drawCircle(
                                color = Color(0xFF2EC4B6).copy(alpha = 0.3f),
                                radius = pulseSize,
                                center = busPos
                            )
                            drawCircle(
                                color = Color(0xFF2EC4B6),
                                radius = 8f,
                                center = busPos
                            )
                        }

                        // Text tags over the Map stops
                        Text(
                            text = "Station A",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 24.dp, top = 40.dp)
                        )
                        Text(
                            text = "Model Town Stop",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                        )
                        Text(
                            text = "📍 Campus",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F4C81),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 24.dp, bottom = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = Color(0xFF2EC4B6))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Next Stop in 3.4 km",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        }
                        Text(
                            text = "Speed: 42 km/h",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Transit warning alerts board
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFF856404),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "⚠️ Traffic Alert: Construction at Canal Rd. Bus 4B taking alternative bypass. Delay expected +8 mins.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF856404),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Route details grid (ETA & Distance)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFF4A261))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ESTIMATED ETA", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                        Text("14 Mins", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF2EC4B6))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("DISTANCE REMAINING", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                        Text("6.8 km", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Driver Information Section
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Driver Initials Circle
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F4C81)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MA",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Muhammad Ali",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Badge(containerColor = Color(0xFFFFF3CD)) {
                                Text("⭐ 4.9", color = Color(0xFF856404), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                        Text(
                            text = "Transit Driver (Bus 4B - Plate LH-7892)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Mock Call: Dialing +92 300 7654321...", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .background(Color(0xFF2EC4B6).copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Driver",
                            tint = Color(0xFF2EC4B6)
                        )
                    }
                }
            }

            // Weekly Timetable schedules with Morning/Evening tabs
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ROUTE SCHEDULE TIMETABLE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab selector row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isMorningTab) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { isMorningTab = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Morning Shift", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isMorningTab) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { isMorningTab = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Evening Shift", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Timetable Grid lines
                    if (isMorningTab) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TimetableRow("Shift Start", "07:15 AM", "Model Town Gate")
                            TimetableRow("Campus Arrival", "08:00 AM", "Main Terminal")
                            TimetableRow("Shift 2 Departure", "09:30 AM", "DHA Block Stop")
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TimetableRow("Shift 1 Departure", "01:30 PM", "Main Terminal")
                            TimetableRow("Shift 2 Departure", "04:30 PM", "Main Terminal")
                            TimetableRow("Terminal Return", "05:15 PM", "Model Town Gate")
                        }
                    }
                }
            }

            // Quick Actions Hub
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "QUICK ACTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionTile(
                        icon = Icons.Default.EventSeat,
                        title = "Reserve Seat",
                        backgroundColor = Color(0xFF0F4C81),
                        onClick = { Toast.makeText(context, "Seat Reservation: Requested successfully for tomorrow morning shift!", Toast.LENGTH_LONG).show() },
                        modifier = Modifier.weight(1f)
                    )

                    ActionTile(
                        icon = Icons.Default.ReportProblem,
                        title = "Report Delay",
                        backgroundColor = Color(0xFFF4A261),
                        onClick = { Toast.makeText(context, "Transit Admin notified of missing / delayed bus station check.", Toast.LENGTH_LONG).show() },
                        modifier = Modifier.weight(1f)
                    )

                    ActionTile(
                        icon = Icons.Default.LocalPhone,
                        title = "Transport Office",
                        backgroundColor = Color(0xFF2EC4B6),
                        onClick = { Toast.makeText(context, "Connecting to University Transport dispatcher center...", Toast.LENGTH_LONG).show() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Student profile details card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "ACCOUNT PROFILE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Registration Number:", color = Color.White.copy(alpha = 0.8f))
                        Text(user.regNumber, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("University:", color = Color.White.copy(alpha = 0.8f))
                        Text(user.university, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Region City:", color = Color.White.copy(alpha = 0.8f))
                        Text(user.city, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Verified Device Name:", color = Color.White.copy(alpha = 0.8f))
                        Text(SMTPClient.getDeviceName(), fontWeight = FontWeight.Bold, color = Color(0xFFF4A261))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TimetableRow(
    title: String,
    time: String,
    station: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(station, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        }
        Text(time, color = Color(0xFFF4A261), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
    }
}

@Composable
fun ActionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = modifier
            .height(84.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

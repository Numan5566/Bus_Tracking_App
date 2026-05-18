package com.example.bus_tracking_app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
    var currentTab by remember { mutableStateOf(0) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B132B),
            Color(0xFF1C2541),
            Color(0xFF2C5364)
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1C2541),
                tonalElevation = 8.dp,
                modifier = Modifier.border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.DirectionsBus, contentDescription = "Tracker") },
                    label = { Text("Tracker") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFF4A261),
                        selectedTextColor = Color(0xFFF4A261),
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = Color.White.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Routes") },
                    label = { Text("Routes") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFF4A261),
                        selectedTextColor = Color(0xFFF4A261),
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = Color.White.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Badge, contentDescription = "Transit Pass") },
                    label = { Text("Pass") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFF4A261),
                        selectedTextColor = Color(0xFFF4A261),
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = Color.White.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.ContactSupport, contentDescription = "Support") },
                    label = { Text("Support") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFF4A261),
                        selectedTextColor = Color(0xFFF4A261),
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = Color.White.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when (currentTab) {
                0 -> TrackerTabContent(user = user, onSignOut = onSignOut)
                1 -> RoutesTabContent()
                2 -> TransitPassTabContent(user = user)
                3 -> SupportTabContent()
            }
        }
    }
}

// ==========================================
// 📍 TAB 1: LIVE MAP TRACKER TAB
// ==========================================
@Composable
fun TrackerTabContent(user: User, onSignOut: () -> Unit) {
    val context = LocalContext.current
    var isMorningTab by remember { mutableStateOf(true) }

    // Pulse & Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "TrackerAnims")
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
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BusMovement"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Live Tracking 🚍",
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
                Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
            }
        }

        // Live Canvas Map Card
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
                            text = "LIVE TRANSIT PATH",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F4C81)
                        )
                        Text(
                            text = "Bus 4B - Gulberg Route",
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

                // Canvas Map
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

                        val start = Offset(w * 0.15f, h * 0.5f)
                        val mid = Offset(w * 0.5f, h * 0.3f)
                        val end = Offset(w * 0.85f, h * 0.7f)

                        val path = Path().apply {
                            moveTo(start.x, start.y)
                            cubicTo(w * 0.3f, h * 0.2f, w * 0.4f, h * 0.6f, mid.x, mid.y)
                            cubicTo(w * 0.6f, h * 0.05f, w * 0.75f, h * 0.9f, end.x, end.y)
                        }

                        // Base road stroke
                        drawPath(path = path, color = Color(0xFFBDC3C7), style = Stroke(width = 8f))
                        // Covered road stroke glow
                        drawPath(path = path, color = Color(0xFF0F4C81).copy(alpha = 0.3f), style = Stroke(width = 12f))

                        // Draw stations
                        drawCircle(color = Color(0xFF0F4C81), radius = 10f, center = start)
                        drawCircle(color = Color.White, radius = 5f, center = start)

                        drawCircle(color = Color(0xFF0F4C81), radius = 10f, center = mid)
                        drawCircle(color = Color.White, radius = 5f, center = mid)

                        drawCircle(color = Color(0xFFF4A261), radius = 14f, center = end)
                        drawCircle(color = Color.White, radius = 7f, center = end)

                        // Pulse Live Bus dot
                        val busX = w * (0.15f + (0.7f * busMovement))
                        val busY = h * (0.5f - 0.2f * kotlin.math.sin(busMovement * Math.PI.toFloat()))
                        val busPos = Offset(busX, busY)

                        drawCircle(color = Color(0xFF2EC4B6).copy(alpha = 0.3f), radius = pulseSize, center = busPos)
                        drawCircle(color = Color(0xFF2EC4B6), radius = 8f, center = busPos)
                    }

                    // Canvas Text tags overlay
                    Text("Kalma Stop", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp, top = 36.dp))
                    Text("Model Town", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp))
                    Text("📍 Campus", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F4C81), modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 12.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = Color(0xFF2EC4B6))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Next Stop: Model Town in 2.8 km", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    }
                    Text("Speed: 45 km/h", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }

        // Live Warning Alerts Ticker
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFF856404), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "⚠️ Traffic Alert: Construction work ongoing at Canal Road. Bus 4B taking alternative Canal Bypass. Delay estimated: 10 mins.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF856404),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ETA Details Widgets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF2EC4B6))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("DISTANCE REMAINING", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                    Text("6.8 km", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Driver Info Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(Color(0xFF0F4C81)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("MA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Muhammad Ali", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(containerColor = Color(0xFFFFF3CD)) {
                            Text("⭐ 4.9", color = Color(0xFF856404), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                    Text("Driver assigned to Bus 4B (Plate LH-7892)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                IconButton(
                    onClick = { Toast.makeText(context, "Dialing Driver: +92 300 7654321...", Toast.LENGTH_LONG).show() },
                    modifier = Modifier.background(Color(0xFF2EC4B6).copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF2EC4B6))
                }
            }
        }

        // Quick Actions Hub
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("QUICK ACTION PANEL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
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
                    onClick = { Toast.makeText(context, "Transport office notified of missing/delayed bus station check.", Toast.LENGTH_LONG).show() },
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    icon = Icons.Default.LocalPhone,
                    title = "Support Desk",
                    backgroundColor = Color(0xFF2EC4B6),
                    onClick = { Toast.makeText(context, "Routing to University dispatcher support desk...", Toast.LENGTH_LONG).show() },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Shift Timetable Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("SHIFT TIMETABLE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (isMorningTab) Color.White.copy(alpha = 0.15f) else Color.Transparent).clickable { isMorningTab = true }.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Morning Shifts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (!isMorningTab) Color.White.copy(alpha = 0.15f) else Color.Transparent).clickable { isMorningTab = false }.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Evening Shifts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isMorningTab) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimetableRow("First Departure", "07:15 AM", "Model Town Gate")
                        TimetableRow("Campus Arrival", "08:00 AM", "Main Terminal")
                        TimetableRow("Second Departure", "09:30 AM", "DHA Block Stop")
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimetableRow("First Shift Return", "01:30 PM", "Main Terminal")
                        TimetableRow("Second Shift Return", "04:30 PM", "Main Terminal")
                        TimetableRow("Terminal Return", "05:15 PM", "Model Town Gate")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ==========================================
// 🗺️ TAB 2: ACTIVE TRANSIT ROUTES TAB
// ==========================================
@Composable
fun RoutesTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Active Campus Routes 🗺️", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Filter and explore active university shuttle routes, pick-up stations, and bus designations.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))

        Spacer(modifier = Modifier.height(4.dp))

        RouteExplorerCard(
            routeName = "Route 1: DHA Phase 5 to Campus",
            busNumber = "Bus 4A",
            stops = listOf("DHA H-Block Stop", "Walton Road", "Kalma Chowk", "Model Town", "Campus Terminal"),
            timings = "07:15 AM (Morning) | 04:30 PM (Evening)"
        )

        RouteExplorerCard(
            routeName = "Route 2: Gulberg & Canal Path",
            busNumber = "Bus 4B (Active)",
            stops = listOf("Liberty Roundabout", "Canal Road Stop", "Barkat Market", "Campus Terminal"),
            timings = "07:30 AM (Morning) | 01:30 PM (Evening)"
        )

        RouteExplorerCard(
            routeName = "Route 3: Johar Town & Wapda Town",
            busNumber = "Bus 8C",
            stops = listOf("Wapda Town Chowk", "Shaukat Khanum", "Johar Town G1", "Campus Terminal"),
            timings = "07:00 AM (Morning) | 05:00 PM (Evening)"
        )
    }
}

@Composable
fun RouteExplorerCard(
    routeName: String,
    busNumber: String,
    stops: List<String>,
    timings: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(routeName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Timings: $timings", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                }
                Badge(containerColor = if (busNumber.contains("Active")) Color(0xFF2EC4B6) else Color.White.copy(alpha = 0.2f)) {
                    Text(busNumber, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("STATION PATH STOPS:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFF4A261))
                    Spacer(modifier = Modifier.height(8.dp))

                    stops.forEachIndexed { index, stop ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Box(
                                modifier = Modifier.size(16.dp).clip(CircleShape).background(if (index == stops.size - 1) Color(0xFFF4A261) else Color(0xFF2EC4B6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text((index + 1).toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stop, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 🎫 TAB 3: DIGITAL STUDENT TRANSIT PASS TAB
// ==========================================
@Composable
fun TransitPassTabContent(user: User) {
    var scanned by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Digital Transit Pass 🎫", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.fillMaxWidth())
        Text("Show this NFC-embedded verified student barcode pass to the driver upon boarding the bus.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(10.dp))

        // RFID Glassmorphism Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2541)),
            elevation = CardDefaults.cardElevation(12.dp),
            modifier = Modifier.fillMaxWidth().aspectRatio(1.586f).border(1.dp, Color(0xFFF4A261).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Diagonal accent lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF0F4C81).copy(alpha = 0.3f), Color(0xFFF4A261).copy(alpha = 0.1f))
                        ),
                        size = size
                    )
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STUDENT TRANSIT PASS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = Color(0xFFF4A261)
                        )
                        Badge(containerColor = Color(0xFF2EC4B6).copy(alpha = 0.2f)) {
                            Text("RFID SECURE", color = Color(0xFF2EC4B6), fontWeight = FontWeight.ExtraBold, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }

                    // Card Middle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("ID: ${user.regNumber}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
                        }
                    }

                    // Card Bottom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(user.university, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Region: ${user.city}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        }
                        Text(
                            text = "● ACTIVE PASS",
                            color = Color(0xFF2EC4B6),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Custom QR Code Graphic via Canvas
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.width(220.dp).height(220.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp).clickable { scanned = !scanned },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw 4 corner anchor squares of a QR Code
                    val boxSize = w * 0.22f
                    drawRect(color = Color.Black, topLeft = Offset(0f, 0f), size = Size(boxSize, boxSize))
                    drawRect(color = Color.White, topLeft = Offset(4f, 4f), size = Size(boxSize - 8f, boxSize - 8f))
                    drawRect(color = Color.Black, topLeft = Offset(8f, 8f), size = Size(boxSize - 16f, boxSize - 16f))

                    drawRect(color = Color.Black, topLeft = Offset(w - boxSize, 0f), size = Size(boxSize, boxSize))
                    drawRect(color = Color.White, topLeft = Offset(w - boxSize + 4f, 4f), size = Size(boxSize - 8f, boxSize - 8f))
                    drawRect(color = Color.Black, topLeft = Offset(w - boxSize + 8f, 8f), size = Size(boxSize - 16f, boxSize - 16f))

                    drawRect(color = Color.Black, topLeft = Offset(0f, h - boxSize), size = Size(boxSize, boxSize))
                    drawRect(color = Color.White, topLeft = Offset(4f, h - boxSize + 4f), size = Size(boxSize - 8f, boxSize - 8f))
                    drawRect(color = Color.Black, topLeft = Offset(8f, h - boxSize + 8f), size = Size(boxSize - 16f, boxSize - 16f))

                    // Draw mock barcode noise patterns inside
                    val steps = 8
                    val stepW = w / steps
                    val stepH = h / steps

                    for (x in 2 until steps - 2) {
                        for (y in 2 until steps - 2) {
                            if ((x + y) % 3 == 0 || (x * y) % 5 == 1) {
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(x * stepW + 4f, y * stepH + 4f),
                                    size = Size(stepW - 8f, stepH - 8f)
                                )
                            }
                        }
                    }
                    // Bottom right dots
                    drawRect(color = Color.Black, topLeft = Offset(w - boxSize + 12f, h - boxSize + 12f), size = Size(16f, 16f))
                    drawRect(color = Color.Black, topLeft = Offset(w - boxSize - 8f, h - boxSize - 8f), size = Size(12f, 12f))
                }

                if (scanned) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Checked", tint = Color(0xFF2EC4B6), modifier = Modifier.size(64.dp))
                    }
                }
            }
        }

        Text(
            text = if (scanned) "✓ SCAN COMPLETED SECURELY" else "👆 TAP QR CODE TO MOCK SCAN BOARDING",
            color = if (scanned) Color(0xFF2EC4B6) else Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ==========================================
// 📞 TAB 4: HELP & SUPPORT DESK TAB
// ==========================================
@Composable
fun SupportTabContent() {
    val context = LocalContext.current
    var issueText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Bus Delay") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Transport Support 📞", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Submit transit feedback, report delay issues, or get in touch with the university dispatch desk directly.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))

        Spacer(modifier = Modifier.height(4.dp))

        // Direct Dial Help
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("DIRECT TRANSPORT DESK", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF0F4C81))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Got an emergency or immediate routing query? Call dispatch team directly.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { Toast.makeText(context, "Calling Dispatch: +92 42 111-123-456...", Toast.LENGTH_LONG).show() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F4C81)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Call Transport Office", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Feedback / Report form card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("LODGE AN ISSUE / FEEDBACK", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)

                // Issue Type Dropdown Mock
                Text("Select Category:", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Bus Delay", "Driver Feedback", "Other").forEach { type ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedType == type) Color(0xFFF4A261) else Color.White.copy(alpha = 0.1f))
                                .clickable { selectedType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(type, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = issueText,
                    onValueChange = { issueText = it },
                    label = { Text("Details of the issue", color = Color.White.copy(alpha = 0.6f)) },
                    singleLine = false,
                    maxLines = 4,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF4A261),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = Color(0xFFF4A261),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )

                Button(
                    onClick = {
                        if (issueText.trim().isEmpty()) {
                            Toast.makeText(context, "Please enter some details before submitting!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Feedback logged! Ticket ID: #${(1000..9999).random()} registered in database.", Toast.LENGTH_LONG).show()
                            issueText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EC4B6)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Support Ticket", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Campus Safety Tips Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("CAMPUS TRANSIT SAFETY RULES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("1. Always stand 3 feet away from the curb when the shuttle approaches.", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(vertical = 2.dp))
                Text("2. Display your Digital Pass on the screen to the driver before boarding.", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(vertical = 2.dp))
                Text("3. Cooperate with university transport marshals during boarding hours.", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(vertical = 2.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

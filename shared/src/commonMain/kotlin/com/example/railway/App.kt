package com.example.railway

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.example.railway.domain.model.*
import com.example.railway.presentation.*
import com.example.railway.ui.navigation.*
import com.example.railway.ui.screen.*
import com.example.railway.ui.theme.RailwayTheme
import com.example.railway.util.USDataGenerator
import com.example.railway.util.StateBoundaries
import com.example.railway.ui.component.GlassPanel
import com.example.railway.ui.component.VibrantGradientBackground
import com.example.railway.util.currentTimeMillis
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun App() {
    var isAdminUser by remember { mutableStateOf(false) }
    var isLoggedIn by remember { mutableStateOf(false) }
    val systemDark = isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(systemDark) }

    var currentDestination by remember { mutableStateOf<Destination>(Destination.Login) }
    val navigationStack = remember { mutableStateListOf<Destination>(Destination.Login) }

    RailwayTheme(isAdmin = isAdminUser, darkTheme = darkTheme) {
        VibrantGradientBackground(isAdmin = isAdminUser, isStatic = currentDestination == Destination.Login) {
            // Database and Repository initialization
            val repository = remember { 
                com.example.railway.data.repository.RailwayRepositoryImpl(
                    com.example.railway.db.RailwayDatabase(com.example.railway.util.getDriver())
                ) 
            }

            // ViewModels
            val adminViewModel = remember { AdminViewModel(repository) }
            val adminState by adminViewModel.state.collectAsState()
            
            val trackingViewModel = remember(adminState.stations) { TrackingViewModel(adminState.stations, repository) }
            val trainPositions by trackingViewModel.trainPositions.collectAsState()
            
            val allRoutes by repository.getAllRoutes().collectAsState(emptyList())
            val routeDiscoveryViewModel = remember(allRoutes) { RouteDiscoveryViewModel(allRoutes, repository) }
            val bookingViewModel = remember { BookingViewModel(repository) }
            val historyViewModel = remember { HistoryViewModel(repository) }
            val notificationService = remember { com.example.railway.domain.service.NotificationService() }

            // Single unified seeding effect
            LaunchedEffect(Unit) {
                val currentStations = repository.getAllStations().first()
                val currentStates = repository.getAllStates().first()

                if (currentStations.isEmpty() || currentStates.isEmpty()) {

                    val initialStations = USDataGenerator.generateStations()
                    val initialRoutes = USDataGenerator.generateRoutes(initialStations)
                    val initialTrains = USDataGenerator.generateTrains(initialRoutes)

                    if (currentStations.isEmpty()) {
                        initialStations.forEach { repository.insertStation(it) }
                        initialRoutes.forEach { repository.insertRoute(it) }
                        initialTrains.forEach { train ->
                            repository.insertTrain(train)
                            train.schedule.forEach { repository.insertSchedule(it) }
                        }
                    }
                    
                    StateBoundaries.allStates.forEach { repository.insertState(it) }
                    
                    if (repository.findUserByUsername("admin") == null) {
                        repository.insertUser(User("admin", "admin", "admin", true))
                    }
                }
            }

            LaunchedEffect(adminState.trains, allRoutes) {
                if (adminState.trains.isNotEmpty() && allRoutes.isNotEmpty()) {
                    trackingViewModel.startScheduledTracking(adminState.trains, allRoutes)
                }
            }

            val historyState by historyViewModel.state.collectAsState()
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    while (true) {
                        notificationService.checkAndSendReminders(historyState.bookings, adminState.trains)
                        notificationService.checkAndSendStatusAlerts(adminState.trains, trainPositions)
                        kotlinx.coroutines.delay(30000) // Check every 30 seconds
                    }
                }
            }

            fun navigateTo(destination: Destination) {
                if (currentDestination == destination) return
                navigationStack.add(destination)
                currentDestination = destination
            }

            fun goBack() {
                if (navigationStack.size > 1) {
                    navigationStack.removeAt(navigationStack.size - 1)
                    currentDestination = navigationStack.last()
                }
            }

            fun logout() {
                isAdminUser = false
                isLoggedIn = false
                navigationStack.clear()
                navigationStack.add(Destination.Login)
                currentDestination = Destination.Login
            }

            Box(modifier = Modifier.fillMaxSize()) {
                val showDock = isLoggedIn && currentDestination != Destination.Login

                // Main Content with Smooth Transitions
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (showDock && currentDestination != Destination.RouteDiscovery) 100.dp else 0.dp)
                ) {
                    AnimatedContent(
                        targetState = currentDestination,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.95f, animationSpec = tween(400)))
                                .togetherWith(fadeOut(animationSpec = tween(300)))
                        },
                        label = "screen_transition"
                    ) { destination ->
                        when (destination) {
                            Destination.Login -> LoginScreen(
                                onLoginSuccess = { admin ->
                                    isAdminUser = admin
                                    isLoggedIn = true
                                    navigateTo(Destination.Home)
                                }
                            )
                            Destination.Home -> HomeScreen(
                                onNavigateToDiscovery = { navigateTo(Destination.RouteDiscovery) },
                                onNavigateToLiveTracking = { navigateTo(Destination.LiveTracking) },
                                isDark = darkTheme,
                                showThemeToggle = !isAdminUser,
                                onToggleTheme = { darkTheme = !darkTheme }
                            )
                            Destination.RouteDiscovery -> RouteDiscoveryScreen(
                                viewModel = routeDiscoveryViewModel,
                                stations = adminState.stations,
                                trains = adminState.trains,
                                trainPositions = trainPositions,
                                isAdmin = isAdminUser,
                                isDark = darkTheme,
                                onBack = { goBack() }
                            )
                            Destination.Stations -> StationExplorerScreen(
                                stations = adminState.stations,
                                routes = allRoutes,
                                trains = adminState.trains,
                                onBack = { goBack() }
                            )
                            Destination.LiveTracking -> LiveTrackingScreen(
                                viewModel = trackingViewModel,
                                stations = adminState.stations,
                                trains = adminState.trains,
                                routes = allRoutes,
                                onBack = { goBack() }
                            )
                            Destination.Booking -> BookingScreen(
                                viewModel = bookingViewModel,
                                trains = adminState.trains,
                                stations = adminState.stations,
                                isAdmin = isAdminUser,
                                isDark = darkTheme,
                                onBookingConfirmed = { booking, cardInfo ->
                                    historyViewModel.addBooking(booking)
                                    adminViewModel.addBooking(booking)

                                    // Meticulously Formatted Terminal Alert
                                    val startStation = adminState.stations.find { it.id == booking.startStationId }
                                    val endStation = adminState.stations.find { it.id == booking.endStationId }
                                    val train = adminState.trains.find { it.id == booking.trainId }
                                    
                                    val departureTime = if (booking.departureTimeMillis > 0) {
                                        formatTimeAmPm(Instant.fromEpochMilliseconds(booking.departureTimeMillis).toLocalDateTime(TimeZone.currentSystemDefault()))
                                    } else "N/A"

                                    val arrivalTime = if (booking.arrivalTimeMillis > 0) {
                                        formatTimeAmPm(Instant.fromEpochMilliseconds(booking.arrivalTimeMillis).toLocalDateTime(TimeZone.currentSystemDefault()))
                                    } else "N/A"

                                    val nowMillis = currentTimeMillis()
                                    val now = Instant.fromEpochMilliseconds(nowMillis).toLocalDateTime(TimeZone.currentSystemDefault())
                                    val formattedTime = formatTimeAmPm(now)
                                    val price = booking.price
                                    val issueDate = "${now.month.name} ${now.day}, ${now.year}"

                                    println("""

[🚨 PURCHASE ALERT 🚨]
==================================================
TICKET ID:          ${booking.id}
PASSENGER:          ${booking.passengerName}
USER ID:            ${booking.userId}

ROUTE:              ${startStation?.name ?: "Unknown"} (${booking.startStationId}) 
                    -> ${endStation?.name ?: "Unknown"} (${booking.endStationId})
                                        
TRAIN INFO:         ${train?.name ?: "Unknown"} (ID: ${booking.trainId})
CARRIAGE:           ${booking.carriageId}
SEAT:               ${booking.seatNumber}

DEPARTURE TIME:     $departureTime
DEPARTURE TERMINAL: ${startStation?.terminal ?: "T1"}
ARRIVAL TIME (ETA): $arrivalTime

REGISTRATION TIME:  $formattedTime
ISSUE DATE:         $issueDate

PAYMENT METHOD:     ${booking.paymentMethod}
AMOUNT:             $price
${if (booking.paymentMethod == PaymentMethod.CARD) """
CARD OWNER:         ${booking.passengerName}
CARD NUMBER:        ${cardInfo.number}
EXPIRY DATE:        ${cardInfo.expiryDate}
CVV:                ${cardInfo.cvv}""" else ""}
==================================================
                                    """.trimIndent())
                                }
                            )
                            Destination.History -> HistoryScreen(
                                viewModel = historyViewModel,
                                stations = adminState.stations,
                                trains = adminState.trains,
                                onBack = { goBack() }
                            )
                            Destination.UserCabinet -> UserCabinetScreen(
                                bookingViewModel = bookingViewModel,
                                isDark = darkTheme,
                                onBack = { goBack() }
                            )
                            Destination.AdminDashboard -> AdminDashboardScreen(viewModel = adminViewModel)
                        }
                    }
                }

                // Floating macOS Dock-style Navigation
                if (showDock) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    ) {
                        GlassPanel(cornerRadius = 32.dp) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DockItem(
                                    selected = currentDestination == Destination.Home,
                                    onClick = { navigateTo(Destination.Home) },
                                    icon = Icons.Rounded.Home,
                                    label = "Home"
                                )
                                DockItem(
                                    selected = currentDestination == Destination.RouteDiscovery,
                                    onClick = { navigateTo(Destination.RouteDiscovery) },
                                    icon = Icons.Rounded.Route,
                                    label = "Routes"
                                )
                                DockItem(
                                    selected = currentDestination == Destination.Stations,
                                    onClick = { navigateTo(Destination.Stations) },
                                    icon = Icons.Rounded.Place,
                                    label = "Stations"
                                )
                                DockItem(
                                    selected = currentDestination == Destination.LiveTracking,
                                    onClick = { navigateTo(Destination.LiveTracking) },
                                    icon = Icons.Rounded.TrackChanges,
                                    label = "Tracking"
                                )
                                DockItem(
                                    selected = currentDestination == Destination.Booking,
                                    onClick = { navigateTo(Destination.Booking) },
                                    icon = Icons.Rounded.Book,
                                    label = if (isAdminUser) "Routes Review" else "Booking"
                                )
                                if (!isAdminUser) {
                                    DockItem(
                                        selected = currentDestination == Destination.UserCabinet,
                                        onClick = { navigateTo(Destination.UserCabinet) },
                                        icon = Icons.Rounded.AccountCircle,
                                        label = "Cabinet"
                                    )
                                    DockItem(
                                        selected = currentDestination == Destination.History,
                                        onClick = { navigateTo(Destination.History) },
                                        icon = Icons.Rounded.History,
                                        label = "History"
                                    )
                                }
                                if (isAdminUser) {
                                    DockItem(
                                        selected = currentDestination == Destination.AdminDashboard,
                                        onClick = { navigateTo(Destination.AdminDashboard) },
                                        icon = Icons.Rounded.AdminPanelSettings,
                                        label = "Admin"
                                    )
                                }
                                
                                VerticalDivider(
                                    modifier = Modifier.height(24.dp).padding(horizontal = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                                
                                DockItem(
                                    selected = false,
                                    onClick = { logout() },
                                    icon = Icons.AutoMirrored.Rounded.Logout,
                                    label = "Logout"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatTimeAmPm(dateTime: LocalDateTime): String {
    val hour = dateTime.hour
    val minute = dateTime.minute
    val second = dateTime.second
    val amPm = if (hour < 12) "am" else "pm"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$displayHour:${minute.toString().padStart(2, '0')}:${second.toString().padStart(2, '0')}$amPm"
}

@Composable
fun DockItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "icon_scale"
    )
    
    val color by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        animationSpec = tween(300),
        label = "icon_color"
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon, 
                contentDescription = label, 
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            AnimatedVisibility(
                visible = selected,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(4.dp)
                        .background(color, androidx.compose.foundation.shape.CircleShape)
                )
            }
        }
    }
}

package com.example.railway

import androidx.compose.animation.*
import com.example.railway.db.RailwayDatabase
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
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.example.railway.domain.auth.AuthManager
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.coroutines.launch

@Composable
fun App() {
    // Database and Repository initialization
    val repository = remember { 
        com.example.railway.data.repository.RailwayRepositoryImpl(
            RailwayDatabase(com.example.railway.util.getDriver()),
        ) 
    }
    val authManager = remember { AuthManager(repository) }
    val authState by authManager.state.collectAsState()
    
    var isAdminUser by remember { mutableStateOf(value = false) }
    var isLoggedIn by remember { mutableStateOf(value = false) }
    
    // Sync with AuthManager
    LaunchedEffect(authState) {
        isAdminUser = authState.role == com.example.railway.domain.auth.UserRole.ADMIN
        isLoggedIn = authState.isLoggedIn
    }
    val systemDark = isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(systemDark) }

    var currentLanguage by remember { mutableStateOf("English") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        currentLanguage = repository.getLanguage()
    }

    val strings = when (currentLanguage) {
        "Hawaii" -> com.example.railway.ui.theme.HiStrings
        "Spanish" -> com.example.railway.ui.theme.EsStrings
        "French" -> com.example.railway.ui.theme.FrStrings
        "German" -> com.example.railway.ui.theme.DeStrings
        "Italian" -> com.example.railway.ui.theme.ItStrings
        else -> com.example.railway.ui.theme.EnStrings
    }

    var currentDestination by remember { mutableStateOf<Destination>(Destination.Login) }
    val navigationStack = remember { mutableStateListOf<Destination>(Destination.Login) }

    // Navigation Sync Effect
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navigationStack.clear()
            navigationStack.add(Destination.Login)
            currentDestination = Destination.Login
        }
    }

    CompositionLocalProvider(com.example.railway.ui.theme.LocalRailwayStrings provides strings) {
        RailwayTheme(isAdmin = isAdminUser, darkTheme = darkTheme) {
            VibrantGradientBackground(isAdmin = isAdminUser, isStatic = currentDestination == Destination.Login) {

                // ViewModels
                val adminViewModel = remember { AdminViewModel(repository) }
                val adminState by adminViewModel.state.collectAsState()
                
                val trackingViewModel = remember(adminState.stations) { TrackingViewModel(adminState.stations, repository) }
                val trainPositions by trackingViewModel.trainPositions.collectAsState()
                
                val allRoutes by repository.getAllRoutes().collectAsState(emptyList())
                val routeDiscoveryViewModel = remember(allRoutes) { RouteDiscoveryViewModel(allRoutes, repository) }
                val loginViewModel = remember { LoginViewModel(repository, authManager) }
                val bookingViewModel = remember { BookingViewModel(repository, authManager) }
                val historyViewModel = remember { HistoryViewModel(repository, authManager) }
                val notificationService = com.example.railway.domain.service.NotificationService

                val aiApiService = remember { 
                    com.example.railway.domain.service.OpenRouterApiService(
                        io.ktor.client.HttpClient {
                            install(io.ktor.client.plugins.logging.Logging) {
                                level = io.ktor.client.plugins.logging.LogLevel.INFO
                            }
                            install(ContentNegotiation) {
                                json(Json { ignoreUnknownKeys = true; isLenient = true })
                            }
                            defaultRequest {
                                header("HTTP-Referer", "https://railtrack.pro")
                                header("X-Title", "RailTrack Pro Support")
                            }
                        }
                    )
                }
                val aiSupportRepository = remember { com.example.railway.domain.repository.AiSupportRepository(aiApiService, repository) }
                val supportChatViewModel = remember { SupportChatViewModel(aiSupportRepository, authManager) }

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

                val historyState by historyViewModel.state.collectAsState()

                LaunchedEffect(adminState.trains, allRoutes, historyState.bookings) {
                    if (adminState.trains.isNotEmpty() && allRoutes.isNotEmpty()) {
                        val userBookedTrainIds = historyState.bookings.asSequence().map { it.trainId }.toSet()
                        trackingViewModel.startScheduledTracking(adminState.trains, allRoutes, userBookedTrainIds)
                    }
                }
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        while (true) {
                            notificationService.checkAndSendReminders(historyState.bookings, adminState.trains)
                            notificationService.checkAndSendStatusAlerts(adminState.trains, trainPositions)
                            kotlinx.coroutines.delay(30000.milliseconds) // Check every 30 seconds
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
                    authManager.logout()
                    navigationStack.clear()
                    navigationStack.add(Destination.Login)
                    currentDestination = Destination.Login
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    val showDock = isLoggedIn && (currentDestination != Destination.Login)

                    // Main Content with Smooth Transitions
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = if (showDock && (currentDestination != Destination.RouteDiscovery)) 100.dp else 0.dp)
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
                                    viewModel = loginViewModel,
                                    onLoginSuccess = { _ ->
                                        navigateTo(Destination.Home)
                                    }
                                )
                                Destination.Home -> HomeScreen(
                                    onNavigateToDiscovery = { navigateTo(Destination.RouteDiscovery) },
                                    onNavigateToLiveTracking = { navigateTo(Destination.LiveTracking) },
                                    isDark = darkTheme,
                                    showThemeToggle = !isAdminUser,
                                    onToggleTheme = { darkTheme = !darkTheme },
                                    currentLanguage = currentLanguage,
                                    onLanguageChange = { 
                                        currentLanguage = it
                                        scope.launch { repository.setLanguage(it) }
                                    }
                                )
                                Destination.RouteDiscovery -> RouteDiscoveryScreen(
                                    viewModel = routeDiscoveryViewModel,
                                    stations = adminState.stations,
                                    trains = adminState.trains,
                                    trainPositions = trainPositions,
                                    myTrainIds = historyState.bookings.asSequence().map { it.trainId }.toSet(),
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
                                    onBookingConfirmed = { _, _ ->
                                        // Removed immediate call as it's now handled by the confirmation screen
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
                                    authManager = authManager,
                                    isDark = darkTheme,
                                    onBack = { goBack() }
                                )
                                Destination.SupportChat -> SupportChatScreen(
                                    viewModel = supportChatViewModel,
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
                                        label = strings.home
                                    )
                                    DockItem(
                                        selected = currentDestination == Destination.RouteDiscovery,
                                        onClick = { navigateTo(Destination.RouteDiscovery) },
                                        icon = Icons.Rounded.Route,
                                        label = strings.routes
                                    )
                                    DockItem(
                                        selected = currentDestination == Destination.Stations,
                                        onClick = { navigateTo(Destination.Stations) },
                                        icon = Icons.Rounded.Place,
                                        label = strings.stations
                                    )
                                    DockItem(
                                        selected = currentDestination == Destination.LiveTracking,
                                        onClick = { navigateTo(Destination.LiveTracking) },
                                        icon = Icons.Rounded.TrackChanges,
                                        label = strings.tracking
                                    )
                                    DockItem(
                                        selected = currentDestination == Destination.Booking,
                                        onClick = { navigateTo(Destination.Booking) },
                                        icon = Icons.Rounded.Book,
                                        label = if (isAdminUser) strings.routesReview else strings.booking
                                    )
                                    if (!isAdminUser) {
                                        DockItem(
                                            selected = currentDestination == Destination.UserCabinet,
                                            onClick = { navigateTo(Destination.UserCabinet) },
                                            icon = Icons.Rounded.AccountCircle,
                                            label = strings.cabinet
                                        )
                                        DockItem(
                                            selected = currentDestination == Destination.History,
                                            onClick = { navigateTo(Destination.History) },
                                            icon = Icons.Rounded.History,
                                            label = strings.history
                                        )
                                        DockItem(
                                            selected = currentDestination == Destination.SupportChat,
                                            onClick = { navigateTo(Destination.SupportChat) },
                                            icon = Icons.Rounded.SupportAgent,
                                            label = strings.support
                                        )
                                    }
                                    if (isAdminUser) {
                                        DockItem(
                                            selected = currentDestination == Destination.AdminDashboard,
                                            onClick = { navigateTo(Destination.AdminDashboard) },
                                            icon = Icons.Rounded.AdminPanelSettings,
                                            label = strings.admin
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
                                        label = strings.logout
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatTimeAmPm(dateTime: LocalDateTime, strings: com.example.railway.ui.theme.RailwayStrings): String {
    val hour = dateTime.hour
    val minute = dateTime.minute
    val second = dateTime.second
    val amPm = if (hour < 12) strings.am else strings.pm
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

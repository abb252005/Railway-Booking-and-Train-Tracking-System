package com.example.railway.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.railway.domain.model.*
import com.example.railway.presentation.RouteDiscoveryViewModel
import com.example.railway.presentation.MapSettings
import com.example.railway.util.StateBoundary
import com.example.railway.util.formatDuration
import com.example.railway.ui.component.GlassPanel
import kotlin.math.*
import kotlin.time.Duration.Companion.milliseconds
import org.jetbrains.compose.resources.painterResource
import railway_booking_and_train_tracking_system.shared.generated.resources.Res
import railway_booking_and_train_tracking_system.shared.generated.resources.nature_bg
import railway_booking_and_train_tracking_system.shared.generated.resources.bcg_admin_1
import railway_booking_and_train_tracking_system.shared.generated.resources.bcg_admin_2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDiscoveryScreen(
    viewModel: RouteDiscoveryViewModel,
    stations: List<Station>,
    trains: List<Train>,
    trainPositions: Map<String, TrainPosition>,
    myTrainIds: Set<String> = emptySet(),
    isAdmin: Boolean = false,
    isDark: Boolean = true,
    onBack: () -> Unit,
) {
    val strings = com.example.railway.ui.theme.LocalRailwayStrings.current
    val effectiveIsDark = if (isAdmin) true else isDark
    val state by viewModel.state.collectAsState()
    var selectedStation by remember { mutableStateOf<Station?>(null) }
    var selectedTrainId by remember { mutableStateOf<String?>(null) }
    var selectedRoute by remember { mutableStateOf<Route?>(null) }
    var selectedState by remember { mutableStateOf<StateBoundary?>(null) }
    
    // Selection state
    var showSourceDialog by remember { mutableStateOf(false) }
    var showDestDialog by remember { mutableStateOf(false) }
    var selectedStateId by remember { mutableStateOf<String?>(null) }

    // Pulse animation for selected state
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Zoom and Pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Selection Animation for State Nodes
    val selectionScale = remember { Animatable(1f) }
    LaunchedEffect(selectedStateId) {
        if (selectedStateId != null) {
            selectionScale.snapTo(1f)
            selectionScale.animateTo(1.4f, tween(300, easing = FastOutSlowInEasing))
            selectionScale.animateTo(1.2f, tween(200))
        } else {
            selectionScale.animateTo(1f, tween(200))
        }
    }

    // Admin Background Transition
    var currentBgIndex by remember { mutableIntStateOf(0) }
    val adminBackgrounds = listOf(Res.drawable.bcg_admin_1, Res.drawable.bcg_admin_2)

    LaunchedEffect(isAdmin) {
        if (isAdmin) {
            while (true) {
                kotlinx.coroutines.delay(60000.milliseconds) // 60 seconds
                currentBgIndex = (currentBgIndex + 1) % adminBackgrounds.size
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(strings.routeDiscovery, fontWeight = FontWeight.Black, color = if (effectiveIsDark) Color.White else Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back, tint = if (effectiveIsDark) Color.White else Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = if (effectiveIsDark) Color.White else Color.Black,
                    navigationIconContentColor = if (effectiveIsDark) Color.White else Color.Black
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Background Selection
            if (isAdmin) {
                AnimatedContent(
                    targetState = adminBackgrounds[currentBgIndex],
                    transitionSpec = {
                        fadeIn(animationSpec = tween(3000)) togetherWith 
                                fadeOut(animationSpec = tween(3000))
                    },
                    label = "admin_bg_transition",
                    modifier = Modifier.fillMaxSize()
                ) { bgRes ->
                    Image(
                        painter = painterResource(bgRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().blur(30.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                // Blurred Nature Background for regular users
                Image(
                    painter = painterResource(Res.drawable.nature_bg),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(20.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Dark/Light Overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (effectiveIsDark) Color.Black.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.4f))
            )

            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                InteractiveMap(
                    stations = stations,
                    routes = state.calculatedPath,
                    allRoutes = viewModel.allRoutes,
                    trainPositions = trainPositions,
                    stateBoundaries = state.stateBoundaries,
                    myTrainIds = myTrainIds,
                    isAdmin = isAdmin,
                    isDark = effectiveIsDark,
                    scale = scale,
                    offset = offset,
                    selectedStateId = selectedStateId,
                    selectionScale = selectionScale.value,
                    pulseScale = pulseScale,
                    pulseAlpha = pulseAlpha,
                    onOffsetChange = { offset += it },
                    onStationClick = { selectedStation = it },
                    onTrainClick = { selectedTrainId = it },
                    onRouteClick = { selectedRoute = it },
                    onStateClick = {
                        selectedStateId = it.name
                        selectedState = it
                    },
                    mapSettings = state.mapSettings
                )

                // Discovery Controls Overlay (Premium Redesign)
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(24.dp)
                        .width(700.dp),
                    elevation = CardDefaults.cardElevation(12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = if (effectiveIsDark) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.8f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                SelectionButton(
                                    label = state.sourceStation?.name ?: strings.selectSourceStation,
                                    icon = Icons.Rounded.LocationOn,
                                    isDark = effectiveIsDark,
                                    onClick = { showSourceDialog = true }
                                )
                            }
                            
                            Icon(
                                Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = (if (effectiveIsDark) Color.White else Color.Black).copy(alpha = 0.3f),
                                modifier = Modifier.size(16.dp)
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                SelectionButton(
                                    label = state.destinationStation?.name ?: strings.selectDestinationStation,
                                    icon = Icons.Rounded.Flag,
                                    isDark = effectiveIsDark,
                                    onClick = { showDestDialog = true }
                                )
                            }
                        }
                        
                        if (state.calculatedPath.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = if (effectiveIsDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Filter Selection
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                com.example.railway.domain.service.RouteCriteria.entries.forEach { criteria ->
                                    FilterChip(
                                        selected = state.criteria == criteria,
                                        onClick = { viewModel.setCriteria(criteria) },
                                        label = { 
                                            val label = when(criteria) {
                                                com.example.railway.domain.service.RouteCriteria.DURATION -> strings.sortDuration
                                                com.example.railway.domain.service.RouteCriteria.PRICE -> strings.sortPrice
                                                com.example.railway.domain.service.RouteCriteria.DEPARTURE -> strings.sortDeparture
                                            }
                                            Text(label, fontSize = 10.sp) 
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            selectedLabelColor = if (effectiveIsDark) Color.White else Color.Black,
                                            labelColor = (if (effectiveIsDark) Color.White else Color.Black).copy(alpha = 0.6f)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = state.criteria == criteria,
                                            borderColor = (if (effectiveIsDark) Color.White else Color.Black).copy(alpha = 0.1f),
                                            selectedBorderColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${strings.distance}${strings.colonSeparator}${state.totalDistance.toInt()} ${strings.km}", style = MaterialTheme.typography.labelSmall, color = if (effectiveIsDark) Color.White else Color.Black)
                                Text("${strings.time}${strings.colonSeparator}${formatDuration(state.totalTimeMinutes.toDouble(), strings)}", style = MaterialTheme.typography.labelSmall, color = if (effectiveIsDark) Color.White else Color.Black)
                            }
                        }
                    }
                }

                // Zoom Controls (Glass Redesign)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MapLayersMenu(
                        settings = state.mapSettings,
                        onSettingsChange = { viewModel.updateMapSettings(it) },
                        isDark = effectiveIsDark
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    GlassZoomButton(
                        icon = Icons.Rounded.Add,
                        onClick = { scale *= 1.2f },
                        isDark = effectiveIsDark
                    )
                    GlassZoomButton(
                        icon = Icons.Rounded.Remove,
                        onClick = { scale = (scale / 1.2f).coerceAtLeast(1f) },
                        isDark = effectiveIsDark
                    )
                }

                if (selectedStation != null) {
                    StationInfoWindow(
                        station = selectedStation!!,
                        allTrains = trains,
                        stations = stations,
                        isDark = effectiveIsDark,
                        onDismiss = { selectedStation = null }
                    )
                }

                if (selectedTrainId != null) {
                    val train = trains.find { it.id == selectedTrainId }
                    val position = trainPositions[selectedTrainId]
                    if (train != null && position != null) {
                        TrainInfoWindow(
                            train = train,
                            position = position,
                            stations = stations,
                            strings = strings,
                            isDark = effectiveIsDark,
                            onDismiss = { selectedTrainId = null }
                        )
                    }
                }

                if (selectedRoute != null) {
                    RouteInfoWindow(
                        route = selectedRoute!!,
                        allTrains = trains,
                        stations = stations,
                        strings = strings,
                        isDark = effectiveIsDark,
                        onDismiss = { selectedRoute = null }
                    )
                }

                if (selectedState != null) {
                    StateTransitDashboard(
                        stateBoundary = selectedState!!,
                        allTrains = trains,
                        trainPositions = trainPositions,
                        stations = stations,
                        allRoutes = viewModel.allRoutes,
                        strings = strings,
                        isDark = effectiveIsDark,
                        onDismiss = { 
                            selectedState = null
                            selectedStateId = null
                        }
                    )
                }
            }
        }
    }

    if (showSourceDialog) {
        StationSelectionDialog(
            stations = stations,
            title = strings.selectStartStation,
            isDark = effectiveIsDark,
            onDismiss = { showSourceDialog = false },
            onSelect = {
                viewModel.setSource(it)
                showSourceDialog = false
            }
        )
    }

    if (showDestDialog) {
        StationSelectionDialog(
            stations = stations,
            title = strings.selectEndStation,
            isDark = effectiveIsDark,
            onDismiss = { showDestDialog = false },
            onSelect = {
                viewModel.setDestination(it)
                showDestDialog = false
            }
        )
    }
}

@Composable
fun StateTransitDashboard(
    stateBoundary: StateBoundary,
    allTrains: List<Train>,
    trainPositions: Map<String, TrainPosition>,
    stations: List<Station>,
    allRoutes: List<Route>,
    strings: com.example.railway.ui.theme.RailwayStrings,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val textColor = if (isDark) Color.White else Color.Black
    val stationsInState = stations.filter { it.state == stateBoundary.abbreviation || it.name.contains(stateBoundary.name) ||
        sqrt((it.latitude - stateBoundary.centroid.first).pow(2) + (it.longitude - stateBoundary.centroid.second).pow(2)) < 3.0
    }
    val stationIds = stationsInState.asSequence().map { it.id }.toSet()

    // Dynamic Stats
    val activeTrainsInState = trainPositions.values.filter { pos ->
        val route = allRoutes.find { it.id == pos.currentRouteId }
        stationIds.contains(route?.sourceStationId) || stationIds.contains(route?.destinationStationId)
    }

    val incomingTrains = allTrains.flatMap { t -> t.schedule }.filter { s -> 
        stationIds.contains(s.destinationStationId) && !stationIds.contains(s.sourceStationId)
    }.size

    val outgoingTrains = allTrains.flatMap { t -> t.schedule }.filter { s -> 
        stationIds.contains(s.sourceStationId) && !stationIds.contains(s.destinationStationId)
    }.size

    // Simulated Statistics based on state data for visual richness
    val seed = stateBoundary.name.hashCode().toLong()
    val random = kotlin.random.Random(seed)
    val dailyPassengers = (50000 + random.nextInt(200000)).toString()
    val cargoVolume = (1000 + random.nextInt(5000)).toString()
    val delayedCount = random.nextInt(5).toString()
    val onTimePercent = (95.0 + random.nextDouble() * 4.5).toString().take(4) + "%"
    val avgSpeed = (120 + random.nextInt(80)).toString() + " " + strings.kmh

    Dialog(onDismissRequest = onDismiss) {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.9f),
            exit = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.9f)
        ) {
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f)
                    .padding(8.dp),
                opacity = 0.95f,
                cornerRadius = 32.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Hub, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Column {
                                Text(
                                    text = stateBoundary.name,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    color = textColor
                                )
                                Text(
                                    text = "${stateBoundary.abbreviation} ${strings.pipeSeparator} Regional Transit Hub",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.background(textColor.copy(alpha = 0.1f), CircleShape)) {
                            Icon(Icons.Rounded.Close, contentDescription = strings.close, tint = textColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // State Information
                        DashboardCard(
                            title = "State Information",
                            isDark = isDark,
                            modifier = Modifier.weight(0.85f)
                        ) {
                            DetailRow(strings.stateAbbreviation, stateBoundary.abbreviation, isDark)
                            DetailRow(strings.population, stateBoundary.population, isDark)
                            DetailRow(strings.stations, stationsInState.size.toString(), isDark)
                            DetailRow(strings.trainsActive, activeTrainsInState.size.toString(), isDark)
                            DetailRow(strings.incomingTrains, incomingTrains.toString(), isDark)
                            DetailRow(strings.outgoingTrains, outgoingTrains.toString(), isDark)
                        }

                        // Transit Statistics
                        DashboardCard(
                            title = strings.transitStatistics,
                            isDark = isDark,
                            modifier = Modifier.weight(1.15f)
                        ) {
                            DetailRow(strings.dailyPassengers, dailyPassengers, isDark)
                            DetailRow(strings.cargoVolume, cargoVolume, isDark)
                            DetailRow(strings.delayedTrains, delayedCount, isDark)
                            DetailRow(strings.onTimePercentage, onTimePercent, isDark, color = Color(0xFF32D74B))
                            DetailRow("Average Speed", avgSpeed, isDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Active Trains Section
                    Text(strings.activeTrainsSection, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (activeTrainsInState.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = textColor.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(strings.noActivity, color = textColor.copy(alpha = 0.4f), modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center)
                        }
                    } else {
                        activeTrainsInState.forEach { pos ->
                            val train = allTrains.find { it.id == pos.trainId }
                            val currentStation = stations.find { it.name.contains(stateBoundary.name) }?.name ?: strings.unknown
                            val nextStation = stations.find { it.id == pos.nextDestinationStationId }?.name ?: strings.unknown

                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                color = textColor.copy(alpha = 0.07f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, textColor.copy(alpha = 0.1f))
                            ) {
                                Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(train?.name ?: strings.unknown, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = textColor)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                                Text(pos.trainId, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                            Column {
                                                Text("Current Station", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.4f))
                                                Text(currentStation, style = MaterialTheme.typography.bodyMedium, color = textColor)
                                            }
                                            Column {
                                                Text("Next Station", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.4f))
                                                Text(nextStation, style = MaterialTheme.typography.bodyMedium, color = textColor)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            TrainInfoChip(strings.status, strings.onTime, Color(0xFF32D74B), isDark)
                                            TrainInfoChip(strings.direction, if (pos.progress > 0.5) "NORTHBOUND" else "SOUTHBOUND", textColor.copy(alpha = 0.6f), isDark)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${pos.speedKmH.toInt()} ${strings.kmh}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 22.sp)
                                        Text("${strings.eta}: ${formatDuration(pos.estimatedTimeRemainingMinutes)}", style = MaterialTheme.typography.labelMedium, color = textColor.copy(alpha = 0.7f))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Live Transit Routes
                    Text(strings.liveTransitRoutes, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    val stateRoutes = allRoutes.filter { r -> stationIds.contains(r.sourceStationId) || stationIds.contains(r.destinationStationId) }
                    if (stateRoutes.isEmpty()) {
                        Text(strings.noUpcomingTrips, color = textColor.copy(alpha = 0.4f))
                    } else {
                        stateRoutes.take(6).forEach { route ->
                            val s1 = stations.find { it.id == route.sourceStationId }?.name ?: strings.unknown
                            val s2 = stations.find { it.id == route.destinationStationId }?.name ?: strings.unknown
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                color = textColor.copy(alpha = 0.04f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("$s1 ${strings.routeArrow} $s2", style = MaterialTheme.typography.bodyLarge, color = textColor, fontWeight = FontWeight.Bold)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Straighten, contentDescription = null, tint = textColor.copy(alpha = 0.3f), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${route.distance.toInt()} ${strings.km}", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Icon(Icons.Rounded.Schedule, contentDescription = null, tint = textColor.copy(alpha = 0.3f), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${route.estimatedTimeMinutes} ${strings.mins}", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
                                        }
                                    }
                                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = textColor.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String, 
    isDark: Boolean, 
    modifier: Modifier = Modifier, 
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = (if (isDark) Color.White else Color.Black).copy(alpha = 0.1f)
    Surface(
        modifier = modifier,
        color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.04f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun TrainInfoChip(label: String, value: String, color: Color, isDark: Boolean) {
    val textColor = (if (isDark) Color.White else Color.Black).copy(alpha = 0.8f)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text("$label: $value", style = MaterialTheme.typography.labelSmall, color = textColor)
    }
}

@Composable
fun InteractiveMap(
    stations: List<Station>,
    routes: List<Route>,
    allRoutes: List<Route>,
    trainPositions: Map<String, TrainPosition>,
    stateBoundaries: List<StateBoundary>,
    myTrainIds: Set<String> = emptySet(),
    isAdmin: Boolean,
    isDark: Boolean,
    scale: Float,
    offset: Offset,
    selectedStateId: String? = null,
    selectionScale: Float = 1f,
    pulseScale: Float = 1f,
    pulseAlpha: Float = 0f,
    onOffsetChange: (Offset) -> Unit,
    onStationClick: (Station) -> Unit,
    onTrainClick: (String) -> Unit,
    onRouteClick: (Route) -> Unit,
    onStateClick: (StateBoundary) -> Unit,
    mapSettings: MapSettings
) {
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = if (isDark) Color.White else Color.Black
    val density = androidx.compose.ui.platform.LocalDensity.current

    // Stable references to frequently changing data to prevent pointerInput restarts
    val currentStations by rememberUpdatedState(stations)
    val currentTrainPositions by rememberUpdatedState(trainPositions)
    val currentAllRoutes by rememberUpdatedState(allRoutes)
    val currentStateBoundaries by rememberUpdatedState(stateBoundaries)
    val currentScale by rememberUpdatedState(scale)
    val currentOffset by rememberUpdatedState(offset)

    var hoveredStateId by remember { mutableStateOf<String?>(null) }
    val hoverAlpha = remember { Animatable(0f) }

    LaunchedEffect(hoveredStateId) {
        if (hoveredStateId != null) {
            hoverAlpha.animateTo(0.6f, tween(150, easing = EaseOutExpo))
        } else {
            hoverAlpha.animateTo(0f, tween(300, easing = EaseInExpo))
        }
    }

    // Helper functions for projection
    fun projectLocal(lat: Double, lng: Double, width: Float, height: Float): Offset {
        val isAlaska = lat > 50.0 && lng < -125.0
        val isHawaii = lat < 24.0 && lng < -125.0
        if (isAlaska) {
            val aLatMin = 54.0; val aLatMax = 71.5
            val aLngMin = -170.0; val aLngMax = -130.0
            val px = (lng - aLngMin) / (aLngMax - aLngMin) * 0.15f * width
            val py = (1f - (lat - aLatMin) / (aLatMax - aLatMin)) * 0.15f * height + (0.8f * height)
            return Offset(px.toFloat(), py.toFloat())
        }
        if (isHawaii) {
            val hLatMin = 18.5; val hLatMax = 22.5
            val hLngMin = -160.0; val hLngMax = -154.0
            val px = (lng - hLngMin) / (hLngMax - hLngMin) * 0.1f * width + (0.18f * width)
            val py = (1f - (lat - hLatMin) / (hLatMax - hLatMin)) * 0.1f * height + (0.85f * height)
            return Offset(px.toFloat(), py.toFloat())
        }
        val latMin = 24.0; val latMax = 50.0
        val lngMin = -125.0; val lngMax = -66.0
        val pad = 0.05f
        val px = (((lng - lngMin) / (lngMax - lngMin)) * (1f - 2*pad) + pad).toFloat() * width
        val py = ((1f - ((lat - latMin) / (latMax - latMin))) * (1f - 2*pad) + pad).toFloat() * height
        return Offset(px, py)
    }

    fun projectToScreen(lat: Double, lng: Double, width: Float, height: Float, currentScale: Float, currentOffset: Offset): Offset {
        val local = projectLocal(lat, lng, width, height)
        return Offset(local.x * currentScale + currentOffset.x, local.y * currentScale + currentOffset.y)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Move) {
                                val pointerPosition = event.changes.first().position
                                val hovered = currentStateBoundaries.find { sb ->
                                    val pos = projectToScreen(sb.centroid.first, sb.centroid.second, width, height, currentScale, currentOffset)
                                    val distSq = (pointerPosition.x - pos.x).pow(2) + (pointerPosition.y - pos.y).pow(2)
                                    val hitRadius = with(density) { 48.dp.toPx() }
                                    distSq < hitRadius * hitRadius
                                }
                                hoveredStateId = hovered?.name
                            } else if (event.type == PointerEventType.Exit) {
                                hoveredStateId = null
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        // 1. Check State Centers (Consistent Hit Radius: 48DP)
                        val clickedStateCenter = currentStateBoundaries.find { sb ->
                            val pos = projectToScreen(sb.centroid.first, sb.centroid.second, width, height, currentScale, currentOffset)
                            val distSq = (tapOffset.x - pos.x).pow(2) + (tapOffset.y - pos.y).pow(2)
                            val hitRadius = with(density) { 48.dp.toPx() }
                            distSq < hitRadius * hitRadius
                        }
                        if (clickedStateCenter != null) {
                            onStateClick(clickedStateCenter)
                            return@detectTapGestures
                        }

                        // 2. Check Stations
                        val clickedStation = currentStations.find { station ->
                            val pos = projectToScreen(station.latitude, station.longitude, width, height, currentScale, currentOffset)
                            val distSq = (tapOffset.x - pos.x).pow(2) + (tapOffset.y - pos.y).pow(2)
                            val hitRadius = with(density) { 32.dp.toPx() }
                            distSq < hitRadius * hitRadius
                        }
                        if (clickedStation != null) {
                            onStationClick(clickedStation)
                            return@detectTapGestures
                        }

                        // 3. Check Active Trains
                        val tappedTrainId = currentTrainPositions.entries.find { (_, pos) ->
                            val route = currentAllRoutes.find { it.id == pos.currentRouteId }
                            val s1 = currentStations.find { it.id == route?.sourceStationId }
                            val s2 = currentStations.find { it.id == route?.destinationStationId }
                            if (s1 != null && s2 != null) {
                                val p1 = projectToScreen(s1.latitude, s1.longitude, width, height, currentScale, currentOffset)
                                val p2 = projectToScreen(s2.latitude, s2.longitude, width, height, currentScale, currentOffset)
                                val trainX = p1.x + (p2.x - p1.x) * pos.progress.toFloat()
                                val trainY = p1.y + (p2.y - p1.y) * pos.progress.toFloat()
                                val distSq = (tapOffset.x - trainX).pow(2) + (tapOffset.y - trainY).pow(2)
                                val hitRadius = with(density) { 36.dp.toPx() }
                                distSq < hitRadius * hitRadius
                            } else false
                        }?.key
                        if (tappedTrainId != null) {
                            onTrainClick(tappedTrainId)
                            return@detectTapGestures
                        }

                        // 4. Check Routes
                        val localTap = Offset((tapOffset.x - currentOffset.x) / currentScale, (tapOffset.y - currentOffset.y) / currentScale)
                        val clickedRoute = currentAllRoutes.find { route ->
                            val s1 = currentStations.find { it.id == route.sourceStationId }
                            val s2 = currentStations.find { it.id == route.destinationStationId }
                            if (s1 != null && s2 != null) {
                                val p1 = projectLocal(s1.latitude, s1.longitude, width, height)
                                val p2 = projectLocal(s2.latitude, s2.longitude, width, height)
                                val tolerance = 15f / currentScale.coerceAtLeast(0.5f)
                                distanceToSegment(localTap, p1, p2) < tolerance
                            } else false
                        }
                        if (clickedRoute != null) {
                            onRouteClick(clickedRoute)
                            return@detectTapGestures
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onOffsetChange(dragAmount)
                        }
                    )
                }
        ) {
            fun project(lat: Double, lng: Double): Offset = projectLocal(lat, lng, size.width, size.height)

            withTransform({
                translate(offset.x, offset.y)
                scale(scale, scale, pivot = Offset.Zero)
            }) {
                // Draw State Outlines (Adaptive Styling)
                if (mapSettings.showStateBoundaries) {
                    stateBoundaries.forEach { state ->
                        if (state.points.isNotEmpty()) {
                            val path = Path().apply {
                                state.points.forEachIndexed { index, (lat, lng) ->
                                    val pos = project(lat, lng)
                                    if (index == 0) moveTo(pos.x, pos.y) else lineTo(pos.x, pos.y)
                                }
                                close()
                            }
                            
                            val bounds = path.getBounds()
                            if (bounds.width > 0 && bounds.height > 0) {
                                val brush = if (isAdmin) {
                                    // Admin: Green gradient with lime hue (Always Dark)
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF002200).copy(alpha = 0.8f), Color(0xFF00FF41).copy(alpha = 0.4f)),
                                        start = bounds.topLeft,
                                        end = bounds.bottomRight
                                    )
                                } else {
                                    // User: Sophisticated Grayscale
                                    val colorStart = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF0F0F0)
                                    val colorEnd = if (isDark) Color(0xFF121212) else Color(0xFFE0E0E0)
                                    Brush.linearGradient(
                                        colors = listOf(colorStart, colorEnd),
                                        start = bounds.topLeft,
                                        end = bounds.bottomRight
                                    )
                                }
                                
                                val finalAlpha = if (mapSettings.focusMyJourney) 0.3f else 1f
                                drawPath(path = path, brush = brush, style = Fill, alpha = finalAlpha)
                            }

                            val strokeColor = if (isAdmin) {
                                // Admin: Neon Green edges
                                Color(0xFF00FF41).copy(alpha = 0.8f)
                            } else {
                                // User: Subtle Grayscale edges
                                if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
                            }

                            drawPath(
                                path = path,
                                color = strokeColor,
                                style = Stroke(width = (if (isAdmin) 1.dp else 0.7.dp).toPx() / scale)
                            )
                        }
                    }
                }

                // Draw all routes (background)
                if (mapSettings.showInfrastructure) {
                    allRoutes.forEach { route ->
                        val s1 = stations.find { it.id == route.sourceStationId }
                        val s2 = stations.find { it.id == route.destinationStationId }
                        if (s1 != null && s2 != null) {
                            val isHighlighted = routes.any { it.id == route.id }
                            val alpha = when {
                                isHighlighted -> 0f // Handled by calculated path drawing below
                                mapSettings.focusMyJourney -> 0.02f
                                else -> 0.1f
                            }
                            
                            if (alpha > 0) {
                                drawLine(
                                    color = onSurfaceColor.copy(alpha = alpha),
                                    start = project(s1.latitude, s1.longitude),
                                    end = project(s2.latitude, s2.longitude),
                                    strokeWidth = (0.5.dp / scale).toPx()
                                )
                            }
                        }
                    }
                }

                // Draw active train tracking (Red Dashed Lines)
                if (mapSettings.showActiveTrains) {
                    trainPositions.values.forEach { pos ->
                        val isMyTrain = myTrainIds.contains(pos.trainId)
                        if (mapSettings.focusMyJourney && !isMyTrain) return@forEach
                        
                        val route = allRoutes.find { it.id == pos.currentRouteId }
                        val s2 = stations.find { it.id == route?.destinationStationId }
                        
                        if (s2 != null) {
                            val trainPos = project(pos.latitude, pos.longitude)
                            val destPos = project(s2.latitude, s2.longitude)
                            
                            drawLine(
                                color = (if (isMyTrain) Color(0xFF32D74B) else Color.Red).copy(alpha = 0.6f),
                                start = trainPos,
                                end = destPos,
                                strokeWidth = (2.dp / scale).toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // Draw calculated path
                routes.forEach { route ->
                    val s1 = stations.find { it.id == route.sourceStationId }
                    val s2 = stations.find { it.id == route.destinationStationId }
                    if (s1 != null && s2 != null) {
                        drawLine(
                            color = tertiaryColor,
                            start = project(s1.latitude, s1.longitude),
                            end = project(s2.latitude, s2.longitude),
                            strokeWidth = (4.dp / scale).toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Draw stations
                if (mapSettings.showInfrastructure) {
                    stations.forEach { station ->
                        val isSource = station.id == routes.firstOrNull()?.sourceStationId
                        val isDest = station.id == routes.lastOrNull()?.destinationStationId
                        val isPart = routes.any { it.sourceStationId == station.id || it.destinationStationId == station.id }
                        
                        val alpha = when {
                            isSource || isDest || isPart -> 1f
                            mapSettings.focusMyJourney -> 0.05f
                            else -> 0.8f
                        }
                        
                        if (alpha > 0.01f) {
                            val pos = project(station.latitude, station.longitude)
                            drawCircle(
                                color = onSurfaceColor.copy(alpha = alpha),
                                radius = (4.dp / scale).toPx(), 
                                center = pos
                            )
                        }
                    }
                }

                // Draw active trains (Red Arrows)
                if (mapSettings.showActiveTrains) {
                    trainPositions.values.forEach { pos ->
                        val isMyTrain = myTrainIds.contains(pos.trainId)
                        if (mapSettings.focusMyJourney && !isMyTrain) return@forEach

                        val route = allRoutes.find { it.id == pos.currentRouteId }
                        val s1 = stations.find { it.id == route?.sourceStationId }
                        val s2 = stations.find { it.id == route?.destinationStationId }
                        
                        if (s1 != null && s2 != null) {
                            val p1 = project(s1.latitude, s1.longitude)
                            val p2 = project(s2.latitude, s2.longitude)
                            
                            val trainX = p1.x + (p2.x - p1.x) * pos.progress.toFloat()
                            val trainY = p1.y + (p2.y - p1.y) * pos.progress.toFloat()
                            
                            val angle = atan2(p2.y - p1.y, p2.x - p1.x) * (180 / PI).toFloat()
                            
                            withTransform(
                                {
                                translate(trainX, trainY)
                                rotate(angle, Offset.Zero)
                            }) {
                                val arrowSize = (10.dp / scale).toPx()
                                val path = Path().apply {
                                    moveTo(arrowSize, 0f)
                                    lineTo(-arrowSize, -arrowSize * 0.7f)
                                    lineTo(-arrowSize, arrowSize * 0.7f)
                                    close()
                                }
                                drawPath(
                                    path = path,
                                    color = if (isMyTrain) Color(0xFF32D74B) else Color.Red
                                )
                            }

                            if (isMyTrain) {
                                drawCircle(
                                    color = Color(0xFF32D74B).copy(alpha = 0.3f),
                                    radius = (24.dp / scale).toPx(),
                                    center = Offset(trainX, trainY)
                                )
                            }
                        }
                    }
                }

                // Draw state center dots (White Dots with Enhanced Interactivity)
                if (mapSettings.showStateBoundaries) {
                    stateBoundaries.forEach { state ->
                        val pos = project(state.centroid.first, state.centroid.second)
                        val isSelected = state.name == selectedStateId
                        val isHovered = state.name == hoveredStateId
                        
                        val alpha = if (mapSettings.focusMyJourney) 0.1f else 0.8f

                        if (isSelected || isHovered) {
                            // Glowing background/selection ring
                            val glowRadius = if (isSelected) (35.dp / scale).toPx() * selectionScale else (30.dp / scale).toPx()
                            val targetAlpha = if (isSelected) 0.5f else hoverAlpha.value

                            // Layered glassy glow
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(primaryColor.copy(alpha = targetAlpha), primaryColor.copy(alpha = targetAlpha * 0.4f), Color.Transparent),
                                    center = pos,
                                    radius = glowRadius
                                ),
                                radius = glowRadius,
                                center = pos
                            )

                            if (isSelected) {
                                // Animated Selection Ring & Pulse
                                drawCircle(
                                    color = Color.White.copy(alpha = pulseAlpha),
                                    radius = (18.dp / scale).toPx() * pulseScale * selectionScale,
                                    center = pos
                                )
                                drawCircle(
                                    color = primaryColor.copy(alpha = 0.4f),
                                    radius = (14.dp / scale).toPx() * selectionScale,
                                    center = pos
                                )
                            } else if (isHovered) {
                                // Hover glass ring
                                drawCircle(
                                    color = Color.White.copy(alpha = hoverAlpha.value * 0.5f),
                                    radius = (12.dp / scale).toPx(),
                                    center = pos,
                                    style = Stroke(width = (2.dp / scale).toPx())
                                )
                            }
                        }

                        // Core Dot with conditional styling
                        drawCircle(
                            color = (if (isSelected) primaryColor else if (isHovered) Color.White else Color.White).copy(alpha = if (isSelected || isHovered) 1f else alpha),
                            radius = (if (isSelected) 9.dp else if (isHovered) 8.dp else 5.dp).toPx() / scale * (if (isSelected) selectionScale else 1f),
                            center = pos
                        )
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.6f),
                            radius = (if (isSelected) 9.dp else if (isHovered) 8.dp else 5.dp).toPx() / scale * (if (isSelected) selectionScale else 1f),
                            center = pos,
                            style = Stroke(width = (1.5.dp / scale).toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MapLayersMenu(
    settings: MapSettings,
    onSettingsChange: (MapSettings) -> Unit,
    isDark: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val textColor = if (isDark) Color.White else Color.Black

    Box(contentAlignment = Alignment.BottomEnd) {
        if (expanded) {
            GlassPanel(
                modifier = Modifier.width(240.dp).padding(bottom = 64.dp),
                cornerRadius = 24.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Map Layers", fontWeight = FontWeight.Black, color = textColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LayerToggle("Infrastructure", settings.showInfrastructure, isDark) {
                        onSettingsChange(settings.copy(showInfrastructure = it))
                    }
                    LayerToggle("Active Trains", settings.showActiveTrains, isDark) {
                        onSettingsChange(settings.copy(showActiveTrains = it))
                    }
                    LayerToggle("State Boundaries", settings.showStateBoundaries, isDark) {
                        onSettingsChange(settings.copy(showStateBoundaries = it))
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = textColor.copy(alpha = 0.1f))
                    
                    LayerToggle("Focus My Journey", settings.focusMyJourney, isDark, icon = Icons.Rounded.MyLocation) {
                        onSettingsChange(settings.copy(focusMyJourney = it))
                    }
                }
            }
        }

        Surface(
            onClick = { expanded = !expanded },
            shape = CircleShape,
            color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.15f),
            contentColor = textColor,
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                Icon(if (expanded) Icons.Rounded.Close else Icons.Rounded.Layers, contentDescription = null)
            }
        }
    }
}

@Composable
fun LayerToggle(
    label: String,
    checked: Boolean,
    isDark: Boolean,
    icon: ImageVector? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    val textColor = if (isDark) Color.White else Color.Black
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(label, style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.8f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.7f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun StationSelectionDialog(
    stations: List<Station>,
    title: String,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSelect: (Station) -> Unit
) {
    val textColor = if (isDark) Color.White else Color.Black
    Dialog(onDismissRequest = onDismiss) {
        GlassPanel(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.height(400.dp)) {
                    items(stations) { station ->
                        ListItem(
                            headlineContent = { Text(station.name, color = textColor) },
                            modifier = Modifier.clickable { onSelect(station) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StationInfoWindow(
    station: Station,
    allTrains: List<Train>,
    stations: List<Station>,
    isDark: Boolean,
    onDismiss: () -> Unit,
) {
    val strings = com.example.railway.ui.theme.LocalRailwayStrings.current
    val textColor = if (isDark) Color.White else Color.Black
    Dialog(onDismissRequest = onDismiss) {
        GlassPanel(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text(station.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = textColor)
                Spacer(modifier = Modifier.height(12.dp))
                
                val isIsolated = station.name == "Alaska" || station.name == "Hawaii"
                val localizedInfo = if (isIsolated) strings.isolatedStationInfo(station.name) else strings.majorStationInfo(station.name)
                
                Text(localizedInfo, color = textColor.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(strings.trainSchedule, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(12.dp))
                
                val stationSchedules = allTrains.flatMap { train ->
                    train.schedule.filter { it.sourceStationId == station.id || it.destinationStationId == station.id }
                        .map { train to it }
                }.sortedBy { it.second.departureTimeMillis }

                if (stationSchedules.isEmpty()) {
                    Text(strings.noTrainsScheduled, color = textColor.copy(alpha = 0.5f))
                } else {
                    stationSchedules.forEach { (train, entry) ->
                        val isArriving = entry.destinationStationId == station.id
                        val otherStationName = stations.find { it.id == (if (isArriving) entry.sourceStationId else entry.destinationStationId) }?.name ?: strings.unknown
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            color = textColor.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(train.name, fontWeight = FontWeight.Bold, color = textColor)
                                Text(if (isArriving) "${strings.arrivingFrom} $otherStationName" else "${strings.departingTo} $otherStationName", color = textColor.copy(alpha = 0.7f))
                                Text("${strings.time}${strings.colonSeparator}${formatTime(if (isArriving) entry.arrivalTimeMillis else entry.departureTimeMillis, strings)}", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text(strings.close)
                }
            }
        }
    }
}

fun formatTime(millis: Long, strings: com.example.railway.ui.theme.RailwayStrings): String {
    return "${(millis / 3600000) % 24}${strings.colonSeparator}${((millis / 60000) % 60).toString().padStart(2, '0')}"
}

@Composable
fun TrainInfoWindow(
    train: Train,
    position: TrainPosition,
    stations: List<Station>,
    strings: com.example.railway.ui.theme.RailwayStrings,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val destStation = stations.find { it.id == position.nextDestinationStationId }?.name ?: strings.unknown
    val textColor = if (isDark) Color.White else Color.Black
    
    Dialog(onDismissRequest = onDismiss) {
        GlassPanel(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = train.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        Text(
                            text = strings.liveTripData,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF32D74B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.close, tint = textColor.copy(alpha = 0.5f))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                DetailRow(strings.status, strings.enRoute, isDark = isDark, color = Color(0xFF32D74B))
                DetailRow(strings.nextStop, destStation, isDark = isDark)
                
                val speedKmH = position.speedKmH
                val speedMph = speedKmH * 0.621371
                DetailRow(strings.currentSpeed, "${speedKmH.toInt()} ${strings.kmh} (${speedMph.toInt()} ${strings.mph})", isDark = isDark)
                
                val occupiedSeats = (train.totalSeats * position.progress).toInt().coerceAtMost(train.totalSeats)
                DetailRow(strings.trainLoad, "$occupiedSeats${strings.slashSeparator}${train.totalSeats} ${strings.passengers}", isDark = isDark)
                
                DetailRow(strings.tripProgress, "${(position.progress * 100).toInt()}%", isDark = isDark)
                DetailRow(strings.etaToDestination, formatDuration(position.estimatedTimeRemainingMinutes), isDark = isDark)
                
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { position.progress.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF32D74B),
                    trackColor = textColor.copy(alpha = 0.1f)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.dismiss)
                }
            }
        }
    }
}

@Composable
fun RouteInfoWindow(
    route: Route,
    allTrains: List<Train>,
    stations: List<Station>,
    strings: com.example.railway.ui.theme.RailwayStrings,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val s1 = stations.find { it.id == route.sourceStationId }?.name ?: strings.unknown
    val s2 = stations.find { it.id == route.destinationStationId }?.name ?: strings.unknown
    val textColor = if (isDark) Color.White else Color.Black

    Dialog(onDismissRequest = onDismiss) {
        GlassPanel(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text(strings.routeDetails, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = textColor)
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailRow(strings.from, s1, isDark = isDark)
                DetailRow(strings.to, s2, isDark = isDark)
                DetailRow(strings.distance, "${route.distance} ${strings.km}", isDark = isDark)
                DetailRow(strings.time, "${route.estimatedTimeMinutes} ${strings.mins}", isDark = isDark)
                DetailRow(strings.status, strings.active, isDark = isDark, color = Color(0xFF32D74B))
                
                Spacer(modifier = Modifier.height(24.dp))
                Text(strings.upcomingTrips, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(12.dp))

                val relatedSchedules = allTrains.flatMap { train ->
                    train.schedule.filter { it.routeId == route.id }
                        .map { train to it }
                }.sortedBy { it.second.departureTimeMillis }

                if (relatedSchedules.isEmpty()) {
                    Text(strings.noUpcomingTrips, color = textColor.copy(alpha = 0.5f))
                } else {
                    relatedSchedules.forEach { (train, entry) ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            color = textColor.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(train.name, fontWeight = FontWeight.Bold, color = textColor)
                                Text("${strings.dep}${strings.colonSeparator}${formatTime(entry.departureTimeMillis, strings)} ${strings.pipeSeparator} ${strings.arr}${strings.colonSeparator}${formatTime(entry.arrivalTimeMillis, strings)}",
                                    style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.7f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text(strings.close)
                }
            }
        }
    }
}


@Composable
fun DetailRow(label: String, value: String, isDark: Boolean = true, color: Color = Color.Unspecified) {
    val baseColor = if (isDark) Color.White else Color.Black
    val finalValueColor = if (color != Color.Unspecified) color else baseColor
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = baseColor.copy(alpha = 0.5f), style = MaterialTheme.typography.bodyMedium)
        Text(value, color = finalValueColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SelectionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.08f),
        contentColor = if (isDark) Color.White else Color.Black
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (isDark) Color(0xFF00FF41) else MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun GlassZoomButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.15f),
        contentColor = if (isDark) Color.White else Color.Black,
        shadowElevation = 4.dp
    ) {
        Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}

fun distanceToSegment(p: Offset, v: Offset, w: Offset): Float {
    val dx = w.x - v.x
    val dy = w.y - v.y
    val l2 = dx * dx + dy * dy
    if (l2 == 0f) return sqrt((p.x - v.x) * (p.x - v.x) + (p.y - v.y) * (p.y - v.y))
    var t = ((p.x - v.x) * dx + (p.y - v.y) * dy) / l2
    t = max(0f, min(1f, t))
    val px = v.x + t * dx
    val py = v.y + t * dy
    return sqrt((p.x - px) * (p.x - px) + (p.y - py) * (p.y - py))
}

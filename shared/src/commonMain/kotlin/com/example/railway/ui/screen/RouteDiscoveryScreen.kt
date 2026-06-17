package com.example.railway.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.railway.domain.model.*
import com.example.railway.presentation.RouteDiscoveryViewModel
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
    isAdmin: Boolean = false,
    isDark: Boolean = true,
    onBack: () -> Unit
) {
    val effectiveIsDark = if (isAdmin) true else isDark
    val state by viewModel.state.collectAsState()
    var selectedStation by remember { mutableStateOf<Station?>(null) }
    var selectedTrainId by remember { mutableStateOf<String?>(null) }
    var selectedRoute by remember { mutableStateOf<Route?>(null) }
    var selectedState by remember { mutableStateOf<StateBoundary?>(null) }
    
    // Selection state
    var showSourceDialog by remember { mutableStateOf(false) }
    var showDestDialog by remember { mutableStateOf(false) }

    // Zoom and Pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

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
                title = { Text("Route Discovery", fontWeight = FontWeight.Black, color = if (effectiveIsDark) Color.White else Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = if (effectiveIsDark) Color.White else Color.Black)
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
                    isAdmin = isAdmin,
                    isDark = effectiveIsDark,
                    scale = scale,
                    offset = offset,
                    onOffsetChange = { offset += it },
                    onStationClick = { selectedStation = it },
                    onTrainClick = { selectedTrainId = it },
                    onRouteClick = { selectedRoute = it },
                    onStateClick = { selectedState = it }
                )

                // Discovery Controls Overlay (Premium Redesign)
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(24.dp)
                        .width(440.dp),
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
                                    label = state.sourceStation?.name ?: "Source",
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
                                    label = state.destinationStation?.name ?: "Destination",
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
                                        label = { Text(criteria.name, fontSize = 10.sp) },
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
                                Text("Distance: ${state.totalDistance.toInt()} km", style = MaterialTheme.typography.labelSmall, color = if (effectiveIsDark) Color.White else Color.Black)
                                Text("Time: ${formatDuration(state.totalTimeMinutes.toDouble())}", style = MaterialTheme.typography.labelSmall, color = if (effectiveIsDark) Color.White else Color.Black)
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
                            onDismiss = { selectedTrainId = null }
                        )
                    }
                }

                if (selectedRoute != null) {
                    RouteInfoWindow(
                        route = selectedRoute!!,
                        allTrains = trains,
                        stations = stations,
                        onDismiss = { selectedRoute = null }
                    )
                }

                if (selectedState != null) {
                    StateInfoWindow(
                        stateBoundary = selectedState!!,
                        allTrains = trains,
                        stations = stations,
                        onDismiss = { selectedState = null }
                    )
                }
            }
        }
    }

    if (showSourceDialog) {
        StationSelectionDialog(
            stations = stations,
            title = "Select Source Station",
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
            title = "Select Destination Station",
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
fun InteractiveMap(
    stations: List<Station>,
    routes: List<Route>,
    allRoutes: List<Route>,
    trainPositions: Map<String, TrainPosition>,
    stateBoundaries: List<StateBoundary>,
    isAdmin: Boolean,
    isDark: Boolean,
    scale: Float,
    offset: Offset,
    onOffsetChange: (Offset) -> Unit,
    onStationClick: (Station) -> Unit,
    onTrainClick: (String) -> Unit,
    onRouteClick: (Route) -> Unit,
    onStateClick: (StateBoundary) -> Unit
) {
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceColor = if (isDark) Color.White else Color.Black

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onOffsetChange(dragAmount)
                }
            }
            .pointerInput(scale, offset, stations, trainPositions, allRoutes, stateBoundaries) {
                detectTapGestures { tapOffset ->
                    val width = size.width
                    val height = size.height

                    fun projectLocal(lat: Double, lng: Double): Offset {
                        // Offset for Alaska and Hawaii (Non-contiguous US)
                        val isAlaska = lat > 50.0 && lng < -125.0
                        val isHawaii = lat < 24.0 && lng < -125.0

                        if (isAlaska) {
                            // Map Alaska to bottom left corner
                            val aLatMin = 54.0; val aLatMax = 71.5
                            val aLngMin = -170.0; val aLngMax = -130.0
                            val px = (lng - aLngMin) / (aLngMax - aLngMin) * 0.15f * width
                            val py = (1f - (lat - aLatMin) / (aLatMax - aLatMin)) * 0.15f * height + (0.8f * height)
                            return Offset(px.toFloat(), py.toFloat())
                        }
                        if (isHawaii) {
                            // Map Hawaii to bottom left (next to Alaska)
                            val hLatMin = 18.5; val hLatMax = 22.5
                            val hLngMin = -160.0; val hLngMax = -154.0
                            val px = (lng - hLngMin) / (hLngMax - hLngMin) * 0.1f * width + (0.18f * width)
                            val py = (1f - (lat - hLatMin) / (hLatMax - hLatMin)) * 0.1f * height + (0.85f * height)
                            return Offset(px.toFloat(), py.toFloat())
                        }

                        // Continental US Projection
                        val latMin = 24.0
                        val latMax = 50.0
                        val lngMin = -125.0
                        val lngMax = -66.0
                        
                        val pad = 0.05f
                        val px = (((lng - lngMin) / (lngMax - lngMin)) * (1f - 2*pad) + pad).toFloat() * width
                        val py = ((1f - ((lat - latMin) / (latMax - latMin))) * (1f - 2*pad) + pad).toFloat() * height
                        return Offset(px, py)
                    }

                    // Transform tap back to local map coordinates to compare with projected points
                    val localTap = Offset(
                        (tapOffset.x - offset.x) / scale,
                        (tapOffset.y - offset.y) / scale
                    )

                    // 1. Check Stations (Priority - Dots)
                    val clickedStation = stations.find { station ->
                        val pos = projectLocal(station.latitude, station.longitude)
                        val distSq = (localTap.x - pos.x) * (localTap.x - pos.x) + 
                                   (localTap.y - pos.y) * (localTap.y - pos.y)
                        
                        // Generous hit radius for the dot
                        val hitRadius = 25f / scale.coerceAtLeast(0.5f)
                        distSq < hitRadius * hitRadius
                    }
                    if (clickedStation != null) {
                        onStationClick(clickedStation)
                        return@detectTapGestures
                    }

                    // 2. Check Active Trains (Red Arrows)
                    val tappedTrainId = trainPositions.entries.find { (_, pos) ->
                        val route = allRoutes.find { it.id == pos.currentRouteId }
                        val s1 = stations.find { it.id == route?.sourceStationId }
                        val s2 = stations.find { it.id == route?.destinationStationId }
                        if (s1 != null && s2 != null) {
                            val p1 = projectLocal(s1.latitude, s1.longitude)
                            val p2 = projectLocal(s2.latitude, s2.longitude)
                            
                            // Interpolate train position accurately
                            val trainX = p1.x + (p2.x - p1.x) * pos.progress.toFloat()
                            val trainY = p1.y + (p2.y - p1.y) * pos.progress.toFloat()
                            
                            val distSq = (localTap.x - trainX) * (localTap.x - trainX) + 
                                       (localTap.y - trainY) * (localTap.y - trainY)
                            
                            // Hit radius for the arrow
                            val hitRadius = 30f / scale.coerceAtLeast(0.5f)
                            distSq < hitRadius * hitRadius
                        } else false
                    }?.key

                    if (tappedTrainId != null) {
                        onTrainClick(tappedTrainId)
                        return@detectTapGestures
                    }

                    // 3. Check All Routes
                    val clickedRoute = allRoutes.find { route ->
                        val s1 = stations.find { it.id == route.sourceStationId }
                        val s2 = stations.find { it.id == route.destinationStationId }
                        if (s1 != null && s2 != null) {
                            val p1 = projectLocal(s1.latitude, s1.longitude)
                            val p2 = projectLocal(s2.latitude, s2.longitude)
                            
                            val tolerance = 15f / scale.coerceAtLeast(0.5f)
                            distanceToSegment(localTap, p1, p2) < tolerance
                        } else false
                    }
                    if (clickedRoute != null) {
                        onRouteClick(clickedRoute)
                        return@detectTapGestures
                    }

                    // 4. Check States (Polygons)
                    val clickedState = stateBoundaries.find { sb ->
                        val cp = projectLocal(sb.centroid.first, sb.centroid.second)
                        val d = sqrt((localTap.x - cp.x).pow(2) + (localTap.y - cp.y).pow(2))
                        d < 150f / scale.coerceAtLeast(0.5f) // Scale-adjusted centroid hit area
                    }

                    if (clickedState != null) {
                        onStateClick(clickedState)
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height

        fun project(lat: Double, lng: Double): Offset {
            // Offset for Alaska and Hawaii (Non-contiguous US)
            val isAlaska = lat > 50.0 && lng < -125.0
            val isHawaii = lat < 24.0 && lng < -125.0

            if (isAlaska) {
                // Map Alaska to bottom left corner
                val aLatMin = 54.0; val aLatMax = 71.5
                val aLngMin = -170.0; val aLngMax = -130.0
                val px = (lng - aLngMin) / (aLngMax - aLngMin) * 0.15f * width
                val py = (1f - (lat - aLatMin) / (aLatMax - aLatMin)) * 0.15f * height + (0.8f * height)
                return Offset(px.toFloat(), py.toFloat())
            }
            if (isHawaii) {
                // Map Hawaii to bottom left (next to Alaska)
                val hLatMin = 18.5; val hLatMax = 22.5
                val hLngMin = -160.0; val hLngMax = -154.0
                val px = (lng - hLngMin) / (hLngMax - hLngMin) * 0.1f * width + (0.18f * width)
                val py = (1f - (lat - hLatMin) / (hLatMax - hLatMin)) * 0.1f * height + (0.85f * height)
                return Offset(px.toFloat(), py.toFloat())
            }

            // Continental US Projection
            val latMin = 24.0
            val latMax = 50.0
            val lngMin = -125.0
            val lngMax = -66.0
            
            val pad = 0.05f
            val px = (((lng - lngMin) / (lngMax - lngMin)) * (1f - 2*pad) + pad).toFloat() * width
            val py = ((1f - ((lat - latMin) / (latMax - latMin))) * (1f - 2*pad) + pad).toFloat() * height
            return Offset(px, py)
        }

        withTransform({
            translate(offset.x, offset.y)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            // Draw State Outlines (Adaptive Styling)
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
                    
                    drawPath(path = path, brush = brush, style = Fill)
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

            // Draw all routes (background)
            allRoutes.forEach { route ->
                val s1 = stations.find { it.id == route.sourceStationId }
                val s2 = stations.find { it.id == route.destinationStationId }
                if (s1 != null && s2 != null) {
                    drawLine(
                        color = onSurfaceColor.copy(alpha = 0.1f),
                        start = project(s1.latitude, s1.longitude),
                        end = project(s2.latitude, s2.longitude),
                        strokeWidth = (0.5.dp / scale).toPx()
                    )
                }
            }

            // Draw active train tracking (Red Dashed Lines)
            trainPositions.values.forEach { pos ->
                val route = allRoutes.find { it.id == pos.currentRouteId }
                val s2 = stations.find { it.id == route?.destinationStationId }
                
                if (s2 != null) {
                    val trainPos = project(pos.latitude, pos.longitude)
                    val destPos = project(s2.latitude, s2.longitude)
                    
                    drawLine(
                        color = Color.Red.copy(alpha = 0.6f),
                        start = trainPos,
                        end = destPos,
                        strokeWidth = (2.dp / scale).toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                        cap = StrokeCap.Round
                    )
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
            stations.forEach { station ->
                val pos = project(station.latitude, station.longitude)
                drawCircle(
                    color = onSurfaceColor,
                    radius = (4.dp / scale).toPx(), 
                    center = pos
                )
            }

            // Draw active trains (Red Arrows)
            trainPositions.values.forEach { pos ->
                val route = allRoutes.find { it.id == pos.currentRouteId }
                val s1 = stations.find { it.id == route?.sourceStationId }
                val s2 = stations.find { it.id == route?.destinationStationId }
                
                if (s1 != null && s2 != null) {
                    val p1 = project(s1.latitude, s1.longitude)
                    val p2 = project(s2.latitude, s2.longitude)
                    
                    val trainX = p1.x + (p2.x - p1.x) * pos.progress.toFloat()
                    val trainY = p1.y + (p2.y - p1.y) * pos.progress.toFloat()
                    
                    val angle = atan2(p2.y - p1.y, p2.x - p1.x) * (180 / PI).toFloat()
                    
                    withTransform({
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
                            color = Color.Red // Active trains are red as requested
                        )
                    }
                }
            }
        }
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
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassPanel(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text(station.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                Text(station.info, color = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Train Schedule", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                
                val stationSchedules = allTrains.flatMap { train ->
                    train.schedule.filter { it.sourceStationId == station.id || it.destinationStationId == station.id }
                        .map { train to it }
                }.sortedBy { it.second.departureTimeMillis }

                if (stationSchedules.isEmpty()) {
                    Text("No trains scheduled.", color = Color.White.copy(alpha = 0.5f))
                } else {
                    stationSchedules.forEach { (train, entry) ->
                        val isArriving = entry.destinationStationId == station.id
                        val otherStationName = stations.find { it.id == (if (isArriving) entry.sourceStationId else entry.destinationStationId) }?.name ?: "Unknown"
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(train.name, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(if (isArriving) "Arriving from $otherStationName" else "Departing to $otherStationName", color = Color.White.copy(alpha = 0.7f))
                                Text("Time: ${formatTime(if (isArriving) entry.arrivalTimeMillis else entry.departureTimeMillis)}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("Close")
                }
            }
        }
    }
}

fun formatTime(millis: Long): String {
    return "${(millis / 3600000) % 24}:${((millis / 60000) % 60).toString().padStart(2, '0')}"
}

@Composable
fun TrainInfoWindow(
    train: Train,
    position: TrainPosition,
    stations: List<Station>,
    onDismiss: () -> Unit
) {
    val destStation = stations.find { it.id == position.nextDestinationStationId }?.name ?: "Unknown"
    
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
                            color = Color.White
                        )
                        Text(
                            text = "LIVE TRIP DATA",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF32D74B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Close", tint = Color.White.copy(alpha = 0.5f))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                DetailRow("Status", "EN ROUTE", color = Color(0xFF32D74B))
                DetailRow("Next Stop", destStation)
                
                val speedKmH = position.speedKmH
                val speedMph = speedKmH * 0.621371
                DetailRow("Current Speed", "${speedKmH.toInt()} km/h (${speedMph.toInt()} mph)")
                
                val occupiedSeats = (train.totalSeats * position.progress).toInt().coerceAtMost(train.totalSeats)
                DetailRow("Train Load", "$occupiedSeats / ${train.totalSeats} Passengers")
                
                DetailRow("Trip Progress", "${(position.progress * 100).toInt()}%")
                DetailRow("ETA to Destination", formatDuration(position.estimatedTimeRemainingMinutes))
                
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { position.progress.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF32D74B),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Dismiss")
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
    onDismiss: () -> Unit
) {
    val s1 = stations.find { it.id == route.sourceStationId }?.name ?: "Unknown"
    val s2 = stations.find { it.id == route.destinationStationId }?.name ?: "Unknown"

    Dialog(onDismissRequest = onDismiss) {
        GlassPanel(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text("Route Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailRow("From", s1)
                DetailRow("To", s2)
                DetailRow("Distance", "${route.distance} km")
                DetailRow("Duration", "${route.estimatedTimeMinutes} mins")
                DetailRow("Status", "ACTIVE", color = Color(0xFF32D74B))
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Upcoming Trips", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                val relatedSchedules = allTrains.flatMap { train ->
                    train.schedule.filter { it.routeId == route.id }
                        .map { train to it }
                }.sortedBy { it.second.departureTimeMillis }

                if (relatedSchedules.isEmpty()) {
                    Text("No upcoming trips found.", color = Color.White.copy(alpha = 0.5f))
                } else {
                    relatedSchedules.forEach { (train, entry) ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(train.name, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Dep: ${formatTime(entry.departureTimeMillis)} | Arr: ${formatTime(entry.arrivalTimeMillis)}", 
                                    style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun StateInfoWindow(
    stateBoundary: StateBoundary,
    allTrains: List<Train>,
    stations: List<Station>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassPanel(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color(0xFF32D74B), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(stateBoundary.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
                        Text("State Railroad Network", style = MaterialTheme.typography.labelSmall, color = Color(0xFF32D74B))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Heuristic: Check if station is "likely" in state by centroid proximity or name matching
                val stationsInState = stations.filter { it.name.contains(stateBoundary.name) || 
                    sqrt((it.latitude - stateBoundary.centroid.first).pow(2) + (it.longitude - stateBoundary.centroid.second).pow(2)) < 3.0
                }
                
                val activeTrainCount = allTrains.filter { train ->
                    train.schedule.any { sch -> stationsInState.any { it.id == sch.sourceStationId || it.id == sch.destinationStationId } }
                }.size

                DetailRow("Connected Hubs", stationsInState.size.toString())
                DetailRow("Running Trains", activeTrainCount.toString())
                DetailRow("Safety Rating", "99.9%", color = Color(0xFF32D74B))
                DetailRow("Operational Mode", "High-Speed AI-Optimized")
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Regional Protocol", style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.6f))
                Text("All intersections monitored by neural track sensors. Standard operations are active across the ${stateBoundary.name} regional block.", 
                    style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))

                Spacer(modifier = Modifier.height(24.dp))
                Text("Upcoming Regional Trips", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                val nextDepartures = allTrains.flatMap { t -> t.schedule.filter { s -> stationsInState.any { it.id == s.sourceStationId } }.map { t to it } }
                    .sortedBy { it.second.departureTimeMillis }
                    .take(5)

                if (nextDepartures.isEmpty()) {
                    Text("No upcoming departures found for this region.", color = Color.White.copy(alpha = 0.5f))
                } else {
                    nextDepartures.forEach { (train, sch) ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(train.name, fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                    Text("Dep: ${formatTime(sch.departureTimeMillis)}", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                                }
                                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("Close Panel")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, color: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodyMedium)
        Text(value, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
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

private fun distanceToSegment(p: Offset, v: Offset, w: Offset): Float {
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

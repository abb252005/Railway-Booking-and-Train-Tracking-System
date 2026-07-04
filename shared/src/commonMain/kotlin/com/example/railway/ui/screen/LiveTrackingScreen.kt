package com.example.railway.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railway.domain.model.*
import com.example.railway.presentation.TrackingViewModel
import com.example.railway.ui.component.GlassPanel
import com.example.railway.util.formatDuration
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTrackingScreen(
    viewModel: TrackingViewModel,
    stations: List<Station>,
    trains: List<Train>,
    routes: List<Route>,
    onBack: () -> Unit
) {
    val strings = com.example.railway.ui.theme.LocalRailwayStrings.current
    val trainPositions by viewModel.trainPositions.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    var showOnlyLive by remember { mutableStateOf(false) }

    val filteredTrains = if (showOnlyLive) {
        trains.filter { trainPositions.containsKey(it.id) }
    } else {
        trains
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(strings.liveRailMap, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back, tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    FilterChip(
                        selected = showOnlyLive,
                        onClick = { showOnlyLive = !showOnlyLive },
                        label = { Text(strings.live.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = if (showOnlyLive) {
                            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.padding(end = 16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.tertiary
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (filteredTrains.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillParentMaxHeight(0.7f).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Rounded.Train,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (showOnlyLive) "No trains are currently active" else "No trains found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            items(filteredTrains) { train ->
                val position = trainPositions[train.id]
                val route = if (position != null) routes.find { it.id == position.currentRouteId } else null
                val sourceStation = if (route != null) stations.find { it.id == route.sourceStationId } else null
                val destStation = if (route != null) stations.find { it.id == route.destinationStationId } else null
                
                val connectionRisk = if (position != null && position.isUserBooked) {
                    val booking = bookings.find { it.trainId == train.id && it.status == TicketStatus.ISSUED }
                    if (booking != null) viewModel.checkConnectionRisk(booking, position) else null
                } else null

                TrainTrackingCard(
                    train = train,
                    position = position,
                    sourceName = sourceStation?.name ?: strings.source,
                    destName = destStation?.name ?: strings.destination,
                    routes = routes,
                    strings = strings,
                    isUserBooked = position?.isUserBooked == true,
                    connectionRisk = connectionRisk,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
        }
    }
}

@Composable
fun TrainTrackingCard(
    train: Train,
    position: TrainPosition?,
    sourceName: String,
    destName: String,
    routes: List<Route>,
    strings: com.example.railway.ui.theme.RailwayStrings,
    isUserBooked: Boolean = false,
    connectionRisk: com.example.railway.domain.service.ConnectionRiskReport? = null,
    modifier: Modifier = Modifier
) {
    val isActive = position != null
    val primaryColor = MaterialTheme.colorScheme.primary

    GlassPanel(
        modifier = modifier.then(
            if (isUserBooked) Modifier.border(2.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            else Modifier
        ),
        border = if (isUserBooked) androidx.compose.foundation.BorderStroke(2.dp, primaryColor) else null
    ) {
        Column(
            modifier = Modifier
                .background(if (isUserBooked) primaryColor.copy(alpha = 0.05f) else Color.Transparent)
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = when {
                            isUserBooked -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Train,
                                contentDescription = null,
                                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = train.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isUserBooked) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        strings.myTrip,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (position != null) {
                                val speedKmH = position.speedKmH
                                val speedMph = speedKmH * 0.621371
                                "${strings.locLabel}: ${formatCoord(position.latitude)}, ${formatCoord(position.longitude)} • ${speedKmH.toInt()} ${strings.kmh} (${speedMph.toInt()} ${strings.mph}) [Max: ${train.maxSpeedMph} ${strings.mph}]"
                            } else "${strings.statusStationary} • Max: ${train.maxSpeedMph} ${strings.mph}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Status Badge
                Surface(
                    color = if (isActive) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isActive) strings.live else strings.na,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))

            // Visual Route Map
            RouteMapView(
                progress = position?.progress?.toFloat() ?: 0f,
                sourceName = sourceName,
                destName = destName,
                isActive = isActive,
                isUserBooked = isUserBooked,
                slowOrders = position?.currentRouteId?.let { rid -> routes.find { it.id == rid }?.slowOrders } ?: emptyList()
            )

            Spacer(modifier = Modifier.height(28.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress Section
                Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.tripProgress,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = if (position != null) "${(position.progress * 100).toInt()}%" else strings.na,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Black
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = { position?.progress?.toFloat() ?: 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    
                    // Weather Indicator
                    position?.weather?.let { weather ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    getWeatherIcon(weather.condition),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "${weather.condition.name} • ${weather.temperatureCelsius.toInt()}°C • Wind: ${weather.windSpeedKmH.toInt()} km/h",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                    
                    // Connection Protection
                    connectionRisk?.let { report ->
                        if (report.riskLevel != com.example.railway.domain.service.RiskLevel.LOW) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = when (report.riskLevel) {
                                    com.example.railway.domain.service.RiskLevel.MISSED -> Color.Red.copy(alpha = 0.1f)
                                    com.example.railway.domain.service.RiskLevel.CRITICAL -> Color.Magenta.copy(alpha = 0.1f)
                                    else -> Color.Yellow.copy(alpha = 0.1f)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.Warning,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = when (report.riskLevel) {
                                                com.example.railway.domain.service.RiskLevel.MISSED -> Color.Red
                                                com.example.railway.domain.service.RiskLevel.CRITICAL -> Color.Magenta
                                                else -> Color(0xFFFFA000)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Connection Risk: ${report.riskLevel.name}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        report.suggestedAction,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Info Chips
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ETA Chip
                    Surface(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Schedule, 
                                contentDescription = null, 
                                modifier = Modifier.size(14.dp),
                                tint = if (isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (position != null) formatDuration(position.estimatedTimeRemainingMinutes, strings) else "${strings.eta}: ${strings.na}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Distance Chip
                    Surface(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Explore, 
                                contentDescription = null, 
                                modifier = Modifier.size(14.dp),
                                tint = if (isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (position != null) "${position.distanceRemainingKm.roundToInt()} ${strings.left}" else "${strings.distance}: ${strings.na}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RouteMapView(
    progress: Float,
    sourceName: String,
    destName: String,
    isActive: Boolean,
    isUserBooked: Boolean = false,
    slowOrders: List<SlowOrder> = emptyList()
) {
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val primaryColor = if (isUserBooked) MaterialTheme.colorScheme.secondary else Color(0xFF0A84FF)
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                sourceName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f)
            )
            Text(
                destName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Track Line
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                drawLine(
                    color = trackColor,
                    start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                
                // Slow Orders
                slowOrders.forEach { slowOrder ->
                    val startX = size.width * slowOrder.startProgress.toFloat()
                    val endX = size.width * slowOrder.endProgress.toFloat()
                    drawLine(
                        color = Color.Yellow,
                        start = androidx.compose.ui.geometry.Offset(startX, size.height / 2),
                        end = androidx.compose.ui.geometry.Offset(endX, size.height / 2),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Butt
                    )
                }

                if (isActive) {
                    drawLine(
                        color = primaryColor, 
                        start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                        end = androidx.compose.ui.geometry.Offset(size.width * progress, size.height / 2),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
// ... rest of the file ...

            // Train Icon positioned by progress
            if (isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Surface(
                        shape = CircleShape,
                        color = primaryColor,
                        modifier = Modifier.size(if (isUserBooked) 40.dp else 32.dp),
                        shadowElevation = if (isUserBooked) 12.dp else 8.dp,
                        border = if (isUserBooked) androidx.compose.foundation.BorderStroke(2.dp, Color.White) else null
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Train,
                                contentDescription = null,
                                modifier = Modifier.size(if (isUserBooked) 22.dp else 18.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            
            // Source Dot
            Surface(
                shape = CircleShape,
                color = if (isActive) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                modifier = Modifier.size(8.dp).align(Alignment.CenterStart)
            ) {}

            // Destination Dot
            Surface(
                shape = CircleShape,
                color = if (isActive) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                modifier = Modifier.size(8.dp).align(Alignment.CenterEnd)
            ) {}
        }
    }
}

private fun formatCoord(coord: Double): String {
    val rounded = (coord * 100).toInt() / 100.0
    return rounded.toString()
}

@Composable
fun getWeatherIcon(condition: WeatherCondition): ImageVector {
    return when (condition) {
        WeatherCondition.CLEAR -> Icons.Rounded.WbSunny
        WeatherCondition.RAIN -> Icons.Rounded.Umbrella
        WeatherCondition.HEAVY_RAIN -> Icons.Rounded.Thunderstorm
        WeatherCondition.SNOW -> Icons.Rounded.AcUnit
        WeatherCondition.HEAVY_SNOW -> Icons.Rounded.AcUnit
        WeatherCondition.FOG -> Icons.Rounded.Cloud
    }
}

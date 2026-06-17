package com.example.railway.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railway.domain.model.Route
import com.example.railway.domain.model.Station
import com.example.railway.domain.model.Train
import com.example.railway.domain.model.TrainPosition
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
    val trainPositions by viewModel.trainPositions.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Live Rail Map", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
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
            items(trains) { train ->
                val position = trainPositions[train.id]
                val route = if (position != null) routes.find { it.id == position.currentRouteId } else null
                val sourceStation = if (route != null) stations.find { it.id == route.sourceStationId } else null
                val destStation = if (route != null) stations.find { it.id == route.destinationStationId } else null
                
                TrainTrackingCard(
                    train = train,
                    position = position,
                    sourceName = sourceStation?.name ?: "N/A",
                    destName = destStation?.name ?: "N/A",
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
    modifier: Modifier = Modifier
) {
    val isActive = position != null

    GlassPanel(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
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
                        Text(
                            text = train.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (position != null) {
                                val speedKmH = position.speedKmH
                                val speedMph = speedKmH * 0.621371
                                "Loc: ${formatCoord(position.latitude)}, ${formatCoord(position.longitude)} • ${speedKmH.toInt()} km/h (${speedMph.toInt()} mph)"
                            } else "Status: Stationary",
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
                            text = if (isActive) "LIVE" else "N/A",
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
                isActive = isActive
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
                            text = "Trip Progress",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = if (position != null) "${(position.progress * 100).toInt()}%" else "N/A",
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
                                text = if (position != null) formatDuration(position.estimatedTimeRemainingMinutes) else "ETA: N/A",
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
                                text = if (position != null) "${position.distanceRemainingKm.roundToInt()} km left" else "Dist: N/A",
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
    isActive: Boolean
) {
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    
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
                if (isActive) {
                    drawLine(
                        color = Color(0xFF0A84FF), 
                        start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                        end = androidx.compose.ui.geometry.Offset(size.width * progress, size.height / 2),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

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
                        color = Color(0xFF0A84FF),
                        modifier = Modifier.size(32.dp),
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Train,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
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

package com.example.railway.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railway.domain.model.*
import com.example.railway.ui.component.GlassPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationExplorerScreen(
    stations: List<Station>,
    routes: List<Route>,
    trains: List<Train>,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf(SortOption.NONE) }

    val filteredStations = remember(searchQuery, stations, sortBy, routes, trains) {
        val filtered = stations.filter { it.name.contains(searchQuery, ignoreCase = true) }
        
        when (sortBy) {
            SortOption.PRICE -> filtered.sortedBy { station ->
                routes.filter { it.sourceStationId == station.id }.map { it.distance * 0.15 }.average().takeIf { !it.isNaN() } ?: Double.MAX_VALUE
            }
            SortOption.DURATION -> filtered.sortedBy { station ->
                routes.filter { it.sourceStationId == station.id }.map { it.estimatedTimeMinutes }.average().takeIf { !it.isNaN() } ?: Double.MAX_VALUE
            }
            SortOption.DEPARTURE -> filtered.sortedBy { station ->
                // Next departure from this station
                trains.flatMap { it.schedule }.filter { it.sourceStationId == station.id }
                    .minOfOrNull { it.departureTimeMillis } ?: Long.MAX_VALUE
            }
            else -> filtered
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Station Explorer", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 64.dp, vertical = 8.dp),
                    placeholder = { Text("Search stations...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    shape = androidx.compose.foundation.shape.CircleShape,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                SecondaryScrollableTabRow(
                    selectedTabIndex = sortBy.ordinal,
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    SortOption.entries.forEach { option ->
                        Tab(
                            selected = sortBy == option,
                            onClick = { sortBy = option },
                            text = { Text(option.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 64.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredStations) { station ->
                val stationRoutes = routes.filter { it.sourceStationId == station.id }
                val avgPrice = stationRoutes.map { it.distance * 0.15 }.average()
                val avgDuration = stationRoutes.map { it.estimatedTimeMinutes }.average()
                val nextDeparture = trains.flatMap { it.schedule }.filter { it.sourceStationId == station.id }
                    .minOfOrNull { it.departureTimeMillis }

                GlassPanel(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text(station.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                        supportingContent = { 
                            Column {
                                Text(station.info, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    if (!avgPrice.isNaN()) {
                                        val priceStr = (avgPrice * 100).toInt().toDouble() / 100
                                        Text("Avg. Price: $$priceStr", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    if (!avgDuration.isNaN()) {
                                        Text("Avg. Duration: ${avgDuration.toInt()}m", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                    if (nextDeparture != null) {
                                        Text("Next Dep: ${formatTimeSimple(nextDeparture)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary)
                                    }
                                }
                            }
                        },
                        leadingContent = {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = androidx.compose.foundation.shape.CircleShape,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}

fun formatTimeSimple(millis: Long): String {
    return "${(millis / 3600000) % 24}:${((millis / 60000) % 60).toString().padStart(2, '0')}"
}

enum class SortOption(val label: String) {
    NONE("All"),
    PRICE("Price"),
    DURATION("Duration"),
    DEPARTURE("Departure")
}

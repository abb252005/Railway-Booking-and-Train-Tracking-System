package com.example.railway.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.railway.domain.model.Station
import com.example.railway.presentation.RouteDiscoveryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDiscoveryScreen(
    viewModel: RouteDiscoveryViewModel,
    stations: List<Station>,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showSourceDialog by remember { mutableStateOf(false) }
    var showDestDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Route Discovery") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedButton(
                        onClick = { showSourceDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(state.sourceStation?.name ?: "Select Source Station")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showDestDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(state.destinationStation?.name ?: "Select Destination Station")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.calculatedPath.isNotEmpty()) {
                Text(
                    "Shortest Path Found",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Distance: ${"%.1f".format(state.totalDistance)} km")
                    Text("Estimated Time: ${state.totalTimeMinutes} mins")
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.calculatedPath) { route ->
                        val sourceName = stations.find { it.id == route.sourceStationId }?.name ?: "Unknown"
                        val destName = stations.find { it.id == route.destinationStationId }?.name ?: "Unknown"
                        
                        ListItem(
                            headlineContent = { Text("$sourceName → $destName") },
                            supportingContent = { Text("${route.distance} km | ${route.estimatedTimeMinutes} mins") },
                            leadingContent = { Icon(Icons.Rounded.Route, contentDescription = null) },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            } else if (state.sourceStation != null && state.destinationStation != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No direct route found.")
                }
            }
        }
    }

    if (showSourceDialog) {
        StationSelectionDialog(
            stations = stations,
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
            onDismiss = { showDestDialog = false },
            onSelect = {
                viewModel.setDestination(it)
                showDestDialog = false
            }
        )
    }
}

@Composable
fun StationSelectionDialog(
    stations: List<Station>,
    onDismiss: () -> Unit,
    onSelect: (Station) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Station") },
        text = {
            LazyColumn {
                items(stations) { station ->
                    ListItem(
                        headlineContent = { Text(station.name) },
                        modifier = Modifier.clickable { onSelect(station) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

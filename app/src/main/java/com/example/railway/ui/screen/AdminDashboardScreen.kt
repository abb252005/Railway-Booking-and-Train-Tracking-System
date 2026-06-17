package com.example.railway.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.railway.presentation.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: AdminViewModel) {
    val state by viewModel.state.collectAsState()
    var showAddStationDialog by remember { mutableStateOf(false) }
    var showAddTrainDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { 
            LargeTopAppBar(
                title = { Text("System Administration", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            ) 
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stations Section
            item {
                SectionHeader(
                    title = "Rail Stations",
                    count = state.stations.size,
                    onAddClick = { showAddStationDialog = true }
                )
            }
            items(state.stations) { station ->
                AdminCard(
                    title = station.name,
                    subtitle = "Coordinates: ${station.latitude}, ${station.longitude}",
                    icon = Icons.Rounded.Place,
                    onDelete = { viewModel.deleteStation(station.id) }
                )
            }

            // Trains Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Active Fleet",
                    count = state.trains.size,
                    onAddClick = { showAddTrainDialog = true }
                )
            }
            items(state.trains) { train ->
                AdminCard(
                    title = train.name,
                    subtitle = "${train.totalSeats} Capacity • ${train.status}",
                    icon = Icons.Rounded.Train,
                    onDelete = { viewModel.deleteTrain(train.id) }
                )
            }
        }
    }

    if (showAddStationDialog) {
        AddStationDialog(
            onDismiss = { showAddStationDialog = false },
            onAdd = { name, lat, lng ->
                viewModel.addStation(name, lat, lng)
                showAddStationDialog = false
            }
        )
    }

    if (showAddTrainDialog) {
        AddTrainDialog(
            onDismiss = { showAddTrainDialog = false },
            onAdd = { name, seats ->
                viewModel.addTrain(name, seats)
                showAddTrainDialog = false
            }
        )
    }
}

@Composable
fun AdminCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title, 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$count items registered", 
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        FilledIconButton(
            onClick = onAddClick,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add")
        }
    }
}

@Composable
fun AddStationDialog(onDismiss: () -> Unit, onAdd: (String, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("") }
    var lng by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Station") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Station Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = lat, 
                    onValueChange = { lat = it }, 
                    label = { Text("Latitude") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = lng, 
                    onValueChange = { lng = it }, 
                    label = { Text("Longitude") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, lat.toDoubleOrNull() ?: 0.0, lng.toDoubleOrNull() ?: 0.0) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddTrainDialog(onDismiss: () -> Unit, onAdd: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var seats by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Fleet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Train Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = seats, 
                    onValueChange = { seats = it }, 
                    label = { Text("Seating Capacity") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, seats.toIntOrNull() ?: 0) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Train")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

package com.example.railway.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import com.example.railway.domain.model.Booking
import com.example.railway.presentation.AdminViewModel
import com.example.railway.ui.component.*
import org.jetbrains.compose.resources.painterResource
import railway_booking_and_train_tracking_system.shared.generated.resources.Res
import railway_booking_and_train_tracking_system.shared.generated.resources.bcg_admin_1
import railway_booking_and_train_tracking_system.shared.generated.resources.bcg_admin_2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: AdminViewModel) {
    val strings = com.example.railway.ui.theme.LocalRailwayStrings.current
    val state by viewModel.state.collectAsState()
    val onSurface = MaterialTheme.colorScheme.onSurface
    
    var showAddStationDialog by remember { mutableStateOf(value = false) }
    var showAddTrainDialog by remember { mutableStateOf(value = false) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }

    // Background Transition Logic (60 seconds)
    var currentBgIndex by remember { mutableIntStateOf(0) }
    val backgrounds = listOf(Res.drawable.bcg_admin_1, Res.drawable.bcg_admin_2)

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60000.milliseconds) // 60 seconds interval
            currentBgIndex = (currentBgIndex + 1) % backgrounds.size
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Immersive Transitioning Blurred Background
        AnimatedContent(
            targetState = backgrounds[currentBgIndex],
            transitionSpec = {
                fadeIn(animationSpec = tween(3000)) togetherWith fadeOut(animationSpec = tween(3000))
            },
            label = "bg_transition",
            modifier = Modifier.fillMaxSize()
        ) { bgRes ->
            Image(
                painter = painterResource(bgRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(if (selectedBooking != null) 60.dp else 40.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Deep technical overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
        )

        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.blur(if (selectedBooking != null) 15.dp else 0.dp),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            strings.adminDashboard,
                            fontWeight = FontWeight.Black, 
                            color = onSurface,
                            fontFamily = FontFamily.Monospace
                        ) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Row(
                modifier = Modifier.padding(padding).fillMaxSize().padding(32.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Stations Module
                Column(modifier = Modifier.weight(1f)) {
                    SectionHeader(strings.stationsDb, strings = strings) { showAddStationDialog = true }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.stations) { station ->
                            GlassPanel {
                                ListItem(
                                    headlineContent = { 
                                        Text(
                                            station.name, 
                                            fontWeight = FontWeight.Bold, 
                                            color = onSurface,
                                            fontFamily = FontFamily.Monospace
                                        ) 
                                    },
                                    supportingContent = { 
                                        Text(
                                            "${strings.lat}${strings.colonSeparator}${station.latitude}${strings.pipeSeparator}${strings.lng}${strings.colonSeparator}${station.longitude}",
                                            color = onSurface.copy(alpha = 0.5f),
                                            fontFamily = FontFamily.Monospace
                                        ) 
                                    },
                                    trailingContent = {
                                        IconButton(onClick = { viewModel.deleteStation(station.id) }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                )
                            }
                        }
                    }
                }

                // Fleet Module
                Column(modifier = Modifier.weight(1f)) {
                    SectionHeader(strings.trainsActive, strings = strings, onAdd = { showAddTrainDialog = true })
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.trains) { train ->
                            GlassPanel {
                                ListItem(
                                    headlineContent = { 
                                        Text(
                                            train.name, 
                                            fontWeight = FontWeight.Bold, 
                                            color = onSurface,
                                            fontFamily = FontFamily.Monospace
                                        ) 
                                    },
                                    supportingContent = { 
                                        val hasSchedule = train.schedule.isNotEmpty()
                                        val trainSuffix = if (hasSchedule) strings.express else strings.hubService
                                        Text(
                                            "${strings.unitsLabel}${strings.colonSeparator}${train.carriages.size}${strings.pipeSeparator}${strings.statusLabel}${strings.colonSeparator}${train.status}${strings.pipeSeparator}${strings.typeLabel}${strings.colonSeparator}$trainSuffix",
                                            color = onSurface.copy(alpha = 0.5f),
                                            fontFamily = FontFamily.Monospace
                                        ) 
                                    },
                                    trailingContent = {
                                        IconButton(onClick = { viewModel.deleteTrain(train.id) }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                )
                            }
                        }
                    }
                }

                // Monitoring Module
                Column(modifier = Modifier.weight(1f)) {
                    SectionHeader(strings.userActivityLog, strings = strings, onAdd = null)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (state.bookings.isEmpty()) {
                            item {
                                Text(
                                    strings.noActivity,
                                    color = onSurface.copy(alpha = 0.3f),
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        items(state.bookings.reversed()) { booking ->
                            val train = state.trains.find { it.id == booking.trainId }
                            val source = state.stations.find { it.id == booking.startStationId }
                            val dest = state.stations.find { it.id == booking.endStationId }
                            
                            GlassPanel(
                                onClick = { selectedBooking = booking }
                            ) {
                                ListItem(
                                    headlineContent = { 
                                        Text(
                                            "${strings.ticketPurchase}: ${booking.passengerName}", 
                                            fontWeight = FontWeight.Bold, 
                                            color = onSurface,
                                            fontFamily = FontFamily.Monospace
                                        ) 
                                    },
                                    supportingContent = { 
                                        Text(
                                            "${strings.trainLabel}${strings.colonSeparator}${train?.name ?: strings.unknown}${strings.pipeSeparator}${strings.routeLabel}${strings.colonSeparator}${source?.name ?: strings.na} ${strings.routeArrow} ${dest?.name ?: strings.na}${strings.pipeSeparator}${strings.amtLabel}${strings.colonSeparator}${booking.price}",
                                            color = onSurface.copy(alpha = 0.5f),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                                        ) 
                                    },
                                    leadingContent = {
                                        Icon(Icons.Rounded.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = selectedBooking != null,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { selectedBooking = null },
                contentAlignment = Alignment.Center
            ) {
                selectedBooking?.let { booking ->
                    val source = state.stations.find { it.id == booking.startStationId }
                    val dest = state.stations.find { it.id == booking.endStationId }

                    AdminTicketAustereView(
                        booking = booking,
                        startStation = source,
                        endStation = dest,
                        modifier = Modifier.width(600.dp).clickable(enabled = false) { },
                        onClose = { selectedBooking = null }
                    )
                }
            }
        }

        if (showAddStationDialog) {
            AddStationDialog(
                strings = strings,
                onDismiss = { showAddStationDialog = false },
                onAdd = { name, lat, lng ->
                    viewModel.addStation(name, lat, lng)
                    showAddStationDialog = false
                }
            )
        }

        if (showAddTrainDialog) {
            AddTrainDialog(
                strings = strings,
                onDismiss = { showAddTrainDialog = false },
                onAdd = { name, seats ->
                    viewModel.addTrain(name, seats)
                    showAddTrainDialog = false
                }
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, strings: com.example.railway.ui.theme.RailwayStrings, onAdd: (() -> Unit)?) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title, 
            style = MaterialTheme.typography.headlineSmall, 
            fontWeight = FontWeight.Black, 
            color = onSurface,
            fontFamily = FontFamily.Monospace
        )
        onAdd?.let {
            Button(
                onClick = it,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.newEntry, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun AddStationDialog(strings: com.example.railway.ui.theme.RailwayStrings, onDismiss: () -> Unit, onAdd: (String, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("") }
    var lng by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.registerStation, fontFamily = FontFamily.Monospace) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = name, onValueChange = { name = it }, label = { Text(strings.name) })
                TextField(value = lat, onValueChange = { lat = it }, label = { Text(strings.lat) })
                TextField(value = lng, onValueChange = { lng = it }, label = { Text(strings.lng) })
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(name, lat.toDoubleOrNull() ?: 0.0, lng.toDoubleOrNull() ?: 0.0) }) {
                Text(strings.execute, fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.abort, fontFamily = FontFamily.Monospace) }
        }
    )
}

@Composable
fun AddTrainDialog(strings: com.example.railway.ui.theme.RailwayStrings, onDismiss: () -> Unit, onAdd: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var seats by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.deployTrain, fontFamily = FontFamily.Monospace) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = name, onValueChange = { name = it }, label = { Text(strings.trainId) })
                TextField(value = seats, onValueChange = { seats = it }, label = { Text(strings.capacity) })
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(name, seats.toIntOrNull() ?: 300) }) {
                Text(strings.deploy, fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel, fontFamily = FontFamily.Monospace) }
        }
    )
}

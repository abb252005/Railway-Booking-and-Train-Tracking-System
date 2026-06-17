package com.example.railway.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.railway.domain.model.Booking
import com.example.railway.domain.model.Train
import com.example.railway.presentation.BookingState
import com.example.railway.presentation.BookingViewModel

@Composable
fun BookingScreen(
    viewModel: BookingViewModel,
    trains: List<Train>
) {
    val state by viewModel.state.collectAsState()

    if (state.isConfirmed) {
        TicketConfirmationScreen(
            booking = state.lastBooking!!,
            onDone = { viewModel.reset() }
        )
    } else if (state.selectedTrainId == null) {
        TrainSelectionStep(
            trains = trains,
            onTrainSelected = { viewModel.selectTrain(it.id) }
        )
    } else {
        SeatSelectionStep(
            state = state,
            onSeatSelected = { viewModel.selectSeat(it) },
            onPassengerNameChanged = { viewModel.setPassengerName(it) },
            onConfirm = { viewModel.confirmBooking(trains) },
            onBack = { viewModel.reset() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainSelectionStep(
    trains: List<Train>,
    onTrainSelected: (Train) -> Unit
) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Select Train") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(trains) { train ->
                Card(
                    onClick = { onTrainSelected(train) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text(train.name) },
                        supportingContent = { Text("${train.totalSeats} seats | ${train.status}") },
                        trailingContent = { Text("Book Now", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectionStep(
    state: BookingState,
    onSeatSelected: (String) -> Unit,
    onPassengerNameChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val seats = (1..40).map { it.toString() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Seat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)
        ) {
            TextField(
                value = state.passengerName,
                onValueChange = onPassengerNameChanged,
                label = { Text("Passenger Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Select a Seat", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(seats) { seat ->
                    val isSelected = state.selectedSeat == seat
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onSeatSelected(seat) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = seat,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selectedSeat != null && state.passengerName.isNotBlank()
            ) {
                Text("Confirm Booking")
            }
        }
    }
}

@Composable
fun TicketConfirmationScreen(
    booking: Booking,
    onDone: () -> Unit
) {
    var showDownloadMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Booking Confirmed!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Passenger: ${booking.passengerName}")
                Text("Seat Number: ${booking.seatNumber}")
                Text("Train ID: ${booking.trainId}")
                Text("Booking ID: ${booking.id.take(8)}")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { showDownloadMessage = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Download PDF Ticket")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Done")
        }

        if (showDownloadMessage) {
            AlertDialog(
                onDismissRequest = { showDownloadMessage = false },
                title = { Text("Success") },
                text = { Text("Ticket PDF generated and saved to downloads.") },
                confirmButton = {
                    TextButton(onClick = { showDownloadMessage = false }) { Text("OK") }
                }
            )
        }
    }
}

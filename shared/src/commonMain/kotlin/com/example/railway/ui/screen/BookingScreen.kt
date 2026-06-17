package com.example.railway.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railway.domain.model.*
import com.example.railway.presentation.BookingState
import com.example.railway.presentation.BookingViewModel
import com.example.railway.ui.component.GlassPanel
import com.example.railway.presentation.CardInfo
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun BookingScreen(
    viewModel: BookingViewModel,
    trains: List<Train>,
    stations: List<Station>,
    isAdmin: Boolean = false,
    isDark: Boolean = true,
    onBookingConfirmed: (Booking, CardInfo) -> Unit
) {
    val state by viewModel.state.collectAsState()

    if (state.isConfirmed) {
        TicketConfirmationScreen(
            booking = state.lastBooking!!,
            stations = stations,
            trains = trains,
            onDone = { viewModel.reset() }
        )
    } else if (state.selectedStartStationId == null) {
        StationAndDateSelectionStep(
            stations = stations,
            isAdmin = isAdmin,
            isDark = isDark,
            onConfirmed = { start, end, date -> viewModel.selectStations(start, end, date) }
        )
    } else if (state.selectedTrainId == null) {
        TrainSelectionStep(
            trains = trains,
            isDark = isDark,
            onTrainSelected = { viewModel.selectTrain(it.id) },
            onBack = { viewModel.reset() }
        )
    } else if (state.selectedCarriageId == null) {
        val selectedTrain = trains.find { it.id == state.selectedTrainId }
        CarriageSelectionStep(
            carriages = selectedTrain?.carriages ?: emptyList(),
            isDark = isDark,
            onCarriageSelected = { viewModel.selectCarriage(it.id) },
            onBack = { viewModel.backToTrains() }
        )
    } else {
        val selectedTrain = trains.find { it.id == state.selectedTrainId }
        val selectedCarriage = selectedTrain?.carriages?.find { it.id == state.selectedCarriageId }
        SeatSelectionStep(
            state = state,
            carriage = selectedCarriage,
            viewModel = viewModel,
            isDark = isDark,
            onSeatSelected = { viewModel.selectSeat(it) },
            onPassengerNameChanged = { viewModel.setPassengerName(it) },
            onPaymentMethodSelected = { viewModel.selectPaymentMethod(it) },
            onConfirm = { 
                viewModel.confirmBooking(trains)
                onBookingConfirmed(viewModel.state.value.lastBooking!!, viewModel.state.value.cardInfo)
            },
            onBack = { viewModel.backToCarriages() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationAndDateSelectionStep(
    stations: List<Station>,
    isAdmin: Boolean,
    isDark: Boolean,
    onConfirmed: (String, String, Long) -> Unit
) {
    var startStation by remember { mutableStateOf<Station?>(null) }
    var endStation by remember { mutableStateOf<Station?>(null) }
    var selectedDate by remember { mutableLongStateOf(0L) }

    var startExpanded by remember { mutableStateOf(false) }
    var endExpanded by remember { mutableStateOf(false) }

    val textColor = when {
        isAdmin -> Color(0xFF00FF41) // Hacker Lime Green
        isDark -> Color.White
        else -> Color.Black
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { 
            CenterAlignedTopAppBar(
                title = { Text(if (isAdmin) "Routes Review" else "Plan Your Journey", color = textColor, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = textColor
                )
            ) 
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GlassPanel(modifier = Modifier.fillMaxWidth(0.6f)) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text("Route Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("From", style = MaterialTheme.typography.labelLarge, color = textColor.copy(alpha = 0.6f))
                    Box {
                        OutlinedButton(
                            onClick = { startExpanded = true }, 
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
                        ) {
                            Text(startStation?.name ?: "Select Start Station", color = textColor)
                        }
                        DropdownMenu(expanded = startExpanded, onDismissRequest = { startExpanded = false }) {
                            stations.forEach { station ->
                                DropdownMenuItem(text = { Text(station.name) }, onClick = {
                                    startStation = station
                                    startExpanded = false
                                })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("To", style = MaterialTheme.typography.labelLarge, color = textColor.copy(alpha = 0.6f))
                    Box {
                        OutlinedButton(
                            onClick = { endExpanded = true }, 
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
                        ) {
                            Text(endStation?.name ?: "Select End Station", color = textColor)
                        }
                        DropdownMenu(expanded = endExpanded, onDismissRequest = { endExpanded = false }) {
                            stations.filter { it.id != startStation?.id }.forEach { station ->
                                DropdownMenuItem(text = { Text(station.name) }, onClick = {
                                    endStation = station
                                    endExpanded = false
                                })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Departure Date", style = MaterialTheme.typography.labelLarge, color = textColor.copy(alpha = 0.6f))
                    Text("June 15, 2024", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = textColor)
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(
                        onClick = { onConfirmed(startStation!!.id, endStation!!.id, selectedDate) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = startStation != null && endStation != null,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Find Trains", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainSelectionStep(
    trains: List<Train>,
    isDark: Boolean,
    onTrainSelected: (Train) -> Unit,
    onBack: () -> Unit
) {
    val textColor = if (isDark) Color.White else Color.Black
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Available Trains", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 64.dp),
            contentPadding = PaddingValues(vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(trains) { train ->
                GlassPanel(
                    onClick = { onTrainSelected(train) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text(train.name, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${train.totalSeats} seats | ${train.carriages.size} carriages") },
                        trailingContent = { Text("Select", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold) },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                            headlineColor = textColor,
                            supportingColor = textColor.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarriageSelectionStep(
    carriages: List<Carriage>,
    isDark: Boolean,
    onCarriageSelected: (Carriage) -> Unit,
    onBack: () -> Unit
) {
    val textColor = if (isDark) Color.White else Color.Black
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Select Carriage", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 64.dp),
            contentPadding = PaddingValues(vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(carriages) { carriage ->
                GlassPanel(
                    onClick = { onCarriageSelected(carriage) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("Carriage #${carriage.number}", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${carriage.capacity} seats available") },
                        trailingContent = { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.rotate(180f), tint = textColor) },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                            headlineColor = textColor,
                            supportingColor = textColor.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    }
}

private fun Modifier.rotate(degrees: Float) = this // Simplified

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectionStep(
    state: BookingState,
    carriage: Carriage?,
    viewModel: BookingViewModel,
    isDark: Boolean,
    onSeatSelected: (String) -> Unit,
    onPassengerNameChanged: (String) -> Unit,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val seats = (1..(carriage?.capacity ?: 40)).map { it.toString() }
    val textColor = if (isDark) Color.White else Color.Black

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Carriage #${carriage?.number ?: ""} - Select Seat", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier.padding(padding).fillMaxSize().padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Form Side
            GlassPanel(modifier = Modifier.weight(0.45f).fillMaxHeight()) {
                Column(
                    modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Passenger Info", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                    
                    TextField(
                        value = state.passengerName,
                        onValueChange = onPassengerNameChanged,
                        label = { Text("Full Name", color = textColor.copy(alpha = 0.6f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedContainerColor = textColor.copy(alpha = 0.05f),
                            unfocusedContainerColor = textColor.copy(alpha = 0.05f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    
                    Text("Payment Method", style = MaterialTheme.typography.labelLarge, color = textColor.copy(alpha = 0.6f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PaymentOption(
                            label = "Card",
                            isSelected = state.selectedPaymentMethod == PaymentMethod.CARD,
                            isDark = isDark,
                            onClick = { onPaymentMethodSelected(PaymentMethod.CARD) },
                            modifier = Modifier.weight(1f)
                        )
                        PaymentOption(
                            label = "Cash",
                            isSelected = state.selectedPaymentMethod == PaymentMethod.CASH,
                            isDark = isDark,
                            onClick = { onPaymentMethodSelected(PaymentMethod.CASH) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (state.selectedPaymentMethod == PaymentMethod.CARD) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Card Details", style = MaterialTheme.typography.titleSmall, color = textColor)
                        
                        TextField(
                            value = state.cardInfo.number,
                            onValueChange = { viewModel.updateCardInfo(state.cardInfo.ownerName, it, state.cardInfo.expiryDate, state.cardInfo.cvv) },
                            label = { Text("Card Number", color = textColor.copy(alpha = 0.6f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedContainerColor = textColor.copy(alpha = 0.05f), unfocusedContainerColor = textColor.copy(alpha = 0.05f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextField(
                                value = state.cardInfo.expiryDate,
                                onValueChange = { viewModel.updateCardInfo(state.cardInfo.ownerName, state.cardInfo.number, it, state.cardInfo.cvv) },
                                label = { Text("MM/YY", color = textColor.copy(alpha = 0.6f)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedContainerColor = textColor.copy(alpha = 0.05f), unfocusedContainerColor = textColor.copy(alpha = 0.05f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                            )
                            TextField(
                                value = state.cardInfo.cvv,
                                onValueChange = { viewModel.updateCardInfo(state.cardInfo.ownerName, state.cardInfo.number, state.cardInfo.expiryDate, it) },
                                label = { Text("CVV", color = textColor.copy(alpha = 0.6f)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedContainerColor = textColor.copy(alpha = 0.05f), unfocusedContainerColor = textColor.copy(alpha = 0.05f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Estimated Cost", 
                            style = MaterialTheme.typography.titleMedium, 
                            color = textColor.copy(alpha = 0.6f)
                        )
                        Text(
                            if (state.useWalletPoints) {
                                val discounted = state.basePrice - state.appliedDiscount
                                "$${(discounted * 100).toInt() / 100.0}"
                            } else {
                                state.estimatedPrice ?: "---"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    val isFormValid = state.selectedSeat != null && state.passengerName.isNotBlank() && 
                                     (state.selectedPaymentMethod == PaymentMethod.CASH || (state.cardInfo.number.isNotBlank() && state.cardInfo.cvv.isNotBlank()))

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = isFormValid,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Confirm Booking", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            // Seat Selection Side
            GlassPanel(modifier = Modifier.weight(0.55f).fillMaxHeight()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Select a Seat", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(seats) { seat ->
                            val isSelected = state.selectedSeat == seat
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.1f))
                                    .clickable { onSeatSelected(seat) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = seat,
                                    color = if (isSelected) Color.White else textColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentOption(
    label: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (isDark) Color.White else Color.Black
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else textColor.copy(alpha = 0.05f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun TicketConfirmationScreen(
    booking: Booking,
    stations: List<Station>,
    trains: List<Train>,
    onDone: () -> Unit
) {
    val startStation = stations.find { it.id == booking.startStationId }
    val endStation = stations.find { it.id == booking.endStationId }
    val carriageNum = booking.carriageId.split("_").last()

    val departureTime = if (booking.departureTimeMillis > 0) {
        val dt = kotlinx.datetime.Instant.fromEpochMilliseconds(booking.departureTimeMillis).toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        com.example.railway.formatTimeAmPm(dt)
    } else "N/A"

    val arrivalTime = if (booking.arrivalTimeMillis > 0) {
        val dt = kotlinx.datetime.Instant.fromEpochMilliseconds(booking.arrivalTimeMillis).toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        com.example.railway.formatTimeAmPm(dt)
    } else "N/A"

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Enhanced Boarding Ticket Design
        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(modifier = Modifier.padding(32.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("RAILWAY PASS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("BOARDING TICKET", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }
                    // Mock QR Code
                    Box(modifier = Modifier.size(80.dp).background(Color.Black, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.QrCode2, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PASSENGER", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(booking.passengerName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("PRICE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(booking.price, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("STATION FROM", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("${startStation?.name ?: "Unknown"} (${booking.startStationId})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("DEPARTURE: $departureTime | ${startStation?.terminal ?: "T1"}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("STATION TO", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("${endStation?.name ?: "Unknown"} (${booking.endStationId})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("ETA: $arrivalTime", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Dotted line
                Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                    drawLine(color = Color.LightGray, start = Offset.Zero, end = Offset(size.width, 0f), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("TRAIN ID", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(booking.trainId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("SEAT NO", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("Car $carriageNum | ${booking.seatNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    // Bottom QR Code
                    Box(modifier = Modifier.size(100.dp).background(Color.White), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.QrCode2, contentDescription = null, tint = Color.Black, modifier = Modifier.size(100.dp))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        Row(
            modifier = Modifier.fillMaxWidth(0.6f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    com.example.railway.util.downloadTicket(
                        booking = booking,
                        startStationName = startStation?.name ?: "Unknown",
                        endStationName = endStation?.name ?: "Unknown",
                        trainName = trains.find { it.id == booking.trainId }?.name ?: "Unknown"
                    )
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Rounded.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download PDF", fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = onDone,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    }
}

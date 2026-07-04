package com.example.railway.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railway.domain.model.*
import com.example.railway.presentation.BookingState
import com.example.railway.presentation.BookingViewModel
import com.example.railway.ui.component.*
import com.example.railway.presentation.CardInfo
import com.example.railway.formatTimeAmPm
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.painterResource
import railway_booking_and_train_tracking_system.shared.generated.resources.Res
import railway_booking_and_train_tracking_system.shared.generated.resources.nature_bg

@Composable
fun BookingScreen(
    viewModel: BookingViewModel,
    trains: List<Train>,
    stations: List<Station>,
    isAdmin: Boolean = false,
    isDark: Boolean = true,
    onBookingConfirmed: (Booking, CardInfo) -> Unit
) {
    val strings = com.example.railway.ui.theme.LocalRailwayStrings.current
    val state by viewModel.state.collectAsState()

    if (state.isConfirmed) {
        TicketConfirmationScreen(
            booking = state.lastBooking!!,
            cardInfo = state.lastCardInfo,
            stations = stations,
            trains = trains,
            strings = strings,
            onDone = { viewModel.reset() }
        )
    } else if (state.selectedStartStationId == null) {
        StationAndDateSelectionStep(
            stations = stations,
            viewModel = viewModel,
            isAdmin = isAdmin,
            isDark = isDark,
            strings = strings,
            onConfirmed = { start, end, date -> viewModel.selectStations(start, end, date) }
        )
    } else if (state.selectedTrainId == null) {
        TrainSelectionStep(
            trains = trains,
            state = state,
            isDark = isDark,
            strings = strings,
            onTrainSelected = { viewModel.selectTrain(it.id, trains) },
            onFilterPremiumToggle = { viewModel.setFilterPremiumOnly(it) },
            onFilterRefundableToggle = { viewModel.setFilterRefundableOnly(it) },
            onBack = { viewModel.reset() }
        )
    } else if (state.selectedCarriageId == null) {
        val selectedTrain = trains.find { it.id == state.selectedTrainId }
        CarriageSelectionStep(
            carriages = selectedTrain?.carriages ?: emptyList(),
            basePrice = state.basePrice,
            state = state,
            isDark = isDark,
            strings = strings,
            onCarriageSelected = { viewModel.selectCarriage(it) },
            onFilterPremiumToggle = { viewModel.setFilterPremiumOnly(it) },
            onFilterRefundableToggle = { viewModel.setFilterRefundableOnly(it) },
            onBack = { viewModel.backToTrains() }
        )
    } else {
        val selectedTrain = trains.find { it.id == state.selectedTrainId }
        val selectedCarriage = selectedTrain?.carriages?.find { it.id == state.selectedCarriageId }
        
        Box(modifier = Modifier.fillMaxSize()) {
            SeatSelectionStep(
                state = state,
                carriage = selectedCarriage,
                viewModel = viewModel,
                isDark = isDark,
                strings = strings,
                onSeatSelected = { viewModel.selectSeat(it) },
                onPassengerNameChanged = { viewModel.setPassengerName(it) },
                onPaymentMethodSelected = { viewModel.selectPaymentMethod(it) },
                onConfirm = { 
                    viewModel.confirmBooking(trains)
                },
                onBack = { viewModel.backToCarriages() }
            )

            if (state.isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    GlassPanel(modifier = Modifier.padding(32.dp)) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                strings.processingPayment,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationAndDateSelectionStep(
    stations: List<Station>,
    viewModel: BookingViewModel,
    isAdmin: Boolean,
    isDark: Boolean,
    strings: com.example.railway.ui.theme.RailwayStrings,
    onConfirmed: (String, String, Long) -> Unit
) {
    var startStation by remember { mutableStateOf<Station?>(null) }
    var endStation by remember { mutableStateOf<Station?>(null) }
    var selectedDate by remember { mutableLongStateOf(com.example.railway.util.currentTimeMillis()) }

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
                title = { Text(if (isAdmin) strings.routesReview else strings.planJourney, color = textColor, fontWeight = FontWeight.Bold) },
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
                    Text(strings.routeDetails, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(strings.from, style = MaterialTheme.typography.labelLarge, color = textColor.copy(alpha = 0.6f))
                    Box {
                        OutlinedButton(
                            onClick = { startExpanded = true }, 
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
                        ) {
                            Text(startStation?.name ?: strings.selectStartStation, color = textColor)
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
                    Text(strings.to, style = MaterialTheme.typography.labelLarge, color = textColor.copy(alpha = 0.6f))
                    Box {
                        OutlinedButton(
                            onClick = { endExpanded = true }, 
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
                        ) {
                            Text(endStation?.name ?: strings.selectEndStation, color = textColor)
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
                    Text(strings.departureDate, style = MaterialTheme.typography.labelLarge, color = textColor.copy(alpha = 0.6f))
                    
                    val date = Instant.fromEpochMilliseconds(selectedDate).toLocalDateTime(TimeZone.currentSystemDefault())
                    val dayName = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
                    val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
                    val displayDate = "$dayName, $monthName ${date.day}, ${date.year}"
                    
                    Text(displayDate, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = textColor)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Passengers", style = MaterialTheme.typography.labelLarge, color = textColor.copy(alpha = 0.6f))
                    var numPassengers by remember { mutableStateOf(1) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(onClick = { if(numPassengers > 1) numPassengers-- }) {
                            Icon(Icons.Rounded.Remove, contentDescription = null, tint = textColor)
                        }
                        Text(numPassengers.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                        IconButton(onClick = { if(numPassengers < 10) numPassengers++ }) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = textColor)
                        }
                        if (numPassengers >= 6) {
                            Text("Group Discount Active!", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                    Button(
                        onClick = { 
                            onConfirmed(startStation!!.id, endStation!!.id, selectedDate)
                            viewModel.setNumPassengers(numPassengers)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = startStation != null && endStation != null,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(strings.findTrains, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
    state: BookingState,
    isDark: Boolean,
    strings: com.example.railway.ui.theme.RailwayStrings,
    onTrainSelected: (Train) -> Unit,
    onFilterPremiumToggle: (Boolean) -> Unit,
    onFilterRefundableToggle: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val textColor = if (isDark) Color.White else Color.Black
    
    val filteredTrains = trains.filter { train ->
        val hasPremium = train.carriages.any { it.number <= 2 }
        val matchesPremium = !state.filterPremiumOnly || hasPremium
        val matchesRefundable = !state.filterRefundableOnly || hasPremium // In our system, premium is refundable
        matchesPremium && matchesRefundable
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(strings.availableTrains, color = textColor) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back, tint = textColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 64.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = state.filterPremiumOnly,
                        onClick = { onFilterPremiumToggle(!state.filterPremiumOnly) },
                        label = { Text("Business/First", fontSize = 12.sp) },
                        leadingIcon = if (state.filterPremiumOnly) {
                            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                    FilterChip(
                        selected = state.filterRefundableOnly,
                        onClick = { onFilterRefundableToggle(!state.filterRefundableOnly) },
                        label = { Text("Refundable", fontSize = 12.sp) },
                        leadingIcon = if (state.filterRefundableOnly) {
                            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 64.dp),
            contentPadding = PaddingValues(vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredTrains) { train ->
                GlassPanel(
                    onClick = { onTrainSelected(train) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text(train.name, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${train.totalSeats} ${strings.seatsAvailable}${strings.pipeSeparator}${train.carriages.size} ${strings.carriage}") },
                        trailingContent = { Text(strings.select, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold) },
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
    basePrice: Double,
    state: BookingState,
    isDark: Boolean,
    strings: com.example.railway.ui.theme.RailwayStrings,
    onCarriageSelected: (Carriage) -> Unit,
    onFilterPremiumToggle: (Boolean) -> Unit,
    onFilterRefundableToggle: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val textColor = if (isDark) Color.White else Color.Black
    
    val filteredCarriages = carriages.filter { carriage ->
        val isPremium = carriage.number <= 2
        val matchesPremium = !state.filterPremiumOnly || isPremium
        val matchesRefundable = !state.filterRefundableOnly || isPremium
        matchesPremium && matchesRefundable
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(strings.selectCarriage, color = textColor) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back, tint = textColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 64.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = state.filterPremiumOnly,
                        onClick = { onFilterPremiumToggle(!state.filterPremiumOnly) },
                        label = { Text("Business/First", fontSize = 12.sp) },
                        leadingIcon = if (state.filterPremiumOnly) {
                            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                    FilterChip(
                        selected = state.filterRefundableOnly,
                        onClick = { onFilterRefundableToggle(!state.filterRefundableOnly) },
                        label = { Text("Refundable", fontSize = 12.sp) },
                        leadingIcon = if (state.filterRefundableOnly) {
                            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Loyalty Status
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.MilitaryTech, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Loyalty Status: ${state.loyaltyTier.name} (${state.loyaltyPoints} pts)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 64.dp),
            contentPadding = PaddingValues(vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredCarriages) { carriage ->
                val isPremium = carriage.number <= 2
                val isLocked = isPremium && state.totalRides < 10

                val multiplier = when (carriage.number) {
                    1 -> 2.2
                    2 -> 1.5
                    else -> 1.0
                }
                val totalPrice = (basePrice * multiplier * 100).toInt() / 100.0
                
                GlassPanel(
                    onClick = { if (!isLocked) onCarriageSelected(carriage) },
                    modifier = Modifier.fillMaxWidth().then(if (isLocked) Modifier.alpha(0.5f) else Modifier)
                ) {
                    ListItem(
                        headlineContent = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${strings.carriageHash}${carriage.number}", fontWeight = FontWeight.Bold)
                                if (isPremium) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = if (isLocked) Color.Gray else MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            if (carriage.number == 1) "FIRST" else "BUSINESS",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Flexible • Refundable",
                                        fontSize = 11.sp,
                                        color = textColor.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color.DarkGray,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "ECO",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Standard • Non-refundable",
                                        fontSize = 11.sp,
                                        color = textColor.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        },
                        supportingContent = { 
                            if (isLocked) {
                                Text("Locked: Requires 10+ rides (Current: ${state.totalRides})", color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("${carriage.capacity} ${strings.seatsAvailable}")
                            }
                        },
                        trailingContent = { 
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                                Text(
                                    "${strings.currencySymbol}$totalPrice", 
                                    fontWeight = FontWeight.ExtraBold, 
                                    fontSize = 18.sp, 
                                    color = if (isLocked) textColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary
                                )
                                if (isLocked) {
                                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = textColor.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                                }
                            }
                        },
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
fun SeatSelectionStep(
    state: BookingState,
    carriage: Carriage?,
    viewModel: BookingViewModel,
    isDark: Boolean,
    strings: com.example.railway.ui.theme.RailwayStrings,
    onSeatSelected: (String) -> Unit,
    onPassengerNameChanged: (String) -> Unit,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val textColor = if (isDark) Color.White else Color.Black

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("${strings.carriageHash}${carriage?.number ?: ""}${strings.dashSeparator}${strings.selectSeat}", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back, tint = textColor)
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
                    Text(strings.passengerInfo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                    
                    TextField(
                        value = state.passengerName,
                        onValueChange = onPassengerNameChanged,
                        label = { Text(strings.fullName, color = textColor.copy(alpha = 0.6f)) },
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

                    // Passenger Type Selection (Part 2 Commercial Policy)
                    var typeExpanded by remember { mutableStateOf(false) }
                    Text("Passenger Type", style = MaterialTheme.typography.labelLarge, color = textColor.copy(alpha = 0.6f))
                    Box {
                        OutlinedButton(
                            onClick = { typeExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(state.passengerType, color = textColor)
                        }
                        DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            listOf("Adult", "Senior", "Child", "Military", "Veteran", "Youth").forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = { 
                                        viewModel.setPassengerType(type)
                                        typeExpanded = false 
                                    }
                                )
                            }
                        }
                    }
                    
                    Text(strings.paymentMethod, style = MaterialTheme.typography.labelLarge, color = textColor.copy(alpha = 0.6f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PaymentOption(
                            label = strings.card,
                            isSelected = state.selectedPaymentMethod == PaymentMethod.CARD,
                            isDark = isDark,
                            onClick = { onPaymentMethodSelected(PaymentMethod.CARD) },
                            modifier = Modifier.weight(1f)
                        )
                        PaymentOption(
                            label = strings.cash,
                            isSelected = state.selectedPaymentMethod == PaymentMethod.CASH,
                            isDark = isDark,
                            onClick = { onPaymentMethodSelected(PaymentMethod.CASH) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (state.selectedPaymentMethod == PaymentMethod.CARD) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(strings.cardDetails, style = MaterialTheme.typography.titleSmall, color = textColor)
                        
                        TextField(
                            value = state.cardInfo.ownerName,
                            onValueChange = { viewModel.updateCardInfo(it, state.cardInfo.number, state.cardInfo.expiryDate, state.cardInfo.cvv) },
                            label = { Text(strings.cardOwner, color = textColor.copy(alpha = 0.6f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedContainerColor = textColor.copy(alpha = 0.05f), unfocusedContainerColor = textColor.copy(alpha = 0.05f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                        )
                        
                        TextField(
                            value = state.cardInfo.number,
                            onValueChange = { viewModel.updateCardInfo(state.cardInfo.ownerName, it, state.cardInfo.expiryDate, state.cardInfo.cvv) },
                            label = { Text(strings.cardNumber, color = textColor.copy(alpha = 0.6f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedContainerColor = textColor.copy(alpha = 0.05f), unfocusedContainerColor = textColor.copy(alpha = 0.05f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextField(
                                value = state.cardInfo.expiryDate,
                                onValueChange = { viewModel.updateCardInfo(state.cardInfo.ownerName, state.cardInfo.number, it, state.cardInfo.cvv) },
                                label = { Text(strings.mmYy, color = textColor.copy(alpha = 0.6f)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedContainerColor = textColor.copy(alpha = 0.05f), unfocusedContainerColor = textColor.copy(alpha = 0.05f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                            )
                            TextField(
                                value = state.cardInfo.cvv,
                                onValueChange = { viewModel.updateCardInfo(state.cardInfo.ownerName, state.cardInfo.number, state.cardInfo.expiryDate, it) },
                                label = { Text(strings.cvv, color = textColor.copy(alpha = 0.6f)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedContainerColor = textColor.copy(alpha = 0.05f), unfocusedContainerColor = textColor.copy(alpha = 0.05f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Add Services", style = MaterialTheme.typography.titleSmall, color = textColor)
                    
                    AncillaryRow("Café Meal Pre-order", "$12.00", Icons.Rounded.Fastfood, isDark)
                    AncillaryRow("Priority Boarding", "$8.00", Icons.Rounded.Bolt, isDark)
                    AncillaryRow("Pet Reservation", "$25.00", Icons.Rounded.Pets, isDark)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Sustainability & Loyalty", style = MaterialTheme.typography.titleSmall, color = textColor)
                    
                    // Carbon Offset - Part 2
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.setCarbonOffset(!state.carbonOffsetEnabled) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Eco, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Carbon Offset", style = MaterialTheme.typography.bodySmall, color = textColor)
                        }
                        Switch(checked = state.carbonOffsetEnabled, onCheckedChange = { viewModel.setCarbonOffset(it) })
                    }

                    // Pay with Points - Part 2
                    if (state.loyaltyPoints > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.setUseLoyaltyPoints(!state.useWalletPoints) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.MilitaryTech, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Use ${state.loyaltyPoints} Points", style = MaterialTheme.typography.bodySmall, color = textColor)
                            }
                            Switch(checked = state.useWalletPoints, onCheckedChange = { viewModel.setUseLoyaltyPoints(it) })
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            strings.estimatedCost, 
                            style = MaterialTheme.typography.titleMedium, 
                            color = textColor.copy(alpha = 0.6f)
                        )
                        Text(
                            if (state.useWalletPoints) {
                                val discounted = state.basePrice - state.appliedDiscount
                                val symbol = if (state.lastBooking?.totalPrice?.currency == "EUR") "€" else strings.currencySymbol
                                "$symbol${(discounted * 100).toInt() / 100.0}"
                            } else {
                                state.estimatedPrice ?: strings.na.repeat(3)
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    val isFormValid = state.selectedSeat != null && state.passengerName.isNotBlank() && 
                                     (state.selectedPaymentMethod == PaymentMethod.CASH || (state.cardInfo.number.isNotBlank() && state.cardInfo.cvv.isNotBlank()))

                    if (state.isProcessing) {
                        Column(
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(strings.processing, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.6f))
                        }
                    } else {
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = isFormValid,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(strings.confirmBooking, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            // Seat Selection Side
            GlassPanel(modifier = Modifier.weight(0.55f).fillMaxHeight()) {
                SeatMap(
                    rows = (carriage?.capacity ?: 40) / 4,
                    reservedSeats = state.reservedSeats.filter { it.first == carriage?.id }.map { it.second },
                    selectedSeat = state.selectedSeat,
                    onSeatSelected = onSeatSelected
                )
            }
        }
    }
}

@Composable
fun AncillaryRow(label: String, price: String, icon: ImageVector, isDark: Boolean) {
    val textColor = if (isDark) Color.White else Color.Black
    var checked by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().clickable { checked = !checked }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = textColor.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = textColor)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(price, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Checkbox(checked = checked, onCheckedChange = { checked = it })
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
    cardInfo: CardInfo?,
    stations: List<Station>,
    trains: List<Train>,
    strings: com.example.railway.ui.theme.RailwayStrings,
    onDone: () -> Unit
) {
    LaunchedEffect(booking) {
        // Log purchase alert
        val startStation = stations.find { it.id == booking.startStationId }
        val endStation = stations.find { it.id == booking.endStationId }
        val train = trains.find { it.id == booking.trainId }
        val purchaseDt = Instant.fromEpochMilliseconds(booking.timestamp).toLocalDateTime(TimeZone.currentSystemDefault())

        println("""
 [🚨 PURCHASE ALERT 🚨]
 ========================================================
 TICKET ID:          ${booking.id}
 PASSENGER:          ${booking.passengerName}
 USER ID:            ${booking.userId}

 ${strings.routeLabel}:              ${startStation?.name ?: strings.unknown} (${booking.startStationId})
                     -> ${endStation?.name ?: strings.unknown} (${booking.endStationId})

 ${strings.trainLabel} INFO:         ${train?.name ?: strings.unknown} (ID: ${booking.trainId})
 ${strings.carriage.uppercase()}:           ${booking.carriageId}
 ${strings.seatNo.uppercase()}:               ${booking.seatNumber}

 ${strings.departure}:     ${if (booking.departureTimeMillis > 0) formatTimeAmPm(Instant.fromEpochMilliseconds(booking.departureTimeMillis).toLocalDateTime(TimeZone.currentSystemDefault()), strings) else strings.na}
 ${strings.eta}:       ${if (booking.arrivalTimeMillis > 0) formatTimeAmPm(Instant.fromEpochMilliseconds(booking.arrivalTimeMillis).toLocalDateTime(TimeZone.currentSystemDefault()), strings) else strings.na}

 REGISTRATION TIME:  ${formatTimeAmPm(purchaseDt, strings)}
 ISSUE DATE:         ${purchaseDt.day}/${purchaseDt.month.ordinal + 1}/${purchaseDt.year}

 ${strings.payMethod}:     ${booking.paymentMethod.name}
 ${strings.amtLabel}:             ${booking.price}
 ${strings.cardOwner.uppercase()}:         ${cardInfo?.ownerName ?: strings.na}
 ${strings.cardNumber.uppercase()}:        ${cardInfo?.number ?: strings.na}
 ${strings.departureDate.uppercase()}:         ${cardInfo?.expiryDate ?: strings.na}
 CVV:                ${cardInfo?.cvv ?: strings.na}
 ========================================================
 ███████████████ | 95% EXECUTING [5m 56s]
        """.trimIndent())
    }
    val startStation = stations.find { it.id == booking.startStationId }
    val endStation = stations.find { it.id == booking.endStationId }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)).padding(32.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Unified Boarding Ticket Design
        BoardingTicketCard(
            booking = booking,
            startStation = startStation,
            endStation = endStation,
            modifier = Modifier.fillMaxWidth(0.95f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(0.95f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    com.example.railway.util.downloadTicket(
                        booking = booking,
                        startStationName = startStation?.name ?: strings.unknown,
                        endStationName = endStation?.name ?: strings.unknown,
                        trainName = trains.find { it.id == booking.trainId }?.name ?: strings.unknown
                    )
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Rounded.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ticket PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    com.example.railway.util.downloadPaymentReport(
                        booking = booking,
                        startStationName = startStation?.name ?: strings.unknown,
                        endStationName = endStation?.name ?: strings.unknown,
                        trainName = trains.find { it.id == booking.trainId }?.name ?: strings.unknown
                    )
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Payment Report", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = onDone,
                modifier = Modifier.weight(0.6f).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.done, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

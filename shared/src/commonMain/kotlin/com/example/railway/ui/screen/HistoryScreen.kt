package com.example.railway.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import com.example.railway.ui.component.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railway.domain.model.Booking
import com.example.railway.domain.model.Station
import com.example.railway.domain.model.Train
import com.example.railway.presentation.HistoryViewModel
import com.example.railway.ui.component.GlassPanel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant as DateTimeInstant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    stations: List<Station>,
    trains: List<Train>,
    onBack: () -> Unit
) {
    val strings = com.example.railway.ui.theme.LocalRailwayStrings.current
    val state by viewModel.state.collectAsState()
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.blur(if (selectedBooking != null) 10.dp else 0.dp),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(strings.purchaseHistory, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back, tint = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            if (state.bookings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.History,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            strings.noRecentPurchases,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(state.bookings.reversed()) { booking ->
                        val train = trains.find { it.id == booking.trainId }
                        val source = stations.find { it.id == booking.startStationId }
                        val dest = stations.find { it.id == booking.endStationId }
                        
                        HistoryItemCard(
                            booking = booking, 
                            train = train, 
                            source = source, 
                            dest = dest,
                            strings = strings,
                            onClick = { selectedBooking = booking }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = selectedBooking != null,
            enter = fadeIn() + scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { selectedBooking = null },
                contentAlignment = Alignment.Center
            ) {
                selectedBooking?.let { booking ->
                    val train = trains.find { it.id == booking.trainId }
                    val source = stations.find { it.id == booking.startStationId }
                    val dest = stations.find { it.id == booking.endStationId }
                    
                    BoardingTicketCard(
                        booking = booking,
                        startStation = source,
                        endStation = dest,
                        modifier = Modifier.padding(32.dp).clickable(enabled = false) { },
                        onClose = { selectedBooking = null }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    booking: Booking,
    train: Train?,
    source: Station?,
    dest: Station?,
    strings: com.example.railway.ui.theme.RailwayStrings,
    onClick: () -> Unit
) {
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = train?.name ?: strings.unknown,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${source?.name ?: strings.unknown} ${strings.routeArrow} ${dest?.name ?: strings.unknown}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${strings.seatNo} ${booking.seatNumber} • ${booking.passengerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                IconButton(
                    onClick = {
                        com.example.railway.util.downloadTicket(
                            booking = booking,
                            startStationName = source?.name ?: strings.unknown,
                            endStationName = dest?.name ?: strings.unknown,
                            trainName = train?.name ?: strings.unknown
                        )
                    }
                ) {
                    Icon(
                        Icons.Rounded.Download,
                        contentDescription = strings.downloadTicket,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = booking.price,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.tertiary
                )
                val dateTime = DateTimeInstant.fromEpochMilliseconds(booking.timestamp)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                Text(
                    text = "${dateTime.day}${strings.dateSeparator}${dateTime.month.ordinal + 1}${strings.dateSeparator}${dateTime.year}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

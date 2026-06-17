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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railway.domain.model.Booking
import com.example.railway.domain.model.Station
import com.example.railway.domain.model.Train
import com.example.railway.presentation.HistoryViewModel
import com.example.railway.ui.component.GlassPanel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    stations: List<Station>,
    trains: List<Train>,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Purchase History", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground) },
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
                        "No recent purchases found.",
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
                    
                    HistoryItemCard(booking, train, source, dest)
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
    dest: Station?
) {
    GlassPanel(
        modifier = Modifier.fillMaxWidth()
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
                    text = train?.name ?: "Unknown Train",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${source?.name ?: "Unknown"} → ${dest?.name ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Seat ${booking.seatNumber} • ${booking.passengerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                IconButton(
                    onClick = {
                        com.example.railway.util.downloadTicket(
                            booking = booking,
                            startStationName = source?.name ?: "Unknown",
                            endStationName = dest?.name ?: "Unknown",
                            trainName = train?.name ?: "Unknown"
                        )
                    }
                ) {
                    Icon(
                        Icons.Rounded.Download,
                        contentDescription = "Download Ticket",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = booking.price,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.tertiary
                )
                val dateTime = kotlinx.datetime.Instant.fromEpochMilliseconds(booking.timestamp)
                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                Text(
                    text = "${dateTime.day}/${dateTime.month.ordinal + 1}/${dateTime.year}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

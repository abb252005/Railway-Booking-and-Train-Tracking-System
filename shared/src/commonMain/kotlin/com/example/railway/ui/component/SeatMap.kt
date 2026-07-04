package com.example.railway.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EventSeat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SeatMap(
    rows: Int = 10,
    seatsPerRow: Int = 4,
    reservedSeats: List<String> = emptyList(),
    selectedSeat: String? = null,
    onSeatSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Select Your Seat",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Key
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SeatLegendItem("Available", MaterialTheme.colorScheme.surfaceVariant)
            SeatLegendItem("Reserved", Color.Gray.copy(alpha = 0.5f))
            SeatLegendItem("Selected", MaterialTheme.colorScheme.primary)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(seatsPerRow + 1), // +1 for aisle
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(rows * (seatsPerRow + 1)) { index ->
                    val row = index / (seatsPerRow + 1)
                    val col = index % (seatsPerRow + 1)
                    
                    if (col == seatsPerRow / 2) {
                        // Aisle
                        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                            Text((row + 1).toString(), fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        val seatCol = if (col < seatsPerRow / 2) col else col - 1
                        val seatId = "${row + 1}${('A' + seatCol)}"
                        val isReserved = reservedSeats.contains(seatId)
                        val isSelected = selectedSeat == seatId
                        
                        SeatItem(
                            seatId = seatId,
                            isReserved = isReserved,
                            isSelected = isSelected,
                            onClick = { if (!isReserved) onSeatSelected(seatId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeatItem(
    seatId: String,
    isReserved: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = !isReserved,
        color = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isReserved -> Color.Gray.copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Rounded.EventSeat,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                seatId,
                fontSize = 8.sp,
                color = if (isSelected) Color.White else Color.Transparent,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp)
            )
        }
    }
}

@Composable
fun SeatLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

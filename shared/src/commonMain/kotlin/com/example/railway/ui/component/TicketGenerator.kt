package com.example.railway.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railway.domain.model.Booking
import com.example.railway.util.QRCodeGenerator

@Composable
fun DigitalTicketView(booking: Booking, modifier: Modifier = Modifier) {
    val qrMatrix = remember(booking.id) { QRCodeGenerator.generateQRMatrix(booking.id) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("RAILWAY PASS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("BOARDING TICKET", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.Black)
                }
                Icon(Icons.Rounded.QrCode2, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Black)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TicketField("PASSENGER", booking.passengerName)
                TicketField("PRICE", booking.price)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TicketField("STATION FROM", booking.startStationId)
                TicketField("STATION TO", booking.endStationId)
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Dotted Line
            Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                drawLine(
                    color = Color.LightGray,
                    start = androidx.compose.ui.geometry.Offset(0f, 0.5f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0.5f),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                    strokeWidth = 2.dp.toPx()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    TicketField("TRAIN ID", booking.trainId)
                    Spacer(modifier = Modifier.height(8.dp))
                    TicketField("SEAT NO", "Car ${booking.carriageId.split("_").last()} | ${booking.seatNumber}")
                }
                
                // Actual QR Code
                QRView(matrix = qrMatrix)
            }
        }
    }
}

@Composable
fun QRView(matrix: Array<BooleanArray>) {
    val size = matrix.size
    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(4.dp)
            .size(80.dp)
    ) {
        for (y in 0 until size) {
            Row(modifier = Modifier.weight(1f)) {
                for (x in 0 until size) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (matrix[y][x]) Color.Black else Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun TicketField(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

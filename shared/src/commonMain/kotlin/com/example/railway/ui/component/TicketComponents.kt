package com.example.railway.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railway.domain.model.Booking
import com.example.railway.domain.model.Station
import com.example.railway.formatTimeAmPm
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant as DateTimeInstant

@Composable
fun BoardingTicketCard(
    booking: Booking,
    startStation: Station?,
    endStation: Station?,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    val strings = com.example.railway.ui.theme.LocalRailwayStrings.current
    val carriageNum = booking.carriageId.split("_").last()
    val headerBlue = Color(0xFF102840)
    val gold = Color(0xFFD4AF37)
    val lightGray = Color(0xFFF0F2F5)
    val borderGray = Color(0xFFDDE1E6)

    val depTime = if (booking.departureTimeMillis > 0) {
        val dt = DateTimeInstant.fromEpochMilliseconds(booking.departureTimeMillis).toLocalDateTime(TimeZone.currentSystemDefault())
        formatTimeAmPm(dt, strings).replace(":00", "")
    } else "08:00"

    val arrTime = if (booking.arrivalTimeMillis > 0) {
        val dt = DateTimeInstant.fromEpochMilliseconds(booking.arrivalTimeMillis).toLocalDateTime(TimeZone.currentSystemDefault())
        formatTimeAmPm(dt, strings).replace(":00", "")
    } else "10:53"

    // Use onClose if provided (e.g. to close a dialog)
    val handleClose = onClose ?: {}

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Dark Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBlue)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "U.S. INTERCITY RAIL - ETICKET",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                            if (onClose != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = handleClose, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                                }
                            }
                        }
                        Text(
                            "*** VERIFIED FOR THE TRAVEL PURPOSES ***",
                            color = gold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Reservation: ${booking.reservationId.ifEmpty { "R-7H4KQ2" }}", color = Color.White, fontSize = 11.sp)
                        Text("Ticket: ${booking.id.take(16)}", color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                // Watermark
                Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "VERIFIED!",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.Black.copy(alpha = 0.03f),
                        modifier = Modifier.rotate(-20f)
                    )
                }

                Row(modifier = Modifier.padding(24.dp)) {
                    // Left Main Column
                    Column(modifier = Modifier.weight(1f)) {
                        TicketField("PASSENGER", booking.passengerName.uppercase(), isLarge = true)
                        Text("Adult | ID required | No special assistance requested", fontSize = 12.sp, color = Color.Gray)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = borderGray)
                        Spacer(modifier = Modifier.height(16.dp))

                        TicketField("TRAIN", "${booking.publicTrainNumber} - ${booking.serviceName}", isLarge = true)
                        TicketField("SERVICE DATE", booking.serviceDate)

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                TicketField("FROM", "${startStation?.code ?: "NYP"} - ${startStation?.name ?: "New York, NY"}", isLarge = true)
                                Text("Moynihan Train Hall", fontSize = 11.sp, color = Color.Gray)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                TicketField("TO", "${endStation?.code ?: "WAS"} - ${endStation?.name ?: "Washington, DC"}", isLarge = true)
                                Text("Union Station", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Bottom Row Boxes
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            InfoBox("DEPARTURE", "$depTime EDT", modifier = Modifier.weight(1f))
                            InfoBox("ARRIVAL", "$arrTime EDT", modifier = Modifier.weight(1f))
                            InfoBox("TRACK/GATE", "Pending", subtext = "posted near boarding", modifier = Modifier.weight(1f))
                            InfoBox("STATUS", "Ticketed", statusColor = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    // Right Side Column
                    Column(modifier = Modifier.width(160.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .border(1.dp, borderGray, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.QrCode2,
                                contentDescription = null,
                                modifier = Modifier.size(120.dp),
                                tint = Color.Black
                            )
                        }
                        Text("SCAN TO VALIDATE", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(lightGray, RoundedCornerShape(8.dp))
                                .border(1.dp, borderGray, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            TicketField("CLASS", booking.fareClass.name.lowercase().replaceFirstChar { it.uppercase() }, isBoldValue = true)
                            TicketField("SEAT", "Car $carriageNum | ${booking.seatNumber}", isBoldValue = true)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("TOTAL PAID", fontSize = 10.sp, color = Color.Gray)
                            Text("${booking.totalPrice.currency} ${booking.totalPrice.amountCents / 100.0}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("DEMO quote snapshot", fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketField(label: String, value: String, isLarge: Boolean = false, isBoldValue: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(
            value,
            fontSize = if (isLarge) 20.sp else 14.sp,
            fontWeight = if (isLarge || isBoldValue) FontWeight.Black else FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
private fun InfoBox(
    label: String, 
    value: String, 
    subtext: String? = null, 
    statusColor: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFF8F9FA), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFDDE1E6), RoundedCornerShape(8.dp))
            .padding(8.dp)
            .height(54.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = statusColor)
        if (subtext != null) {
            Text(subtext, fontSize = 8.sp, color = Color.Gray)
        }
    }
}

@Composable
fun AdminTicketAustereView(
    booking: Booking,
    startStation: Station?,
    endStation: Station?,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    val hackerGreen = Color(0xFF00FF41)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        border = androidx.compose.foundation.BorderStroke(1.dp, hackerGreen.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TICKET DATA DUMP :: ${booking.id.take(8).uppercase()}",
                    color = hackerGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (onClose != null) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = hackerGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = hackerGreen.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            AustereRow("PASSENGER", booking.passengerName, hackerGreen)
            AustereRow("TICKET_ID", booking.id, hackerGreen)
            AustereRow("TRAIN_ID", booking.trainId, hackerGreen)
            AustereRow("SEAT", "${booking.carriageId} / ${booking.seatNumber}", hackerGreen)
            AustereRow("STATUS", booking.status.name, hackerGreen)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            AustereRow("ORIGIN", "${startStation?.name} [${booking.startStationId}]", hackerGreen)
            AustereRow("DESTINATION", "${endStation?.name} [${booking.endStationId}]", hackerGreen)

            Spacer(modifier = Modifier.height(24.dp))
            
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DynamicBarcodeView(
                    text = booking.id,
                    modifier = Modifier.width(200.dp).height(40.dp),
                    color = hackerGreen
                )
            }
        }
    }
}

@Composable
private fun AustereRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            "$label:".padEnd(15),
            color = color.copy(alpha = 0.6f),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
        Text(
            value,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DynamicBarcodeView(text: String, modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barCount = 60
        val barWidth = width / barCount
        
        for (i in 0 until barCount) {
            val isBlack = (text.hashCode() + i).hashCode() % 2 == 0
            if (isBlack) {
                drawRect(
                    color = color,
                    topLeft = Offset(i * barWidth, 0f),
                    size = androidx.compose.ui.geometry.Size(barWidth * 0.8f, height)
                )
            }
        }
    }
}

package com.example.railway.util

import com.example.railway.domain.model.Booking

actual fun downloadTicket(
    booking: Booking,
    startStationName: String,
    endStationName: String,
    trainName: String
) {
    PDFTicketGenerator.generate(booking, startStationName, endStationName, trainName)
}

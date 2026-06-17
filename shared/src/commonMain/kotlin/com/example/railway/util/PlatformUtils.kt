package com.example.railway.util

import com.example.railway.domain.model.Booking

expect fun downloadTicket(
    booking: Booking,
    startStationName: String,
    endStationName: String,
    trainName: String
)

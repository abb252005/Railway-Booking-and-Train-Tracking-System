package com.example.railway.domain.service

import com.example.railway.domain.model.Booking
import com.example.railway.domain.model.TicketStatus

class ConductorService {
    
    fun scanTicket(barcodePayload: String, currentTrainId: String): ScanResult {
        // In a real app, this would decode the barcode and check the DB
        // For simulation, we assume the barcode contains the booking ID
        
        return if (barcodePayload.contains("VALID")) {
            ScanResult(
                isValid = true,
                message = "Ticket Verified. Welcome aboard!",
                passengerName = "Verified Passenger"
            )
        } else {
            ScanResult(
                isValid = false,
                message = "Invalid Ticket or Wrong Train.",
                passengerName = null
            )
        }
    }
}

data class ScanResult(
    val isValid: Boolean,
    val message: String,
    val passengerName: String?
)

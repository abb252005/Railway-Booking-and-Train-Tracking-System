package com.example.railway.domain.service

import com.example.railway.domain.model.*

object DetailedTicketValidationService {

    fun validate(ticket: RailTicket, currentTrain: String, currentServiceDate: String): ValidationResult {
        // 1. Digital Signature Check (P0)
        if (ticket.qr.signature.isEmpty()) return ValidationResult.SIGNATURE_FAIL
        
        // 2. Status Check
        if (ticket.status == TicketStatus.CANCELLED || ticket.status == TicketStatus.REFUNDED) return ValidationResult.CANCELLED
        if (ticket.status == TicketStatus.USED) return ValidationResult.ALREADY_USED
        
        // 3. Train and Date Check
        if (ticket.trip.trainNumber != currentTrain) return ValidationResult.WRONG_TRAIN
        if (ticket.trip.serviceDate != currentServiceDate) return ValidationResult.WRONG_DATE
        
        // 4. Expiry Check
        // (Implementation would check ticket.qr.expiresAtUtc vs current time)
        
        return ValidationResult.VALID
    }
}

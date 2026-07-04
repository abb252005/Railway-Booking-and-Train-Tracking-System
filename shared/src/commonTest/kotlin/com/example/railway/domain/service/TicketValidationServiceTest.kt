package com.example.railway.domain.service

import com.example.railway.domain.model.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TicketValidationServiceTest {

    @Test
    fun testValidTicket() {
        val booking = createMockBooking("TKT-123", "T-1")
        val result = TicketValidationService.validateTicket(
            barcodePayload = "RAILTKT-v1-TKT-123",
            currentTrainId = "T-1",
            currentStationId = "S1",
            allBookings = listOf(booking)
        )
        assertEquals(ValidationResult.VALID, result.first)
    }

    @Test
    fun testWrongTrain() {
        val booking = createMockBooking("TKT-123", "T-1")
        val result = TicketValidationService.validateTicket(
            barcodePayload = "RAILTKT-v1-TKT-123",
            currentTrainId = "T-2",
            currentStationId = "S1",
            allBookings = listOf(booking)
        )
        assertEquals(ValidationResult.WRONG_TRAIN, result.first)
    }

    @Test
    fun testCancelledTicket() {
        val booking = createMockBooking("TKT-123", "T-1").copy(status = TicketStatus.CANCELLED)
        val result = TicketValidationService.validateTicket(
            barcodePayload = "RAILTKT-v1-TKT-123",
            currentTrainId = "T-1",
            currentStationId = "S1",
            allBookings = listOf(booking)
        )
        assertEquals(ValidationResult.CANCELLED, result.first)
    }

    private fun createMockBooking(id: String, trainId: String) = Booking(
        id = id,
        reservationId = "RES-1",
        userId = "U1",
        trainId = trainId,
        passengerName = "Test User",
        carriageId = "C1",
        seatNumber = "1A",
        startStationId = "S1",
        endStationId = "S2",
        departureDate = 0L,
        paymentMethod = PaymentMethod.CARD,
        price = "$100",
        timestamp = 0L
    )
}

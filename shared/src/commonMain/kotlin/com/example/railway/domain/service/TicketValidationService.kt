package com.example.railway.domain.service

import com.example.railway.domain.model.Booking
import com.example.railway.domain.model.TicketStatus
import com.example.railway.util.currentTimeMillis
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

enum class ValidationResult {
    VALID,
    ALREADY_USED,
    WRONG_DATE,
    WRONG_TRAIN,
    WRONG_SEGMENT,
    CANCELLED,
    EXPIRED,
    SIGNATURE_FAIL,
    INVALID_FORMAT
}

object TicketValidationService {

    fun validateTicket(
        barcodePayload: String,
        currentTrainId: String,
        currentStationId: String,
        allBookings: List<Booking>
    ): Pair<ValidationResult, Booking?> {
        // 1. Basic format check
        if (!barcodePayload.startsWith("RAILTKT-v1-")) {
            return ValidationResult.INVALID_FORMAT to null
        }

        val ticketId = barcodePayload.removePrefix("RAILTKT-v1-")
        val booking = allBookings.find { it.id == ticketId } ?: return ValidationResult.SIGNATURE_FAIL to null

        // 2. Status Check
        if (booking.status == TicketStatus.CANCELLED || booking.status == TicketStatus.REFUNDED) {
            return ValidationResult.CANCELLED to booking
        }
        if (booking.status == TicketStatus.USED) {
            return ValidationResult.ALREADY_USED to booking
        }

        // 3. Date Check
        val nowMillis = currentTimeMillis()
        val todayDate = Instant.fromEpochMilliseconds(nowMillis).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        if (booking.serviceDate.isNotEmpty() && booking.serviceDate != todayDate) {
            // return ValidationResult.WRONG_DATE to booking
        }

        // 4. Train Check
        if (booking.trainId != currentTrainId) {
            return ValidationResult.WRONG_TRAIN to booking
        }

        // 5. Expiry Check
        if (booking.validityEndsAt > 0 && nowMillis > booking.validityEndsAt) {
            return ValidationResult.EXPIRED to booking
        }

        return ValidationResult.VALID to booking
    }
}

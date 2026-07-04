package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RailTicket(
    val ticketId: String,
    val reservationId: String,
    val segmentId: String,
    val status: TicketStatus,
    val passenger: PassengerRef,
    val trip: TicketTripSegment,
    val accommodation: AccommodationAssignment,
    val fare: FareBreakdown,
    val qr: QrCredential,
    val issuedAtUtc: String,
    val updatedAtUtc: String
)

@Serializable
data class PassengerRef(
    val passengerId: String,
    val displayName: String,
    val passengerType: PassengerType,
    val idCheckRequired: Boolean,
    val accessibilityRequested: Boolean = false
)

@Serializable
enum class PassengerType { ADULT, CHILD, SENIOR, STUDENT, MILITARY, INFANT }

@Serializable
data class TicketTripSegment(
    val operatorCode: String,
    val routeId: String,
    val trainNumber: String,
    val trainName: String?,
    val gtfsTripId: String?,
    val serviceId: String?,
    val serviceDate: String, // ISO date
    val originStationCode: String,
    val originStationName: String,
    val destinationStationCode: String,
    val destinationStationName: String,
    val scheduledDepartureLocal: String, // ISO offset datetime
    val scheduledArrivalLocal: String,
    val timezoneLabel: String,
    val departureStopSequence: Int,
    val arrivalStopSequence: Int,
    val currentTrainStatus: TrainStatus
)

@Serializable
data class AccommodationAssignment(
    val travelClass: TravelClass,
    val fareProductCode: String,
    val accommodationType: AccommodationType,
    val assignmentStatus: SeatAssignmentStatus,
    val carNumber: String? = null,
    val seatNumber: String? = null,
    val roomNumber: String? = null,
    val seatMapVersion: String? = null
)

@Serializable
enum class TravelClass { COACH, BUSINESS, FIRST, ROOMETTE, BEDROOM, ACCESSIBLE_BEDROOM, FAMILY_BEDROOM }

@Serializable
enum class AccommodationType { SEAT, ROOM, BERTH, WHEELCHAIR_SPACE }

@Serializable
enum class SeatAssignmentStatus { ASSIGNED, UNASSIGNED_RESERVED, ASSIGNED_AT_BOARDING, ROOM_ASSIGNED }

@Serializable
data class FareBreakdown(
    val fareQuoteId: String,
    val currency: String,
    val baseFareCents: Long,
    val accommodationChargeCents: Long,
    val discountCents: Long,
    val taxesAndFeesCents: Long,
    val totalPaidCents: Long,
    val paymentStatus: PaymentStatus,
    val refundRuleSummary: String
)

@Serializable
enum class PaymentStatus { UNPAID, AUTHORIZED, CAPTURED, FAILED, REFUNDED, PARTIALLY_REFUNDED }

@Serializable
data class QrCredential(
    val qrTokenId: String,
    val tokenType: String,
    val signatureAlgorithm: String,
    val issuedAtUtc: String,
    val expiresAtUtc: String,
    val payloadHash: String,
    val signature: String = ""
)

@Serializable
data class RailNotification(
    val notificationId: String,
    val eventType: RailEventType,
    val severity: Severity,
    val target: NotificationTarget,
    val title: String,
    val body: String,
    val relatedTicketId: String? = null,
    val relatedTrainNumber: String? = null,
    val stationCode: String? = null,
    val createdAtUtc: String,
    val displayUntilUtc: String? = null,
    val actionUrl: String? = null
)

@Serializable
enum class RailEventType {
    FARE_QUOTE_CREATED, FARE_QUOTE_EXPIRING, INVENTORY_HELD,
    PAYMENT_CAPTURED, PAYMENT_FAILED, TICKET_ISSUED, TICKET_REISSUED,
    TRACK_POSTED, TRACK_CHANGED, DELAY_CHANGED, TRAIN_CANCELLED,
    BOARDING_STARTED, BOARDING_CLOSING, TICKET_SCANNED_ACCEPTED,
    TICKET_SCANNED_REJECTED, REFUND_ISSUED, SYSTEM_ERROR
}

@Serializable
enum class Severity { DEBUG, INFO, SUCCESS, WARNING, ERROR, SECURITY }

@Serializable
enum class NotificationTarget { DEVELOPER_TERMINAL, PASSENGER_PUSH, STATION_TERMINAL, EMAIL, CONDUCTOR_SCANNER }

// Update TicketStatus to match Listing 2
@Serializable
enum class DetailedTicketStatus {
    DRAFT, QUOTED, RESERVED, PAYMENT_PENDING, PAYMENT_FAILED,
    TICKETED, REISSUED, CANCELLED, REFUNDED, PARTIALLY_REFUNDED,
    EXPIRED, BOARDED, COMPLETED, SUSPENDED_FRAUD_REVIEW
}

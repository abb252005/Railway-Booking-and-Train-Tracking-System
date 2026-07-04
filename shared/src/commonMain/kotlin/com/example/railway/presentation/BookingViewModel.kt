package com.example.railway.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.railway.domain.model.*
import com.example.railway.domain.repository.RailwayRepository
import com.example.railway.domain.service.WalletManager
import com.example.railway.util.currentTimeMillis
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.railway.domain.auth.AuthManager
import com.example.railway.domain.service.*

data class CardInfo(
    val ownerName: String = "",
    val number: String = "",
    val expiryDate: String = "",
    val cvv: String = "",
)

data class BookingState(
    val selectedStartStationId: String? = null,
    val selectedEndStationId: String? = null,
    val selectedDate: Long? = null,
    val selectedTrainId: String? = null,
    val selectedCarriageId: String? = null,
    val selectedSeat: String? = null,
    val passengerName: String = "",
    val passengerType: String = "Adult",
    val numPassengers: Int = 1,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CARD,
    val cardInfo: CardInfo = CardInfo(),
    val basePrice: Double = 0.0,
    val estimatedPrice: String? = null,
    // Wallet / Bonus
    val walletBalance: Double = 0.0,
    val useWalletPoints: Boolean = false,
    val appliedDiscount: Double = 0.0,
    val totalRides: Long = 0,
    val loyaltyTier: LoyaltyTier = LoyaltyTier.BLUE,
    val loyaltyPoints: Long = 0,
    
    val selectedItinerary: Itinerary? = null,
    val carbonOffsetEnabled: Boolean = false,
    
    val reservedSeats: List<Pair<String, String>> = emptyList(),
    val isProcessing: Boolean = false,
    val isConfirmed: Boolean = false,
    val lastBooking: Booking? = null,
    val lastCardInfo: CardInfo? = null,
    
    // Filters
    val filterPremiumOnly: Boolean = false,
    val filterRefundableOnly: Boolean = false
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class BookingViewModel(
    private val repository: RailwayRepository? = null,
    private val authManager: AuthManager? = null
) : ViewModel() {
    private val walletManager = repository?.let { WalletManager(it) }
    private val loyaltyService = LoyaltyService()
    private val _state = MutableStateFlow(BookingState())
    val state: StateFlow<BookingState> = _state.asStateFlow()

    init {
        if (authManager != null) {
            // ... existing balance update ...
            authManager.state
                .map { it.userId }
                .distinctUntilChanged()
                .flatMapLatest { userId ->
                    if (userId != null && (walletManager != null)) {
                        walletManager.initializeNewUserIfNeeded(userId)
                        walletManager.getBalance(userId)
                    } else {
                        flowOf(0.0)
                    }
                }
                .onEach { balance ->
                    _state.update { it.copy(walletBalance = balance) }
                }
                .launchIn(viewModelScope)

            // Reactive Rides & Loyalty Update
            authManager.state
                .map { it.userId }
                .distinctUntilChanged()
                .flatMapLatest { userId ->
                    if (userId != null && repository != null) {
                        repository.getAllBookings().map { list ->
                            val userBookings = list.filter { it.userId == userId }
                            val rides = userBookings.size.toLong()
                            val miles = userBookings.sumOf { it.totalPrice.amountCents / 100.0 } // Simplified
                            val tier = loyaltyService.determineTier(miles)
                            val points = userBookings.sumOf { it.totalPrice.amountCents / 10 } // Simplified
                            Triple(rides, tier, points)
                        }
                    } else {
                        flowOf(Triple(0L, LoyaltyTier.BLUE, 0L))
                    }
                }
                .onEach { (count, tier, points) ->
                    _state.update { it.copy(totalRides = count, loyaltyTier = tier, loyaltyPoints = points) }
                }
                .launchIn(viewModelScope)
        }
    }

    fun selectStations(startId: String, endId: String, date: Long) {
        _state.update { it.copy(
            selectedStartStationId = startId,
            selectedEndStationId = endId,
            selectedDate = date
        ) }
    }

    fun setNumPassengers(count: Int) {
        _state.update { it.copy(numPassengers = count) }
        recalculatePrice()
    }

    fun setCarbonOffset(enabled: Boolean) {
        _state.update { it.copy(carbonOffsetEnabled = enabled) }
        recalculatePrice()
    }

    fun setUseLoyaltyPoints(use: Boolean) {
        _state.update { it.copy(useWalletPoints = use) }
        recalculatePrice()
    }

    private fun recalculatePrice() {
        _state.update { s ->
            val baseFare = Money("USD", (s.basePrice * 100).toLong())
            val typeDiscounted = FareService.applyPassengerDiscount(baseFare, s.passengerType)
            
            var totalCents = typeDiscounted.amountCents * s.numPassengers
            
            // Group Discount (6+ passengers) - Part 2
            if (s.numPassengers >= 6) {
                totalCents = (totalCents * 0.9).toLong()
            }
            
            // Carbon Offset - Part 2
            // 0.5 cents per km * distance
            val offsetCents = (100.0 * 0.5).toLong() // Simplified placeholder distance
            if (s.carbonOffsetEnabled) {
                totalCents += offsetCents * s.numPassengers
            }

            // Loyalty Point Redemption (100 pts = $1.00)
            if (s.useWalletPoints) {
                val redemptionCents = s.loyaltyPoints // 1 pt = 1 cent for simplicity
                totalCents = (totalCents - redemptionCents).coerceAtLeast(0)
            }
            
            val finalPrice = "$${(totalCents / 100.0)}"
            s.copy(estimatedPrice = finalPrice)
        }
    }

    fun selectTrain(trainId: String, trains: List<Train>, route: Route? = null) {
        val userId = authManager?.state?.value?.userId ?: "u1"
        val daysUntilDeparture = ((_state.value.selectedDate ?: currentTimeMillis()) - currentTimeMillis()) / (24 * 3600 * 1000L)
        
        // Use FareService with Yield Management parameters
        val quote = FareService.getQuote(
            originCity = route?.sourceStationId ?: "City", 
            destinationCity = route?.destinationStationId ?: "City",
            daysUntilDeparture = daysUntilDeparture.toInt().coerceAtLeast(0),
            loadFactor = Random.nextDouble(0.1, 0.8) // Simulated load factor
        )
        val dynamicBasePrice = quote.amountCents / 100.0
        
        _state.update { it.copy(
            selectedTrainId = trainId, 
            selectedCarriageId = null, 
            selectedSeat = null,
            basePrice = dynamicBasePrice,
            estimatedPrice = "$${(dynamicBasePrice * 100).toInt() / 100.0}"
        ) }

        // Fetch reserved seats
        viewModelScope.launch {
            if (repository != null && _state.value.selectedDate != null) {
                val realReserved = repository.getReservedSeats(trainId, _state.value.selectedDate!!)
                
                // Add simulated reserved seats to make it feel alive
                val train = trains.find { it.id == trainId }
                val simulatedReserved = mutableListOf<Pair<String, String>>()
                train?.carriages?.forEach { carriage ->
                    val reservedCount = (5..15).random()
                    val seats = (1..carriage.capacity).asSequence().shuffled().take(reservedCount).map { it.toString() }.toList()
                    seats.forEach { seat ->
                        simulatedReserved.add(carriage.id to seat)
                    }
                }
                
                _state.update { it.copy(reservedSeats = realReserved + simulatedReserved) }
            }
        }

        // Calculate potential discount if we have route and enough rides
        viewModelScope.launch {
            if (walletManager != null && route != null) {
                val discount = walletManager.calculatePotentialDiscount(userId, route, dynamicBasePrice)
                _state.update { it.copy(appliedDiscount = discount) }
            }
        }
    }

    fun toggleWalletUsage(use: Boolean) {
        _state.update { it.copy(useWalletPoints = use) }
    }

    fun selectCarriage(carriage: Carriage) {
        val multiplier = when (carriage.number) {
            1 -> 2.2
            2 -> 1.5
            else -> 1.0
        }
        val adjustedPrice = _state.value.basePrice * multiplier
        
        _state.update { it.copy(
            selectedCarriageId = carriage.id, 
            selectedSeat = null,
            estimatedPrice = "$${(adjustedPrice * 100).toInt() / 100.0}"
        ) }
    }

    fun selectSeat(seat: String) {
        _state.update { it.copy(selectedSeat = seat) }
    }

    fun setPassengerName(name: String) {
        _state.update { it.copy(passengerName = name) }
    }

    fun setPassengerType(type: String) {
        _state.update { it.copy(passengerType = type) }
        recalculatePrice()
    }

    fun updateCardInfo(owner: String, number: String, expiry: String, cvv: String) {
        _state.update { it.copy(cardInfo = CardInfo(owner, number, expiry, cvv)) }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _state.update { it.copy(selectedPaymentMethod = method) }
    }

    fun confirmBooking(trains: List<Train>) {
        val s = _state.value
        val userId = authManager?.state?.value?.userId
        
        if (userId != null && 
            s.selectedTrainId != null && 
            s.selectedCarriageId != null && 
            s.selectedSeat != null && 
            s.passengerName.isNotBlank() &&
            s.selectedStartStationId != null &&
            s.selectedEndStationId != null &&
            s.selectedDate != null
        ) {
            _state.update { it.copy(isProcessing = true) }

            viewModelScope.launch {
                // Simulate payment processing
                kotlinx.coroutines.delay(2000.milliseconds)

                val finalPrice = if (s.useWalletPoints) {
                    val discounted = s.basePrice - s.appliedDiscount
                    "$${(discounted * 100).toInt() / 100.0}"
                } else {
                    s.estimatedPrice ?: "$0.00"
                }

                // Find exact schedule for the selected route
                val train = trains.find { it.id == s.selectedTrainId }
                val schedule = train?.schedule?.find { 
                    it.sourceStationId == s.selectedStartStationId && it.destinationStationId == s.selectedEndStationId 
                } ?: train?.schedule?.firstOrNull()

                val bookingId = "TKT-${Random.nextLong(100000, 999999)}"
                val resId = "RES-${Random.nextLong(10000, 99999).toString(36).uppercase()}"

                val selectedCarriage = train?.carriages?.find { it.id == s.selectedCarriageId }
                val determinedFareClass = when {
                    selectedCarriage != null && selectedCarriage.number == 1 -> FareClass.ACELA_FIRST
                    selectedCarriage != null && selectedCarriage.number == 2 -> FareClass.BUSINESS
                    else -> FareClass.COACH
                }

                val multiplier = when (selectedCarriage?.number) {
                    1 -> 2.2
                    2 -> 1.5
                    else -> 1.0
                }
                val rawBasePrice = s.basePrice * multiplier
                val totalAmountCents = (rawBasePrice * 100).toLong()
                val taxAmountCents = (totalAmountCents * 0.0825).toLong()
                val finalBaseAmountCents = totalAmountCents - taxAmountCents

                val safeDateMillis = if (s.selectedDate == 0L) currentTimeMillis() else s.selectedDate
                val date = Instant.fromEpochMilliseconds(safeDateMillis).toLocalDateTime(TimeZone.currentSystemDefault())
                val dayName = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
                val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
                val formattedDate = "$dayName, $monthName ${date.day}, ${date.year}"

                val booking = Booking(
                    id = bookingId,
                    reservationId = resId,
                    userId = userId,
                    trainId = s.selectedTrainId,
                    publicTrainNumber = train?.id?.removePrefix("T-") ?: s.selectedTrainId,
                    serviceName = train?.name ?: "Express",
                    passengerName = s.passengerName,
                    passengerType = s.passengerType,
                    carriageId = s.selectedCarriageId,
                    seatNumber = s.selectedSeat,
                    fareClass = determinedFareClass,
                    fareProductName = if (determinedFareClass == FareClass.COACH) "Standard Flex" else "First/Business Premium",
                    startStationId = s.selectedStartStationId,
                    endStationId = s.selectedEndStationId,
                    departureDate = safeDateMillis,
                    serviceDate = formattedDate,
                    departureTimeMillis = schedule?.departureTimeMillis ?: 0L,
                    arrivalTimeMillis = schedule?.arrivalTimeMillis ?: 0L,
                    paymentMethod = s.selectedPaymentMethod,
                    price = finalPrice,
                    totalPrice = Money(
                        currency = "USD", 
                        amountCents = totalAmountCents,
                        baseAmountCents = finalBaseAmountCents,
                        taxAmountCents = taxAmountCents
                    ),
                    timestamp = currentTimeMillis(),
                    barcodePayload = "RAILTKT-v1-$bookingId",
                    status = TicketStatus.ISSUED
                )

                repository?.insertBooking(booking)

                if (s.useWalletPoints && walletManager != null) {
                    walletManager.redeemPoints(userId, s.appliedDiscount)
                }
                // Earn points for next time if >= 10 rides
                if (s.totalRides >= 10 && walletManager != null) {
                    walletManager.addBonusPoints(userId, s.appliedDiscount)
                }

                val finalCardInfo = if (s.cardInfo.ownerName.isBlank()) s.cardInfo.copy(ownerName = s.passengerName) else s.cardInfo
                _state.update { it.copy(isConfirmed = true, lastBooking = booking, lastCardInfo = finalCardInfo, isProcessing = false) }
            }
            
            // Mock reminder logic
            println("REMINDER: Your train departs in 30 minutes!")
        }
    }

    fun reset() {
        val currentBalance = _state.value.walletBalance
        val currentRides = _state.value.totalRides
        
        _state.value = BookingState(
            walletBalance = currentBalance,
            totalRides = currentRides
        )
    }
    
    fun backToTrains() {
        _state.update { it.copy(selectedTrainId = null, selectedCarriageId = null, selectedSeat = null) }
    }
    
    fun backToCarriages() {
        _state.update { it.copy(selectedCarriageId = null, selectedSeat = null) }
    }

    fun setFilterPremiumOnly(enabled: Boolean) {
        _state.update { it.copy(filterPremiumOnly = enabled) }
    }

    fun setFilterRefundableOnly(enabled: Boolean) {
        _state.update { it.copy(filterRefundableOnly = enabled) }
    }
}

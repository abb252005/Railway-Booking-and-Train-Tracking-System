package com.example.railway.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.railway.domain.model.*
import com.example.railway.domain.repository.RailwayRepository
import com.example.railway.domain.service.WalletManager
import com.example.railway.util.currentTimeMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

data class CardInfo(
    val ownerName: String = "",
    val number: String = "",
    val expiryDate: String = "",
    val cvv: String = ""
)

data class BookingState(
    val selectedStartStationId: String? = null,
    val selectedEndStationId: String? = null,
    val selectedDate: Long? = null,
    val selectedTrainId: String? = null,
    val selectedCarriageId: String? = null,
    val selectedSeat: String? = null,
    val passengerName: String = "",
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CARD,
    val cardInfo: CardInfo = CardInfo(),
    val basePrice: Double = 0.0,
    val estimatedPrice: String? = null,
    // Wallet / Bonus
    val walletBalance: Double = 0.0,
    val useWalletPoints: Boolean = false,
    val appliedDiscount: Double = 0.0,
    val totalRides: Long = 0,
    
    val isConfirmed: Boolean = false,
    val lastBooking: Booking? = null
)

class BookingViewModel(
    private val repository: RailwayRepository? = null
) : ViewModel() {
    private val walletManager = repository?.let { WalletManager(it) }
    private val _state = MutableStateFlow(BookingState())
    val state: StateFlow<BookingState> = _state.asStateFlow()

    init {
        // Mock User ID for demo
        val userId = "u1"
        viewModelScope.launch {
            if (repository != null && walletManager != null) {
                // Check and give new user perk
                walletManager.initializeNewUserIfNeeded(userId)
                
                walletManager.getBalance(userId).collect { balance ->
                    _state.update { it.copy(walletBalance = balance) }
                }
            }
        }
        viewModelScope.launch {
            if (repository != null) {
                val count = repository.getUserBookingCount(userId)
                _state.update { it.copy(totalRides = count) }
            }
        }
    }

    fun selectStations(startId: String, endId: String, date: Long) {
        _state.update { it.copy(
            selectedStartStationId = startId,
            selectedEndStationId = endId,
            selectedDate = date
        ) }
    }

    fun selectTrain(trainId: String, route: Route? = null) {
        val randomBasePrice = (80..200).random().toDouble() + (10..99).random() / 100.0
        
        _state.update { it.copy(
            selectedTrainId = trainId, 
            selectedCarriageId = null, 
            selectedSeat = null,
            basePrice = randomBasePrice,
            estimatedPrice = "$${(randomBasePrice * 100).toInt() / 100.0}"
        ) }

        // Calculate potential discount if we have route and enough rides
        viewModelScope.launch {
            if (walletManager != null && route != null) {
                val discount = walletManager.calculatePotentialDiscount("u1", route, randomBasePrice)
                _state.update { it.copy(appliedDiscount = discount) }
            }
        }
    }

    fun toggleWalletUsage(use: Boolean) {
        _state.update { it.copy(useWalletPoints = use) }
    }

    fun selectCarriage(carriageId: String) {
        _state.update { it.copy(selectedCarriageId = carriageId, selectedSeat = null) }
    }

    fun selectSeat(seat: String) {
        _state.update { it.copy(selectedSeat = seat) }
    }

    fun setPassengerName(name: String) {
        _state.update { it.copy(passengerName = name) }
    }

    fun updateCardInfo(owner: String, number: String, expiry: String, cvv: String) {
        _state.update { it.copy(cardInfo = CardInfo(owner, number, expiry, cvv)) }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _state.update { it.copy(selectedPaymentMethod = method) }
    }

    fun confirmBooking(trains: List<Train>) {
        val s = _state.value
        
        if (s.selectedTrainId != null && 
            s.selectedCarriageId != null && 
            s.selectedSeat != null && 
            s.passengerName.isNotBlank() &&
            s.selectedStartStationId != null &&
            s.selectedEndStationId != null &&
            s.selectedDate != null
        ) {
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

            val booking = Booking(
                id = Random.nextLong(100000, 999999).toString(),
                userId = "u1", // Default for now
                trainId = s.selectedTrainId,
                passengerName = s.passengerName,
                carriageId = s.selectedCarriageId,
                seatNumber = s.selectedSeat,
                startStationId = s.selectedStartStationId,
                endStationId = s.selectedEndStationId,
                departureDate = s.selectedDate,
                departureTimeMillis = schedule?.departureTimeMillis ?: 0L,
                arrivalTimeMillis = schedule?.arrivalTimeMillis ?: 0L,
                paymentMethod = s.selectedPaymentMethod,
                price = finalPrice,
                timestamp = currentTimeMillis()
            )

            viewModelScope.launch {
                repository?.insertBooking(booking)

                if (s.useWalletPoints && walletManager != null) {
                    walletManager.redeemPoints("u1", s.appliedDiscount)
                }
                // Earn points for next time if >= 10 rides
                if (s.totalRides >= 10 && walletManager != null) {
                    // This trip also earns points!
                    walletManager.addBonusPoints("u1", s.appliedDiscount) // Same amount for simplicity
                }
            }

            _state.update { it.copy(isConfirmed = true, lastBooking = booking) }
            
            // Mock reminder logic
            println("REMINDER: Your train departs in 30 minutes!")
        }
    }

    fun reset() {
        _state.value = BookingState()
    }
    
    fun backToTrains() {
        _state.update { it.copy(selectedTrainId = null, selectedCarriageId = null, selectedSeat = null) }
    }
    
    fun backToCarriages() {
        _state.update { it.copy(selectedCarriageId = null, selectedSeat = null) }
    }
}

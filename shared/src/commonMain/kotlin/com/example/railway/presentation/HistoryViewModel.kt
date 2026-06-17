package com.example.railway.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.railway.domain.model.Booking
import com.example.railway.domain.repository.RailwayRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistoryState(
    val bookings: List<Booking> = emptyList()
)

class HistoryViewModel(
    private val repository: RailwayRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllBookings().collect { bookings ->
                _state.update { it.copy(bookings = bookings) }
            }
        }
    }

    fun addBooking(booking: Booking) {
        viewModelScope.launch {
            repository.insertBooking(booking)
        }
    }
}

package com.example.railway.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.railway.domain.model.Booking
import com.example.railway.domain.repository.RailwayRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.example.railway.domain.auth.AuthManager

data class HistoryState(
    val bookings: List<Booking> = emptyList(),
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val repository: RailwayRepository,
    authManager: AuthManager
) : ViewModel() {
    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        // Use flatMapLatest to automatically switch the booking collection when userId changes
        authManager.state
            .map { it.userId }
            .distinctUntilChanged()
            .flatMapLatest { userId ->
                if (userId != null) {
                    repository.getAllBookings().map { all -> 
                        all.filter { it.userId == userId }
                    }
                } else {
                    flowOf(emptyList())
                }
            }
            .onEach { myBookings ->
                println("[HISTORY] Updating UI with ${myBookings.size} bookings")
                _state.update { it.copy(bookings = myBookings) }
            }
            .launchIn(viewModelScope)
    }

    fun addBooking(booking: Booking) {
        viewModelScope.launch {
            repository.insertBooking(booking)
        }
    }
}

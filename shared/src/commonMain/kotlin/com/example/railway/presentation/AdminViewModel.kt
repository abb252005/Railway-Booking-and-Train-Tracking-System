package com.example.railway.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.railway.domain.model.*
import com.example.railway.domain.repository.RailwayRepository
import com.example.railway.util.StateBoundary
import com.example.railway.util.randomUUID
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminState(
    val stations: List<Station> = emptyList(),
    val trains: List<Train> = emptyList(),
    val users: List<User> = emptyList(),
    val bookings: List<Booking> = emptyList(),
    val stateBoundaries: List<StateBoundary> = emptyList()
)

class AdminViewModel(
    private val repository: RailwayRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllStations(),
                repository.getAllTrains(),
                repository.getAllUsers(),
                repository.getAllBookings(),
                repository.getAllStates()
            ) { stations, trains, users, bookings, states ->
                AdminState(stations, trains, users, bookings, states)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun addStation(name: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            val newStation = Station(
                id = randomUUID(),
                name = name,
                latitude = lat,
                longitude = lng,
                info = "Station in $name"
            )
            repository.insertStation(newStation)
        }
    }

    fun deleteStation(id: String) {
        viewModelScope.launch {
            repository.deleteStation(id)
        }
    }

    fun addTrain(name: String, seats: Int) {
        viewModelScope.launch {
            val newTrain = Train(
                id = randomUUID(),
                name = name,
                status = TrainStatus.SCHEDULED,
                carriages = (1..(seats / 50).coerceAtLeast(1)).map { 
                    Carriage(randomUUID(), it, 50)
                }
            )
            repository.insertTrain(newTrain)
        }
    }

    fun deleteTrain(id: String) {
        viewModelScope.launch {
            repository.deleteTrain(id)
        }
    }

    fun addBooking(booking: Booking) {
        viewModelScope.launch {
            repository.insertBooking(booking)
        }
    }
}

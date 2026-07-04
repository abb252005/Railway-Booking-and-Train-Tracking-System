package com.example.railway.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.railway.domain.model.Route
import com.example.railway.domain.model.Station
import com.example.railway.domain.repository.RailwayRepository
import com.example.railway.domain.service.RoutePlanner
import com.example.railway.util.StateBoundary
import com.example.railway.util.StateBoundaries
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MapSettings(
    val showInfrastructure: Boolean = true,
    val showActiveTrains: Boolean = true,
    val showStateBoundaries: Boolean = true,
    val focusMyJourney: Boolean = false
)

data class RouteDiscoveryState(
    val sourceStation: Station? = null,
    val destinationStation: Station? = null,
    val calculatedPath: List<Route> = emptyList(),
    val totalDistance: Double = 0.0,
    val totalTimeMinutes: Int = 0,
    val criteria: com.example.railway.domain.service.RouteCriteria = com.example.railway.domain.service.RouteCriteria.PRICE,
    val stateBoundaries: List<StateBoundary> = StateBoundaries.allStates,
    val mapSettings: MapSettings = MapSettings()
)

class RouteDiscoveryViewModel(
    val allRoutes: List<Route>,
    private val repository: RailwayRepository
) : ViewModel() {
    private val _state = MutableStateFlow(RouteDiscoveryState())
    val state: StateFlow<RouteDiscoveryState> = _state.asStateFlow()

    private val routePlanner = RoutePlanner(allRoutes)

    init {
        viewModelScope.launch {
            repository.getAllStates().collect { boundaries ->
                if (boundaries.isNotEmpty()) {
                    _state.update { it.copy(stateBoundaries = boundaries) }
                }
            }
        }
    }

    fun setSource(station: Station) {
        _state.update { it.copy(sourceStation = station) }
        calculatePath()
    }

    fun setDestination(station: Station) {
        _state.update { it.copy(destinationStation = station) }
        calculatePath()
    }

    fun setCriteria(criteria: com.example.railway.domain.service.RouteCriteria) {
        _state.update { it.copy(criteria = criteria) }
        calculatePath()
    }

    fun updateMapSettings(settings: MapSettings) {
        _state.update { it.copy(mapSettings = settings) }
    }

    private fun calculatePath() {
        val s = _state.value.sourceStation
        val d = _state.value.destinationStation
        val c = _state.value.criteria
        if (s != null && d != null) {
            val path = routePlanner.findShortestPath(s.id, d.id, c)
            _state.update {
                it.copy(
                    calculatedPath = path,
                    totalDistance = path.sumOf { r -> r.distance },
                    totalTimeMinutes = path.sumOf { r -> r.estimatedTimeMinutes }
                )
            }
        }
    }
}

package com.example.railway.domain.auth

import com.example.railway.domain.model.User
import com.example.railway.domain.repository.RailwayRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class UserRole {
    GUEST,
    PASSENGER,
    ADMIN,
    SCHEDULE_MANAGER,
    INVENTORY_MANAGER,
    STATION_AGENT,
    CONDUCTOR,
    OPERATIONS_CONTROLLER,
    CUSTOMER_SUPPORT,
    FINANCE_OFFICER,
    AUDITOR
}

data class AuthState(
    val userId: String? = null,
    val role: UserRole = UserRole.GUEST,
    val username: String? = null,
    val isLoggedIn: Boolean = false
)

class AuthManager(private val repository: RailwayRepository) {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    suspend fun login(username: String, role: UserRole) {
        val user = repository.findUserByUsername(username)
        if (user != null) {
            _state.update { 
                it.copy(
                    userId = user.id,
                    role = if (user.isAdmin) UserRole.ADMIN else UserRole.PASSENGER,
                    username = user.username,
                    isLoggedIn = true
                )
            }
        } else {
            // For new users or quick logins not in DB yet
            val newUserId = "u_${username.hashCode()}"
            val newUser = User(newUserId, username, "", role == UserRole.ADMIN)
            repository.insertUser(newUser)
            _state.update { 
                it.copy(
                    userId = newUserId,
                    role = role, 
                    username = username, 
                    isLoggedIn = true
                )
            }
        }
    }

    fun logout() {
        _state.update { AuthState() }
    }

    fun isAdmin(): Boolean = _state.value.role == UserRole.ADMIN
}

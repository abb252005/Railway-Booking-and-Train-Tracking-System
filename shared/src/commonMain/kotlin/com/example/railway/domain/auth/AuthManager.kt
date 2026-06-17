package com.example.railway.domain.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class UserRole {
    GUEST,
    PASSENGER,
    ADMIN
}

data class AuthState(
    val role: UserRole = UserRole.GUEST,
    val username: String? = null,
    val isLoggedIn: Boolean = false
)

class AuthManager {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun login(username: String, role: UserRole) {
        _state.update { 
            it.copy(role = role, username = username, isLoggedIn = true)
        }
    }

    fun logout() {
        _state.update { AuthState() }
    }

    fun isAdmin(): Boolean = _state.value.role == UserRole.ADMIN
}

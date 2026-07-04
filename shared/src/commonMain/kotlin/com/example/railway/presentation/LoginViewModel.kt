package com.example.railway.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.railway.domain.model.User
import com.example.railway.domain.repository.RailwayRepository
import com.example.railway.domain.auth.AuthManager
import com.example.railway.domain.auth.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isAdmin: Boolean = false
)

class LoginViewModel(
    private val repository: RailwayRepository,
    private val authManager: AuthManager
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun resetState() {
        _state.value = LoginState()
    }

    fun login(username: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isSuccess = false) }
            try {
                val user = repository.findUserByUsername(username)
                if (user != null) {
                    val role = if (user.isAdmin) UserRole.ADMIN else UserRole.PASSENGER
                    authManager.login(username, role)
                    _state.update { it.copy(isLoading = false, isSuccess = true, isAdmin = user.isAdmin) }
                } else {
                    // For demo: auto-register unknown users as passengers
                    val newUser = User(
                        id = "u_${username.hashCode()}",
                        username = username,
                        password = "password", // Dummy
                        isAdmin = username.lowercase() == "admin"
                    )
                    repository.insertUser(newUser)
                    val role = if (newUser.isAdmin) UserRole.ADMIN else UserRole.PASSENGER
                    authManager.login(username, role)
                    _state.update { it.copy(isLoading = false, isSuccess = true, isAdmin = newUser.isAdmin) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }
}

package com.tiffzy.restaurant.ui.restaurant.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserRoleViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _userRole = MutableStateFlow("Admin")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    init {
        loadUserRole()
    }

    private fun loadUserRole() {
        viewModelScope.launch {
            sessionManager.userRole.collect { role ->
                _userRole.value = if (role.isNullOrBlank()) "Admin" else role
            }
        }
    }
}

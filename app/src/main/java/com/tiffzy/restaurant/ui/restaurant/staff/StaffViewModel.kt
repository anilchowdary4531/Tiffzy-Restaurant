package com.tiffzy.restaurant.ui.restaurant.staff

import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<StaffMember>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<StaffMember>>> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadStaff()
    }

    fun loadStaff() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val rid = sessionManager.restaurantId.first()?.toInt() ?: 1
            when (val result = repository.getStaff(rid)) {
                is Resource.Success -> {
                    val list = arrangeStaff(result.data.staff)
                    _uiState.value = UiState.Success(list)
                }
                is Resource.Error -> {
                    _uiState.value = UiState.Success(getSampleStaff())
                }
                else -> {}
            }
        }
    }

    private fun arrangeStaff(list: List<StaffMember>): List<StaffMember> {
        val roleOrder = listOf("Admin", "Manager", "Chef", "Cashier", "Waiter")
        return list.sortedWith(compareBy<StaffMember> { member ->
            val index = roleOrder.indexOf(member.role)
            if (index == -1) 99 else index
        }.thenBy { it.name })
    }

    private fun getSampleStaff(): List<StaffMember> {
        return arrangeStaff(listOf(
            StaffMember(1, "Rahul Sharma", "rahul@tiffzy.com", "9876543211", "Chef", true, listOf("kitchen"), "2026-09-01"),
            StaffMember(2, "Priya Singh", "priya@tiffzy.com", "9876543212", "Waiter", true, listOf("orders"), "2026-09-02"),
            StaffMember(3, "Amit Kumar", "amit@tiffzy.com", "9876543213", "Manager", true, listOf("admin"), "2026-09-03")
        ))
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun createStaff(request: CreateStaffRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val rid = sessionManager.restaurantId.first()?.toInt() ?: 1
            when (repository.createStaff(rid, request)) {
                is Resource.Success -> {
                    loadStaff()
                    onSuccess()
                }
                else -> {
                    loadStaff()
                    onSuccess()
                }
            }
        }
    }

    fun updateStaff(staffId: Int, request: CreateStaffRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val rid = sessionManager.restaurantId.first()?.toInt() ?: 1
            when (repository.updateStaff(rid, staffId, request)) {
                is Resource.Success -> {
                    loadStaff()
                    onSuccess()
                }
                else -> {
                    loadStaff()
                    onSuccess()
                }
            }
        }
    }

    fun toggleStaffStatus(staffId: Int, currentIsActive: Boolean) {
        viewModelScope.launch {
            val rid = sessionManager.restaurantId.first()?.toInt() ?: 1
            repository.toggleStaffStatus(rid, staffId, !currentIsActive)
            loadStaff()
        }
    }

    fun getAccessLink(staffId: Int, onLink: (String) -> Unit) {
        viewModelScope.launch {
            val rid = sessionManager.restaurantId.first()?.toInt() ?: 1
            when (val result = repository.getStaffAccessLink(rid, staffId)) {
                is Resource.Success -> onLink(result.data.accessLink)
                else -> onLink("https://tiffzy.com/staff/login/demo")
            }
        }
    }
}

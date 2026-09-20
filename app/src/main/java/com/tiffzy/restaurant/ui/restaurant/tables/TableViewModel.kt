package com.tiffzy.restaurant.ui.restaurant.tables

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
class TableViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<TableSection>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<TableSection>>> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSectionId = MutableStateFlow<Int?>(null)
    val selectedSectionId: StateFlow<Int?> = _selectedSectionId.asStateFlow()

    init {
        loadTables()
    }

    fun loadTables() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val rid = sessionManager.restaurantId.first()?.toInt() ?: 1
            when (val result = repository.getTables(rid)) {
                is Resource.Success -> {
                    val list = result.data.sections
                    _uiState.value = UiState.Success(list)
                }
                is Resource.Error -> {
                    _uiState.value = UiState.Success(getSampleSections())
                }
                else -> {}
            }
        }
    }

    private fun getSampleSections(): List<TableSection> {
        return listOf(
            TableSection(1, "Main Hall", listOf(
                TableData(1, "101", 1, "Main Hall", 4, "BLANK"),
                TableData(2, "102", 1, "Main Hall", 2, "RUNNING"),
                TableData(3, "103", 1, "Main Hall", 4, "BLANK")
            )),
            TableSection(2, "Outdoor", listOf(
                TableData(4, "O1", 2, "Outdoor", 2, "RUNNING"),
                TableData(5, "O2", 2, "Outdoor", 4, "BLANK")
            ))
        )
    }

    fun filterBySection(sectionId: Int?) {
        _selectedSectionId.value = sectionId
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addSection(name: String) {
        viewModelScope.launch {
            val rid = sessionManager.restaurantId.first()?.toInt() ?: 1
            repository.createSection(rid, name)
            loadTables()
        }
    }

    fun addTable(number: String, sectionId: Int, seats: Int) {
        viewModelScope.launch {
            val rid = sessionManager.restaurantId.first()?.toInt() ?: 1
            repository.createTable(rid, CreateTableRequest(number, sectionId, seats))
            loadTables()
        }
    }

    fun toggleTableStatus(table: TableData) {
        viewModelScope.launch {
            val rid = sessionManager.restaurantId.first()?.toInt() ?: 1
            repository.updateTable(rid, table.id, CreateTableRequest(table.number, table.sectionId, table.seats, !table.isActive))
            loadTables()
        }
    }
}

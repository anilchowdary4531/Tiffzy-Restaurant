package com.tiffzy.restaurant.ui.restaurant.billing

import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.data.model.Category
import com.tiffzy.restaurant.data.model.MenuItem
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillDraft(
    val id: String,
    val customerName: String = "",
    val customerPhone: String = "",
    val items: List<CartItem> = emptyList(),
    val isHeld: Boolean = false
)

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
) {
    val total: Double get() = menuItem.price * quantity
}

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _bills = MutableStateFlow<List<BillDraft>>(listOf(BillDraft("1")))
    val bills: StateFlow<List<BillDraft>> = _bills.asStateFlow()

    private val _activeBillIndex = MutableStateFlow(0)
    val activeBillIndex: StateFlow<Int> = _activeBillIndex.asStateFlow()

    val activeBill: StateFlow<BillDraft> = combine(_bills, _activeBillIndex) { bills, index ->
        bills.getOrElse(index) { BillDraft("new") }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), BillDraft("1"))

    init {
        loadMenu()
    }

    private fun loadMenu() {
        viewModelScope.launch {
            val rid = sessionManager.restaurantId.first()?.toInt() ?: return@launch
            when (val result = repository.refreshMenu(rid)) {
                is Resource.Success -> {
                    _menuItems.value = result.data
                    _categories.value = result.data.map { it.category }.distinct()
                }
                else -> {}
            }
        }
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addItemToBill(item: MenuItem) {
        val currentBills = _bills.value.toMutableList()
        val index = _activeBillIndex.value
        val bill = currentBills[index]
        
        val existingItem = bill.items.find { it.menuItem.id == item.id }
        val newItems = if (existingItem != null) {
            bill.items.map {
                if (it.menuItem.id == item.id) it.copy(quantity = it.quantity + 1) else it
            }
        } else {
            bill.items + CartItem(item, 1)
        }
        
        currentBills[index] = bill.copy(items = newItems)
        _bills.value = currentBills
    }

    fun updateQuantity(item: MenuItem, delta: Int) {
        val currentBills = _bills.value.toMutableList()
        val index = _activeBillIndex.value
        val bill = currentBills[index]
        
        val newItems = bill.items.mapNotNull {
            if (it.menuItem.id == item.id) {
                val newQty = it.quantity + delta
                if (newQty > 0) it.copy(quantity = newQty) else null
            } else it
        }
        
        currentBills[index] = bill.copy(items = newItems)
        _bills.value = currentBills
    }

    fun createNewBill() {
        val newId = (_bills.value.size + 1).toString()
        _bills.value = _bills.value + BillDraft(newId)
        _activeBillIndex.value = _bills.value.size - 1
    }

    fun switchBill(index: Int) {
        _activeBillIndex.value = index
    }

    fun clearCart() {
        val currentBills = _bills.value.toMutableList()
        val index = _activeBillIndex.value
        currentBills[index] = currentBills[index].copy(items = emptyList())
        _bills.value = currentBills
    }

    fun updateCustomerInfo(name: String, phone: String) {
        val currentBills = _bills.value.toMutableList()
        val index = _activeBillIndex.value
        currentBills[index] = currentBills[index].copy(customerName = name, customerPhone = phone)
        _bills.value = currentBills
    }
}

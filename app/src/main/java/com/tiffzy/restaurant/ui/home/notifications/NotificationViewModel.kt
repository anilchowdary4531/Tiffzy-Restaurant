package com.tiffzy.restaurant.ui.home.notifications

import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.Notification
import com.tiffzy.restaurant.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : BaseViewModel() {

    private val _notificationsState = MutableStateFlow<UiState<List<Notification>>>(UiState.Loading)
    val notificationsState: StateFlow<UiState<List<Notification>>> = _notificationsState.asStateFlow()

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            _notificationsState.value = UiState.Loading
            when (val result = repository.getNotifications()) {
                is Resource.Success -> _notificationsState.value = UiState.Success(result.data)
                is Resource.Error -> _notificationsState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            repository.markAsRead(id)
            fetchNotifications()
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            repository.deleteNotification(id)
            fetchNotifications()
        }
    }
}

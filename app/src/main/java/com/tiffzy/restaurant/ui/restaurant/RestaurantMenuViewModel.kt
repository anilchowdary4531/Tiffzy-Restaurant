package com.tiffzy.restaurant.ui.restaurant

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.data.model.MenuItem
import com.tiffzy.restaurant.data.model.MenuRequest
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

sealed class MenuUiState {
    object Loading : MenuUiState()
    data class Success(val menu: List<MenuItem>) : MenuUiState()
    data class Error(val message: String) : MenuUiState()
}

@HiltViewModel
class RestaurantMenuViewModel @Inject constructor(
    private val application: Application,
    private val repository: RestaurantRepository,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        observeMenu()
        loadMenu()
    }

    private fun observeMenu() {
        viewModelScope.launch {
            val ridString = sessionManager.restaurantId.first()
            if (ridString != null) {
                repository.getCachedMenu(ridString.toInt()).collect { menu ->
                    if (menu.isNotEmpty()) {
                        _uiState.value = MenuUiState.Success(menu)
                    }
                }
            }
        }
    }

    fun loadMenu() {
        viewModelScope.launch {
            if (_uiState.value !is MenuUiState.Success) {
                _uiState.value = MenuUiState.Loading
            }
            val ridString = sessionManager.restaurantId.first()
            if (ridString != null) {
                when (val result = repository.refreshMenu(ridString.toInt())) {
                    is Resource.Error -> {
                        if (_uiState.value !is MenuUiState.Success) {
                            _uiState.value = MenuUiState.Error(result.message)
                        }
                    }
                    else -> {}
                }
            } else {
                _uiState.value = MenuUiState.Error("Unauthorized")
            }
        }
    }

    fun saveMenuItem(
        id: Int? = null,
        name: String,
        description: String?,
        category: String,
        image: String?,
        price: Double,
        isAvailable: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val ridString = sessionManager.restaurantId.first()
            if (ridString != null) {
                val restaurantId = ridString.toInt()
                val request = MenuRequest(name, description, category, image, price, isAvailable)
                val result = if (id == null) {
                    repository.createMenuItem(restaurantId, request)
                } else {
                    repository.updateMenuItem(restaurantId, id, request)
                }
                
                if (result is Resource.Success) {
                    loadMenu()
                    onSuccess()
                }
            }
            _isSaving.value = false
        }
    }

    fun deleteMenuItem(menuId: Int) {
        viewModelScope.launch {
            val ridString = sessionManager.restaurantId.first()
            if (ridString != null) {
                repository.deleteMenuItem(ridString.toInt(), menuId)
                loadMenu()
            }
        }
    }

    fun toggleAvailability(item: MenuItem) {
        viewModelScope.launch {
            val ridString = sessionManager.restaurantId.first()
            if (ridString != null) {
                val restaurantId = ridString.toInt()
                val request = MenuRequest(
                    name = item.name,
                    description = item.description,
                    category = item.category,
                    image = item.image,
                    price = item.price,
                    isAvailable = !item.isAvailable
                )
                repository.updateMenuItem(restaurantId, item.id, request)
                loadMenu()
            }
        }
    }

    fun uploadImage(uri: Uri, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val ridString = sessionManager.restaurantId.first()
            if (ridString != null) {
                val file = uriToFile(uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val result = repository.uploadMenuImage(ridString.toInt(), body)
                if (result is Resource.Success) {
                    onResult(result.data.upload.publicUrl)
                } else {
                    onResult(null)
                }
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = application.contentResolver.openInputStream(uri)
        val file = File(application.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }
}

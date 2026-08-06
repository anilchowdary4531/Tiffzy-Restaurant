package com.tiffzy.restaurant.ui.home.address

import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.Address
import com.tiffzy.restaurant.data.model.CreateAddressRequest
import com.tiffzy.restaurant.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val repository: LocationRepository
) : BaseViewModel() {

    private val _addressesState = MutableStateFlow<UiState<List<Address>>>(UiState.Loading)
    val addressesState: StateFlow<UiState<List<Address>>> = _addressesState.asStateFlow()

    private val _addAddressState = MutableStateFlow<UiState<Address>>(UiState.Idle)
    val addAddressState: StateFlow<UiState<Address>> = _addAddressState.asStateFlow()

    private val _currentMapLocation = MutableStateFlow(LatLng(17.3850, 78.4867))
    val currentMapLocation: StateFlow<LatLng> = _currentMapLocation.asStateFlow()

    private val _geocodedAddress = MutableStateFlow("")
    val geocodedAddress: StateFlow<String> = _geocodedAddress.asStateFlow()

    init {
        fetchAddresses()
    }

    fun fetchAddresses() {
        viewModelScope.launch {
            _addressesState.value = UiState.Loading
            when (val result = repository.getSavedAddresses()) {
                is Resource.Success -> _addressesState.value = UiState.Success(result.data)
                is Resource.Error -> _addressesState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun updateMapLocation(latLng: LatLng) {
        _currentMapLocation.value = latLng
        viewModelScope.launch {
            val addressString = repository.getAddressFromLocation(latLng.latitude, latLng.longitude)
            _geocodedAddress.value = addressString ?: "Address not found"
        }
    }

    fun saveAddress(label: String, name: String, phone: String, notes: String, isDefault: Boolean) {
        viewModelScope.launch {
            _addAddressState.value = UiState.Loading
            val location = _currentMapLocation.value
            val request = CreateAddressRequest(
                label = label,
                name = name,
                phone = phone,
                line1 = _geocodedAddress.value,
                city = "Hyderabad", // Extracted from geocoder in production
                state = "Telangana",
                latitude = location.latitude,
                longitude = location.longitude,
                notes = notes,
                isDefault = isDefault
            )
            
            when (val result = repository.createAddress(request)) {
                is Resource.Success -> {
                    _addAddressState.value = UiState.Success(result.data)
                    fetchAddresses()
                }
                is Resource.Error -> _addAddressState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun deleteAddress(id: Int) {
        viewModelScope.launch {
            repository.deleteAddress(id)
            fetchAddresses()
        }
    }

    fun setDefaultAddress(id: Int) {
        viewModelScope.launch {
            repository.setDefaultAddress(id)
            fetchAddresses()
        }
    }

    fun useCurrentLocation() {
        viewModelScope.launch {
            val location = repository.getCurrentLocation()
            location?.let {
                updateMapLocation(LatLng(it.latitude, it.longitude))
            }
        }
    }
}

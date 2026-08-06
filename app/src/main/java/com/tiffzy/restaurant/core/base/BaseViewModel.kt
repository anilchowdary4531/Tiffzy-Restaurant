package com.tiffzy.restaurant.core.base

import androidx.lifecycle.ViewModel
import com.tiffzy.restaurant.core.result.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel : ViewModel() {
    
    // We can add common functionality here, like shared UI events (Toast, Navigation)
    // but keeping it simple for now as per requirements.
}

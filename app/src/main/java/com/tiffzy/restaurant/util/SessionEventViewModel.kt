package com.tiffzy.restaurant.util

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionEventViewModel @Inject constructor(
    val eventBus: SessionEventBus
) : ViewModel()

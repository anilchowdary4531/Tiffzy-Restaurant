package com.tiffzy.restaurant.ui.home.reviews

import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: ReviewRepository
) : BaseViewModel() {

    private val _reviewsState = MutableStateFlow<UiState<ReviewListResponse>>(UiState.Loading)
    val reviewsState: StateFlow<UiState<ReviewListResponse>> = _reviewsState.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<GenericResponse>>(UiState.Idle)
    val actionState: StateFlow<UiState<GenericResponse>> = _actionState.asStateFlow()

    fun fetchReviews(slug: String, page: Int = 1) {
        viewModelScope.launch {
            _reviewsState.value = UiState.Loading
            when (val result = repository.getRestaurantReviews(slug, page)) {
                is Resource.Success -> _reviewsState.value = UiState.Success(result.data)
                is Resource.Error -> _reviewsState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun addReview(slug: String, rating: Double, comment: String, images: List<MultipartBody.Part>? = null, menuItemId: Int? = null) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            val request = ReviewRequest(rating = rating, comment = comment, menuItemId = menuItemId)
            when (val result = repository.addReview(slug, request)) {
                is Resource.Success -> {
                    // If images are provided, upload them (simplified logic, usually backend might return review ID)
                    // For now, assuming success means we can refresh
                    fetchReviews(slug)
                    _actionState.value = UiState.Success(result.data)
                }
                is Resource.Error -> _actionState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun deleteReview(reviewId: Int, slug: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            when (val result = repository.deleteReview(reviewId)) {
                is Resource.Success -> {
                    fetchReviews(slug)
                    _actionState.value = UiState.Success(result.data)
                }
                is Resource.Error -> _actionState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }
    
    fun resetActionState() {
        _actionState.value = UiState.Idle
    }
}

package com.vijay.image_loader.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vijay.image_loader.data.model.Image
import com.vijay.image_loader.domain.repository.ImageRepository
import com.vijay.image_loader.utils.ImagePagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    val images: Flow<PagingData<Image>> = Pager(
        config = PagingConfig(pageSize = 10, prefetchDistance = 5, enablePlaceholders = false, maxSize = 100 ),
        pagingSourceFactory = { ImagePagingSource(repository, 10) }
    ).flow.cachedIn(viewModelScope)

    init {
        fetchImages()
    }

    private fun fetchImages() {
        viewModelScope.launch {
            try {
                repository.fetchImages(10, 0)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load images: ${e.message}"
            }
        }
    }
}

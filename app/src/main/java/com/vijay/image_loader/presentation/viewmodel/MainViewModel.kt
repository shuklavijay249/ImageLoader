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
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 10 // Items per page
        private const val PREFETCH_DISTANCE = 5  // Number of items to prefetch
        private const val MAX_CACHE_SIZE = 100  // Max size of cached items

    }

    val images: Flow<PagingData<Image>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            prefetchDistance = PREFETCH_DISTANCE,   // Preload 10 items before reaching the end of the list
            enablePlaceholders = false,
            maxSize = MAX_CACHE_SIZE            // Maximum number of cached images

        ),
        pagingSourceFactory = { ImagePagingSource(repository, PAGE_SIZE) }
    ).flow.cachedIn(viewModelScope)
}

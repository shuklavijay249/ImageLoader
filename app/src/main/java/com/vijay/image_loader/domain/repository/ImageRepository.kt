package com.vijay.image_loader.domain.repository

import com.vijay.image_loader.data.model.ApiResponse


interface ImageRepository {
    // Make the function open so it can be overridden in the implementation
    suspend fun fetchImages(limit: Int, offset: Int): String
}

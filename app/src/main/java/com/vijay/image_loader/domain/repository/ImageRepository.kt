package com.vijay.image_loader.domain.repository

interface ImageRepository {
    suspend fun fetchImages(limit: Int, offset: Int): String
}

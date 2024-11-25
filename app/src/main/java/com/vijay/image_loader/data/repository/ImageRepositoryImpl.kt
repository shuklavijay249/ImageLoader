package com.vijay.image_loader.data.repository

import com.vijay.image_loader.data.api.ApiService
import com.google.gson.Gson
import com.vijay.image_loader.domain.repository.ImageRepository
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ImageRepository {
    override suspend fun fetchImages(limit: Int, offset: Int): String {
        // API response
        val response = apiService.getMediaCoverages(limit, offset)

        // Convert response to JSON string
        return Gson().toJson(response)
    }
}

package com.vijay.image_loader.data.api

import com.vijay.image_loader.data.model.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {
    @GET("media-coverages")
    suspend fun getMediaCoverages(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): List<ApiResponse>
}

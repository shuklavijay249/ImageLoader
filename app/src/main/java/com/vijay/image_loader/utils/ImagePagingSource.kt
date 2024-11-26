package com.vijay.image_loader.utils

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vijay.image_loader.data.model.ApiResponse
import com.vijay.image_loader.data.model.Image
import com.vijay.image_loader.domain.repository.ImageRepository

class ImagePagingSource(
    private val repository: ImageRepository,
    private val pageSize: Int
) : PagingSource<Int, Image>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Image> {
        return try {
            val currentPage = params.key ?: 0
            val response = repository.fetchImages(pageSize)
            val apiResponse: List<ApiResponse> = Gson().fromJson(
                response,
                object : TypeToken<List<ApiResponse>>() {}.type
            )

            val images = apiResponse.map {
                Image(
                    id = it.id,
                    title = it.title,
                    thumbnailUrl = "${it.thumbnail.domain}/${it.thumbnail.basePath}/0/${it.thumbnail.key}",
                    language = it.language,
                    mediaType = it.mediaType
                )
            }

            LoadResult.Page(
                data = images,
                prevKey = if (currentPage == 0) null else currentPage - 1,
                nextKey = if (images.isEmpty()) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(Exception("Error loading data. Please check your connection."))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Image>): Int? {
        return state.anchorPosition?.let { state.closestPageToPosition(it)?.prevKey?.plus(1) }
        }
    }

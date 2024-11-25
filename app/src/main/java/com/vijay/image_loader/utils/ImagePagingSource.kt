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
            val offset = currentPage * pageSize
            val rawResponse = repository.fetchImages(pageSize, offset)

            val apiResponseList: List<ApiResponse> = Gson().fromJson(
                rawResponse,
                object : TypeToken<List<ApiResponse>>() {}.type
            )

            val images = apiResponseList.mapIndexed { index, apiResponse ->
                val position = offset + index
                Image(
                    id = apiResponse.id,
                    title = apiResponse.title,
                    language = apiResponse.language,
//                    thumbnailUrl = "${apiResponse.thumbnail.domain}/${apiResponse.thumbnail.basePath}/$currentPage/${apiResponse.thumbnail.key}",
                    thumbnailUrl = "${apiResponse.thumbnail.domain}/${apiResponse.thumbnail.basePath}/0/${apiResponse.thumbnail.key}",
                    mediaType = apiResponse.mediaType
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
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }

}

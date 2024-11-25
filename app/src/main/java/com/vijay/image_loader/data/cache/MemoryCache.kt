package com.vijay.image_loader.data.cache

import android.graphics.Bitmap
import android.util.LruCache

object MemoryCache {
    private val cache = LruCache<String, Bitmap>(calculateCacheSize())

    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }

    fun get(key: String): Bitmap? = cache.get(key)

    private fun calculateCacheSize(): Int {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        return maxMemory / 8
    }
}

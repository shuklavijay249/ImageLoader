package com.vijay.image_loader.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object ImageUtils {

    private val memoryCache = mutableMapOf<String, Bitmap>()
    private val cacheDir = File("/path/to/cacheDir") // Update with your context

    fun loadImageFromCache(key: String): Bitmap? {
        return memoryCache[key] ?: loadImageFromDiskCache(key)
    }

    fun loadImageFromDiskCache(key: String): Bitmap? {
        val file = File(cacheDir, key)
        return if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            memoryCache[key] = bitmap
            bitmap
        } else {
            null
        }
    }

    fun saveImageToDiskCache(key: String, bitmap: Bitmap) {
        val file = File(cacheDir, key)
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
        }
        memoryCache[key] = bitmap
    }

    fun fetchImageFromNetwork(url: String, cacheKey: String): Bitmap? {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()
            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            saveImageToDiskCache(cacheKey, bitmap)
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

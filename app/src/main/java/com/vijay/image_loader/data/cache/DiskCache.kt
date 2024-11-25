package com.vijay.image_loader.data.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

class DiskCache(private val context: Context) {
    private val cacheDir = File(context.cacheDir, "images").apply { mkdirs() }

    fun put(key: String, bitmap: Bitmap) {
        val file = File(cacheDir, key.hashCode().toString())
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    fun get(key: String): Bitmap? {
        val file = File(cacheDir, key.hashCode().toString())
        return if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
    }
}

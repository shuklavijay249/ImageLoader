package com.vijay.image_loader.presentation.view

import android.app.Dialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.vijay.image_loader.R
import com.vijay.image_loader.data.cache.DiskCache
import com.vijay.image_loader.data.cache.MemoryCache
import com.vijay.image_loader.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the adapter
        adapter = ImageAdapter { url ->
            showImagePopup(url) // Open image
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.images.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        // Observe errors
        lifecycleScope.launch {
            viewModel.errorMessage.collectLatest { message ->
                if (message.isNotEmpty()) {
                    Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showImagePopup(imageUrl: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_image)
        val imageView = dialog.findViewById<ImageView>(R.id.dialogImageView)

        MemoryCache.get(imageUrl)?.let {
            imageView.setImageBitmap(it)
        } ?: run {
            val diskCachedBitmap = DiskCache(this).get(imageUrl)
            if (diskCachedBitmap != null) {
                imageView.setImageBitmap(diskCachedBitmap)
            } else {
                // Show placeholder if image not cached
                imageView.setImageResource(R.drawable.image_background)
            }
        }

        dialog.show()
    }
}

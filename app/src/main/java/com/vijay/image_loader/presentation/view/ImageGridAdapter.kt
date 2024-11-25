package com.vijay.image_loader.presentation.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vijay.image_loader.R
import com.vijay.image_loader.data.cache.DiskCache
import com.vijay.image_loader.data.cache.MemoryCache
import com.vijay.image_loader.data.model.Image
import com.vijay.image_loader.databinding.ItemImageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class ImageGridAdapter(private val onImageClick: (String) -> Unit) :
    PagingDataAdapter<Image, ImageGridAdapter.ImageViewHolder>(ImageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = getItem(position)
        image?.let { holder.bind(it) }
    }

    inner class ImageViewHolder(private val binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val jobMap = mutableMapOf<ImageView, Job>()

        fun bind(image: Image) {
            val context = binding.root.context
            val imageUrl = image.thumbnailUrl

            // Set a placeholder image initially
            binding.imageView.setImageResource(R.drawable.image_background)

            // Try to load from memory cache
            MemoryCache.get(imageUrl)?.let {
                binding.imageView.setImageBitmap(it)
            } ?: run {
                // Try to load from disk cache
                val diskCachedBitmap = DiskCache(context).get(imageUrl)
                if (diskCachedBitmap != null) {
                    // Update memory cache and display
                    MemoryCache.put(imageUrl, diskCachedBitmap)
                    binding.imageView.setImageBitmap(diskCachedBitmap)
                } else {
                    // Load from the network
                    loadImageFromNetwork(imageUrl, context, binding.imageView)
                }
            }

            binding.root.setOnClickListener {
                onImageClick(imageUrl)
            }
        }

       // private val jobMap = mutableMapOf<ImageView, Job>()

        private fun loadImageFromNetwork(
            url: String,
            context: Context,
            imageView: ImageView
        ) {
            // Cancel any previous job for this ImageView
            jobMap[imageView]?.cancel()

            // Start a new job
            val job = CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bitmap = downloadImage(url)
                    if (bitmap != null) {
                        DiskCache(context).put(url, bitmap)
                        MemoryCache.put(url, bitmap)

                        withContext(Dispatchers.Main) {
                            if (imageView.tag == url) {
                                imageView.setImageBitmap(bitmap)
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        imageView.setImageResource(R.drawable.image_background)
                    }
                }
            }

            imageView.tag = url
            jobMap[imageView] = job
        }

        private fun downloadImage(url: String): Bitmap? {
            return try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.inputStream.use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (e: Exception) {
                null // Return null on failure
            }
        }
    }

    // DiffUtil callback to compare images
    class ImageDiffCallback : DiffUtil.ItemCallback<Image>() {
        override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
            return oldItem == newItem
        }
    }
}

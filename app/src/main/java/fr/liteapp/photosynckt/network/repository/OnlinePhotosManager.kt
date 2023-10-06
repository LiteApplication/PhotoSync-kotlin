package fr.liteapp.photosynckt.network.repository

import android.content.Context
import android.util.Log
import fr.liteapp.photosynckt.TAG
import fr.liteapp.photosynckt.db.GalleryDAO
import fr.liteapp.photosynckt.network.api.PhotoSyncApi
import fr.liteapp.photosynckt.network.response.PhotosPageResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class OnlinePhotosManager(
    val api: PhotoSyncApi,
    val context: Context,
    val userRepository: UserRepository,
    val galleryDAO: GalleryDAO
) {

    fun forceDatabaseRefresh(
        progress: (Float, String) -> Unit, callback: (Boolean) -> Unit
    ) {
        // Use a coroutine to avoid blocking the UI
        CoroutineScope(Dispatchers.IO).launch {
            // We get the token from the user repository
            progress(0.0f, "Getting token")
            val token = userRepository.getToken()
            if (token == null) {
                callback(false)
                return@launch
            }
            // Now request the server for all the photos
            val response: Response<PhotosPageResult>
            try {
                progress(0.1f, "Requesting photos")
                response = api.getAllPhotos(token).execute()
            } catch (e: Exception) {
                callback(false)

                Log.e(TAG, "forceDatabaseRefresh: ${e.message}")
                return@launch
            }

            if (!response.isSuccessful) {
                callback(false)

                Log.e(TAG, "forceDatabaseRefresh: ${response.errorBody()?.string()}")
                return@launch
            }

            val photos = response.body()
            if (photos == null) {
                callback(false)

                Log.e(TAG, "forceDatabaseRefresh: Response body is null")
                return@launch
            }

            progress(0.5f, "Converting photos")
            // Save the photos to the database
            val galleryItems = photos.photos.map {
                val ret = it.toGalleryItem()
                Log.d(TAG, "forceDatabaseRefresh: $ret")
                ret
            }

            progress(0.8f, "Saving photos")
            // Separate the gallery items into chunks of 127 items
            val galleryItemsChunks = galleryItems.chunked(127)
            val totalChunks = galleryItemsChunks.size
            var currentChunk = 0
            // Save the chunks to the database
            galleryItemsChunks.forEach {
                currentChunk++
                progress((0.8 + 0.2 * currentChunk.toFloat() / totalChunks.toFloat()).toFloat(), "Saving photos")
                galleryDAO.insert(*it.toTypedArray())
            }

            progress(1.0f, "Done")
            callback(true)
        }
    }
}
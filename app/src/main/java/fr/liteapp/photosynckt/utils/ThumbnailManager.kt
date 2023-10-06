package fr.liteapp.photosynckt.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.util.Log
import androidx.compose.ui.unit.Dp
import fr.liteapp.photosynckt.TAG
import fr.liteapp.photosynckt.db.GalleryDAO
import fr.liteapp.photosynckt.db.GalleryItem
import fr.liteapp.photosynckt.thumbnailSize
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.Executors

fun Dp.toPx(): Int {
    return (this.value * Resources.getSystem().displayMetrics.density).toInt()
}

class ThumbnailManager constructor(
    private val context: Context, private val galleryDAO: GalleryDAO
) {
    private var poolGenerators = Executors.newCachedThreadPool()

    private var poolFileCheckers = Executors.newCachedThreadPool()
    private var managerThread: Thread? = null

    // Create the thumbnail directory
    val thumbnailDir = File(context.cacheDir, "thumbnails")

    init {
        if (!thumbnailDir.exists()) {
            thumbnailDir.mkdirs()
        }
    }

    fun scheduleThumbnailGeneration(item: GalleryItem, then: (Boolean) -> Unit) {
        poolGenerators.execute {
            generateThumbnail(item, galleryDAO) { success ->
                if (!success) {
                    Log.e(
                        TAG,
                        "scheduleThumbnailGeneration: Failed to generate thumbnail for ${item.itemPath}"
                    )
                }
                then(success)
            }
        }
    }

    fun checkThumbnailAvailability(item: GalleryItem, then: (Boolean) -> Unit) {
        poolFileCheckers.execute {
            if (item.itemThumbnail == null) {
                then(false)
                return@execute
            }
            val thumbnailFile = File(item.itemThumbnail!!)
            then(thumbnailFile.exists())
        }
    }

    private fun generateThumbnail(
        item: GalleryItem, dao: GalleryDAO, callback: (Boolean) -> Unit
    ) {
        var thumbnailFile: File? = null
        // Generate the thumbnail
        var bitmap: Bitmap? = null

        // Check if the item exists
        if (!File(item.itemPath).exists()) {
            Log.e(
                TAG, "loadThumbnail: File does not exist ${item.itemPath}"
            )
            callback(false)
            return
        }
        try {
            // Load the image into the memory
            bitmap = BitmapFactory.decodeFile(item.itemPath)
        } catch (e: FileNotFoundException) {
            Log.e(
                TAG, "loadThumbnail: File not found ${item.itemPath}"
            )
            thumbnailFile = null
        } catch (e: Exception) {
            Log.e(
                TAG, "loadThumbnail: Failed to generate thumbnail for ${item.itemPath}", e
            )
            thumbnailFile = null
        }
        if (bitmap == null) {
            callback(false)
            return
        }
        // Resize the file to 96x96 dp
        bitmap = ThumbnailUtils.extractThumbnail(
            bitmap, thumbnailSize.toPx(), thumbnailSize.toPx()
        )
        // Create a new file in the cache directory
        thumbnailFile = File(thumbnailDir, "tb_${item.id}.jpg")
        // Write the thumbnail to the file
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, thumbnailFile.outputStream())
        // Close the file
        thumbnailFile.outputStream().close()
        // Clear the bitmap
        bitmap.recycle()
        // Update the item in the database
        item.itemThumbnail = thumbnailFile.absolutePath
        dao.update(item)
        callback(true)
    }
}
package fr.liteapp.photosynckt.utils

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.core.database.getLongOrNull
import fr.liteapp.photosynckt.TAG
import fr.liteapp.photosynckt.db.DiscoveredFoldersDAO
import fr.liteapp.photosynckt.db.GalleryDAO
import fr.liteapp.photosynckt.db.GalleryItem
import fr.liteapp.photosynckt.progressReport
import java.io.File

class GalleryIndex constructor(
    private val context: Context,
    private val galleryDAO: GalleryDAO,
    private val discoveredFoldersDAO: DiscoveredFoldersDAO
) {
    private var indexThread: Thread? = null

    /**
     * Executes a function on the index thread
     * @param callable Function to execute
     * @return true if the function was executed, false otherwise
     */
    private fun executeOnIndexThread(callable: () -> Unit): Boolean {
        if (indexThread == null) {
            indexThread = Thread {
                callable()
                indexThread = null
            }
            indexThread?.start()
            return true
        }
        Log.w(TAG, "executeOnIndexThread: Index thread is already running")
        return false
    }

    /**
     * Indexes the gallery for the first time
     * @param progression Callback to report progress, takes two arguments: current progress and total progress
     * @param callback Callback to report when indexing is done, takes one argument:
     * true if indexing was successful, false otherwise.
     * The callback will not be called if indexing was not started
     * @note This function will not start indexing if the gallery already has items
     * @return true if indexing was started, false otherwise
     */
    fun firstIndex(progression: (Int, Int) -> Unit, callback: (Boolean) -> Unit): Boolean {
        return executeOnIndexThread {
            Log.d(TAG, "firstIndex: Starting indexing")
            val hiddenFolders: List<String> = discoveredFoldersDAO.getHiddenFolders()
            val indexBatch : MutableList<GalleryItem> = mutableListOf()

            if (galleryDAO.hasItems() > 0) {
                Log.w(TAG, "firstIndex: Gallery already has items")
                callback(false)
                return@executeOnIndexThread
            }

            if (discoveredFoldersDAO.hasFolders() == 0) {
                Log.w(TAG, "firstIndex: No folders to index")
                callback(false)
                return@executeOnIndexThread
            }

            galleryDAO.deleteAll()

            val projection = listOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN,

                // Fallback dates
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATE_MODIFIED,

                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Video.Media.DURATION,
                MediaStore.Images.Media.IS_FAVORITE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Images.Media.ORIENTATION

            )


            val selection =
                "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?"


            val selectionArgs = arrayOf(
                "image/%",
                "video/%",
            )

            val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection.toTypedArray(),
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateTakenColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val dateAddedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val dateModifiedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)

                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DURATION)
                val favoriteColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.IS_FAVORITE)
                val mimeTypeColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val orientationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION)

                val totalItems = cursor.count

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val dateTaken = cursor.getLong(dateTakenColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val dateModified = cursor.getLong(dateModifiedColumn)
                    val path = cursor.getString(dataColumn)
                    val size = cursor.getLong(sizeColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val isFavorite = cursor.getInt(favoriteColumn) == 1
                    val duration = cursor.getLongOrNull(durationColumn)
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val orientation = cursor.getInt(orientationColumn)
                    val contentUri = if (duration == null)
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    else
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    val uri = ContentUris.withAppendedId(
                        contentUri, id
                    )

                    // Check if the file exists
                    if (!File(path).exists()) {
                        Log.d(TAG, "firstIndex: Skipping non-existent file: $path")
                        continue
                    }

                    val parentFolderPath = File(path).parentFile?.absolutePath
                    if (hiddenFolders.contains(parentFolderPath) || parentFolderPath == null) {
                        //Log.d(TAG, "firstIndex: Skipping hidden folder: $path")
                        continue
                    }
                    val parentFolder = discoveredFoldersDAO.getFolder(parentFolderPath)
                    if (parentFolder == null) {
                        Log.w(TAG, "firstIndex: Skipping unknown folder: $parentFolderPath")
                        continue
                    }

                    val date = if (dateTaken != 0L) dateTaken else if (dateAdded != 0L) dateAdded * 1000L else dateModified * 1000L

                    val galleryItem = GalleryItem(
                        id = id,
                        itemName = displayName,
                        itemDate = date,
                        itemUri = uri,
                        itemSize = size,
                        itemWidth = width,
                        itemHeight = height,
                        itemDuration = duration,
                        itemType = mimeType,
                        itemThumbnail = null,
                        isFavorite = isFavorite,
                        syncCompleted = false,
                        parentFolder = parentFolder.id,
                        itemOrientation = orientation,
                        itemPath = path
                    )
                    indexBatch.add(galleryItem)

                    if (cursor.position % progressReport == 0) {
                        galleryDAO.insert(*indexBatch.toTypedArray())
                        indexBatch.clear()
                        progression(cursor.position, totalItems)
                    }
                }
                galleryDAO.insert(*indexBatch.toTypedArray())
                indexBatch.clear()
                progression(totalItems, totalItems)
            }
            callback(true)
        }
    }

}


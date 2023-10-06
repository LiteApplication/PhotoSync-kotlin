package fr.liteapp.photosynckt

import androidx.compose.ui.unit.dp

/**
 * The tag used for logging.
 */
const val TAG = "PhotoSyncKt"

/**
 * The number of items to fetch when displaying the gallery.
 */
const val galleryPageSize = 100

/**
 * The number of items to fetch when displaying the folders.
 */
const val folderPageSize = 10

/**
 * The size of the thumbnails in the gallery.
 */
val thumbnailSize = 96.dp

/**
 * The number of items to process before reporting progress when indexing the gallery.
 */
const val progressReport = 20

const val diskCacheSize = 1024L * 1024L * 1024L * 2L // 2GB
const val screenCount = 10

package fr.liteapp.photosynckt.network.data

import android.net.Uri
import fr.liteapp.photosynckt.db.GalleryItem
import fr.liteapp.photosynckt.network.ApiClient

data class OnlinePhoto(
    val thumbnailColor: String,
    val date: Long,
    val extension: String,
    val format: String,
    val hash: String,
    val id: Long,
    val metadata: Map<String, String>,
    val owner: String,
    val path: String,
    val rights: List<String>,
    val type: String,
    val userTags: Map<String, String>
) {
    override fun toString(): String {
        return "OnlinePhoto(id='$id', path='$path')"
    }


    fun toGalleryItem(): GalleryItem {
        return GalleryItem(
            isRemote = true,
            id = id,
            itemPath = path,
            itemName = path,
            isFavorite = false,
            itemDate = date,
            itemSize = 0,
            itemType = type,
            syncCompleted = false,
            itemHeight = 0,
            itemWidth = 0,
            itemUri = null,
        )
    }
}

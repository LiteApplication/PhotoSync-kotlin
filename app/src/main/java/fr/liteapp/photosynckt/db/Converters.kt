package fr.liteapp.photosynckt.db

import android.net.Uri
import androidx.room.TypeConverter
import fr.liteapp.photosynckt.ui.components.DisplayedItems

class Converters {
    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun toUri(uriString: String?): Uri? {
        return uriString?.let { Uri.parse(it) }
    }

    @TypeConverter
    fun fromGalleryItem(galleryItem: GalleryItem): DisplayedItems {
        return DisplayedItems(
            galleryItem = galleryItem,
            _type = 0
        )
    }

    @TypeConverter
    fun toGalleryItem(displayedItems: DisplayedItems): GalleryItem? {
        return displayedItems.galleryItem
    }

}
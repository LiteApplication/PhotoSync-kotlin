package fr.liteapp.photosynckt.ui.components

import android.util.Log
import fr.liteapp.photosynckt.TAG
import fr.liteapp.photosynckt.db.GalleryItem
import java.util.Calendar

data class DisplayedItems(
    val galleryItem: GalleryItem? = null,
    val date: Long? = null,
    val year: Int? = null,
    val _type: Int = 0 // 0 = galleryItem, 1 = date, 2 = year
)


fun isSameDate(date1: Long?, date2: Long?): Calendar? {
    if (date1 == null || date2 == null) {
        Log.d(TAG, "isSameDate: One of the dates is null")
        return null
    }
    val cal1 = Calendar.getInstance()
    cal1.timeInMillis = date1
    val cal2 = Calendar.getInstance()
    cal2.timeInMillis = date2

    if (cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR) ||
        cal1.get(Calendar.DAY_OF_YEAR) != cal2.get(Calendar.DAY_OF_YEAR)
    ) {
        return cal2
    }
    return null
}
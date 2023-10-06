package fr.liteapp.photosynckt.db

import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gallery",
    indices = [Index(value = ["item_uri"], unique = true), Index(value = ["id"]), Index(value = ["parent_folder"])],
    foreignKeys = [ForeignKey(
        entity = LocalFolder::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("parent_folder"),
        onDelete = ForeignKey.CASCADE
    )]
)
/**
 * @param itemUri uri to the item (null if the item is only on the server)
 * @param itemPath path to the item (if it is a local file)
 * @param itemName name of the item
 * @param isFavorite whether the item is a favorite
 * @param syncCompleted whether the item has been synced
 * @param itemType mime type of the item
 * @param itemSize size of the item
 * @param itemDate date of the item (in that order : taken, added, modified) milliseconds since epoch
 * @param itemWidth width of the item
 * @param itemHeight height of the item
 * @param itemOrientation orientation of the item (from MediaStore)
 * @param itemDuration duration of the item if it is a video
 * @param itemThumbnail thumbnail of the item (for remote items)
 * @param parentFolder parent folder of the item (foreign key)
 * @param isRemote whether the item is stored exclusively on the remote server
 */
data class GalleryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "item_uri") val itemUri: Uri?,
    @ColumnInfo(name = "item_path") val itemPath: String?,
    @ColumnInfo(name = "item_name") val itemName: String,
    @ColumnInfo(name = "is_favorite") var isFavorite: Boolean,
    @ColumnInfo(name = "sync_completed") var syncCompleted: Boolean,
    @ColumnInfo(name = "item_type") val itemType: String,
    @ColumnInfo(name = "item_size") val itemSize: Long,
    @ColumnInfo(name = "item_date") val itemDate: Long,
    @ColumnInfo(name = "item_width") val itemWidth: Int,
    @ColumnInfo(name = "item_height") val itemHeight: Int,
    @ColumnInfo(name = "item_orientation") val itemOrientation: Int = 0,
    @ColumnInfo(name = "item_duration") var itemDuration: Long? = null,
    @ColumnInfo(name = "item_thumbnail") var itemThumbnail: String? = null,
    @ColumnInfo(name = "parent_folder") var parentFolder: Int? = null,
    @ColumnInfo(name = "is_remote") var isRemote: Boolean = false,
)

class GalleryItemSaver : Saver<GalleryItem, Bundle> {

    override fun restore(value: Bundle): GalleryItem {
        val item = GalleryItem(
            id = value.getLong("id"),
            itemUri = Uri.parse(value.getString("itemUri")),
            itemPath = value.getString("itemPath"),
            itemName = value.getString("itemName") ?: "",
            isFavorite = value.getBoolean("isFavorite"),
            syncCompleted = value.getBoolean("syncCompleted"),
            itemType = value.getString("itemType") ?: "",
            itemSize = value.getLong("itemSize"),
            itemDate = value.getLong("itemDate"),
            itemWidth = value.getInt("itemWidth"),
            itemHeight = value.getInt("itemHeight"),
            itemOrientation = value.getInt("itemOrientation"),
            itemDuration = value.getLong("itemDuration"),
            itemThumbnail = value.getString("itemThumbnail"),
            parentFolder = value.getInt("parentFolder"),
            isRemote = value.getBoolean("isRemote"),

        )
        if (item.parentFolder == -1) item.parentFolder = null
        if (item.itemDuration == -1L) item.itemDuration = null

        return item
    }

    override fun SaverScope.save(value: GalleryItem): Bundle {
        val bundle = Bundle()
        bundle.putLong("id", value.id)
        bundle.putString("itemUri", value.itemUri.toString())
        bundle.putString("itemPath", value.itemPath)
        bundle.putString("itemName", value.itemName)
        bundle.putBoolean("isFavorite", value.isFavorite)
        bundle.putBoolean("syncCompleted", value.syncCompleted)
        bundle.putString("itemType", value.itemType)
        bundle.putLong("itemSize", value.itemSize)
        bundle.putLong("itemDate", value.itemDate)
        bundle.putInt("itemWidth", value.itemWidth)
        bundle.putInt("itemHeight", value.itemHeight)
        bundle.putInt("itemOrientation", value.itemOrientation)
        bundle.putLong("itemDuration", value.itemDuration ?: -1L)
        bundle.putString("itemThumbnail", value.itemThumbnail)
        bundle.putInt("parentFolder", value.parentFolder ?: -1)
        bundle.putBoolean("isRemote", value.isRemote)
        return bundle
    }

}


val initialGalleryItem = GalleryItem(
    id = -1,
    itemUri = Uri.EMPTY,
    itemPath = "",
    itemName = "",
    isFavorite = false,
    syncCompleted = false,
    itemType = "image/jpeg",
    itemSize = 0,
    itemDate = 0,
    itemWidth = 0,
    itemHeight = 0,
    itemOrientation = 0,
    itemDuration = null,
    itemThumbnail = null,
    parentFolder = null,
    isRemote = false,
)

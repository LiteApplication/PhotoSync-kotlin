package fr.liteapp.photosynckt.db

import android.os.Bundle
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.room.*

@Entity(tableName = "discovered_folders", indices = [Index(value = ["folder_path"], unique = true)])
data class LocalFolder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "folder_path") val folderPath: String,
    @ColumnInfo(name = "folder_name") val folderName: String,
    @ColumnInfo(name = "is_favorite") var isFavorite: Boolean,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean,
    @ColumnInfo(name = "is_hidden") var isHidden: Boolean,
)


class LocalFolderSaver : Saver<LocalFolder, Bundle> {

    override fun restore(value: Bundle): LocalFolder {
        return LocalFolder(
            id = value.getInt("id"),
            folderPath = value.getString("folderPath") ?: "",
            folderName = value.getString("folderName") ?: "",
            isFavorite = value.getBoolean("isFavorite"),
            isSynced = value.getBoolean("isSynced"),
            isHidden = value.getBoolean("isHidden")
        )
    }

    override fun SaverScope.save(value: LocalFolder): Bundle {
        val bundle = Bundle()
        bundle.putInt("id", value.id)
        bundle.putString("folderPath", value.folderPath)
        bundle.putString("folderName", value.folderName)
        bundle.putBoolean("isFavorite", value.isFavorite)
        bundle.putBoolean("isSynced", value.isSynced)
        bundle.putBoolean("isHidden", value.isHidden)
        return bundle
    }
}

val initialLocalFolder = LocalFolder(
    id = 0,
    folderPath = "",
    folderName = "",
    isFavorite = false,
    isSynced = false,
    isHidden = false
)

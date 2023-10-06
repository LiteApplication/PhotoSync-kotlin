package fr.liteapp.photosynckt.db

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface DiscoveredFoldersDAO {
    @Query("SELECT * FROM discovered_folders WHERE is_hidden = 0 ORDER BY is_favorite DESC, is_synced DESC, folder_path ASC")
    fun getFolders(): List<LocalFolder>

    @Query("SELECT * FROM discovered_folders WHERE is_hidden = 0 ORDER BY is_favorite DESC, is_synced DESC, folder_path ASC")
    fun getFoldersPagingSource(): PagingSource<Int, LocalFolder>

    @Query("SELECT folder_path FROM discovered_folders WHERE is_hidden = 1")
    fun getHiddenFolders(): List<String>

    @Query("SELECT * FROM discovered_folders WHERE folder_path = :folderPath")
    fun getFolder(folderPath: String): LocalFolder?

    @Query("SELECT COUNT(*) FROM (SELECT * FROM discovered_folders LIMIT 1)")
    fun hasFolders(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg folders: LocalFolder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(folder: LocalFolder)

    @Delete
    fun delete(folder: LocalFolder)

    @Update
    fun update(folder: LocalFolder)

    @Query("DELETE FROM discovered_folders WHERE folder_path = :folderPath")
    fun delete(folderPath: String)

    @Query("UPDATE discovered_folders SET is_favorite = :isFavorite WHERE folder_path = :folderPath")
    fun updateFavorite(folderPath: String, isFavorite: Boolean)

    @Query("UPDATE discovered_folders SET is_synced = :isSynced WHERE folder_path = :folderPath")
    fun updateSynced(folderPath: String, isSynced: Boolean)

    @Query("UPDATE discovered_folders SET is_hidden = :isHidden WHERE folder_path = :folderPath")
    fun updateHidden(folderPath: String, isHidden: Boolean)

    @Query("UPDATE discovered_folders SET folder_name = :folderName WHERE folder_path = :folderPath")
    fun updateName(folderPath: String, folderName: String)

    @Query("UPDATE discovered_folders SET is_hidden = 0 WHERE is_hidden = 1")
    fun unhideAll()
}
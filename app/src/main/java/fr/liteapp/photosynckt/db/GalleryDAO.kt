package fr.liteapp.photosynckt.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface GalleryDAO {
    @Query("SELECT * FROM gallery ORDER BY item_date DESC")
    fun getItems(): PagingSource<Int, GalleryItem>

    @Query("SELECT COUNT(*) FROM (SELECT * FROM gallery LIMIT 1)")
    fun hasItems(): Int

    @Query("SELECT * FROM gallery ORDER BY item_date DESC LIMIT 1")
    fun getLatestItem(): GalleryItem

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg items: GalleryItem)

    @Delete
    fun delete(item: GalleryItem)

    @Update
    fun update(item: GalleryItem)

    @Query("SELECT * FROM gallery WHERE parent_folder = :parent_path ORDER BY item_date DESC")
    fun getItemsInFolder(parent_path: Int): PagingSource<Int, GalleryItem>

    @Query("DELETE FROM gallery WHERE parent_folder = :parent_path")
    fun deleteItemsInFolder(parent_path: Int)

    @Query("DELETE FROM gallery")
    fun deleteAll()

    @Query("SELECT COUNT(*) FROM gallery WHERE id = :id")
    fun hasItem(id: Long): Int

}
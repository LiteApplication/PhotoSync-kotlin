package fr.liteapp.photosynckt.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [LocalFolder::class, GalleryItem::class],
    version = 1,
    exportSchema = false, // TODO: Remove this
    autoMigrations = []
)
@TypeConverters(Converters::class)
abstract class PhotoSyncKtDB : RoomDatabase() {
    abstract fun discoveredFoldersDAO(): DiscoveredFoldersDAO
    abstract fun galleryDAO(): GalleryDAO
}

private lateinit var INSTANCE: PhotoSyncKtDB

fun getDatabase(context: Context): PhotoSyncKtDB {
    synchronized(PhotoSyncKtDB::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext, PhotoSyncKtDB::class.java, "photosynckt.db"
            ).fallbackToDestructiveMigration() // TODO: Remove this
                .build()
        }
    }
    return INSTANCE
}

fun getDatabase(): PhotoSyncKtDB {
    if (!::INSTANCE.isInitialized) {
        throw UninitializedPropertyAccessException("Database not initialized")
    }
    return INSTANCE
}
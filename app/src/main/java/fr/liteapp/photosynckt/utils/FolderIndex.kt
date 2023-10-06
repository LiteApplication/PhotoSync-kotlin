package fr.liteapp.photosynckt.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import fr.liteapp.photosynckt.R
import fr.liteapp.photosynckt.TAG
import fr.liteapp.photosynckt.db.DiscoveredFoldersDAO
import fr.liteapp.photosynckt.db.LocalFolder
import java.io.File


private var indexThread: Thread? = null
fun indexFolders(dao: DiscoveredFoldersDAO, context: Context, callback: (Int) -> Unit): Thread? {
    // Check if another thread is already indexing
    if (indexThread != null) {
        Log.w(TAG, "indexFolders: Another thread is already indexing")
        return null
    }

    // Create a new thread to do the indexing
    val thread = Thread {
        // Get the root of the storage
        val storageRoot = File(Environment.getExternalStorageDirectory().path)
        Log.d(TAG, "indexFolders: ${storageRoot.path}")

        val knownFolders = dao.getFolders()
        val notFound = knownFolders.filter { folder -> !File(folder.folderPath).exists() }
        val newFolders: MutableList<LocalFolder> = mutableListOf()

        // Get the list of extensions from the XML file extensions.xml
        val extensions = context.resources.getStringArray(R.array.index_extensions)


        // Walk the file tree
        var ignoreHidden = ""
        storageRoot.walk().forEach {
            if (it.isDirectory) {
                // Check if the folder is hidden
                if (it.name.startsWith(".")) {
                    ignoreHidden = it.absolutePath
                    return@forEach
                }

                if (ignoreHidden != "" && it.absolutePath.startsWith(ignoreHidden)) {
                    return@forEach
                } else {
                    ignoreHidden = ""
                }


                // Check if the folder is already known
                val knownFolder =
                    knownFolders.find { folder -> folder.folderPath == it.absolutePath }
                if (knownFolder == null) { // Not known
                    // Check if the folder contains pictures
                    val pictures = it.listFiles { file -> file.extension.lowercase() in extensions }
                    if (pictures != null) {
                        if (pictures.isNotEmpty()) {
                            // Add the folder to the list of folders to be added to the database
                            val hide = it.name.startsWith(".") || it.absolutePath.contains("com.whatsapp")
                            Log.d(TAG, "indexFolders: ${it.absolutePath}, $hide")
                            newFolders.add(
                                LocalFolder(
                                    folderPath = it.absolutePath,
                                    folderName = it.name,
                                    isFavorite = false,
                                    isSynced = false,
                                    isHidden = hide,
                                )
                            )
                            Log.d(TAG, "indexFolders: Found a new folder ${it.absolutePath}")
                        }
                    }
                }
            }

        }

        Log.d(TAG, "indexFolders: ${newFolders.size}")

        // Remove the folders that were not found
        notFound.forEach { folder ->
            dao.delete(folder)
        }

        // Add the folders that were found
        dao.insertAll(*newFolders.toTypedArray())

        indexThread = null
        callback(newFolders.size - notFound.size + knownFolders.size)
    }
    indexThread = thread
    thread.start()
    return thread


}


fun checkEmpty(dao: DiscoveredFoldersDAO, callback: (Boolean) -> Unit): Thread {
    val thread = Thread {
        val folders = dao.hasFolders()
        callback(folders == 0)
    }
    thread.start()
    return thread
}



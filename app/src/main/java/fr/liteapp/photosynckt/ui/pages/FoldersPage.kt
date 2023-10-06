package fr.liteapp.photosynckt.ui.pages

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HideSource
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import fr.liteapp.photosynckt.TAG
import fr.liteapp.photosynckt.db.DiscoveredFoldersDAO
import fr.liteapp.photosynckt.db.LocalFolder
import fr.liteapp.photosynckt.db.LocalFolderSaver
import fr.liteapp.photosynckt.folderPageSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersPage(
    dao: DiscoveredFoldersDAO,
    paddingValues: PaddingValues = PaddingValues(16.dp),
    nestedScrollConnection: NestedScrollConnection
) {

    val pager = remember {
        Pager(
            config = PagingConfig(
                pageSize = folderPageSize,
                enablePlaceholders = true,
                prefetchDistance = folderPageSize / 2,
                initialLoadSize = folderPageSize
            )

        ) {
            dao.getFoldersPagingSource()
        }
    }

    val lazyPagingFolders = pager.flow.collectAsLazyPagingItems()


    FoldersList(
        lazyPagingFolders = lazyPagingFolders,
        dao = dao,
        paddingValues = paddingValues,
        nestedScrollConnection = nestedScrollConnection
    )
}


@Composable
fun FoldersList(
    lazyPagingFolders: LazyPagingItems<LocalFolder>,
    dao: DiscoveredFoldersDAO,
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection
) {
    LazyColumn(
        userScrollEnabled = true,
        contentPadding = paddingValues,
        modifier = Modifier
            .nestedScroll(nestedScrollConnection)
            .padding(4.dp, 4.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(count = lazyPagingFolders.itemCount,
            key = lazyPagingFolders.itemKey { folder -> folder.id }) { index ->
            val folder = lazyPagingFolders[index] ?: return@items

            FolderListItem(
                localFolder = folder, dao = dao
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListItem(localFolder: LocalFolder, dao: DiscoveredFoldersDAO) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val thisFolder by rememberSaveable(
        stateSaver = LocalFolderSaver()
    ) { mutableStateOf(localFolder) }


    Card(

        modifier = Modifier.animateContentSize(
            animationSpec = SpringSpec(
                dampingRatio = 0.6f, stiffness = 400f
            )
        ),
        shape = MaterialTheme.shapes.medium, onClick = { expanded = !expanded },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Folder,
                contentDescription = "Folder",
                modifier = Modifier.padding(16.dp)

            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 16.dp)
            ) {
                Text(
                    text = thisFolder.folderName, style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = thisFolder.folderPath, style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = SpringSpec(
                        dampingRatio = 0.6f, stiffness = 400f
                    )
                ),
        ) {
            if (expanded) {
                val scope = rememberCoroutineScope()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,

                    ) {
                    IconButton(onClick = {
                        thisFolder.isFavorite = !thisFolder.isFavorite
                        scope.launch {
                            updateFolder(dao, thisFolder)
                        }
                    }) {
                        Icon(
                            if (thisFolder.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite"
                        )
                    }
                    IconButton(onClick = {
                        thisFolder.isSynced = !thisFolder.isSynced
                        scope.launch {
                            updateFolder(dao, thisFolder)
                        }
                    }) {
                        Icon(
                            if (thisFolder.isSynced) Icons.Default.Sync else Icons.Default.SyncDisabled,
                            contentDescription = "Sync this folder"
                        )
                    }
                    IconButton(onClick = {
                        thisFolder.isHidden = !thisFolder.isHidden
                        scope.launch {
                            updateFolder(dao, thisFolder)
                        }
                    }) {
                        Icon(
                            Icons.Default.HideSource, contentDescription = "Hide this folder"
                        )
                    }
                }
            }
        }
    }
}

suspend fun updateFolder(
    dao: DiscoveredFoldersDAO, folder: LocalFolder
) {
    Log.d(TAG, "updateFolder: Updating $folder")
    withContext(Dispatchers.IO) {
        dao.update(folder)
    }
}


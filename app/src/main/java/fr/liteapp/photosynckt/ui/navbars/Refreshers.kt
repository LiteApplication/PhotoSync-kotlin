package fr.liteapp.photosynckt.ui.navbars

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fr.liteapp.photosynckt.TAG
import fr.liteapp.photosynckt.db.DiscoveredFoldersDAO
import fr.liteapp.photosynckt.network.repository.OnlinePhotosManager
import fr.liteapp.photosynckt.utils.GalleryIndex
import fr.liteapp.photosynckt.utils.indexFolders


@Composable
fun RefreshGalleryIndex(galleryIndex: GalleryIndex, callback: () -> Unit) {
    var progress by rememberSaveable { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        galleryIndex.firstIndex(progression = { current, total ->
            progress = current.toFloat() / total.toFloat()
        }, callback = {
            if (!it) Log.e(TAG, "Failed to index gallery")
            callback()
        })
    }

    Dialog(
        onDismissRequest = {}, properties = DialogProperties(
            dismissOnBackPress = false, dismissOnClickOutside = false,
        )
    ) {
        Card(
            shape = MaterialTheme.shapes.extraSmall, colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Indexing photos...", style = MaterialTheme.typography.bodyLarge)
                LinearProgressIndicator(progress = progress, modifier = Modifier.padding(16.dp))
                Text(text = "This may take a while", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun UpdateFolderDatabase(dao: DiscoveredFoldersDAO, callback: () -> Unit) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        indexFolders(dao = dao, context = context) {
            Log.i(TAG, "UpdateFolderDatabase: Found $it folders")
            callback()
        }
    }

    Dialog(
        onDismissRequest = {}, properties = DialogProperties(
            dismissOnBackPress = false, dismissOnClickOutside = false,
        )
    ) {
        Card(
            modifier = Modifier,
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Indexing folders...", style = MaterialTheme.typography.bodyLarge)
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                Text(text = "This may take a while", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun FirstServerSync(onlinePhotosManager: OnlinePhotosManager, callback: () -> Unit) {
    var progress by rememberSaveable { mutableFloatStateOf(0f) }
    var message by rememberSaveable { mutableStateOf("Getting token") }

    LaunchedEffect(Unit) {
        onlinePhotosManager.forceDatabaseRefresh(progress = { p, s: String ->
            progress = p
            message = s
        }, callback = {
            if (!it) Log.e(TAG, "Failed to sync with server")
            callback()
        })
    }

    Dialog(
        onDismissRequest = {}, properties = DialogProperties(
            dismissOnBackPress = false, dismissOnClickOutside = false,
        )
    ) {
        Card(
            shape = MaterialTheme.shapes.extraSmall, colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Synchronizing photos ...", style = MaterialTheme.typography.bodyLarge)
                LinearProgressIndicator(progress = progress, modifier = Modifier.padding(16.dp))
                Text(text = message, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
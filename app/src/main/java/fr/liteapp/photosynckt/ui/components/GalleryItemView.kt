package fr.liteapp.photosynckt.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import fr.liteapp.photosynckt.TAG
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GalleryItemView(item: DisplayedItems) {
    if (item._type == 1) {
        Log.w(TAG, "GalleryItemView: Tried to display a date divider as a gallery item");
        return
    } else if (item._type == 0 && item.galleryItem != null) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxSize()
        ) {
            GlideImage(
                item.galleryItem.itemUri,
                modifier = Modifier.fillMaxSize(),
                contentDescription = null,
                contentScale = ContentScale.Crop
            ) {
                it
            }
        }

    }
    else {
        Log.w(TAG, "GalleryItemView: BRUH WHAT HAPPENED", Exception("Reached else statement in GalleryItemView with item: $item"));
    }
}

@Composable
fun DateDivider(date: Long, displayYear: Boolean = false) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = date
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column {
            if (displayYear) {
                Text(
                    text = calendar.get(Calendar.YEAR).toString(),
                    style = MaterialTheme.typography.displayLarge
                )
            }
            // Print the day of the month (by name)
            Text(
                text = "${calendar.get(Calendar.DAY_OF_MONTH)} ${
                    calendar.getDisplayName(
                        Calendar.MONTH,
                        Calendar.LONG,
                        Locale.ENGLISH
                    )
                }",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

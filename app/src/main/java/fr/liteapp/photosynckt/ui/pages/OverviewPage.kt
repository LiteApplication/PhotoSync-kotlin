package fr.liteapp.photosynckt.ui.pages

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.insertSeparators
import androidx.paging.map
import com.bumptech.glide.Priority
import com.bumptech.glide.integration.compose.rememberGlidePreloadingData
import com.bumptech.glide.signature.MediaStoreSignature
import fr.liteapp.photosynckt.TAG
import fr.liteapp.photosynckt.db.GalleryDAO
import fr.liteapp.photosynckt.galleryPageSize
import fr.liteapp.photosynckt.network.ApiClient
import fr.liteapp.photosynckt.thumbnailSize
import fr.liteapp.photosynckt.ui.components.DateDivider
import fr.liteapp.photosynckt.ui.components.DisplayedItems
import fr.liteapp.photosynckt.ui.components.GalleryItemView
import fr.liteapp.photosynckt.ui.components.isSameDate
import kotlinx.coroutines.flow.map


@Composable
fun Overview(
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
    dao: GalleryDAO,
) {
    val pagingDataFlow = remember {
        Pager(
            config = PagingConfig(
                pageSize = galleryPageSize,
                prefetchDistance = galleryPageSize * 2,
                initialLoadSize = galleryPageSize * 4
            )
        ) {
            dao.getItems()
        }.flow.map {
            it.map { item ->// Convert to DisplayedItems
                DisplayedItems(_type = 0, galleryItem = item)
            }.insertSeparators { before, after ->
                if (before == null) {
                    // add date header for the first item
                    if (after != null) {
                        when (after._type) {
                            0 -> {
                                Log.d(TAG, "Overview: added date header for the first item")
                                return@insertSeparators DisplayedItems(
                                    date = after.galleryItem?.itemDate, _type = 1
                                )
                            }

                            1, 2 -> {
                                return@insertSeparators null
                            }
                        }
                    }
                    return@insertSeparators null
                }
                if (after == null) {
                    return@insertSeparators null
                }

                if (before._type == 0 && after._type == 0) {
                    val date1 = before.galleryItem?.itemDate
                    val date2 = after.galleryItem?.itemDate
                    val cal = isSameDate(date1, date2)
                    if (cal != null) {
                        return@insertSeparators DisplayedItems(
                            date = cal.timeInMillis, _type = 1
                        )
                    }
                    return@insertSeparators null
                }
                return@insertSeparators null
            }
        }
    }

    val lazyPagingItems = pagingDataFlow.collectAsLazyPagingItems()

    val preloadingData = rememberGlidePreloadingData(
        data = lazyPagingItems.itemSnapshotList.items,
        preloadImageSize = Size(100f, 100f),
    ) { item, requestBuilder ->
        if (item.galleryItem == null) {
            // Placeholder with app icon
            requestBuilder.load(null as Uri?).priority(Priority.LOW)
        } else {
            // Create the itemUri field if the item is remote and was not synced
            val itemUri =
                if (item.galleryItem.isRemote and !item.galleryItem.syncCompleted) ApiClient.getThumbnailUri(
                    item.galleryItem.id.toString()
                )
                else item.galleryItem.itemUri

            requestBuilder.load(itemUri).thumbnail(requestBuilder.clone().sizeMultiplier(0.25f))
                .signature(
                    MediaStoreSignature(
                        item.galleryItem.itemType,
                        item.galleryItem.itemDate,
                        item.galleryItem.itemOrientation
                    )
                ).centerCrop().priority(Priority.NORMAL)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(thumbnailSize),
        modifier = Modifier
            .nestedScroll(nestedScrollConnection)
            .padding(paddingValues),
    ) {
        items(lazyPagingItems.itemCount, span = { index ->
            val item = lazyPagingItems.itemSnapshotList.getOrNull(index)
            if (item == null) {
                GridItemSpan(1)
            } else if (item._type == 1) {
                GridItemSpan(maxLineSpan)
            } else {
                GridItemSpan(1)
            }

        }) { index ->
            if (index < preloadingData.size) {
                val itemPreload = lazyPagingItems.get(index = index)
                if (itemPreload == null) {
                    Log.d(
                        TAG,
                        "Overview: item missing from lazyPagingItems: $index / ${lazyPagingItems.itemCount}"
                    )
                    Log.d(TAG, "Overview: preloadingData size: ${preloadingData.size}")
                    Log.d(TAG, "Overview: Missing item : ${lazyPagingItems[index]}")
                    return@items
                }
                if (itemPreload._type == 1) {
                    DateDivider(
                        date = itemPreload.date!!, displayYear = false
                    )
                } else {
                    GalleryItemView(itemPreload)
                }
            } else {
                Log.d(
                    TAG,
                    "Overview: item missing from preloadingData: $index / ${lazyPagingItems.itemCount}"
                )
            }
        }
    }
}




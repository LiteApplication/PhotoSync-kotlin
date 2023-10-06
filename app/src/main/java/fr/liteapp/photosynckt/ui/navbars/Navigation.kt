package fr.liteapp.photosynckt.ui.navbars

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import fr.liteapp.photosynckt.R
import fr.liteapp.photosynckt.TAG
import fr.liteapp.photosynckt.db.PhotoSyncKtDB
import fr.liteapp.photosynckt.network.data.User
import fr.liteapp.photosynckt.network.repository.OnlinePhotosManager
import fr.liteapp.photosynckt.network.repository.UserRepository
import fr.liteapp.photosynckt.utils.GalleryIndex


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MyTopBar(
    route: String,
    database: PhotoSyncKtDB,
    scrollBehavior: TopAppBarScrollBehavior,
    galleryIndex: GalleryIndex,
    screenNavController: NavController,
    userRepository: UserRepository,
    onlinePhotosManager: OnlinePhotosManager
) {
    var refreshClicked by rememberSaveable { mutableStateOf(false) }
    var refreshOnlineClicked by rememberSaveable {
        mutableStateOf(false)
    }

    TopAppBar(colors = TopAppBarDefaults.largeTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ),
        title = {
            when (route) {
                "overview" -> Text(stringResource(R.string.app_name))
                "favorites" -> Text("Favorites")
                "folders" -> Text("Folders")
                "search" -> Text("Search")
                else -> Text("Fail")
            }
        },
        scrollBehavior = scrollBehavior,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        actions = {
            when (route) {
                "overview" -> {
                    IconButton(onClick = {
                        refreshClicked = true
                    }) {
                        Icon(
                            Icons.Default.Refresh, contentDescription = "Refresh"
                        )
                        if (refreshClicked) {
                            RefreshGalleryIndex(galleryIndex = galleryIndex) {
                                refreshClicked = false

                            }
                        }
                    }
                    IconButton(onClick = {
                        if (userRepository.isLoggedIn()) screenNavController.navigate("profile")
                        else screenNavController.navigate("login")
                    }) {
                        Icon(
                            Icons.Default.AccountCircle, contentDescription = "Account"
                        )
                    }
                    IconButton(
                        onClick = {
                            refreshOnlineClicked = true
                        }
                    ){
                        Icon(Icons.Default.Downloading, contentDescription = "Refresh Online")
                        if (refreshOnlineClicked) {
                            FirstServerSync(onlinePhotosManager = onlinePhotosManager) {
                                refreshOnlineClicked = false
                            }
                        }
                    }
                }

                "favorites" -> Unit
                "folders" -> IconButton(onClick = {
                    refreshClicked = true
                }) {
                    Icon(
                        Icons.Default.Refresh, contentDescription = "Refresh"
                    )
                    if (refreshClicked) {
                        UpdateFolderDatabase(dao = database.discoveredFoldersDAO()) {
                            refreshClicked = false
                        }
                    }
                }

                else -> Unit
            }
        })
}

@Composable
fun MyBottomBar(navController: NavController) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(bottom = 0.dp, top = 0.dp)

    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val colors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.inversePrimary
        )
        val iconPaddingValues =
            Modifier.padding(top = 0.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
        val textPaddingValues = Modifier.padding(top = 51.dp)
        val alwaysShowLabel = false

        NavigationBarItem(
            selected = currentRoute == "overview",
            icon = {
                Icon(
                    Icons.Outlined.Photo,
                    contentDescription = "Overview",
                    modifier = iconPaddingValues
                )
            },
            label = { Text("Overview", modifier = textPaddingValues) },
            onClick = {
                navController.navigate("overview") {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = colors,
            alwaysShowLabel = alwaysShowLabel,
        )

        NavigationBarItem(selected = currentRoute == "favorites", icon = {
            Icon(
                Icons.Outlined.Star,
                contentDescription = "Favorites",
                modifier = iconPaddingValues
            )
        }, label = { Text("Favorites", modifier = textPaddingValues) }, onClick = {
            navController.navigate("favorites") {
                launchSingleTop = true
                restoreState = true
            }
        }, colors = colors, alwaysShowLabel = alwaysShowLabel

        )

        NavigationBarItem(
            selected = currentRoute == "search",
            onClick = {
                navController.navigate("search") {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = "Search",
                    modifier = iconPaddingValues
                )
            },
            label = { Text("Search", modifier = textPaddingValues) },
            colors = colors,
            alwaysShowLabel = alwaysShowLabel,
        )

        NavigationBarItem(selected = currentRoute == "folders", icon = {
            Icon(
                Icons.Outlined.Folder,
                contentDescription = "Folders",
                modifier = iconPaddingValues
            )
        }, label = { Text("Folders", modifier = textPaddingValues) }, onClick = {
            navController.navigate("folders") {
                launchSingleTop = true
                restoreState = true
            }
        }, colors = colors, alwaysShowLabel = alwaysShowLabel

        )
    }
}

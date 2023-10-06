package fr.liteapp.photosynckt.ui.pages

import android.view.Window
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.liteapp.photosynckt.Greeting
import fr.liteapp.photosynckt.db.PhotoSyncKtDB
import fr.liteapp.photosynckt.network.repository.OnlinePhotosManager
import fr.liteapp.photosynckt.network.repository.UserRepository
import fr.liteapp.photosynckt.ui.navbars.MyBottomBar
import fr.liteapp.photosynckt.ui.navbars.MyTopBar
import fr.liteapp.photosynckt.utils.GalleryIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryActivity(
    screenNavController: NavController,
    photoSyncKtDB: PhotoSyncKtDB,
    scrollBehavior: TopAppBarScrollBehavior,
    galleryIndex: GalleryIndex,
    userRepository: UserRepository,
    onlinePhotosManager: OnlinePhotosManager,
    window: Window

) {
    val tabNavController = rememberNavController()

    Scaffold(topBar = {
        val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        MyTopBar(
            route = currentRoute ?: "fail",
            database = photoSyncKtDB,
            scrollBehavior = scrollBehavior,
            galleryIndex = galleryIndex,
            screenNavController = screenNavController,
            userRepository = userRepository,
            onlinePhotosManager = onlinePhotosManager
        )
    }, bottomBar = { MyBottomBar(navController = tabNavController) }, content = {
        val paddingValues = it
        window.statusBarColor = MaterialTheme.colorScheme.primaryContainer.toArgb()

        NavHost(tabNavController,
            startDestination = "overview",
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }) {
            composable("overview") {
                Overview(
                    paddingValues = paddingValues,
                    dao = photoSyncKtDB.galleryDAO(),
                    nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                )
            }
            composable("favorites") {
                Greeting(
                    name = "Favorites", modifier = Modifier.padding(paddingValues)
                )
            }

            composable("search") {
                Greeting(
                    name = "Search", modifier = Modifier.padding(paddingValues)
                )
            }

            composable("folders") {
                FoldersPage(
                    dao = photoSyncKtDB.discoveredFoldersDAO(),
                    paddingValues = paddingValues,
                    nestedScrollConnection = scrollBehavior.nestedScrollConnection
                )
            }


        }
    })
}

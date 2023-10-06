package fr.liteapp.photosynckt

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.liteapp.photosynckt.db.getDatabase
import fr.liteapp.photosynckt.network.ApiClient
import fr.liteapp.photosynckt.network.repository.OnlinePhotosManager
import fr.liteapp.photosynckt.network.repository.UserRepository
import fr.liteapp.photosynckt.ui.pages.GalleryActivity
import fr.liteapp.photosynckt.ui.pages.LoginScreen
import fr.liteapp.photosynckt.ui.pages.MyAccountScreen
import fr.liteapp.photosynckt.ui.theme.PhotoSyncKtTheme
import fr.liteapp.photosynckt.utils.GalleryIndex
import fr.liteapp.photosynckt.utils.ThumbnailManager

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Environment.isExternalStorageManager()) {
            Toast.makeText(applicationContext, "Allow PhotoSync to access files", Toast.LENGTH_LONG)
                .show()
            // Request External Storage Access
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
        // Request MediaStore Access (probably not needed tho)
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 27012004
        )

        val db = getDatabase(applicationContext)
        val galleryIndex = GalleryIndex(
            context = applicationContext,
            galleryDAO = db.galleryDAO(),
            discoveredFoldersDAO = db.discoveredFoldersDAO()
        )

        // Network stuff
        ApiClient.setContext(applicationContext) // Set the context for the API client (get the base URL)
        val photoSyncApi = ApiClient.getPhotoSyncApi
        // Init UserRepository
        val userRepository = UserRepository(photoSyncApi, applicationContext)

        // Initialise OnlinePhotosManager
        val onlinePhotosManager = OnlinePhotosManager(
            api = photoSyncApi,
            context = applicationContext,
            userRepository = userRepository,
            galleryDAO = db.galleryDAO()
        )

        // Initialise the singleton ThumbnailManager
        ThumbnailManager(applicationContext, db.galleryDAO())

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().penaltyLog().build()
        )


        setContent {
            PhotoSyncKtTheme {
                val screenControl = rememberNavController()
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()


                NavHost(navController = screenControl, startDestination = "main") {
                    composable("login") {
                        LoginScreen(
                            userRepository = userRepository,
                            modifier = Modifier,
                            context = applicationContext,
                        ) {
                            // On successful login, navigate to the main screen
                            screenControl.navigate("main")
                        }
                    }

                    composable("profile") {
                        MyAccountScreen(
                            userRepository = userRepository,
                            modifier = Modifier,
                            context = applicationContext
                        ) {
                            // On successful logout, navigate to the login screen
                            screenControl.navigate("login")
                        }
                    }

                    composable("main") {
                        GalleryActivity(
                            screenNavController = screenControl,
                            photoSyncKtDB = db,
                            scrollBehavior = scrollBehavior,
                            galleryIndex = galleryIndex,
                            userRepository = userRepository,
                            onlinePhotosManager = onlinePhotosManager,
                            window = window
                        )
                    }

                }
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Page $name!", modifier = modifier, style = MaterialTheme.typography.displayLarge
    )
}

package fr.liteapp.photosynckt.network

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.getString
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fr.liteapp.photosynckt.R
import fr.liteapp.photosynckt.TAG
import fr.liteapp.photosynckt.network.api.PhotoSyncApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Get the base API URL from the resources R.string.server_url
    private var baseUrl: String = ""
    private var token: String? = null

    fun getToken(): String? {
        return token
    }

    fun setContext(context: Context) {
        baseUrl = getString(context, R.string.server_url)
        setToken(context)
    }

    fun setToken(token: String?) {
        this.token = token
    }

    fun setToken(context: Context) {
        // Get the token from the shared preferences
        val sharedPref = context.getSharedPreferences(
            "current_user", Context.MODE_PRIVATE
        )

        val token = sharedPref.getString("token", null)
        if (token != null) {
            this.token = token
        }
    }

    // Gson
    private val gson: Gson by lazy {
        GsonBuilder().setLenient().create()
    }

    // OkHttp
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpExceptionInterceptor())
            .build()
    }

    // Create the Retrofit instance
    private val retrofit: Retrofit by lazy {
        Log.d(TAG, "Creating Retrofit instance for $baseUrl")
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val getPhotoSyncApi: PhotoSyncApi by lazy {
        retrofit.create(PhotoSyncApi::class.java)
    }

    fun getBaseUrl(): String {
        return baseUrl
    }

    fun getThumbnailUri(id: String, size: Int = 0): Uri {
        return Uri.parse("${baseUrl}timg/get/$id/$size")
    }
}
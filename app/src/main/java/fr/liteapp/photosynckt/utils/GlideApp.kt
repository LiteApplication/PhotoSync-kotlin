package fr.liteapp.photosynckt.utils

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.load.engine.executor.GlideExecutor
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import fr.liteapp.photosynckt.diskCacheSize
import fr.liteapp.photosynckt.network.ApiClient
import fr.liteapp.photosynckt.network.repository.UserRepository
import fr.liteapp.photosynckt.screenCount
import okhttp3.OkHttpClient
import java.io.InputStream

@GlideModule
class GalleryGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDefaultRequestOptions(
            RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        )
        val memoryCalculator =
            MemorySizeCalculator.Builder(context).setMemoryCacheScreens(screenCount.toFloat())
                .build()
        builder.setMemoryCache(LruResourceCache(memoryCalculator.memoryCacheSize.toLong()))
        builder.setDiskCache(
            DiskLruCacheFactory(
                "${context.cacheDir}/image_cache", diskCacheSize
            )
        )
        val bitmapCalculator =
            MemorySizeCalculator.Builder(context).setBitmapPoolScreens(screenCount.toFloat())
                .build()
        builder.setBitmapPool(LruBitmapPool(bitmapCalculator.bitmapPoolSize.toLong()))
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {

        // Build the OkHttpClient to add the Token to the header
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = ApiClient.getToken()?.let {
                original.newBuilder().header("Token", it) // Add Token to header
                    .method(original.method(), original.body())
            }
            if (requestBuilder == null) chain.proceed(original)
            else chain.proceed(requestBuilder.build())
        }
        val okHttpClient = httpClient.build()
        registry.replace(
            GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(okHttpClient)
        )
    }
}
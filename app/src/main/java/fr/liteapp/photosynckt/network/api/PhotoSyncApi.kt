package fr.liteapp.photosynckt.network.api

import fr.liteapp.photosynckt.network.request.Credentials
import fr.liteapp.photosynckt.network.response.ApiStatus
import fr.liteapp.photosynckt.network.response.LoginStatus
import fr.liteapp.photosynckt.network.response.PhotosPageResult
import fr.liteapp.photosynckt.network.response.TokenStatus
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Streaming

interface PhotoSyncApi {
    /*** ACCOUNTS ***/
    @GET("admin/test")
    fun testAdmin(@Header("Token") token: String): Call<ApiStatus>

    @GET("accounts/test")
    fun getUserInfos(@Header("Token") token: String): Call<LoginStatus>

    @POST("accounts/login")
    fun login(@Body credentials: Credentials): Call<TokenStatus>

    @PUT("accounts/create")
    fun create(@Body credentials: Credentials): Call<TokenStatus>

    @POST("accounts/logout")
    fun logout(@Header("Token") token: String): Call<ApiStatus>


    /*** GALLERY ***/
    @POST("files/page/{page}/pageSize/{pageSize}")
    fun getPhotos(@Header("Token") token: String, @Path("page") page: Int, @Path("pageSize") pageSize: Int): Call<PhotosPageResult>

    @GET("files/file-list")
    fun getAllPhotos(@Header("Token") token: String): Call<PhotosPageResult>



    /*** THUMBNAILS ***/
    @GET("timg/get/{photoId}/0")
    @Streaming
    fun downloadThumbnail(@Header("Token") token: String, @Path("photoId") photoId: String): Call<ResponseBody>
}
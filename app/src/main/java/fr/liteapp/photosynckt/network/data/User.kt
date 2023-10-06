package fr.liteapp.photosynckt.network.data

import com.google.gson.annotations.SerializedName

data class User(
    val created : Int? = null,
    @SerializedName("fullname")
    val fullName : String? = null,
    @SerializedName("user_id")
    val userId : String? = null,
    val username : String? = null,
    var token : String? = null,
    val admin: Boolean? = null
)

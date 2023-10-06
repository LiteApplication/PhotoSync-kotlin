package fr.liteapp.photosynckt.network.response

import com.google.gson.annotations.SerializedName
import fr.liteapp.photosynckt.network.data.User

data class LoginStatus(
    val message: String? = null,

    @SerializedName("created")
    val created : Int = 0,
    @SerializedName("fullname")
    val fullName : String = "",
    @SerializedName("user_id")
    val userId : String = "",
    @SerializedName("username")
    val username : String = ""
) {
    override fun toString(): String {
        return message ?: "<no message>"
    }

    fun isSuccessful(): Boolean {
        return message == "OK"
    }

    fun hasData(): Boolean {
        return userId != ""
    }

    fun getUser() : User {
        return User(created, fullName, userId, username)
    }
}


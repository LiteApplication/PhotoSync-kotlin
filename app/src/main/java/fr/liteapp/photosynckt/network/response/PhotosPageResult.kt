package fr.liteapp.photosynckt.network.response

import com.google.gson.annotations.SerializedName
import fr.liteapp.photosynckt.network.data.OnlinePhoto

data class PhotosPageResult(
    val message: String,
    @SerializedName("files")
    val photos: List<OnlinePhoto>,
){
    fun isSuccessful(): Boolean {
        return message == "OK"
    }

    fun hasData(): Boolean {
        return isSuccessful() && photos.isNotEmpty()
    }

    override fun toString(): String {
        return message
    }
}

package fr.liteapp.photosynckt.network.response

data class ApiStatus(
    val message: String? = null,
) {
    override fun toString(): String {
        return message ?: "<no message>"
    }

    fun isSuccessful(): Boolean {
        return message == "OK"
    }
}


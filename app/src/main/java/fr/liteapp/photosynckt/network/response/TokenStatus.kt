package fr.liteapp.photosynckt.network.response

data class TokenStatus(
    val message : String = "",
    val token : String? = null,
) {

    constructor(message: String): this(message, null)
    override fun toString(): String {
        return message ?: "<no message>"
    }

    fun isSuccessful(): Boolean {
        return message == "OK"
    }
}

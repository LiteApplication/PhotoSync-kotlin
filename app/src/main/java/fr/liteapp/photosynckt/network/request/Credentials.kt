package fr.liteapp.photosynckt.network.request

data class Credentials(
    val username: String,
    val password: String,
    val fullname: String,
) {
    constructor(username: String, password: String) : this(username, password, "")
}

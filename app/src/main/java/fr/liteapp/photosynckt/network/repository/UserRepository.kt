package fr.liteapp.photosynckt.network.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import fr.liteapp.photosynckt.TAG
import fr.liteapp.photosynckt.network.ApiClient
import fr.liteapp.photosynckt.network.api.PhotoSyncApi
import fr.liteapp.photosynckt.network.data.User
import fr.liteapp.photosynckt.network.request.Credentials
import fr.liteapp.photosynckt.network.response.ApiStatus
import fr.liteapp.photosynckt.network.response.LoginStatus
import fr.liteapp.photosynckt.network.response.TokenStatus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.ReadOnlyProperty


class UserRepository(
    val api: PhotoSyncApi, val context: Context
) {
    private val UninitializedValue = "UninitializedValue###"
    private var myToken: String? = UninitializedValue


    init {
        ApiClient.setContext(context)
    }

    fun getToken(): String? {
        if (myToken == UninitializedValue) {
            myToken = getSharedPrefsToken(context)
            myToken?.let { ApiClient.setToken(it) }
        }

        Log.d(TAG, "getToken: $myToken")
        return myToken
    }

    val user: User by lazy {
        getSharedPrefsUser(context)
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    private fun getSharedPrefsToken(context: Context): String? {
        val sharedPref = context.getSharedPreferences("current_user", Context.MODE_PRIVATE)
        return sharedPref.getString("token", null)
    }

    private fun setSharedPrefsToken(context: Context, token: String) {
        val sharedPref = context.getSharedPreferences("current_user", Context.MODE_PRIVATE)

        with(sharedPref.edit()) {
            putString("token", token)
            apply()
        }

        myToken = token
    }

    private fun getSharedPrefsUser(context: Context): User {
        val sharedPref = context.getSharedPreferences("current_user", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "")
        val fullName = sharedPref.getString("fullname", "")
        val userId = sharedPref.getString("user_id", "")
        val token = sharedPref.getString("token", "")
        val created = sharedPref.getInt("created", 0)

        var admin: Boolean? = null;
        if (sharedPref.contains("admin")) {
            admin = sharedPref.getBoolean("admin", false)
        }
        return User(created, fullName, userId, username, token, admin)
    }

    private fun setSharedPrefsUser(user: User, context: Context) {
        // Save the user as a shared preference
        val sharedPref = context.getSharedPreferences("current_user", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("created", user.created ?: 0)
            putString("username", user.username)
            putString("fullname", user.fullName)
            putString("user_id", user.userId)
            putString("token", user.token)
            if (user.admin != null) putBoolean("admin", user.admin)
            apply()
        }
    }

    fun forceUserRefresh(context: Context, callback: (LoginStatus) -> Unit) {
        val token = this.getToken()
        if (token != null) {
            api.getUserInfos(token).enqueue(object : Callback<LoginStatus> {
                override fun onResponse(
                    call: Call<LoginStatus>, response: Response<LoginStatus>
                ) {
                    val user = response.body()?.getUser()
                    if (user != null) {
                        user.token = token
                        setSharedPrefsUser(user, context)
                    }
                    callback(response.body() ?: LoginStatus("No response"))
                }

                override fun onFailure(call: Call<LoginStatus>, t: Throwable) {
                    callback(LoginStatus(t.message ?: "Failure"))
                }
            })
        } else Log.e(TAG, "forceUserRefresh: Full user refresh requested without a token")
    }

    fun login(
        context: Context, username: String, password: String, callback: (TokenStatus) -> Unit
    ) {
        val credentials = Credentials(username, password)
        Log.d(TAG, "login: Connecting to the server")
        api.login(credentials).enqueue(object : Callback<TokenStatus> {
            override fun onResponse(call: Call<TokenStatus>, response: Response<TokenStatus>) {
                if (response.isSuccessful) {
                    response.body()?.token?.let {
                        setSharedPrefsToken(
                            context, it
                        )
                        forceUserRefresh(context) {}
                    } // Save the token to the database
                    callback(response.body() ?: TokenStatus("No response"))
                } else {
                    callback(response.body() ?: TokenStatus("No response (${response.code()})"))
                }
            }

            override fun onFailure(call: Call<TokenStatus>, t: Throwable) {
                callback(TokenStatus(t.message ?: "Failure"))
            }
        })
    }

    fun logout(context: Context, callback: (ApiStatus) -> Unit) {
        val token = this.getToken()
        if (token != null && token != "") {
            api.logout(token).enqueue(object : Callback<ApiStatus> {
                override fun onResponse(call: Call<ApiStatus>, response: Response<ApiStatus>) {
                    if (response.isSuccessful) {
                        setSharedPrefsUser(User(null, null, null, null, null, null), context)
                        ApiClient.setToken(null)
                    }
                    callback(response.body() ?: ApiStatus("No response"))
                }

                override fun onFailure(call: Call<ApiStatus>, t: Throwable) {
                    callback(ApiStatus(t.message ?: "Failure"))
                }
            })
        }
    }

    fun create(
        context: Context,
        username: String,
        password: String,
        fullname: String,
        callback: (TokenStatus) -> Unit
    ) {
        val credentials = Credentials(username, password, fullname)
        api.create(credentials).enqueue(object : Callback<TokenStatus> {
            override fun onResponse(call: Call<TokenStatus>, response: Response<TokenStatus>) {
                response.body()?.token?.let {
                    setSharedPrefsToken(
                        context, it
                    )
                    forceUserRefresh(context) {}
                } // Save the token to the database
                callback(response.body() ?: TokenStatus("No response"))
            }

            override fun onFailure(call: Call<TokenStatus>, t: Throwable) {
                callback(TokenStatus(t.message ?: "Failure"))
            }
        })
    }

}

package fr.liteapp.photosynckt.ui.pages

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import fr.liteapp.photosynckt.network.repository.UserRepository

@Composable
fun MyAccountScreen(
    userRepository: UserRepository,
    modifier: Modifier = Modifier,
    context: Context,
    onLogout: () -> Unit
) {
    val user = userRepository.user

    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Text(text = "Username: ${user.username}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Full name: ${user.fullName}")
        Text(text = "User ID: ${user.userId}")
        Text(text = "Created: ${user.created}")
        Text(text = "Token: ${user.token}")
        Text(text = "Admin: ${user.admin}")

        Button(onClick = {
            userRepository.logout(context) {
                if (it.isSuccessful()) onLogout()
                else Toast.makeText(context, "Unable to logout: ${it.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }) {
            Text(text = "Logout")
        }
    }

}
package fr.liteapp.photosynckt.ui.pages

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.liteapp.photosynckt.TAG
import fr.liteapp.photosynckt.network.repository.UserRepository

@Composable
fun LoginScreen(
    userRepository: UserRepository,
    modifier: Modifier = Modifier,
    context: Context,
    onLogin: () -> Unit
) {
    // In the center, add a box with rounded corners
    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        val username = remember { mutableStateOf(TextFieldValue()) }
        val password = remember { mutableStateOf(TextFieldValue()) }
        val fullName = remember { mutableStateOf(TextFieldValue()) }
        val loginRegister = remember { mutableStateOf(false) } // False for login


        // Add the app icon
        /*Image(
            painter = painterResource(id = R.mipmap.ic_launcher_round),
            contentDescription = "PhotoSync",
            alignment = Alignment.Center,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(100.dp)
                .width(100.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )*/
        Text(
            text = "PhotoSync", style = TextStyle(
                fontSize = 40.sp,
                fontFamily = FontFamily.Default,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(20.dp))
        TextField(label = { Text(text = "Username") },
            value = username.value,
            onValueChange = { username.value = it })

        if (loginRegister.value) {
            Spacer(modifier = Modifier.height(20.dp))
            TextField(label = { Text(text = "Full Name") },
                value = fullName.value,
                onValueChange = { fullName.value = it })
        }

        Spacer(modifier = Modifier.height(20.dp))
        TextField(label = { Text(text = "Password") },
            value = password.value,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            onValueChange = { password.value = it })

        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Button(
                onClick = {
                    if (loginRegister.value) {
                        userRepository.create(
                            context = context,
                            username = username.value.text,
                            password = password.value.text,
                            fullname = fullName.value.text
                        ) {
                            if (it.isSuccessful()) {
                                onLogin()
                            }
                            else {
                                Toast.makeText(context, "Account creation failed : ${it.message}", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, "LoginScreen: Account creation failed : ${it.message}")
                            }
                        }
                    }
                    else {
                        userRepository.login(
                            context = context,
                            username = username.value.text,
                            password = password.value.text
                        ) {
                            if (it.isSuccessful()) {
                                onLogin()
                            }
                            else {
                                Toast.makeText(context, "Login failed : ${it.message}", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, "LoginScreen: Login failed : ${it.message}")
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (loginRegister.value) Text(text = "Create an account")
                else Text(text = "Login")
            }
        }

        // Create an account
        Spacer(modifier = Modifier.height(5.dp))
        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Button(
                onClick = {
                    loginRegister.value = !loginRegister.value
                },
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                if (loginRegister.value) Text(text = "Login")
                else Text(text = "Create an account")
            }
        }

    }
}

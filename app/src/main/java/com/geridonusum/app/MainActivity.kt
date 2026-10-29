package com.geridonusum.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.geridonusum.app.ui.AuthScreen
import com.geridonusum.app.ui.HomeScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    var loggedIn by remember {
                        mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
                    }
                    if (loggedIn) {
                        HomeScreen(onLogout = {
                            FirebaseAuth.getInstance().signOut()
                            loggedIn = false
                        })
                    } else {
                        AuthScreen(onLoggedIn = { loggedIn = true })
                    }
                }
            }
        }
    }
}
package com.geridonusum.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.geridonusum.app.data.BinRepository
import com.geridonusum.app.model.RecyclingBin
import com.geridonusum.app.ui.AuthScreen
import com.geridonusum.app.ui.MapScreen
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
                        var bins by remember { mutableStateOf<List<RecyclingBin>>(emptyList()) }
                        var loadError by remember { mutableStateOf(false) }

                        // Giriş yapılınca dinlemeye başla, çıkış yapılınca otomatik bırak
                        DisposableEffect(Unit) {
                            val registration = BinRepository.listenBins(
                                onResult = {
                                    bins = it
                                    loadError = false
                                },
                                onError = { loadError = true }
                            )
                            onDispose { registration.remove() }
                        }

                        MapScreen(
                            bins = bins,
                            errorMessage = if (loadError) "Kutular yüklenemedi. İnternetini kontrol et." else null,
                            onLogout = {
                                FirebaseAuth.getInstance().signOut()
                                loggedIn = false
                            }
                        )
                    } else {
                        AuthScreen(onLoggedIn = { loggedIn = true })
                    }
                }
            }
        }
    }
}
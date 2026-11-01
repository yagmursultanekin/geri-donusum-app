package com.geridonusum.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.geridonusum.app.data.BinRepository
import com.geridonusum.app.model.RecyclingBin
import com.geridonusum.app.ui.LeaderboardScreen
import com.geridonusum.app.ui.LoginScreen
import com.geridonusum.app.ui.MapScreen
import com.geridonusum.app.ui.ProfileScreen
import com.geridonusum.app.ui.RegisterScreen
import com.google.firebase.auth.FirebaseAuth

private enum class AuthPage { LOGIN, REGISTER }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    var loggedIn by remember {
                        mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
                    }
                    var authPage by remember { mutableStateOf(AuthPage.LOGIN) }
                    var notice by remember { mutableStateOf<String?>(null) }
                    var lastEmail by remember { mutableStateOf("") }

                    if (loggedIn) {
                        var bins by remember { mutableStateOf<List<RecyclingBin>>(emptyList()) }
                        var loadError by remember { mutableStateOf(false) }
                        var tab by remember { mutableIntStateOf(0) }
                        var askLogout by remember { mutableStateOf(false) }

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

                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f)) {
                                when (tab) {
                                    0 -> MapScreen(
                                        bins = bins,
                                        errorMessage = if (loadError) "Kutular yüklenemedi. İnternetini kontrol et." else null,
                                        onLogout = { askLogout = true }
                                    )
                                    1 -> LeaderboardScreen()
                                    else -> ProfileScreen(onLogout = { askLogout = true })
                                }
                            }
                            NavigationBar {
                                NavigationBarItem(
                                    selected = tab == 0,
                                    onClick = { tab = 0 },
                                    icon = { Text("🗺️") },
                                    label = { Text("Harita") }
                                )
                                NavigationBarItem(
                                    selected = tab == 1,
                                    onClick = { tab = 1 },
                                    icon = { Text("🏆") },
                                    label = { Text("Sıralama") }
                                )
                                NavigationBarItem(
                                    selected = tab == 2,
                                    onClick = { tab = 2 },
                                    icon = { Text("👤") },
                                    label = { Text("Profil") }
                                )
                            }
                        }

                        if (askLogout) {
                            AlertDialog(
                                onDismissRequest = { askLogout = false },
                                title = { Text("Çıkış yap") },
                                text = { Text("Hesabından çıkmak istediğine emin misin?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        askLogout = false
                                        FirebaseAuth.getInstance().signOut()
                                        authPage = AuthPage.LOGIN
                                        notice = null
                                        lastEmail = ""
                                        loggedIn = false
                                    }) { Text("Çıkış yap") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { askLogout = false }) { Text("Vazgeç") }
                                }
                            )
                        }
                    } else {
                        when (authPage) {
                            AuthPage.LOGIN -> LoginScreen(
                                prefillEmail = lastEmail,
                                infoMessage = notice,
                                onLoggedIn = {
                                    notice = null
                                    loggedIn = true
                                },
                                onGoRegister = {
                                    notice = null
                                    authPage = AuthPage.REGISTER
                                }
                            )
                            AuthPage.REGISTER -> RegisterScreen(
                                onRegistered = { email ->
                                    lastEmail = email
                                    notice = "Hesabın oluşturuldu. Şimdi giriş yapabilirsin."
                                    authPage = AuthPage.LOGIN
                                },
                                onGoLogin = { authPage = AuthPage.LOGIN }
                            )
                        }
                    }
                }
            }
        }
    }
}
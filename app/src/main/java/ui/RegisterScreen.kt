package com.geridonusum.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.geridonusum.app.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private fun strengthColor(level: StrengthLevel): Color = when (level) {
    StrengthLevel.ZAYIF -> Color(0xFFD32F2F)
    StrengthLevel.ORTA -> Color(0xFFF9A825)
    StrengthLevel.GUCLU -> Color(0xFF2E7D32)
}

@Composable
fun RegisterScreen(
    onRegistered: (String) -> Unit,
    onGoLogin: () -> Unit
) {
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val strength = PasswordChecker.check(password)

    fun submit() {
        error = null
        val cleanEmail = email.trim()
        val cleanNickname = nickname.trim()

        if (cleanNickname.length < 3 || cleanNickname.length > 20) {
            error = "Takma ad 3 ile 20 karakter arasında olmalı."
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            error = "Geçerli bir e-posta gir."
            return
        }
        if (strength.level == StrengthLevel.ZAYIF) {
            error = "Şifren çok zayıf. En az \"Orta\" seviyeye çıkar."
            return
        }
        if (password != confirm) {
            error = "Şifreler aynı değil."
            return
        }

        loading = true
        auth.createUserWithEmailAndPassword(cleanEmail, password)
            .addOnCompleteListener { task ->
                val user = task.result?.user
                if (!task.isSuccessful || user == null) {
                    loading = false
                    error = "Kayıt olunamadı. Bu e-posta ile zaten hesap açılmış olabilir " +
                            "ya da bilgiler hatalı."
                    return@addOnCompleteListener
                }

                val profile = UserProfile(
                    id = user.uid,
                    nickname = cleanNickname,
                    totalPoints = 0
                )
                db.collection("users").document(user.uid).set(profile)
                    .addOnCompleteListener { saveTask ->
                        if (saveTask.isSuccessful) {
                            // Kayıttan sonra giriş yapılmış sayılmasın, giriş ekranına dönülsün
                            auth.signOut()
                            loading = false
                            onRegistered(cleanEmail)
                        } else {
                            // Profil kaydedilemediyse yarım hesap kalmasın
                            user.delete().addOnCompleteListener {
                                auth.signOut()
                                loading = false
                                error = "Profil kaydedilemedi. Tekrar dene."
                            }
                        }
                    }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Kayıt Ol", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { if (it.length <= 20) nickname = it },
            label = { Text("Takma ad (sıralamada görünür)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Şifre"
        )

        if (password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            val color = strengthColor(strength.level)
            LinearProgressIndicator(
                progress = { strength.level.progress },
                color = color,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Şifre gücü: ${strength.level.label}",
                color = color,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
            if (strength.level != StrengthLevel.GUCLU) {
                Text(
                    text = "Güçlendirmek için: ${strength.missing.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        PasswordField(
            value = confirm,
            onValueChange = { confirm = it },
            label = "Şifre (tekrar)"
        )
        if (confirm.isNotEmpty()) {
            val same = password == confirm
            Text(
                text = if (same) "Şifreler eşleşiyor" else "Şifreler aynı değil",
                color = if (same) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }

        error?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { submit() },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Kaydediliyor..." else "Kayıt Ol")
        }

        TextButton(onClick = onGoLogin) {
            Text("Zaten hesabım var, giriş yap")
        }
    }
}
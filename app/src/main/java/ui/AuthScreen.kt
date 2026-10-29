package com.geridonusum.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.geridonusum.app.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AuthScreen(onLoggedIn: () -> Unit) {
    var isRegister by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    fun submit() {
        error = null
        val cleanEmail = email.trim()
        val cleanNickname = nickname.trim()

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            error = "Geçerli bir e-posta gir."
            return
        }
        if (password.length < 8) {
            error = "Şifre en az 8 karakter olmalı."
            return
        }
        if (isRegister && (cleanNickname.length < 3 || cleanNickname.length > 20)) {
            error = "Takma ad 3 ile 20 karakter arasında olmalı."
            return
        }

        loading = true

        if (isRegister) {
            auth.createUserWithEmailAndPassword(cleanEmail, password)
                .addOnCompleteListener { task ->
                    val user = task.result?.user
                    if (!task.isSuccessful || user == null) {
                        loading = false
                        error = "Kayıt olunamadı. Bilgileri kontrol edip tekrar dene."
                        return@addOnCompleteListener
                    }
                    val profile = UserProfile(
                        id = user.uid,
                        nickname = cleanNickname,
                        totalPoints = 0
                    )
                    db.collection("users").document(user.uid).set(profile)
                        .addOnCompleteListener { saveTask ->
                            loading = false
                            if (saveTask.isSuccessful) {
                                onLoggedIn()
                            } else {
                                // Profil kaydedilemediyse yarım hesap kalmasın
                                user.delete()
                                auth.signOut()
                                error = "Profil kaydedilemedi. Tekrar dene."
                            }
                        }
                }
        } else {
            auth.signInWithEmailAndPassword(cleanEmail, password)
                .addOnCompleteListener { task ->
                    loading = false
                    if (task.isSuccessful) {
                        onLoggedIn()
                    } else {
                        error = "E-posta ya da şifre hatalı."
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isRegister) "Kayıt Ol" else "Giriş Yap",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (isRegister) {
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Takma ad (sıralamada görünür)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

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
            Text(
                if (loading) "Bekle..." else if (isRegister) "Kayıt Ol" else "Giriş Yap"
            )
        }

        TextButton(onClick = {
            isRegister = !isRegister
            error = null
        }) {
            Text(
                if (isRegister) "Zaten hesabım var, giriş yap"
                else "Hesabım yok, kayıt ol"
            )
        }
    }
}
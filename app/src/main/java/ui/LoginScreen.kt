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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    prefillEmail: String,
    infoMessage: String?,
    onLoggedIn: () -> Unit,
    onGoRegister: () -> Unit
) {
    var email by remember { mutableStateOf(prefillEmail) }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

    fun submit() {
        error = null
        val cleanEmail = email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            error = "Geçerli bir e-posta gir."
            return
        }
        if (password.isEmpty()) {
            error = "Şifreni gir."
            return
        }
        loading = true
        auth.signInWithEmailAndPassword(cleanEmail, password)
            .addOnCompleteListener { task ->
                loading = false
                if (task.isSuccessful) {
                    onLoggedIn()
                } else {
                    error = when (task.exception) {
                        is FirebaseTooManyRequestsException ->
                            "Çok fazla deneme yaptın. Biraz bekleyip tekrar dene."
                        is FirebaseNetworkException ->
                            "İnternet bağlantını kontrol et."
                        else -> "E-posta ya da şifre hatalı."
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
        Text("♻️", style = MaterialTheme.typography.displayMedium)
        Text(
            text = "Geri Dönüşüm",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Hesabına giriş yap",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        infoMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.primary)
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

        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Şifre"
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
            Text(if (loading) "Giriş yapılıyor..." else "Giriş Yap")
        }

        TextButton(onClick = onGoRegister) {
            Text("Hesabım yok, kayıt ol")
        }
    }
}
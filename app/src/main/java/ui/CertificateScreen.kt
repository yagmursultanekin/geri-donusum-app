package com.geridonusum.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CertificateScreen(
    nickname: String,
    rank: Int,
    points: Int,
    onBack: () -> Unit
) {
    // Ad soyad sadece bu ekranda görünür, hiçbir yere kaydedilmez ya da gönderilmez
    var fullName by remember { mutableStateOf("") }
    val today = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).format(Date())
    val shownName = fullName.trim().ifEmpty { nickname }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = fullName,
            onValueChange = { if (it.length <= 40) fullName = it },
            label = { Text("Belgede görünecek ad soyad (kaydedilmez)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("♻️", style = MaterialTheme.typography.displayMedium)
                Text(
                    text = "DUYARLI VATANDAŞ BELGESİ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text("Bu belge", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = shownName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "adlı kullanıcıya, geri dönüşüm uygulamasında $points puan toplayarak " +
                            "$rank. sıraya yerleştiği ve çevreye duyarlılığını gösterdiği için verilmiştir.",
                    textAlign = TextAlign.Center
                )
                Text(today, style = MaterialTheme.typography.bodySmall)
            }
        }

        Text(
            text = "Belgeyi ekran görüntüsü alarak kaydedebilirsin.",
            style = MaterialTheme.typography.bodySmall
        )
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Geri dön")
        }
    }
}
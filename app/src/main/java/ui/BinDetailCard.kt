package com.geridonusum.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geridonusum.app.data.RecyclingRepository
import com.geridonusum.app.data.ReportRepository
import com.geridonusum.app.model.BinReport
import com.geridonusum.app.model.FillLevel
import com.geridonusum.app.model.RecyclingBin
import com.geridonusum.app.model.ReportType
import com.geridonusum.app.model.WasteType

@Composable
fun BinDetailCard(bin: RecyclingBin, onClose: () -> Unit) {
    // Geri dönüşüm kaydı
    var wasteType by remember(bin.id) { mutableStateOf<WasteType?>(bin.acceptedTypes.firstOrNull()) }
    var code by remember(bin.id) { mutableStateOf("") }
    var recycling by remember(bin.id) { mutableStateOf(false) }
    var recycleInfo by remember(bin.id) { mutableStateOf<String?>(null) }
    var recycleIsError by remember(bin.id) { mutableStateOf(false) }

    // Bildirim
    var type by remember(bin.id) { mutableStateOf(ReportType.DOLULUK) }
    var fill by remember(bin.id) { mutableStateOf(FillLevel.DOLU) }
    var message by remember(bin.id) { mutableStateOf("") }
    var sending by remember(bin.id) { mutableStateOf(false) }
    var info by remember(bin.id) { mutableStateOf<String?>(null) }
    var infoIsError by remember(bin.id) { mutableStateOf(false) }

    fun recycle() {
        val selected = wasteType
        if (selected == null) {
            recycleInfo = "Bir atık türü seç."
            recycleIsError = true
            return
        }
        if (code.isBlank()) {
            recycleInfo = "Kutu kodunu gir."
            recycleIsError = true
            return
        }
        recycling = true
        recycleInfo = null
        RecyclingRepository.recycle(bin.id, selected, code) { ok ->
            recycling = false
            if (ok) {
                recycleInfo = "Tebrikler! +${selected.points} puan kazandın."
                recycleIsError = false
                code = ""
            } else {
                recycleInfo = "Kaydedilemedi. Kodu kontrol et, son kayıttan 30 saniye geçtiğinden ve kutunun bu atığı kabul ettiğinden emol."
                recycleIsError = true
            }
        }
    }

    fun send() {
        val text = message.trim()
        if (type == ReportType.ONERI && text.length < 5) {
            info = "Öneri en az 5 karakter olmalı."
            infoIsError = true
            return
        }
        sending = true
        info = null
        val report = BinReport(
            binId = bin.id,
            type = type,
            message = text,
            fillLevel = if (type == ReportType.DOLULUK) fill else null
        )
        ReportRepository.submit(report) { ok ->
            sending = false
            if (ok) {
                info = "Bildirimin alındı, teşekkürler!"
                infoIsError = false
                message = ""
            } else {
                info = "Gönderilemedi. Tekrar dene."
                infoIsError = true
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(
            modifier = Modifier
                .heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = bin.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onClose) { Text("Kapat") }
            }
            Text("Doluluk: ${bin.fillLevel.displayName}")
            Text("Hasar: ${if (bin.damaged) "Var" else "Yok"}")
            Text("Kabul: ${bin.acceptedTypes.joinToString { it.displayName }}")

            // --- Geri dönüşüm yaptım ---
            Text(
                text = "Geri dönüşüm yaptım",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 12.dp)
            )
            bin.acceptedTypes.forEach { option ->
                OptionRow("${option.displayName} (+${option.points} puan)", wasteType == option) {
                    wasteType = option
                }
            }
            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 30) code = it },
                label = { Text("Kutu kodu") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            recycleInfo?.let {
                Text(
                    text = it,
                    color = if (recycleIsError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
            Button(
                onClick = { recycle() },
                enabled = !recycling,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (recycling) "Kaydediliyor..." else "Puanı kaydet")
            }

            // --- Bildirim gönder ---
            Text(
                text = "Bildirim gönder",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 16.dp)
            )
            ReportType.entries.forEach { option ->
                OptionRow(option.displayName, type == option) { type = option }
            }

            if (type == ReportType.DOLULUK) {
                Text("Kutu şu an nasıl?")
                FillLevel.entries.forEach { option ->
                    OptionRow(option.displayName, fill == option) { fill = option }
                }
            }

            OutlinedTextField(
                value = message,
                onValueChange = { if (it.length <= 300) message = it },
                label = {
                    Text(if (type == ReportType.ONERI) "Önerin" else "Not (isteğe bağlı)")
                },
                modifier = Modifier.fillMaxWidth()
            )

            info?.let {
                Text(
                    text = it,
                    color = if (infoIsError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = { send() },
                enabled = !sending,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (sending) "Gönderiliyor..." else "Gönder")
            }
        }
    }
}

@Composable
private fun OptionRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label)
    }
}
package com.geridonusum.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.geridonusum.app.data.LeaderboardRepository
import com.geridonusum.app.model.UserProfile
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LeaderboardScreen() {
    var users by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var loadError by remember { mutableStateOf(false) }
    var showCertificate by remember { mutableStateOf(false) }

    val myId = FirebaseAuth.getInstance().currentUser?.uid

    DisposableEffect(Unit) {
        val registration = LeaderboardRepository.listenTop(
            onResult = {
                users = it
                loadError = false
            },
            onError = { loadError = true }
        )
        onDispose { registration.remove() }
    }

    // Puanı 0 olanlar listede görünmez. Aynı puandaki kullanıcılar aynı sırayı alır.
    val ranked: List<Pair<Int, UserProfile>> = users
        .filter { it.totalPoints > 0 }
        .map { user ->
            val rank = users.indexOfFirst { it.totalPoints == user.totalPoints } + 1
            rank to user
        }

    val myEntry = ranked.firstOrNull { it.second.id == myId }
    val eligibleForCertificate = myEntry != null && myEntry.first <= 3

    if (showCertificate && myEntry != null) {
        CertificateScreen(
            nickname = myEntry.second.nickname,
            rank = myEntry.first,
            points = myEntry.second.totalPoints,
            onBack = { showCertificate = false }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Sıralama",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "İlk 3 kişiye Duyarlı Vatandaş Belgesi verilir.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        if (loadError) {
            Text(
                text = "Sıralama yüklenemedi. İnternetini kontrol et.",
                color = MaterialTheme.colorScheme.error
            )
        }

        if (eligibleForCertificate) {
            Button(
                onClick = { showCertificate = true },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text("Tebrikler! Belgemi gör")
            }
        }

        if (ranked.isEmpty() && !loadError) {
            Text("Henüz puan kazanan yok. İlk sen ol!")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ranked) { (rank, user) ->
                val mine = user.id == myId
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (mine) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (rank) {
                                1 -> "🥇"
                                2 -> "🥈"
                                3 -> "🥉"
                                else -> "#$rank"
                            },
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = if (mine) "${user.nickname} (sen)" else user.nickname,
                            fontWeight = if (mine) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${user.totalPoints} puan")
                    }
                }
            }
        }
    }
}
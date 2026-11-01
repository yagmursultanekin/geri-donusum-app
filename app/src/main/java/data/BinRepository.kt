package com.geridonusum.app.data

import com.geridonusum.app.model.RecyclingBin
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object BinRepository {

    // Kutuları canlı dinler: veritabanında bir şey değişirse onResult tekrar çağrılır.
    fun listenBins(
        onResult: (List<RecyclingBin>) -> Unit,
        onError: () -> Unit
    ): ListenerRegistration {
        return FirebaseFirestore.getInstance()
            .collection("bins")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onError()
                    return@addSnapshotListener
                }
                val bins = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(RecyclingBin::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        // Hatalı yazılmış bir kayıt tüm haritayı bozmasın
                        null
                    }
                }
                onResult(bins)
            }
    }
}
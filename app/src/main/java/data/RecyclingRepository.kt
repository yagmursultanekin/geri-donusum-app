package com.geridonusum.app.data

import com.geridonusum.app.model.WasteType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object RecyclingRepository {

    // Kayıt ve puan artışı tek bir paket (batch) olarak gider: ya ikisi birden olur, ya hiçbiri.
    fun recycle(
        binId: String,
        wasteType: WasteType,
        binCode: String,
        onDone: (Boolean) -> Unit
    ) {
        // Kullanıcı kimliği ekrandan değil, giriş yapmış hesaptan alınır
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onDone(false)
            return
        }

        val db = FirebaseFirestore.getInstance()
        val recordRef = db.collection("records").document()
        val userRef = db.collection("users").document(uid)

        val batch = db.batch()
        batch.set(
            recordRef,
            mapOf(
                "userId" to uid,
                "binId" to binId,
                "wasteType" to wasteType.name,
                "pointsEarned" to wasteType.points,
                "binCode" to binCode.trim().take(30),
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        batch.update(
            userRef,
            mapOf(
                "totalPoints" to FieldValue.increment(wasteType.points.toLong()),
                "lastRecycleAt" to FieldValue.serverTimestamp(),
                "lastRecordId" to recordRef.id
            )
        )
        batch.commit().addOnCompleteListener { task ->
            onDone(task.isSuccessful)
        }
    }
}
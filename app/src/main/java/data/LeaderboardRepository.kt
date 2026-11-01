package com.geridonusum.app.data

import com.geridonusum.app.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

object LeaderboardRepository {

    // En yüksek puanlı kullanıcıları canlı dinler
    fun listenTop(
        limit: Long = 20,
        onResult: (List<UserProfile>) -> Unit,
        onError: () -> Unit
    ): ListenerRegistration {
        return FirebaseFirestore.getInstance()
            .collection("users")
            .orderBy("totalPoints", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onError()
                    return@addSnapshotListener
                }
                val users = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(UserProfile::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                onResult(users)
            }
    }
}
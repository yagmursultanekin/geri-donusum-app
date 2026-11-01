package com.geridonusum.app.data

import com.geridonusum.app.model.BinReport
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object ReportRepository {

    fun submit(report: BinReport, onDone: (Boolean) -> Unit) {
        // Kullanıcı kimliği ekrandan değil, giriş yapmış hesaptan alınır
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onDone(false)
            return
        }
        val db = FirebaseFirestore.getInstance()

        // Takma ad da ekrandan değil, kullanıcının kendi profilinden okunur
        db.collection("users").document(uid).get().addOnCompleteListener { profileTask ->
            val nickname = profileTask.result?.getString("nickname")
            if (!profileTask.isSuccessful || nickname == null) {
                onDone(false)
                return@addOnCompleteListener
            }

            val ref = db.collection("reports").document()
            val safeReport = report.copy(
                id = ref.id,
                userId = uid,
                nickname = nickname,
                message = report.message.trim().take(300)
            )
            ref.set(safeReport).addOnCompleteListener { task ->
                onDone(task.isSuccessful)
            }
        }
    }
}
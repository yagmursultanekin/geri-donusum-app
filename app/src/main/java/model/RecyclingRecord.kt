package com.geridonusum.app.model

data class RecyclingRecord(
    val id: String = "",
    val userId: String = "",
    val binId: String = "",
    val wasteType: WasteType = WasteType.PLASTIK,
    val pointsEarned: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
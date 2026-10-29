package com.geridonusum.app.model

enum class ReportType(val displayName: String) {
    DOLULUK("Doluluk bildirimi"),
    HASAR("Hasar bildirimi"),
    ONERI("Geliştirme önerisi")
}

data class BinReport(
    val id: String = "",
    val binId: String = "",
    val userId: String = "",
    val type: ReportType = ReportType.ONERI,
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
package com.geridonusum.app.model

data class RecyclingBin(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val acceptedTypes: List<WasteType> = emptyList(),
    val fillLevel: FillLevel = FillLevel.BOS,
    val damaged: Boolean = false
)
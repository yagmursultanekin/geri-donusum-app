package com.geridonusum.app.data

import com.geridonusum.app.model.FillLevel
import com.geridonusum.app.model.RecyclingBin
import com.geridonusum.app.model.WasteType

// Geçici örnek veri. Koordinatlar örnektir, kendi şehrine göre değiştirebilirsin.
object SampleBins {
    val list = listOf(
        RecyclingBin(
            id = "bin-1",
            name = "Örnek Kutu 1 - Kadıköy İskele",
            latitude = 40.9916,
            longitude = 29.0252,
            acceptedTypes = listOf(WasteType.PLASTIK, WasteType.KAGIT, WasteType.CAM),
            fillLevel = FillLevel.YARI,
            damaged = false
        ),
        RecyclingBin(
            id = "bin-2",
            name = "Örnek Kutu 2 - Moda",
            latitude = 40.9877,
            longitude = 29.0257,
            acceptedTypes = listOf(WasteType.PIL, WasteType.ELEKTRONIK),
            fillLevel = FillLevel.DOLU,
            damaged = false
        ),
        RecyclingBin(
            id = "bin-3",
            name = "Örnek Kutu 3 - Beşiktaş",
            latitude = 41.0422,
            longitude = 29.0067,
            acceptedTypes = listOf(WasteType.PLASTIK, WasteType.METAL),
            fillLevel = FillLevel.BOS,
            damaged = true
        ),
        RecyclingBin(
            id = "bin-4",
            name = "Örnek Kutu 4 - Üsküdar",
            latitude = 41.0262,
            longitude = 29.0150,
            acceptedTypes = listOf(WasteType.KAGIT, WasteType.CAM, WasteType.METAL),
            fillLevel = FillLevel.BOS,
            damaged = false
        )
    )
}
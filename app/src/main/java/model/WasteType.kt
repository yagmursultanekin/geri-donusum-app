package com.geridonusum.app.model

enum class WasteType(val displayName: String, val points: Int) {
    PLASTIK("Plastik", 10),
    KAGIT("Kağıt", 8),
    CAM("Cam", 12),
    METAL("Metal", 15),
    PIL("Pil", 20),
    ELEKTRONIK("Elektronik Atık", 25)
}
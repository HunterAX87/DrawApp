package com.example.drawapp

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap

data class PathData(
    val path: Path = Path(),
    val color: Color = Color.Blue,
    val lineWidth: Float = 5f,
    val cap:StrokeCap= StrokeCap.Round
)

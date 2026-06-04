package org.example.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.example.R

// Large font family (hand-drawn sketchy aesthetic)
// Map all weights to cabin_sketch_bold to prevent fallback to system Roboto when using FontWeight.Black
val CabinSketchFamily = FontFamily(
    Font(R.font.cabin_sketch_bold, FontWeight.Thin),
    Font(R.font.cabin_sketch_bold, FontWeight.ExtraLight),
    Font(R.font.cabin_sketch_bold, FontWeight.Light),
    Font(R.font.cabin_sketch_bold, FontWeight.Normal),
    Font(R.font.cabin_sketch_bold, FontWeight.Medium),
    Font(R.font.cabin_sketch_bold, FontWeight.SemiBold),
    Font(R.font.cabin_sketch_bold, FontWeight.Bold),
    Font(R.font.cabin_sketch_bold, FontWeight.ExtraBold),
    Font(R.font.cabin_sketch_bold, FontWeight.Black)
)

// Small matching font family (clean, matching, and clear)
// Map all weights to shift thin weights to medium/semibold and prevent system fallback
val CabinFamily = FontFamily(
    Font(R.font.cabin_regular, FontWeight.Thin),
    Font(R.font.cabin_regular, FontWeight.ExtraLight),
    Font(R.font.cabin_medium, FontWeight.Light),
    Font(R.font.cabin_medium, FontWeight.Normal),
    Font(R.font.cabin_semibold, FontWeight.Medium),
    Font(R.font.cabin_semibold, FontWeight.SemiBold),
    Font(R.font.cabin_bold, FontWeight.Bold),
    Font(R.font.cabin_bold, FontWeight.ExtraBold),
    Font(R.font.cabin_bold, FontWeight.Black)
)

val AppTypography = androidx.compose.material3.Typography(
    displayLarge = androidx.compose.ui.text.TextStyle(fontFamily = CabinFamily),
    displayMedium = androidx.compose.ui.text.TextStyle(fontFamily = CabinFamily),
    displaySmall = androidx.compose.ui.text.TextStyle(fontFamily = CabinFamily),
    headlineLarge = androidx.compose.ui.text.TextStyle(fontFamily = CabinFamily),
    headlineMedium = androidx.compose.ui.text.TextStyle(fontFamily = CabinFamily),
    headlineSmall = androidx.compose.ui.text.TextStyle(fontFamily = CabinFamily),
    titleLarge = androidx.compose.ui.text.TextStyle(fontFamily = CabinFamily),
    titleMedium = androidx.compose.ui.text.TextStyle(fontFamily = CabinFamily),
    titleSmall = androidx.compose.ui.text.TextStyle(fontFamily = CabinFamily),
    bodyLarge = androidx.compose.ui.text.TextStyle(fontFamily = CabinFamily),
    bodyMedium = androidx.compose.ui.text.TextStyle(fontFamily = CabinFamily),
    bodySmall = androidx.compose.ui.text.TextStyle(fontFamily = CabinFamily),
    labelLarge = androidx.compose.ui.text.TextStyle(fontFamily = CabinFamily),
    labelMedium = androidx.compose.ui.text.TextStyle(fontFamily = CabinFamily),
    labelSmall = androidx.compose.ui.text.TextStyle(fontFamily = CabinFamily)
)

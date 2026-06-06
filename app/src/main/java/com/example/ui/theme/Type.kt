package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.unit.sp
import com.example.R

// Define Google Fonts Provider
val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// 1. DM Serif Display (italic style by default is what we load)
val DMSerifDisplayFont = GoogleFont("DM Serif Display")
val DMSerifDisplayFontFamily = FontFamily(
    Font(googleFont = DMSerifDisplayFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = DMSerifDisplayFont, fontProvider = fontProvider, weight = FontWeight.Normal, style = FontStyle.Italic)
)

// 2. DM Mono for monospace labels, badges, metadata
val DMMonoFont = GoogleFont("DM Mono")
val DMMonoFontFamily = FontFamily(
    Font(googleFont = DMMonoFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = DMMonoFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = DMMonoFont, fontProvider = fontProvider, weight = FontWeight.Bold)
)

// 3. Instrument Sans for body text
val InstrumentSansFont = GoogleFont("Instrument Sans")
val InstrumentSansFontFamily = FontFamily(
    Font(googleFont = InstrumentSansFont, fontProvider = fontProvider, weight = FontWeight.Light),
    Font(googleFont = InstrumentSansFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = InstrumentSansFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = InstrumentSansFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = InstrumentSansFont, fontProvider = fontProvider, weight = FontWeight.Bold)
)

// Create the Typography object mapped to display, body, and label text configurations:
// - displayLarge/displayMedium/displaySmall -> DM Serif Display (Italic)
// - labelLarge/labelMedium/labelSmall -> DM Mono
// - bodyLarge/bodyMedium/bodySmall -> Instrument Sans
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = DMSerifDisplayFontFamily,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = DMSerifDisplayFontFamily,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = DMSerifDisplayFontFamily,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InstrumentSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InstrumentSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InstrumentSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = DMMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 1.4.sp // ~0.1em letter spacing
    ),
    labelMedium = TextStyle(
        fontFamily = DMMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = DMMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.0.sp
    )
)

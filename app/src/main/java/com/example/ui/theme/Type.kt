package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppLanguage

val PlusJakartaFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal)
)

val DevanagariFontFamily = FontFamily(
    Font(R.font.noto_sans_devanagari, FontWeight.Normal)
)

fun krishiTypography(language: AppLanguage): Typography {
    val activeFont = when (language) {
        AppLanguage.HINDI -> DevanagariFontFamily
        AppLanguage.ENGLISH -> PlusJakartaFontFamily
    }
    return Typography(
        headlineMedium = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 34.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 30.sp
        ),
        titleLarge = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 28.sp
        ),
        titleMedium = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 26.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 22.sp
        ),
        labelLarge = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 22.sp
        ),
        labelMedium = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    )
}

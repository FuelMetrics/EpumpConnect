package africa.epump.connect.efueling.ui.theme

import africa.epump.connect.R
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val satoshiFonts = FontFamily(
    Font(R.font.satoshi_regular),
    Font(R.font.satoshi_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(R.font.satoshi_light, weight = FontWeight.Light),
    Font(R.font.satoshi_italic_light, weight = FontWeight.Light, style = FontStyle.Italic),
    Font(R.font.satoshi_bold, weight = FontWeight.Bold),
    Font(R.font.satoshi_bold_italic, weight = FontWeight.Bold, style = FontStyle.Italic),
    Font(R.font.satoshi_medium, weight = FontWeight.Medium),
    Font(R.font.satoshi_medium_italic, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(R.font.satoshi_black, weight = FontWeight.ExtraBold),
    Font(R.font.satoshi_black_italic, weight = FontWeight.ExtraBold, style = FontStyle.Italic)
)

val millikFonts = FontFamily(
    Font(R.font.millik)
)

// Set of Material typography styles to start with
val MillikTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = millikFonts,
        fontWeight = FontWeight.Normal,
        fontSize = 48.sp,
        lineHeight = 57.6.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = millikFonts,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 28.8.sp,
        letterSpacing = 0.5.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = millikFonts,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 16.8.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = millikFonts,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 19.2.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = millikFonts,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 16.8.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = millikFonts,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 14.4.sp,
        letterSpacing = 0.5.sp
    ),
)

val SatoshiTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = satoshiFonts,
        fontWeight = FontWeight.Normal,
        fontSize = 48.sp,
        lineHeight = 57.6.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = satoshiFonts,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 28.8.sp,
        letterSpacing = 0.5.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = satoshiFonts,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 16.8.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = satoshiFonts,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 19.2.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = satoshiFonts,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 16.8.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = satoshiFonts,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 14.4.sp,
        letterSpacing = 0.5.sp
    ),
)
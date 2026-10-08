package io.github.aouarius.nudibranche.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import io.github.aouarius.nudibranche.core.Rarity

object CardColors {
    val Background = Color(0xFF16181D)
    val CardFrame = Color(0xFF1B1D22)
    val CardInner = Color(0xFF23262D)
    val CardBorder = Color(0xFF3A3F49)
    val Text = Color(0xFFE8E8E8)
    val TextMuted = Color(0xFF9AA3B0)
    val Label = Color(0xFF8D96A4)
    val Locked = Color(0xFF131519)
    val LockedInner = Color(0xFF16181D)

    fun frame(rarity: Rarity): Brush = when (rarity) {
        Rarity.HAEUFIG -> Brush.linearGradient(listOf(Color(0xFF4A5059), Color(0xFF1B1D22)))
        Rarity.SELTEN -> Brush.linearGradient(listOf(Color(0xFF1D4F7A), Color(0xFF101A26)))
        Rarity.LEGENDAER -> Brush.linearGradient(listOf(Color(0xFF3C2A6B), Color(0xFF120C22)))
    }

    fun tag(rarity: Rarity): Brush = when (rarity) {
        Rarity.HAEUFIG -> Brush.horizontalGradient(listOf(Color(0xFF8A929E), Color(0xFF8A929E)))
        Rarity.SELTEN -> Brush.horizontalGradient(listOf(Color(0xFF5B8FD6), Color(0xFF5B8FD6)))
        Rarity.LEGENDAER -> Brush.horizontalGradient(listOf(Color(0xFFB993FF), Color(0xFFF0A6C8)))
    }
}

@Composable
fun NudibrancheTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFB993FF),
            onPrimary = Color(0xFF16181D),
            background = CardColors.Background,
            surface = CardColors.CardInner,
            onSurface = CardColors.Text,
        ),
        content = content,
    )
}

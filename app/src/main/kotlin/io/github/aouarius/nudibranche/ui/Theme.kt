package io.github.aouarius.nudibranche.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.aouarius.nudibranche.core.Rarity

/** Dark app chrome around light, paper-like cards. */
object AppColors {
    val Background = Color(0xFF16181D)
    val Surface = Color(0xFF23262D)
    val Border = Color(0xFF3A3F49)
    val Text = Color(0xFFE8E8E8)
    val TextMuted = Color(0xFFB4BCC8)
    val Accent = Color(0xFFB993FF)
}

object CardColors {
    val Paper = Color(0xFFFDF8EC)
    val Ink = Color(0xFF0F0F0F)
    val InkMuted = Color(0xFF3B3B3B)
    val Label = Color(0xFF5B4E37)
    val Divider = Color(0xFFC9BEA6)
    val ArtBorder = Color(0xFFC9A94A)
    val ArtBackground = Color(0xFF1D4E74)
    val Handwriting = Color(0xFF1F3A73)
    val Rule = Color(0xFFD9E2EE)

    val LockedFrame = Color(0xFF3A3F47)
    val LockedPaper = Color(0xFF23272E)
    val LockedText = Color(0xFFB4BAC4)

    private val Holo = listOf(
        Color(0xFFFF6EC4), Color(0xFFFFD36E), Color(0xFF6EFFC4), Color(0xFF6EC8FF), Color(0xFFC46EFF),
    )

    fun frame(rarity: Rarity): Brush = when (rarity) {
        Rarity.HAEUFIG -> Brush.linearGradient(listOf(Color(0xFF9AA4AD), Color(0xFFD7DDE2)))
        Rarity.SELTEN -> Brush.linearGradient(listOf(Color(0xFF2F6FD6), Color(0xFF7FB2FF)))
        Rarity.LEGENDAER -> Brush.linearGradient(Holo)
    }

    fun tag(rarity: Rarity): Brush = when (rarity) {
        Rarity.HAEUFIG -> Brush.horizontalGradient(listOf(Color(0xFF7D8790), Color(0xFF7D8790)))
        Rarity.SELTEN -> Brush.horizontalGradient(listOf(Color(0xFF2F6FD6), Color(0xFF2F6FD6)))
        Rarity.LEGENDAER -> Brush.horizontalGradient(listOf(Color(0xFFC46EFF), Color(0xFFFF6EC4)))
    }
}

/** Spacing steps used across the app, so gaps line up. */
/**
 * Keeps content clear of the status and navigation bars (and the keyboard). Some phones give
 * full-screen dialogs no bar sizes at all, so there is always at least room for a bar.
 */
fun Modifier.clearOfSystemBars(): Modifier = windowInsetsPadding(WindowInsets.safeDrawing.union(MinimumBars))

private val MinimumBars = WindowInsets(top = 32.dp, bottom = 56.dp)

object Space {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
}

@Composable
fun NudibrancheTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AppColors.Accent,
            onPrimary = AppColors.Background,
            background = AppColors.Background,
            surface = AppColors.Surface,
            onSurface = AppColors.Text,
            onSurfaceVariant = AppColors.TextMuted,
            outline = AppColors.Border,
        ),
        content = content,
    )
}

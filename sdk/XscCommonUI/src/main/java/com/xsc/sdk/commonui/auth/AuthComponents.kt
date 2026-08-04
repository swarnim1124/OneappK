package com.xsc.sdk.commonui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.OneAppMotion

/**
 * Shared layout for the password-recovery screens.
 *
 * Forgot password, Verify OTP and Reset password were three copies of the same
 * structure - a 60dp top spacer, a 56dp tinted icon, a bold 28sp title, a 15sp centred
 * caption, then the form - each with its own hardcoded sizes that had already drifted
 * apart. This is that structure, once.
 */
@Composable
fun AuthScreenLayout(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LocalSpacing.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // MainActivity calls enableEdgeToEdge(), so the app draws behind the
                // status and navigation bars. Without this the heading sits under the
                // clock and signal icons. safeDrawing covers both system bars and the
                // display cutout.
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                // Keeps the form clear of the keyboard rather than letting it cover the
                // field being typed into.
                .imePadding()
                .padding(horizontal = spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(visible = visible, enter = OneAppMotion.contentEnter(0)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.xl))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(spacing.md))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 340.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.xxl))

            AnimatedVisibility(visible = visible, enter = OneAppMotion.contentEnter(1)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.lg),
                    content = content
                )
            }

            Spacer(modifier = Modifier.height(spacing.xxl))
        }
    }
}

enum class BannerTone { Success, Error }

/**
 * Inline status message.
 *
 * Replaces the ad-hoc blocks these screens used - a bare red `Text` for errors and a
 * tinted `Row` for success, styled slightly differently on each screen. Animating in
 * means surrounding content slides rather than jumping.
 */
@Composable
fun InlineBanner(
    message: String?,
    tone: BannerTone,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val visible = !message.isNullOrBlank()

    val container = when (tone) {
        BannerTone.Success -> MaterialTheme.colorScheme.primaryContainer
        BannerTone.Error -> MaterialTheme.colorScheme.errorContainer
    }
    val onContainer = when (tone) {
        BannerTone.Success -> MaterialTheme.colorScheme.onPrimaryContainer
        BannerTone.Error -> MaterialTheme.colorScheme.onErrorContainer
    }
    val icon = when (tone) {
        BannerTone.Success -> Icons.Default.CheckCircle
        BannerTone.Error -> Icons.Default.ErrorOutline
    }

    AnimatedVisibility(
        visible = visible,
        enter = OneAppMotion.bannerEnter(),
        exit = OneAppMotion.bannerExit(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(container, MaterialTheme.shapes.small)
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = onContainer,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = message.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = onContainer
            )
        }
    }
}

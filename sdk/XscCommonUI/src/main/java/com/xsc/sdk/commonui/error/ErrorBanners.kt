package com.xsc.sdk.commonui.error

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsc.sdk.theme.OneAppMotion

/**
 * Inline validation/auth error, shown next to the field or action it belongs to -
 * never a Snackbar/Toast. Per spec: warning icon + text, 13sp, destructive-red text
 * on a destructive-red-at-8%-opacity background, 8dp corners, 10dp padding, full
 * width, left-aligned. Callers place it themselves (typically between the last input
 * and the primary button) - this only renders the banner itself.
 *
 * [onRetry], when supplied, renders a "Retry" button alongside the message - for
 * network/timeout failures specifically (see the spec's "Network retry" rule); most
 * auth/validation cases pass null since retrying without changing anything wouldn't
 * fix them.
 *
 * Wrapped in [AnimatedVisibility] by the caller (see [ErrorBannerVisibility]) rather
 * than internally, so a caller that wants no animation (e.g. a snapshot test) can
 * render [InlineErrorBanner] directly.
 */
@Composable
fun InlineErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = message,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f)
        )
        if (onRetry != null) {
            TextButton(onClick = onRetry, modifier = Modifier.padding(top = 0.dp)) {
                Text("Retry", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/** Standard enter/exit for [InlineErrorBanner] and [TracedErrorCard] - the same
 * expand/fade rhythm PremiumTextField's own field-level error already uses, so a
 * banner appearing doesn't feel like a different component from the rest of the
 * form. */
@Composable
fun ErrorBannerVisibility(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = OneAppMotion.bannerEnter(),
        exit = OneAppMotion.bannerExit()
    ) { content() }
}

/**
 * Traced backend/permission error, for the "developer-traced" API failures - a card
 * within the screen's content area rather than an inline field banner, since these
 * aren't about one input. Two lines: [headline] (what/code/why, e.g. "Could not
 * load profile: [403] Insufficient permissions" - this is
 * [com.xsc.oneapp.core.result.AppError.Traced.message], the whole first line, not
 * just its own `whatFailed` sub-field) and [context] (the permission string,
 * dispatch call, or reference id underneath -
 * [com.xsc.oneapp.core.result.AppError.Traced.context]). [onRetry] is optional -
 * when supplied, a "Retry" button is shown; omit it for a failure retrying won't fix
 * (see core.result.isRetryable).
 */
@Composable
fun TracedErrorCard(
    headline: String,
    context: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = headline,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = context,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        if (onRetry != null) {
            TextButton(
                onClick = onRetry,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text("Retry", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

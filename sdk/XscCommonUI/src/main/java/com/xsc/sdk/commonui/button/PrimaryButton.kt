package com.xsc.sdk.commonui.button

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.xsc.sdk.theme.OneAppMotion

/**
 * The app's primary call to action.
 *
 * Behaviour is unchanged - same parameters, same `enabled && !isLoading` gating, same
 * click contract. What changed is the feel:
 *
 *  - A spring-driven press scale. Springs retarget on interruption, so a rapid
 *    double-tap stays smooth where a tween would restart and stutter.
 *  - The label/spinner swap crossfades instead of hard-cutting, removing the jolt when
 *    a submit begins.
 *  - `heightIn(min=)` replaces a fixed 52dp height, so the button grows rather than
 *    clipping its label at large system font scales.
 *  - The spinner carries a content description; a loading button previously announced
 *    itself to TalkBack as an empty button.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) OneAppMotion.PressedScale else 1f,
        animationSpec = OneAppMotion.pressSpring(),
        label = "primaryButtonPressScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .scale(scale)
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(OneAppMotion.shortTween()) togetherWith fadeOut(OneAppMotion.shortTween())
            },
            label = "primaryButtonContent"
        ) { loading ->
            Box(contentAlignment = Alignment.Center) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(22.dp)
                            .semantics { contentDescription = "Working" },
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = text, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

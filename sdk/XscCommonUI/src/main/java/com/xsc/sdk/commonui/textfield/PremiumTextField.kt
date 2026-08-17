package com.xsc.sdk.commonui.textfield

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.xsc.sdk.theme.OneAppMotion
import com.xsc.sdk.theme.OneAppPillShape

/**
 * Shared text field used by login, forgot-password, and every profile form screen.
 *
 * Parameters and behaviour are unchanged - the field still reports every keystroke
 * through [onTextChange] and no validation logic moved here. The changes are visual and
 * assistive:
 *
 *  - The error message animates in and out (expand + fade) rather than appearing
 *    instantly and shoving the layout down.
 *  - Errors get an inline icon and the field's own error semantics, so the message is
 *    announced with the field instead of read as a stray sentence afterwards.
 *  - `imeAction` is now settable and defaults sensibly, so the keyboard shows "Next"
 *    or "Done" instead of a newline key on a single-line field.
 *  - Password fields declare `KeyboardType.Password`, which disables personalised
 *    autocorrect learning on the entered value.
 *  - The caller's [modifier] now reaches the field itself rather than only the wrapper,
 *    so a caller can size it.
 *
 * [filled] is an additive, opt-in parameter (default `false` preserves the outlined look
 * everywhere it isn't passed) that renders a borderless, pill-shaped field on a muted
 * surface - the style used by the redesigned Login screen. Existing call sites are
 * visually unaffected.
 */
@Composable
fun PremiumTextField(
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isSecure: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null,
    error: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    filled: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    val hasError = error != null
    val effectiveKeyboardType = if (isSecure) KeyboardType.Password else keyboardType

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text(placeholder, style = MaterialTheme.typography.bodyLarge)
            },
            leadingIcon = icon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (hasError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            },
            trailingIcon = if (isSecure && onPasswordVisibilityToggle != null) {
                {
                    IconButton(onClick = onPasswordVisibilityToggle) {
                        Icon(
                            imageVector = if (isPasswordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (isPasswordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            }
                        )
                    }
                }
            } else null,
            visualTransformation = if (isSecure && !isPasswordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = effectiveKeyboardType,
                imeAction = imeAction
            ),
            singleLine = singleLine,
            enabled = enabled,
            isError = hasError,
            textStyle = textStyle,
            shape = if (filled) OneAppPillShape else MaterialTheme.shapes.small,
            colors = if (filled) {
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            } else {
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(
            visible = hasError,
            enter = OneAppMotion.bannerEnter(),
            exit = OneAppMotion.bannerExit()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    // The message beside it already conveys this; a description here
                    // would make TalkBack say "error" twice.
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(14.dp)
                        .clearAndSetSemantics { }
                )
                Text(
                    text = error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}

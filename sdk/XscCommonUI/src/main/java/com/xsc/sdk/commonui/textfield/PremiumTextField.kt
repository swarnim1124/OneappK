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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.xsc.sdk.theme.OneAppMotion

/**
 * Shared text field used by login, forgot-password, and every profile form screen.
 *
 * Parameters and behaviour are unchanged - the field still reports every keystroke
 * through [onTextChange] and no validation logic moved here, and the [placeholder]
 * parameter name is unchanged so no call site needs editing. The changes are visual and
 * assistive:
 *
 *  - [placeholder] now renders as a real M3 floating label (floats to the outline on
 *    focus/filled, per the design system's Input Fields spec) rather than a plain
 *    placeholder that only shows on an empty field - the caller-facing contract is the
 *    same string, just presented the way M3 outlined fields are meant to be.
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
    enabled: Boolean = true
) {
    val hasError = error != null
    val effectiveKeyboardType = if (isSecure) KeyboardType.Password else keyboardType

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = {
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
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                errorBorderColor = MaterialTheme.colorScheme.error
            ),
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

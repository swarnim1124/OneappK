package com.xsc.oneapp.feature.login.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.login.ui.effect.LoginEffect
import com.xsc.oneapp.feature.login.ui.event.LoginEvent
import com.xsc.oneapp.feature.login.ui.state.LoginError
import com.xsc.oneapp.feature.login.ui.viewmodel.LoginViewModel
import com.xsc.sdk.commonui.button.PrimaryButton
import com.xsc.sdk.commonui.error.ErrorBannerVisibility
import com.xsc.sdk.commonui.error.InlineErrorBanner
import com.xsc.sdk.commonui.error.TracedErrorCard
import com.xsc.sdk.commonui.logo.OneAppLogo
import com.xsc.sdk.commonui.textfield.PremiumTextField
import com.xsc.sdk.theme.LocalDarkTheme
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.LocalThemeToggle
import com.xsc.sdk.theme.OneAppMotion
import kotlinx.coroutines.flow.collectLatest

/**
 * Sign-in.
 *
 * Presentation only - the effect collection, the events dispatched to [LoginViewModel]
 * and the Toast on error are exactly the behaviour that was here before. No validation
 * moved into this file.
 *
 * Fixed while restyling: the previous layout put `Spacer(Modifier.weight(1f))` inside a
 * `verticalScroll` Column. A scrolling column measures children against unbounded
 * height, so Compose resolves weighted children to zero - the spacer silently did
 * nothing and the footer sat directly under the card rather than at the bottom.
 * Replaced with explicit spacing, which is what the original intended.
 */
@Composable
fun LoginScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isDarkMode = LocalDarkTheme.current
    val toggleTheme = LocalThemeToggle.current
    val spacing = LocalSpacing.current

    // Drives the one-shot entrance animation. Purely visual; it gates nothing.
    var contentVisible by remember { mutableStateOf(value = false) }
    LaunchedEffect(Unit) { contentVisible = true }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LoginEffect.NavigateToDashboard -> onNavigateToDashboard()
                is LoginEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // MainActivity calls enableEdgeToEdge(), so content draws behind the
                // system bars. Without this the logo sits under the status bar clock.
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                // Lifts the form clear of the keyboard instead of letting it cover the
                // password field on short screens.
                .imePadding()
                .padding(horizontal = spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(
                visible = contentVisible,
                enter = OneAppMotion.contentEnter(indexOffset = 0)
            ) {
                LoginHeader()
            }

            Spacer(modifier = Modifier.height(spacing.xxl))

            AnimatedVisibility(
                visible = contentVisible,
                enter = OneAppMotion.contentEnter(indexOffset = 1)
            ) {
                LoginFormCard(
                    email = state.emailInput,
                    password = state.passwordInput,
                    isPasswordVisible = state.isPasswordVisible,
                    isLoading = state.isLoading,
                    error = state.error,
                    onEmailChange = { viewModel.onEvent(LoginEvent.EmailChanged(it)) },
                    onPasswordChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
                    onTogglePasswordVisibility = { viewModel.onEvent(LoginEvent.TogglePasswordVisibility) },
                    onForgotPassword = {
                        viewModel.onEvent(LoginEvent.NavigatingAway)
                        onNavigateToForgotPassword()
                    },
                    onRetry = { viewModel.onEvent(LoginEvent.SubmitLogin) }
                ) { viewModel.onEvent(LoginEvent.SubmitLogin) }
            }

            Spacer(modifier = Modifier.height(spacing.xxl))

            AnimatedVisibility(
                visible = contentVisible,
                enter = OneAppMotion.contentEnter(indexOffset = 2)
            ) {
                TermsFooter()
            }

            Spacer(modifier = Modifier.height(spacing.xl))
        }

        ThemeToggleButton(isDarkMode = isDarkMode, onToggle = toggleTheme)
    }
}

@Composable
private fun LoginHeader() {
    val spacing = LocalSpacing.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ElevatedCard(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Box(modifier = Modifier.padding(spacing.md)) {
                OneAppLogo()
            }
        }

        Spacer(modifier = Modifier.height(spacing.xxl))

        Text(
            text = "Welcome back",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(spacing.sm))
        Text(
            text = "Sign in to continue to OneApp",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoginFormCard(
    email: String,
    password: String,
    isPasswordVisible: Boolean,
    isLoading: Boolean,
    error: LoginError?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onForgotPassword: () -> Unit,
    onRetry: () -> Unit,
    onSubmit: () -> Unit
) {
    val spacing = LocalSpacing.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            // Stops the card stretching to an unreadable width on tablets.
            .widthIn(max = 480.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.lg)
        ) {
            PremiumTextField(
                text = email,
                onTextChange = onEmailChange,
                placeholder = "Email or Username",
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )

            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                PremiumTextField(
                    text = password,
                    onTextChange = onPasswordChange,
                    placeholder = "Password",
                    icon = Icons.Default.Lock,
                    imeAction = ImeAction.Done,
                    isSecure = true,
                    isPasswordVisible = isPasswordVisible,
                    onPasswordVisibilityToggle = onTogglePasswordVisibility
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // A real TextButton rather than a bare clickable Box: correct button
                    // semantics, ripple and a guaranteed 48dp target come for free.
                    TextButton(onClick = onForgotPassword) {
                        Text(
                            text = "Forgot password?",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Positioned between the last input and the submit button, per spec.
            // Auth/validation cases render as a single-line inline banner; a traced
            // backend/permission failure gets the two-line card instead - both share
            // the same enter/exit rhythm so neither feels like a separate component.
            ErrorBannerVisibility(visible = error != null) {
                when (error) {
                    is LoginError.Network -> InlineErrorBanner(
                        message = error.message,
                        onRetry = onRetry
                    )
                    is LoginError.Traced -> TracedErrorCard(
                        headline = error.appError.message,
                        context = error.appError.context,
                        onRetry = if (error.isRetryable) onRetry else null
                    )
                    null -> Unit
                    else -> InlineErrorBanner(message = error.message)
                }
            }

            PrimaryButton(
                text = "Sign in",
                onClick = onSubmit,
                isLoading = isLoading
            )
        }
    }
}

@Composable
private fun TermsFooter() {
    Text(
        text = "By continuing you agree to our Terms & Privacy Policy",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.widthIn(max = 320.dp)
    )
}

@Composable
private fun ThemeToggleButton(isDarkMode: Boolean, onToggle: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) OneAppMotion.PressedScale else 1f,
        animationSpec = OneAppMotion.pressSpring(),
        label = "themeTogglePressScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(LocalSpacing.current.lg),
        contentAlignment = Alignment.TopEnd
    ) {
        IconButton(
            onClick = onToggle,
            interactionSource = interactionSource,
            modifier = Modifier
                .size(48.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                )
        ) {
            Crossfade(
                targetState = isDarkMode,
                animationSpec = tween(OneAppMotion.DurationLong),
                label = "themeIcon"
            ) { dark ->
                Icon(
                    imageVector = if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (dark) "Switch to light theme" else "Switch to dark theme",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

package com.xsc.oneapp.feature.login.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.unit.sp
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.login.ui.effect.LoginEffect
import com.xsc.oneapp.feature.login.ui.event.LoginEvent
import com.xsc.oneapp.feature.login.ui.viewmodel.LoginViewModel
import com.xsc.sdk.commonui.button.PrimaryButton
import com.xsc.sdk.commonui.logo.OneAppLogo
import com.xsc.sdk.commonui.textfield.PremiumTextField
import com.xsc.sdk.theme.LocalDarkTheme
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.LocalThemeToggle
import com.xsc.sdk.theme.OneAppMotion
import com.xsc.sdk.theme.OneAppPillShape
import kotlinx.coroutines.flow.collectLatest

/**
 * Sign-in.
 *
 * Presentation only - the effect collection, the events dispatched to [LoginViewModel]
 * and the Toast on error are exactly the behaviour that was here before. No validation
 * moved into this file.
 *
 * Restyled to match the Stitch "Welcome Back" design: pill-shaped filled fields, a
 * gradient pill sign-in button, and a "Contact Admin" prompt. The pill/gradient button
 * look and the filled-field look are additive, opt-in parameters on the shared
 * [PrimaryButton] / [PremiumTextField] components (default behaviour elsewhere in the
 * app is unchanged).
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(
                visible = contentVisible,
                enter = OneAppMotion.contentEnter(indexOffset = 0)
            ) {
                LoginHeader()
            }

            Spacer(modifier = Modifier.height(spacing.xxl))

            AnimatedContent(
                targetState = state.isMfaRequired,
                transitionSpec = {
                    fadeIn(animationSpec = tween(OneAppMotion.DurationLong)) togetherWith
                            fadeOut(animationSpec = tween(OneAppMotion.DurationLong))
                },
                label = "LoginMfaTransition"
            ) { isMfa ->
                if (!isMfa) {
                    LoginFormCard(
                        email = state.emailInput,
                        password = state.passwordInput,
                        isPasswordVisible = state.isPasswordVisible,
                        isLoading = state.isLoading,
                        onEmailChange = { viewModel.onEvent(LoginEvent.EmailChanged(it)) },
                        onPasswordChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
                        onTogglePasswordVisibility = { viewModel.onEvent(LoginEvent.TogglePasswordVisibility) },
                        onForgotPassword = onNavigateToForgotPassword,
                    ) { viewModel.onEvent(LoginEvent.SubmitLogin) }
                } else {
                    MfaFormCard(
                        otp = state.otpInput,
                        isBackupCodeMode = state.isBackupCodeMode,
                        isLoading = state.isLoading,
                        onOtpChange = { viewModel.onEvent(LoginEvent.OtpChanged(it)) },
                        onToggleMfaMode = { viewModel.onEvent(LoginEvent.ToggleMfaMode) },
                        onBack = { viewModel.onEvent(LoginEvent.BackToLogin) },
                        onSubmit = { viewModel.onEvent(LoginEvent.SubmitMfa) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            AnimatedVisibility(
                visible = contentVisible,
                enter = OneAppMotion.contentEnter(indexOffset = 2)
            ) {
                // TEMPORARY: no "contact admin" flow exists anywhere in the codebase yet
                // (no support screen, no mailto target, no endpoint). This reproduces the
                // Stitch design's prompt with a static, local acknowledgement so it isn't
                // a dead-looking link; see Backend Endpoint Requirements for the real flow.
                ContactAdminPrompt(
                    onContactAdmin = {
                        Toast.makeText(
                            context,
                            "Please contact your institution's administrator for account access.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(spacing.xxl))

            AnimatedVisibility(
                visible = contentVisible,
                enter = OneAppMotion.contentEnter(indexOffset = 3)
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
        OneAppLogo()

        Spacer(modifier = Modifier.height(spacing.xxl))

        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
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
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onForgotPassword: () -> Unit,
    onSubmit: () -> Unit
) {
    val spacing = LocalSpacing.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            // Stops the card stretching to an unreadable width on tablets.
            .widthIn(max = 480.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                imeAction = ImeAction.Next,
                filled = true
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
                    onPasswordVisibilityToggle = onTogglePasswordVisibility,
                    filled = true
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

            PrimaryButton(
                text = "Log In",
                onClick = onSubmit,
                isLoading = isLoading,
                shape = OneAppPillShape,
                containerGradient = Brush.horizontalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                )
            )
        }
    }
}

@Composable
private fun MfaFormCard(
    otp: String,
    isBackupCodeMode: Boolean,
    isLoading: Boolean,
    onOtpChange: (String) -> Unit,
    onToggleMfaMode: () -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    val spacing = LocalSpacing.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.lg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(spacing.sm))
                Text(
                    text = if (isBackupCodeMode) "Backup Code" else "Two-Step Verification",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = if (isBackupCodeMode) {
                    "Enter one of your 8-character recovery codes to sign in."
                } else {
                    "Enter the 6-digit code from your authenticator app."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PremiumTextField(
                text = otp,
                onTextChange = onOtpChange,
                placeholder = if (isBackupCodeMode) "8-character code" else "6-digit code",
                icon = if (isBackupCodeMode) Icons.Default.VpnKey else Icons.Default.Pin,
                keyboardType = if (isBackupCodeMode) KeyboardType.Ascii else KeyboardType.NumberPassword,
                imeAction = ImeAction.Done,
                isSecure = false,
                filled = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    letterSpacing = 4.sp
                )
            )

            PrimaryButton(
                text = "Verify",
                onClick = onSubmit,
                isLoading = isLoading,
                shape = OneAppPillShape,
                containerGradient = Brush.horizontalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                )
            )

            TextButton(
                onClick = onToggleMfaMode,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = if (isBackupCodeMode) "Use Authenticator App" else "Lost access? Use backup code",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ContactAdminPrompt(onContactAdmin: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Don't have an account? ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Contact Admin",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onContactAdmin)
        )
    }
}

@Composable
private fun TermsFooter() {
    val bodyColor = MaterialTheme.colorScheme.onSurfaceVariant
    val linkColor = MaterialTheme.colorScheme.primary

    // TEMPORARY: "Terms" and "Privacy Policy" are styled as links per the Stitch design,
    // but no Terms of Service / Privacy Policy document or screen exists in the app yet,
    // so they are inert for now - see Backend Endpoint Requirements.
    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = bodyColor)) {
            append("By continuing, you agree to our ")
        }
        withStyle(SpanStyle(color = linkColor, fontWeight = FontWeight.SemiBold)) {
            append("Terms")
        }
        withStyle(SpanStyle(color = bodyColor)) {
            append(" and ")
        }
        withStyle(SpanStyle(color = linkColor, fontWeight = FontWeight.SemiBold)) {
            append("Privacy Policy")
        }
        withStyle(SpanStyle(color = bodyColor)) {
            append(".")
        }
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
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

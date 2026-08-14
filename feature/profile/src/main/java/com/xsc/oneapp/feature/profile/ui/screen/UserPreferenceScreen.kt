package com.xsc.oneapp.feature.profile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.profile.ui.components.ProfileFormCard
import com.xsc.oneapp.feature.profile.ui.components.ProfileHeaderRow
import com.xsc.oneapp.core.result.AppError
import com.xsc.oneapp.feature.profile.ui.state.UserPreferenceEvent
import com.xsc.oneapp.feature.profile.ui.state.UserPreferenceState
import com.xsc.oneapp.feature.profile.ui.viewmodel.UserPreferenceViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import com.xsc.sdk.commonui.button.PrimaryButton
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.commonui.textfield.PremiumTextField
import com.xsc.sdk.theme.LocalSpacing

/**
 * Restyled only. The four preference fields, their state and the exact
 * `SaveUserPreference` payload keys are unchanged. `onNavigateBack`, previously accepted
 * but never rendered, is wired to the up button.
 *
 * The fields stay free-text rather than becoming dropdowns: the accepted values for
 * theme, language, timezone and landing module are decided by the backend contract, and
 * constraining the UI to a guessed list could silently block a value the server accepts.
 * The placeholders are clarified instead.
 */
@Composable
fun UserPreferenceScreen(
    viewModel: UserPreferenceViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val spacing = LocalSpacing.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(UserPreferenceEvent.LoadUserPreference)
    }

    RecordScaffold(title = "Settings", onBack = onNavigateBack) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val currentState = state) {
                is UserPreferenceState.Loading -> LoadingState()

                is UserPreferenceState.Success -> {
                    val pref = currentState.userPreference
                    var theme by remember { mutableStateOf(pref.theme) }
                    var language by remember { mutableStateOf(pref.language) }
                    var timezone by remember { mutableStateOf(pref.timezone) }
                    var landingModule by remember { mutableStateOf(pref.defaultLandingModule) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(
                                horizontal = spacing.screenHorizontal,
                                vertical = spacing.xl
                            )
                    ) {
                        ResponsiveContent(verticalArrangement = Arrangement.spacedBy(spacing.lg)) {
                            ProfileHeaderRow(
                                icon = Icons.Default.Settings,
                                title = "App and account settings",
                                subtitle = "Applies to your account on every device"
                            )

                            ProfileFormCard {
                                PremiumTextField(
                                    text = theme,
                                    onTextChange = { theme = it },
                                    placeholder = "Theme — light or dark",
                                    imeAction = ImeAction.Next
                                )
                                PremiumTextField(
                                    text = language,
                                    onTextChange = { language = it },
                                    placeholder = "Language — e.g. en",
                                    imeAction = ImeAction.Next
                                )
                                PremiumTextField(
                                    text = timezone,
                                    onTextChange = { timezone = it },
                                    placeholder = "Timezone — e.g. UTC",
                                    imeAction = ImeAction.Next
                                )
                                PremiumTextField(
                                    text = landingModule,
                                    onTextChange = { landingModule = it },
                                    placeholder = "Default landing module",
                                    imeAction = ImeAction.Done
                                )
                            }

                            PrimaryButton(
                                text = "Save preferences",
                                onClick = {
                                    viewModel.onEvent(
                                        UserPreferenceEvent.SaveUserPreference(
                                            mapOf(
                                                "theme" to theme,
                                                "language" to language,
                                                "timezone" to timezone,
                                                "defaultLandingModule" to landingModule
                                            )
                                        )
                                    )
                                }
                            )

                            OutlinedButton(
                                onClick = { viewModel.onEvent(UserPreferenceEvent.ResetUserPreference) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reset to defaults")
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.xxl))
                    }
                }

                is UserPreferenceState.BusinessError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(UserPreferenceEvent.LoadUserPreference) }
                )

                is UserPreferenceState.NetworkError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(UserPreferenceEvent.LoadUserPreference) }
                )

                is UserPreferenceState.UnexpectedError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(UserPreferenceEvent.LoadUserPreference) },
                    context = (currentState.appError as? AppError.Traced)?.context
                )

                is UserPreferenceState.Empty -> EmptyState(
                    message = "No preferences saved yet.",
                    actionLabel = "Reload",
                    onAction = { viewModel.onEvent(UserPreferenceEvent.LoadUserPreference) }
                )
            }
        }
    }
}

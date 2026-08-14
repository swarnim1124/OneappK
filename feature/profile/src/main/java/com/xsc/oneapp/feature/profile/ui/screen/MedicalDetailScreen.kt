package com.xsc.oneapp.feature.profile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.profile.ui.components.ProfileFormCard
import com.xsc.oneapp.feature.profile.ui.components.ProfileHeaderRow
import com.xsc.oneapp.core.result.AppError
import com.xsc.oneapp.feature.profile.ui.state.MedicalDetailEvent
import com.xsc.oneapp.feature.profile.ui.state.MedicalDetailState
import com.xsc.oneapp.feature.profile.ui.viewmodel.MedicalDetailViewModel
import com.xsc.sdk.commonui.button.PrimaryButton
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.commonui.textfield.PremiumTextField
import com.xsc.sdk.theme.LocalSpacing

/**
 * Restyled only. The comma-separated allergies convention and the exact
 * `SaveMedicalDetail` payload map - including
 * `split(",").map { trim() }.filter { isNotEmpty() }` - are byte-for-byte unchanged.
 * `onNavigateBack`, previously accepted but never rendered, is wired to the up button.
 */
@Composable
fun MedicalDetailScreen(
    viewModel: MedicalDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val spacing = LocalSpacing.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(MedicalDetailEvent.LoadMedicalDetail)
    }

    RecordScaffold(title = "Medical details", onBack = onNavigateBack) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val currentState = state) {
                is MedicalDetailState.Loading -> LoadingState()

                is MedicalDetailState.Success -> {
                    val detail = currentState.medicalDetail
                    var doctorName by remember { mutableStateOf(detail.doctorName) }
                    var doctorContact by remember { mutableStateOf(detail.doctorContact) }
                    var insurance by remember { mutableStateOf(detail.insurancePolicyNo) }
                    // Comma separated string for the allergies array in the UI, as before.
                    var allergies by remember { mutableStateOf(detail.allergies.joinToString(", ")) }

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
                                icon = Icons.Default.Favorite,
                                title = "Health information",
                                subtitle = "Shown to staff in an emergency"
                            )

                            ProfileFormCard {
                                PremiumTextField(
                                    text = doctorName,
                                    onTextChange = { doctorName = it },
                                    placeholder = "Doctor name",
                                    imeAction = ImeAction.Next
                                )
                                PremiumTextField(
                                    text = doctorContact,
                                    onTextChange = { doctorContact = it },
                                    placeholder = "Doctor contact",
                                    // Presentation only: surfaces the phone keypad. The
                                    // stored value is still whatever the user types.
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                )
                                PremiumTextField(
                                    text = insurance,
                                    onTextChange = { insurance = it },
                                    placeholder = "Insurance policy number",
                                    imeAction = ImeAction.Next
                                )
                                PremiumTextField(
                                    text = allergies,
                                    onTextChange = { allergies = it },
                                    placeholder = "Allergies (comma separated)",
                                    imeAction = ImeAction.Done
                                )
                            }

                            PrimaryButton(
                                text = "Save medical details",
                                onClick = {
                                    viewModel.onEvent(
                                        MedicalDetailEvent.SaveMedicalDetail(
                                            mapOf(
                                                "doctorName" to doctorName,
                                                "doctorContact" to doctorContact,
                                                "insurancePolicyNo" to insurance,
                                                "allergies" to allergies.split(",")
                                                    .map { it.trim() }
                                                    .filter { it.isNotEmpty() }
                                            )
                                        )
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(spacing.xxl))
                    }
                }

                is MedicalDetailState.BusinessError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(MedicalDetailEvent.LoadMedicalDetail) }
                )

                is MedicalDetailState.NetworkError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(MedicalDetailEvent.LoadMedicalDetail) }
                )

                is MedicalDetailState.UnexpectedError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(MedicalDetailEvent.LoadMedicalDetail) },
                    context = (currentState.appError as? AppError.Traced)?.context
                )

                is MedicalDetailState.Empty -> EmptyState(
                    message = "No medical details on record yet.",
                    actionLabel = "Reload",
                    onAction = { viewModel.onEvent(MedicalDetailEvent.LoadMedicalDetail) }
                )
            }
        }
    }
}

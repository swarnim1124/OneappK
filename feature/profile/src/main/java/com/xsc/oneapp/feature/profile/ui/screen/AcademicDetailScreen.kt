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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.School
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
import com.xsc.oneapp.feature.profile.ui.components.ProfileSectionCard
import com.xsc.oneapp.core.result.AppError
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.oneapp.feature.profile.ui.state.AcademicDetailEvent
import com.xsc.oneapp.feature.profile.ui.state.AcademicDetailState
import com.xsc.oneapp.feature.profile.ui.viewmodel.AcademicDetailViewModel
import com.xsc.sdk.commonui.button.PrimaryButton
import com.xsc.sdk.commonui.textfield.PremiumTextField
import com.xsc.sdk.theme.LocalSpacing

@Composable
fun AcademicDetailScreen(
    viewModel: AcademicDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val spacing = LocalSpacing.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(AcademicDetailEvent.LoadAcademicDetail)
    }

    RecordScaffold(title = "Academic identifiers", onBack = onNavigateBack) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val currentState = state) {
                is AcademicDetailState.Loading -> LoadingState()

                is AcademicDetailState.Success -> {
                    val detail = currentState.academicDetail

                    var idCardNumber by remember {
                        mutableStateOf(detail.identifiers.find { it.type == "idCardNumber" }?.identifierValue ?: "")
                    }
                    var biometricId by remember {
                        mutableStateOf(detail.identifiers.find { it.type == "biometricId" }?.identifierValue ?: "")
                    }

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
                                icon = Icons.Default.Badge,
                                title = "Enrollment ${detail.enrollmentNumber}",
                                subtitle = detail.employeeCode
                                    .takeIf { it.isNotEmpty() }
                                    ?.let { "Employee code $it" }
                            )

                            val academicRows = buildList {
                                if (detail.branch.isNotBlank()) add("Branch" to detail.branch)
                                if (detail.batch.isNotBlank()) add("Batch" to detail.batch)
                                detail.semester?.let { add("Semester" to it.toString()) }
                                if (detail.section.isNotBlank()) add("Section" to detail.section)
                                detail.cgpa?.let { add("CGPA" to String.format("%.2f", it)) }
                            }
                            if (academicRows.isNotEmpty()) {
                                ProfileSectionCard(
                                    title = "Academic info",
                                    icon = Icons.Default.School,
                                    rows = academicRows
                                )
                            }

                            ProfileFormCard {
                                PremiumTextField(
                                    text = idCardNumber,
                                    onTextChange = { idCardNumber = it },
                                    placeholder = "ID card number",
                                    imeAction = ImeAction.Next
                                )
                                PremiumTextField(
                                    text = biometricId,
                                    onTextChange = { biometricId = it },
                                    placeholder = "Biometric ID",
                                    imeAction = ImeAction.Done
                                )
                            }

                            PrimaryButton(
                                text = "Update identifiers",
                                onClick = {
                                    viewModel.onEvent(
                                        AcademicDetailEvent.SaveAcademicIdentifiers(
                                            idCardNumber = idCardNumber.ifEmpty { null },
                                            biometricId = biometricId.ifEmpty { null }
                                        )
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(spacing.xxl))
                    }
                }

                is AcademicDetailState.BusinessError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(AcademicDetailEvent.LoadAcademicDetail) }
                )

                is AcademicDetailState.NetworkError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(AcademicDetailEvent.LoadAcademicDetail) }
                )

                is AcademicDetailState.UnexpectedError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(AcademicDetailEvent.LoadAcademicDetail) },
                    context = (currentState.appError as? AppError.Traced)?.context
                )

                is AcademicDetailState.Empty -> EmptyState(
                    message = "No academic identifiers on record yet.",
                    actionLabel = "Reload",
                    onAction = { viewModel.onEvent(AcademicDetailEvent.LoadAcademicDetail) }
                )
            }
        }
    }
}

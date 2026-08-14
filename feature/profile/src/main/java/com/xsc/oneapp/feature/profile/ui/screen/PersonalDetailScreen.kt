package com.xsc.oneapp.feature.profile.ui.screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.profile.domain.model.Address
import com.xsc.oneapp.feature.profile.domain.model.PersonalDetail
import com.xsc.oneapp.feature.profile.ui.components.ProfileSectionCard
import com.xsc.oneapp.core.result.AppError
import com.xsc.oneapp.feature.profile.ui.state.PersonalDetailEvent
import com.xsc.oneapp.feature.profile.ui.state.PersonalDetailState
import com.xsc.oneapp.feature.profile.ui.viewmodel.PersonalDetailViewModel
import com.xsc.sdk.commonui.avatar.InitialsAvatar
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.OneAppMotion

@Composable
fun PersonalDetailScreen(
    viewModel: PersonalDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(PersonalDetailEvent.LoadPersonalDetail)
    }

    RecordScaffold(title = "Personal details", onBack = onNavigateBack) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val currentState = state) {
                is PersonalDetailState.Loading -> LoadingState()

                is PersonalDetailState.Success -> PersonalDetailReadOnlyView(
                    detail = currentState.personalDetail,
                    scrollState = scrollState
                )

                is PersonalDetailState.BusinessError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(PersonalDetailEvent.LoadPersonalDetail) }
                )

                is PersonalDetailState.NetworkError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(PersonalDetailEvent.LoadPersonalDetail) }
                )

                is PersonalDetailState.UnexpectedError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(PersonalDetailEvent.LoadPersonalDetail) },
                    context = (currentState.appError as? AppError.Traced)?.context
                )

                is PersonalDetailState.Empty -> EmptyState(
                    message = "No personal details found for your account.",
                    actionLabel = "Reload",
                    onAction = { viewModel.onEvent(PersonalDetailEvent.LoadPersonalDetail) }
                )
            }
        }
    }
}

@Composable
private fun PersonalDetailReadOnlyView(
    detail: PersonalDetail,
    scrollState: androidx.compose.foundation.ScrollState
) {
    val spacing = LocalSpacing.current

    val fullName = listOf(detail.firstName, detail.middleName, detail.lastName)
        .filter { it.isNotBlank() }
        .joinToString(" ")

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(visible = visible, enter = OneAppMotion.contentEnter(0)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                InitialsAvatar(name = fullName)
                Spacer(modifier = Modifier.height(spacing.lg))
                Text(
                    text = fullName.ifBlank { "—" },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = "Managed by your institution · read-only",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.xl))

        AnimatedVisibility(visible = visible, enter = OneAppMotion.contentEnter(1)) {
            ResponsiveContent(verticalArrangement = Arrangement.spacedBy(spacing.lg)) {
                ProfileSectionCard(
                    title = "Identity",
                    icon = Icons.Default.Person,
                    rows = buildList {
                        add("First name" to detail.firstName)
                        add("Middle name" to detail.middleName)
                        add("Last name" to detail.lastName)
                        add("Date of birth" to detail.dob)
                        add("Gender" to detail.gender)
                        if (detail.nationality.isNotBlank()) add("Nationality" to detail.nationality)
                        if (detail.primaryLanguage.isNotBlank()) add("Primary language" to detail.primaryLanguage)
                        if (detail.maritalStatus.isNotBlank()) add("Marital status" to detail.maritalStatus)
                    }
                )

                ProfileSectionCard(
                    title = "Contact",
                    icon = Icons.Default.Call,
                    rows = buildList {
                        add("Mobile" to detail.mobile)
                        add("Email" to detail.email)
                        add("Alternate email" to detail.alternateEmail)
                    }
                )

                ProfileSectionCard(
                    title = "Record",
                    icon = Icons.Default.Info,
                    rows = buildList {
                        if (detail.bloodGroup.isNotBlank()) add("Blood group" to detail.bloodGroup)
                        add("Created" to detail.createdAt)
                        add("Last updated" to detail.updatedAt)
                    }
                )

                detail.addresses.forEach { address ->
                    AddressSectionCard(address = address)
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.xxl))
    }
}

@Composable
private fun AddressSectionCard(address: Address) {
    val lines = buildList {
        if (address.number.isNotBlank()) add("Number" to address.number)
        if (address.line1.isNotBlank()) add("Address line 1" to address.line1)
        if (address.line2.isNotBlank()) add("Address line 2" to address.line2)
        if (address.city.isNotBlank()) add("City" to address.city)
        if (address.district.isNotBlank()) add("District" to address.district)
        if (address.state.isNotBlank()) add("State" to address.state)
        if (address.country.isNotBlank()) add("Country" to address.country)
        if (address.postalCode.isNotBlank()) add("PIN code" to address.postalCode)
    }
    if (lines.isEmpty()) return

    val label = address.addressType.ifBlank { "Address" } +
        if (address.isPrimary) " (Primary)" else ""

    ProfileSectionCard(
        title = label,
        icon = Icons.Default.Home,
        rows = lines
    )
}

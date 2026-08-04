package com.xsc.oneapp.feature.profile.ui.screen

import androidx.compose.animation.AnimatedVisibility
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
import com.xsc.oneapp.feature.profile.domain.model.PersonalDetail
import com.xsc.oneapp.feature.profile.ui.components.EmptyState
import com.xsc.oneapp.feature.profile.ui.components.ErrorState
import com.xsc.oneapp.feature.profile.ui.components.LoadingState
import com.xsc.oneapp.feature.profile.ui.components.ProfileAvatar
import com.xsc.oneapp.feature.profile.ui.components.ProfileContentColumn
import com.xsc.oneapp.feature.profile.ui.components.ProfileScaffold
import com.xsc.oneapp.feature.profile.ui.components.ProfileSectionCard
import com.xsc.oneapp.feature.profile.ui.state.PersonalDetailEvent
import com.xsc.oneapp.feature.profile.ui.state.PersonalDetailState
import com.xsc.oneapp.feature.profile.ui.viewmodel.PersonalDetailViewModel
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.OneAppMotion

/**
 * Read-only view of the signed-in user's personal detail record, straight from
 * `m_profile / sm_personalDetail / personalDetail:view`. Identity fields (legal name,
 * DOB, gender, nationality, ...) are institution-of-record data - this screen
 * deliberately has no edit affordance so the app never lets a user silently override
 * what the backend has on file. Corrections should go through whatever admin/back-office
 * flow the institution uses for `personalDetail:update`, not a self-service form here.
 *
 * Restyled only. The state machine, the `LoadPersonalDetail` event on entry and every
 * retry path are unchanged. The `onNavigateBack` callback - previously accepted and never
 * used, leaving no visible way out of the screen - is now wired to the standard up button.
 */
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

    ProfileScaffold(title = "Personal details", onNavigateBack = onNavigateBack) { padding ->
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

                is PersonalDetailState.BusinessError -> ErrorState(message = currentState.message) {
                    viewModel.onEvent(PersonalDetailEvent.LoadPersonalDetail)
                }

                is PersonalDetailState.NetworkError -> ErrorState(message = currentState.message) {
                    viewModel.onEvent(PersonalDetailEvent.LoadPersonalDetail)
                }

                is PersonalDetailState.UnexpectedError -> ErrorState(message = currentState.message) {
                    viewModel.onEvent(PersonalDetailEvent.LoadPersonalDetail)
                }

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
                ProfileAvatar(name = fullName)
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
            ProfileContentColumn {
                ProfileSectionCard(
                    title = "Identity",
                    icon = Icons.Default.Person,
                    rows = buildList {
                        add("First name" to detail.firstName)
                        add("Middle name" to detail.middleName)
                        add("Last name" to detail.lastName)
                        add("Date of birth" to detail.dob)
                        add("Gender" to detail.genderId.toString())
                        detail.nationalityId?.let { add("Nationality" to it.toString()) }
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
                        detail.bloodGroupId?.let { add("Blood group" to it.toString()) }
                        add("Created" to detail.createdAt)
                        add("Last updated" to detail.updatedAt)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.xxl))
    }
}

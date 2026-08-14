@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.profile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.xsc.oneapp.feature.profile.domain.model.FamilyDetail
import com.xsc.oneapp.feature.profile.ui.components.ProfilePersonCard
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.oneapp.core.result.AppError
import com.xsc.oneapp.feature.profile.ui.state.FamilyDetailEffect
import com.xsc.oneapp.feature.profile.ui.state.FamilyDetailEvent
import com.xsc.oneapp.feature.profile.ui.state.FamilyDetailState
import com.xsc.oneapp.feature.profile.ui.viewmodel.FamilyDetailViewModel
import com.xsc.sdk.commonui.textfield.PremiumTextField
import com.xsc.sdk.theme.LocalSpacing
import kotlinx.coroutines.flow.collectLatest

/**
 * Restyled only. The snackbar effect collection, both dialog triggers, the
 * name-combining payload convention and the "reason is required to delete" validation
 * are all unchanged.
 *
 * A Scaffold is used directly rather than ProfileScaffold because this screen needs a
 * snackbar host, which ProfileScaffold does not expose.
 */
@Composable
fun FamilyDetailScreen(
    viewModel: FamilyDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val spacing = LocalSpacing.current
    var editingDetail by remember { mutableStateOf<FamilyDetail?>(null) }
    var deletingDetail by remember { mutableStateOf<FamilyDetail?>(null) }
    var addingNew by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(FamilyDetailEvent.LoadFamilyDetail)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is FamilyDetailEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
                is FamilyDetailEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family details", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate up"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add family member") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = { addingNew = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = state) {
                is FamilyDetailState.Loading -> LoadingState()

                is FamilyDetailState.Success -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = spacing.screenHorizontal,
                        vertical = spacing.lg
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    items(currentState.familyDetails, key = { it.id }) { detail ->
                        ProfilePersonCard(
                            name = "${detail.firstName} ${detail.lastName}".trim(),
                            subtitle = detail.occupation,
                            badge = detail.relationshipTypeName.takeIf { it.isNotBlank() },
                            contactLines = listOf(
                                Icons.Default.Phone to detail.mobileNo,
                                Icons.Default.Email to detail.email
                            ),
                            onEdit = { editingDetail = detail },
                            onDelete = { deletingDetail = detail }
                        )
                    }
                }

                is FamilyDetailState.BusinessError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(FamilyDetailEvent.LoadFamilyDetail) }
                )

                is FamilyDetailState.NetworkError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(FamilyDetailEvent.LoadFamilyDetail) }
                )

                is FamilyDetailState.UnexpectedError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(FamilyDetailEvent.LoadFamilyDetail) },
                    context = (currentState.appError as? AppError.Traced)?.context
                )

                is FamilyDetailState.Empty -> EmptyState(
                    message = "No family records on file yet.",
                    actionLabel = "Add family member",
                    onAction = { addingNew = true }
                )
            }
        }

        editingDetail?.let { detail ->
            FamilyEditDialog(
                detail = detail,
                onDismiss = { editingDetail = null },
                onSave = { fieldsToUpdate ->
                    viewModel.onEvent(FamilyDetailEvent.SaveFamilyDetail(detail.id, fieldsToUpdate))
                    editingDetail = null
                }
            )
        }

        if (addingNew) {
            FamilyAddDialog(
                onDismiss = { addingNew = false },
                onSave = { fields ->
                    viewModel.onEvent(FamilyDetailEvent.AddFamilyDetail(fields))
                    addingNew = false
                }
            )
        }

        deletingDetail?.let { detail ->
            FamilyDeleteDialog(
                detail = detail,
                onDismiss = { deletingDetail = null },
                onConfirm = { reason ->
                    viewModel.onEvent(FamilyDetailEvent.DeleteFamilyDetail(detail.id, reason))
                    deletingDetail = null
                }
            )
        }
    }
}

/**
 * [AddFamilyDetailUseCase] has no confirmed field-name contract the way update does
 * (no PROFILE_API_CONTRACT.md comment covers `add` specifically) - this mirrors
 * update's confirmed name/mobile/email/occupation fields as the most defensible
 * inference (same entity, same table), not a verified contract. studentId is added
 * separately by the ViewModel, which already resolves it for every other call here.
 */
@Composable
private fun FamilyAddDialog(
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    val spacing = LocalSpacing.current
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var mobileNo by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Add family member", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(
                    text = firstName,
                    onTextChange = { firstName = it },
                    placeholder = "First name",
                    imeAction = ImeAction.Next
                )
                PremiumTextField(
                    text = lastName,
                    onTextChange = { lastName = it },
                    placeholder = "Last name",
                    imeAction = ImeAction.Next
                )
                PremiumTextField(
                    text = relation,
                    onTextChange = { relation = it },
                    placeholder = "Relation (e.g. 1=Mother, 2=Father)",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
                PremiumTextField(
                    text = mobileNo,
                    onTextChange = { mobileNo = it },
                    placeholder = "Mobile",
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
                PremiumTextField(
                    text = email,
                    onTextChange = { email = it },
                    placeholder = "Email",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
                PremiumTextField(
                    text = occupation,
                    onTextChange = { occupation = it },
                    placeholder = "Occupation",
                    imeAction = ImeAction.Done
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val payload = mutableMapOf<String, Any>(
                        "name" to "$firstName $lastName".trim(),
                        "mobile" to mobileNo
                    )
                    relation.toIntOrNull()?.let { payload["relation"] = it }
                    if (email.isNotBlank()) payload["email"] = email
                    if (occupation.isNotBlank()) payload["occupation"] = occupation
                    onSave(payload)
                },
                enabled = firstName.isNotBlank() && mobileNo.isNotBlank() && relation.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun FamilyEditDialog(
    detail: FamilyDetail,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    val spacing = LocalSpacing.current
    var firstName by remember { mutableStateOf(detail.firstName) }
    var lastName by remember { mutableStateOf(detail.lastName) }
    var mobileNo by remember { mutableStateOf(detail.mobileNo) }
    var email by remember { mutableStateOf(detail.email) }
    var occupation by remember { mutableStateOf(detail.occupation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Edit family member", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(
                    text = firstName,
                    onTextChange = { firstName = it },
                    placeholder = "First name",
                    imeAction = ImeAction.Next
                )
                PremiumTextField(
                    text = lastName,
                    onTextChange = { lastName = it },
                    placeholder = "Last name",
                    imeAction = ImeAction.Next
                )
                PremiumTextField(
                    text = mobileNo,
                    onTextChange = { mobileNo = it },
                    placeholder = "Mobile",
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
                PremiumTextField(
                    text = email,
                    onTextChange = { email = it },
                    placeholder = "Email",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
                PremiumTextField(
                    text = occupation,
                    onTextChange = { occupation = it },
                    placeholder = "Occupation",
                    imeAction = ImeAction.Done
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // PROFILE_API_CONTRACT.md familyDetail/update only honors name/mobile/
                // email/occupation/annualIncome inside fieldsToUpdate - firstName/
                // lastName/mobileNo are silently ignored, so first+last are combined
                // into the single "name" field the backend actually reads.
                onSave(
                    mapOf(
                        "name" to "$firstName $lastName".trim(),
                        "mobile" to mobileNo,
                        "email" to email,
                        "occupation" to occupation
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun FamilyDeleteDialog(
    detail: FamilyDetail,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = {
            Text(
                "Delete ${detail.firstName} ${detail.lastName}?",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                Text(
                    "This removes them from your family details.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PremiumTextField(
                    text = reason,
                    onTextChange = { reason = it },
                    placeholder = "Reason for removal",
                    imeAction = ImeAction.Done,
                    // Presentation of the existing rule: the confirm button was already
                    // gated on a non-blank reason, but nothing told the user why it was
                    // disabled.
                    error = if (reason.isBlank()) "A reason is required" else null
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason) },
                enabled = reason.isNotBlank()
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

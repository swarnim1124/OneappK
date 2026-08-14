@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.profile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.profile.domain.model.EmergencyContact
import com.xsc.oneapp.feature.profile.ui.components.ProfilePersonCard
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.oneapp.core.result.AppError
import com.xsc.oneapp.feature.profile.ui.state.EmergencyContactEffect
import com.xsc.oneapp.feature.profile.ui.state.EmergencyContactEvent
import com.xsc.oneapp.feature.profile.ui.state.EmergencyContactState
import com.xsc.oneapp.feature.profile.ui.viewmodel.EmergencyContactViewModel
import com.xsc.sdk.commonui.textfield.PremiumTextField
import com.xsc.sdk.theme.LocalSpacing
import kotlinx.coroutines.flow.collectLatest

/**
 * Restyled only. The snackbar effect collection, the edit/delete dialog triggers and the
 * `priorityOrder 1 = primary` payload convention are unchanged.
 *
 * A Scaffold is used directly rather than ProfileScaffold because this screen needs a
 * snackbar host, which ProfileScaffold does not expose.
 */
@Composable
fun EmergencyContactScreen(
    viewModel: EmergencyContactViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val spacing = LocalSpacing.current
    var editingContact by remember { mutableStateOf<EmergencyContact?>(null) }
    var deletingContact by remember { mutableStateOf<EmergencyContact?>(null) }
    var addingNew by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(EmergencyContactEvent.LoadEmergencyContacts)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is EmergencyContactEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
                is EmergencyContactEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency contacts", style = MaterialTheme.typography.titleLarge) },
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
                text = { Text("Add contact") },
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
                is EmergencyContactState.Loading -> LoadingState()

                is EmergencyContactState.Success -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = spacing.screenHorizontal,
                        vertical = spacing.lg
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    items(currentState.contacts, key = { it.id }) { contact ->
                        ProfilePersonCard(
                            name = "${contact.firstName} ${contact.lastName}".trim(),
                            badge = if (contact.isPrimary) "PRIMARY" else null,
                            contactLines = listOf(
                                Icons.Default.Phone to contact.mobile,
                                Icons.Default.Email to contact.email
                            ),
                            onEdit = { editingContact = contact },
                            onDelete = { deletingContact = contact }
                        )
                    }
                }

                is EmergencyContactState.BusinessError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(EmergencyContactEvent.LoadEmergencyContacts) }
                )

                is EmergencyContactState.NetworkError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(EmergencyContactEvent.LoadEmergencyContacts) }
                )

                is EmergencyContactState.UnexpectedError -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.onEvent(EmergencyContactEvent.LoadEmergencyContacts) },
                    context = (currentState.appError as? AppError.Traced)?.context
                )

                is EmergencyContactState.Empty -> EmptyState(
                    message = "No emergency contacts on record yet.",
                    actionLabel = "Add contact",
                    onAction = { addingNew = true }
                )
            }
        }

        if (addingNew) {
            EmergencyContactAddDialog(
                onDismiss = { addingNew = false },
                onSave = { fields ->
                    viewModel.onEvent(EmergencyContactEvent.AddEmergencyContact(fields))
                    addingNew = false
                }
            )
        }

        editingContact?.let { contact ->
            EmergencyContactEditDialog(
                contact = contact,
                onDismiss = { editingContact = null },
                onSave = { fieldsToUpdate ->
                    viewModel.onEvent(EmergencyContactEvent.SaveEmergencyContact(contact.id, fieldsToUpdate))
                    editingContact = null
                }
            )
        }

        deletingContact?.let { contact ->
            AlertDialog(
                onDismissRequest = { deletingContact = null },
                shape = MaterialTheme.shapes.large,
                title = {
                    Text(
                        "Delete ${contact.firstName} ${contact.lastName}?",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        "This removes them from your emergency contacts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onEvent(EmergencyContactEvent.DeleteEmergencyContact(contact.id))
                        deletingContact = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingContact = null }) { Text("Cancel") }
                }
            )
        }
    }
}

/**
 * [AddEmergencyContactUseCase] has no confirmed field-name contract the way update
 * does - this mirrors update's confirmed name/mobile/priorityOrder fields as the most
 * defensible inference (same entity), not a verified contract. No email field, for the
 * same reason the edit dialog omits one - the contract for this entity never persists
 * it through either action.
 */
@Composable
private fun EmergencyContactAddDialog(
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    val spacing = LocalSpacing.current
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var isPrimary by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Add emergency contact", style = MaterialTheme.typography.headlineSmall) },
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
                    text = mobile,
                    onTextChange = { mobile = it },
                    placeholder = "Mobile",
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Primary contact", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Called first in an emergency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = isPrimary, onCheckedChange = { isPrimary = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val payload = mutableMapOf<String, Any>(
                        "name" to "$firstName $lastName".trim(),
                        "mobile" to mobile,
                        "priorityOrder" to if (isPrimary) 1 else 2
                    )
                    relation.toIntOrNull()?.let { payload["relation"] = it }
                    onSave(payload)
                },
                enabled = firstName.isNotBlank() && mobile.isNotBlank() && relation.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EmergencyContactEditDialog(
    contact: EmergencyContact,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    val spacing = LocalSpacing.current
    var firstName by remember { mutableStateOf(contact.firstName) }
    var lastName by remember { mutableStateOf(contact.lastName) }
    var mobile by remember { mutableStateOf(contact.mobile) }
    var isPrimary by remember { mutableStateOf(contact.isPrimary) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Edit contact", style = MaterialTheme.typography.headlineSmall) },
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
                    text = mobile,
                    onTextChange = { mobile = it },
                    placeholder = "Mobile",
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                )
                // No Email field here: PROFILE_API_CONTRACT.md's emergencyContact/update
                // only honors name/mobile/priorityOrder - email can never be persisted
                // through this action, so showing an editable field for it would silently
                // discard input.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Primary contact", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Called first in an emergency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = isPrimary, onCheckedChange = { isPrimary = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Contract: fieldsToUpdate only honors name/mobile/priorityOrder.
                // priorityOrder 1 = primary; any other value = not primary.
                onSave(
                    mapOf(
                        "name" to "$firstName $lastName".trim(),
                        "mobile" to mobile,
                        "priorityOrder" to if (isPrimary) 1 else 2
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

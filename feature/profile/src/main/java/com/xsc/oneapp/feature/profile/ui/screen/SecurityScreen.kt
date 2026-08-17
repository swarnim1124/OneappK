package com.xsc.oneapp.feature.profile.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.foundation.Image
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.xsc.sdk.qrcode.QrCodeGenerator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.profile.domain.model.MfaMethod
import com.xsc.oneapp.feature.profile.ui.event.SecurityEvent
import com.xsc.oneapp.feature.profile.ui.viewmodel.SecurityViewModel
import com.xsc.sdk.commonui.button.PrimaryButton
import com.xsc.sdk.commonui.textfield.PremiumTextField
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.OneAppPillShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    onNavigateBack: () -> Unit,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    val context = LocalContext.current

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.onEvent(SecurityEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.lg)
        ) {
            MfaStatusCard(
                methods = state.mfaMethods,
                onEnableMfa = { viewModel.onEvent(SecurityEvent.InitiateEnrollment) },
                onDisableMfa = { viewModel.onEvent(SecurityEvent.RequestDisableMfa(it)) },
                onRegenerateBackupCodes = { viewModel.onEvent(SecurityEvent.RegenerateBackupCodes(it)) }
            )
        }
    }

    if (state.showEnrollmentModal) {
        MfaEnrollmentModal(
            enrollmentData = state.enrollmentData!!,
            otpInput = state.otpInput,
            isLoading = state.isLoading,
            onOtpChange = { viewModel.onEvent(SecurityEvent.OtpChanged(it)) },
            onDismiss = { viewModel.onEvent(SecurityEvent.CloseEnrollmentModal) },
            onConfirm = { viewModel.onEvent(SecurityEvent.FinalizeEnrollment) }
        )
    }

    if (state.showDisableConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(SecurityEvent.CancelDisableMfa) },
            title = { Text("Disable 2FA") },
            text = { Text("Disabling 2FA reduces your account security. Are you sure you want to proceed?") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(SecurityEvent.ConfirmDisableMfa) }) {
                    Text("Disable", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(SecurityEvent.CancelDisableMfa) }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (state.showBackupCodesModal) {
        BackupCodesModal(
            codes = state.backupCodesToDisplay,
            onDismiss = { viewModel.onEvent(SecurityEvent.CloseBackupCodesModal) }
        )
    }
}

@Composable
private fun MfaStatusCard(
    methods: List<MfaMethod>,
    onEnableMfa: () -> Unit,
    onDisableMfa: (MfaMethod) -> Unit,
    onRegenerateBackupCodes: (String) -> Unit
) {
    val spacing = LocalSpacing.current
    val activeMethod = methods.find { it.isActive }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = if (activeMethod != null) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(spacing.md))
                Text(
                    text = "Two-Factor Authentication",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(spacing.md))
            Text(
                text = "Protect your account with an extra layer of security.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(spacing.lg))

            if (activeMethod != null) {
                StatusBadge(isActive = true)
                Spacer(modifier = Modifier.height(spacing.lg))
                
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(spacing.md)) {
                        Text(
                            text = "Authenticator App",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Primary • Verified",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(spacing.md))
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            TextButton(onClick = { onRegenerateBackupCodes(activeMethod.id) }) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(spacing.xs))
                                Text("Backup Codes")
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(
                                onClick = { onDisableMfa(activeMethod) },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Disable")
                            }
                        }
                    }
                }
            } else {
                StatusBadge(isActive = false)
                Spacer(modifier = Modifier.height(spacing.lg))
                PrimaryButton(
                    text = "Enable 2FA",
                    onClick = onEnableMfa,
                    shape = OneAppPillShape
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(isActive: Boolean) {
    Surface(
        color = if (isActive) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
        shape = OneAppPillShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (isActive) Color(0xFF4CAF50) else Color.Gray, OneAppPillShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isActive) "ACTIVE" else "DISABLED",
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MfaEnrollmentModal(
    enrollmentData: com.xsc.oneapp.feature.profile.domain.model.MfaEnrollment,
    otpInput: String,
    isLoading: Boolean,
    onOtpChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val spacing = LocalSpacing.current
    val clipboardManager = LocalClipboardManager.current
    val secret = enrollmentData.provisioningUri.substringAfter("secret=").substringBefore("&")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.lg)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.lg)
        ) {
            Text("Set up Authenticator", style = MaterialTheme.typography.headlineSmall)
            
            Text(
                "Scan this QR code in your authenticator app (Google Authenticator, Authy, etc.)",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )

            // QR Code Placeholder
            val qrBitmap: android.graphics.Bitmap? = remember(enrollmentData.provisioningUri) {
                com.xsc.sdk.qrcode.QrCodeGenerator.generateQrCode(enrollmentData.provisioningUri)
            }

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(64.dp))
                        Text("QR Code Generation Failed", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Text("Or enter this code manually:")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                    .padding(spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = secret,
                    modifier = Modifier.weight(1f),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { clipboardManager.setText(AnnotatedString(secret)) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                }
            }

            HorizontalDivider()

            Text("Your Recovery Backup Codes", fontWeight = FontWeight.Bold)
            Text(
                "Keep these codes in a safe place. You can use them if you lose access to your device.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
                    .padding(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                enrollmentData.backupCodes.chunked(2).forEach { pair ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        pair.forEach { code ->
                            Text(
                                text = code,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            Text("Enter the 6-digit code to confirm:")
            PremiumTextField(
                text = otpInput,
                onTextChange = onOtpChange,
                placeholder = "000000",
                icon = Icons.Default.Lock,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                filled = true,
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    textAlign = TextAlign.Center,
                    letterSpacing = 8.sp
                )
            )

            PrimaryButton(
                text = "Activate 2FA",
                onClick = onConfirm,
                isLoading = isLoading,
                shape = OneAppPillShape
            )
            
            Spacer(modifier = Modifier.height(spacing.xl))
        }
    }
}

@Composable
private fun BackupCodesModal(
    codes: List<String>,
    onDismiss: () -> Unit
) {
    val spacing = LocalSpacing.current
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Backup Codes") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                Text("Your old backup codes are now invalid. Please save these new codes.")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                        .padding(spacing.md),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    codes.chunked(2).forEach { pair ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            pair.forEach { code ->
                                Text(
                                    text = code,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
        dismissButton = {
            IconButton(onClick = { clipboardManager.setText(AnnotatedString(codes.joinToString("\n"))) }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy All")
            }
        }
    )
}

package com.xsc.oneapp.feature.fee.ui.state

/**
 * Per-action gating for the Fee screen's admin affordances - the first real (non
 * defense-in-depth) consumer of [com.xsc.sdk.auth.SessionManager.hasPermission] in
 * this app. Every field decides whether to *show* an action, never whether a request
 * is safe to *send* - the backend remains the only real gate, exactly as
 * `hasPermission`'s own kdoc requires.
 *
 * Defaults to fully denied so a session that hasn't resolved its permissions yet
 * (or a session with none) never briefly flashes an admin control before a real
 * check completes.
 */
data class FeePermissions(
    val canAddFeeStructure: Boolean = false,
    val canUpdateFeeStructure: Boolean = false,
    val canDeleteFeeStructure: Boolean = false,
    val canAssignFee: Boolean = false,
    val canDeleteFeeAssignment: Boolean = false,
    val canGrantConcession: Boolean = false,
    val canApproveRefund: Boolean = false
)

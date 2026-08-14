package com.xsc.sdk.auth

/**
 * Derives the permission string an [SessionManager.hasPermission] check needs for a given
 * dispatch call, mirroring the backend's exact derivation (xsc_security/authorization.py,
 * confirmed by the backend team's RBAC audit): `f"{mod}.{subMod}.{action}.{action_type_safe}"`,
 * where `action_type_safe` falls back to `"default"` when actionType is blank.
 *
 * There is no manifest or mapping table on either side - the same four fields already
 * passed to [com.xsc.sdk.network.APIClient.request] are the permission key, so a caller
 * about to make a request can derive the exact permission it needs from its own call site
 * rather than maintaining a second, parallel list that can drift out of sync with the
 * requests it's supposed to describe.
 */
fun requiredPermissionFor(module: String, submodule: String, action: String, actionType: String): String {
    val safeActionType = actionType.ifBlank { "default" }
    return "$module.$submodule.$action.$safeActionType"
}

package com.xsc.sdk.network.api

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/**
 * The one request envelope every OneApp module speaks. See any *_API_CONTRACT.md
 * "Transport and request envelope" section - mod/subMod/action/actionType/payload.
 */
data class DispatchRequest(
    val mod: String,
    val subMod: String,
    val action: String,
    val actionType: String,
    val payload: Map<String, Any?> = emptyMap()
)

/**
 * The one response envelope every OneApp module returns. `data` is left as a raw
 * JsonElement because its shape depends entirely on which action was called -
 * callers deserialize it into the DTO they expect (see ApiClient / APIClient).
 *
 * `error_code` is modeled as JsonElement rather than String because most modules
 * return a string ("", "GRACE_WINDOW_EXPIRED", "BIZ_SESSION_LOCKED", ...) but
 * m_curriculum's VALIDATION_ERROR can return an array of per-field messages.
 */
data class DispatchResponse(
    val status: String,
    val message: String? = null,
    val data: JsonElement? = null,
    @SerializedName("error_code") val errorCode: JsonElement? = null
) {
    val isSuccess: Boolean get() = status == "success"
}

/** Best-effort flattening of [DispatchResponse.errorCode] into a single display string. */
fun DispatchResponse.errorCodeAsString(): String {
    val code = errorCode ?: return ""
    return when {
        code.isJsonNull -> ""
        code.isJsonPrimitive -> code.asString
        code.isJsonArray -> code.asJsonArray.joinToString(", ") { it.asString }
        else -> code.toString()
    }
}

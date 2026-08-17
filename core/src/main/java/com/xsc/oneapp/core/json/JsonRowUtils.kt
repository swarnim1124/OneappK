package com.xsc.oneapp.core.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * Several *_API_CONTRACT.md `view` actions intentionally return raw database rows
 * ("Field names may be snake_case and vary by table. Client code should not assume
 * all example fields exist on every row." - m_exam contract §10) instead of a fixed
 * DTO shape. These helpers let a screen render that data defensively - by trying a
 * short list of candidate field names - without a full DTO per row shape.
 */
object JsonRowUtils {

    /** Normalizes a `view` response's `data` into a list of row objects, whether the
     * backend returned a JSON array or a single object. */
    fun asRows(data: JsonElement?): List<JsonObject> = when {
        data == null || data.isJsonNull -> emptyList()
        data.isJsonArray -> data.asJsonArray.mapNotNull { it as? JsonObject }
        data.isJsonObject -> listOf(data.asJsonObject)
        else -> emptyList()
    }

    /** The single object form of [asRows], for actions that return one aggregate
     * record rather than a row list (m_fees `feeInvoice:view` returns a statement
     * object - totalDebits/totalCredits/outstandingBalance - not a ledger array). */
    fun asObject(data: JsonElement?): JsonObject? = when {
        data == null || data.isJsonNull -> null
        data.isJsonObject -> data.asJsonObject
        else -> null
    }

    fun firstString(obj: JsonObject, vararg keys: String): String? {
        for (key in keys) {
            val el = obj.get(key)
            if (el != null && !el.isJsonNull) {
                return if (el.isJsonPrimitive) el.asString else el.toString()
            }
        }
        return null
    }

    /**
     * Numeric read for the same schema-less rows. Kept separate from [firstString]
     * because money fields have to be compared and summed (is there an outstanding
     * balance? enable "Pay now"), and a Gson primitive of 40000.0 stringified then
     * re-parsed is a needless round trip that also loses the "field was absent"
     * versus "field was zero" distinction that a nullable Double preserves.
     *
     * Accepts a JSON number or a numeric string, since the backend returns amounts
     * as both depending on whether the column came back through SQLAlchemy's Numeric
     * type or a Pydantic float.
     */
    fun firstDouble(obj: JsonObject, vararg keys: String): Double? {
        for (key in keys) {
            val el = obj.get(key)
            if (el != null && !el.isJsonNull && el.isJsonPrimitive) {
                val primitive = el.asJsonPrimitive
                if (primitive.isNumber) return primitive.asDouble
                if (primitive.isString) primitive.asString.trim().toDoubleOrNull()?.let { return it }
            }
        }
        return null
    }

    fun firstStringArray(obj: JsonObject, vararg keys: String): List<String> {
        for (key in keys) {
            val el = obj.get(key)
            if (el is JsonArray) {
                return el.mapNotNull { if (it.isJsonPrimitive) it.asString else null }
            }
        }
        return emptyList()
    }

    /** First array found under any of [keys], normalized to row objects. Used for
     * responses that nest their line items inside the aggregate object. */
    fun firstRowArray(obj: JsonObject, vararg keys: String): List<JsonObject> {
        for (key in keys) {
            val el = obj.get(key)
            if (el is JsonArray) return el.mapNotNull { it as? JsonObject }
        }
        return emptyList()
    }
}

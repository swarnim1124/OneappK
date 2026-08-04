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

    fun firstString(obj: JsonObject, vararg keys: String): String? {
        for (key in keys) {
            val el = obj.get(key)
            if (el != null && !el.isJsonNull) {
                return if (el.isJsonPrimitive) el.asString else el.toString()
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
}

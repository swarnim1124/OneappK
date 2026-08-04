package com.xsc.oneapp.core.json

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonRowUtilsTest {

    @Test
    fun `asRows returns empty list for null data`() {
        assertTrue(JsonRowUtils.asRows(null).isEmpty())
    }

    @Test
    fun `asRows returns empty list for JsonNull`() {
        val element = JsonParser.parseString("null")

        assertTrue(JsonRowUtils.asRows(element).isEmpty())
    }

    @Test
    fun `asRows unwraps a JSON array of row objects`() {
        val element = JsonParser.parseString("""[{"a":1},{"b":2}]""")

        val rows = JsonRowUtils.asRows(element)

        assertEquals(2, rows.size)
        assertEquals(1, rows[0].get("a").asInt)
        assertEquals(2, rows[1].get("b").asInt)
    }

    @Test
    fun `asRows wraps a single JSON object as a one-row list`() {
        val element = JsonParser.parseString("""{"a":1}""")

        val rows = JsonRowUtils.asRows(element)

        assertEquals(1, rows.size)
        assertEquals(1, rows[0].get("a").asInt)
    }

    @Test
    fun `firstString returns the first present, non-null key`() {
        val obj = JsonParser.parseString("""{"from_date":"2026-01-01"}""").asJsonObject

        assertEquals("2026-01-01", JsonRowUtils.firstString(obj, "start_date", "from_date"))
    }

    @Test
    fun `firstString returns null when none of the candidate keys are present`() {
        val obj = JsonParser.parseString("""{"unrelated":"value"}""").asJsonObject

        assertNull(JsonRowUtils.firstString(obj, "start_date", "from_date"))
    }

    @Test
    fun `firstStringArray returns the first matching array key`() {
        val obj = JsonParser.parseString("""{"tags":["a","b","c"]}""").asJsonObject

        assertEquals(listOf("a", "b", "c"), JsonRowUtils.firstStringArray(obj, "labels", "tags"))
    }

    @Test
    fun `firstStringArray returns empty list when no candidate key is an array`() {
        val obj = JsonParser.parseString("""{"tags":"not-an-array"}""").asJsonObject

        assertTrue(JsonRowUtils.firstStringArray(obj, "labels", "tags").isEmpty())
    }
}

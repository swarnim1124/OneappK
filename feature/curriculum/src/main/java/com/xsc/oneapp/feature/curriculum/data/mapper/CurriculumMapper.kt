package com.xsc.oneapp.feature.curriculum.data.mapper

import com.google.gson.JsonObject
import com.xsc.oneapp.core.json.JsonRowUtils
import com.xsc.oneapp.feature.curriculum.domain.model.Course
import com.xsc.oneapp.feature.curriculum.domain.model.Programme
import com.xsc.oneapp.feature.curriculum.domain.model.Syllabus

/** m_curriculum contract rows are schema-less like exam's - see JsonRowUtils. Field
 * names verified against real labeled response examples (2026-07-30). */
fun JsonObject.toProgramme(): Programme = Programme(
    name = JsonRowUtils.firstString(this, "prog_name", "progName") ?: "Programme",
    code = JsonRowUtils.firstString(this, "prog_code", "progCode"),
    totalTerms = JsonRowUtils.firstString(this, "total_terms", "totalTerms"),
    totalCredits = JsonRowUtils.firstString(this, "total_credits", "totalCredits")
)

fun JsonObject.toCourse(): Course = Course(
    id = JsonRowUtils.firstString(this, "id"),
    code = JsonRowUtils.firstString(this, "crs_code", "crsCode"),
    name = JsonRowUtils.firstString(this, "crs_name", "crsName"),
    creditValue = JsonRowUtils.firstString(this, "credit_value", "creditValue"),
    lectureHours = JsonRowUtils.firstString(this, "lecture_hours", "lectureHours"),
    tutorialHours = JsonRowUtils.firstString(this, "tutorial_hours", "tutorialHours"),
    practicalHours = JsonRowUtils.firstString(this, "practical_hours", "practicalHours")
)

fun JsonObject.toSyllabus(): Syllabus = Syllabus(
    id = JsonRowUtils.firstString(this, "id"),
    programmeId = JsonRowUtils.firstString(this, "prog_id", "progId"),
    name = JsonRowUtils.firstString(this, "curr_name", "currName")
)

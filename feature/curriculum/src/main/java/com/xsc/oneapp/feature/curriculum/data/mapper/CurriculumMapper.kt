package com.xsc.oneapp.feature.curriculum.data.mapper

import com.google.gson.JsonObject
import com.xsc.oneapp.feature.curriculum.domain.model.Course
import com.xsc.oneapp.feature.curriculum.domain.model.Programme
import com.xsc.oneapp.feature.curriculum.domain.model.Syllabus
import com.xsc.oneapp.core.json.JsonRowUtils

/**
 * m_curriculum rows are schema-less dictionaries - see JsonRowUtils. Field names
 * verified against real labeled response examples (2026-07-30).
 *
 * The `name`/`code` families were widened on 2026-08-14. The mapper only accepted
 * the ORM column names (`prog_name`, `crs_code`, `curr_name`), so when a handler
 * answered through a Pydantic response model instead - camelCase, or the generic
 * `name`/`code` this module's sibling m_fees also returns - every programme rendered
 * with the literal placeholder title "Programme" and no code. A list of identical
 * "Programme" rows is indistinguishable from a broken screen, which is part of what
 * "curriculum isn't wired properly" was describing.
 */
fun JsonObject.toProgramme(): Programme = Programme(
    name = JsonRowUtils.firstString(this, "prog_name", "progName", "programmeName", "name")
        ?: "Programme",
    code = JsonRowUtils.firstString(this, "prog_code", "progCode", "programmeCode", "code"),
    totalTerms = JsonRowUtils.firstString(this, "total_terms", "totalTerms"),
    totalCredits = JsonRowUtils.firstString(this, "total_credits", "totalCredits")
)

fun JsonObject.toCourse(): Course = Course(
    id = JsonRowUtils.firstString(this, "id", "crs_id", "crsId", "courseId"),
    code = JsonRowUtils.firstString(this, "crs_code", "crsCode", "courseCode", "code"),
    name = JsonRowUtils.firstString(this, "crs_name", "crsName", "courseName", "name"),
    creditValue = JsonRowUtils.firstString(this, "credit_value", "creditValue", "credits"),
    lectureHours = JsonRowUtils.firstString(this, "lecture_hours", "lectureHours"),
    tutorialHours = JsonRowUtils.firstString(this, "tutorial_hours", "tutorialHours"),
    practicalHours = JsonRowUtils.firstString(this, "practical_hours", "practicalHours")
)

fun JsonObject.toSyllabus(): Syllabus = Syllabus(
    id = JsonRowUtils.firstString(this, "id", "curr_id", "currId", "curriculumId"),
    programmeId = JsonRowUtils.firstString(this, "prog_id", "progId", "programmeId"),
    name = JsonRowUtils.firstString(this, "curr_name", "currName", "curriculumName", "name")
)

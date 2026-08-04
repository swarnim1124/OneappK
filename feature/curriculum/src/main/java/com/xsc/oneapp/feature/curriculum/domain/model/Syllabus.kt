package com.xsc.oneapp.feature.curriculum.domain.model

/** Named "Syllabus" here (rather than "Curriculum", which would collide with this
 * whole feature module's own name) even though the backend's action/entity is
 * literally called "curriculum" - see CurriculumEndpoint.kt. */
data class Syllabus(
    val id: String?,
    val programmeId: String?,
    val name: String?
)

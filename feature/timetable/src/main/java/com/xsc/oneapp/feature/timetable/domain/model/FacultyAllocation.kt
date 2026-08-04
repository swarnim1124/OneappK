package com.xsc.oneapp.feature.timetable.domain.model

/** sch_course_ops.tb_fac_crs_assignment. */
data class FacultyAllocation(
    val id: String?,
    val courseOfferingId: String?,
    val facultyId: String?,
    val assignmentRoleId: String?,
    val workloadPercent: String?,
    val isPrimary: String?,
    val remarks: String?
)

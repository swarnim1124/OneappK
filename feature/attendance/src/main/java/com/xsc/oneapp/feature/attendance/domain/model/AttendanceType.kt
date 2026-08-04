package com.xsc.oneapp.feature.attendance.domain.model

/** sch_lookup.tb_lookup WHERE cat_id = 70 - the institution's custom attendance
 * marking codes (present/absent/excused/...). */
data class AttendanceType(
    val id: String?,
    val lookupCode: String?,
    val lookupName: String?,
    val lookupValue: String?,
    val isActive: String?
)

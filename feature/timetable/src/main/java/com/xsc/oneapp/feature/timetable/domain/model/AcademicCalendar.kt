package com.xsc.oneapp.feature.timetable.domain.model

/** Not a row list - the backend returns a single proxy-metadata object ("Academic
 * calendar data is managed by the academic structure module. This view proxies
 * the relevant term data for timetable usage."), so this is fetched as a single
 * nullable object rather than a List, same convention as profile's MedicalDetail. */
data class AcademicCalendar(
    val institutionId: String?,
    val academicYearId: String?,
    val termId: String?,
    val note: String?
)

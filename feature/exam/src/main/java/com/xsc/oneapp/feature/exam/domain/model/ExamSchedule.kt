package com.xsc.oneapp.feature.exam.domain.model

data class ExamSchedule(
    val id: String?,
    val name: String,
    val fromDate: String?,
    val toDate: String?,
    val status: String?,
    val examType: String?
)

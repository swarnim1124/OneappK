package com.xsc.oneapp.feature.curriculum.domain.repository

import com.xsc.oneapp.feature.curriculum.domain.model.Course
import com.xsc.oneapp.feature.curriculum.domain.model.Programme
import com.xsc.oneapp.feature.curriculum.domain.model.Syllabus

interface CurriculumRepository {
    suspend fun getProgrammes(): List<Programme>
    suspend fun getCourses(): List<Course>
    suspend fun getSyllabus(): List<Syllabus>
}

package com.xsc.oneapp.feature.curriculum.domain.usecase

import com.xsc.oneapp.feature.curriculum.domain.model.Course
import com.xsc.oneapp.feature.curriculum.domain.repository.CurriculumRepository
import javax.inject.Inject

class GetCoursesUseCase @Inject constructor(
    private val repository: CurriculumRepository
) {
    suspend operator fun invoke(): List<Course> = repository.getCourses()
}

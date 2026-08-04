package com.xsc.oneapp.feature.curriculum.domain.usecase

import com.xsc.oneapp.feature.curriculum.domain.model.Programme
import com.xsc.oneapp.feature.curriculum.domain.repository.CurriculumRepository
import javax.inject.Inject

class GetProgrammesUseCase @Inject constructor(
    private val repository: CurriculumRepository
) {
    suspend operator fun invoke(): List<Programme> = repository.getProgrammes()
}

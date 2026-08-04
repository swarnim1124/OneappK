package com.xsc.oneapp.feature.timetable.domain.usecase

import com.xsc.oneapp.feature.timetable.domain.model.Substitution
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import javax.inject.Inject

class GetSubstitutionsUseCase @Inject constructor(
    private val repository: TimetableRepository
) {
    suspend operator fun invoke(): List<Substitution> = repository.getSubstitutions()
}

package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.FeeStructureComponent
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import javax.inject.Inject

/** Admin write actions for m_fees contract §3.1 (Fee Structure). */

class CreateFeeStructureUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(
        code: String,
        name: String,
        academicYearId: String,
        programmeId: String,
        components: List<FeeStructureComponent>,
        effectiveFrom: String,
        effectiveTo: String? = null
    ): String? = repository.createFeeStructure(
        code, name, academicYearId, programmeId, components, effectiveFrom, effectiveTo
    )
}

class UpdateFeeStructureStatusUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(structureId: String, status: String) =
        repository.updateFeeStructureStatus(structureId, status)
}

class DeleteFeeStructureUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(structureId: String) = repository.deleteFeeStructure(structureId)
}

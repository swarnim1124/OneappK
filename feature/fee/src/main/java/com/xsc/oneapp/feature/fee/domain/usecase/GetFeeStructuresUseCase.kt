package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.FeeStructure
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import javax.inject.Inject

class GetFeeStructuresUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(): List<FeeStructure> = repository.getFeeStructures()
}

package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import javax.inject.Inject

class GetFeePenaltiesUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(): List<FeePenalty> = repository.getFeePenalties()
}

package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import javax.inject.Inject

class GetMyFeeConcessionsUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(): List<FeeConcession> = repository.getMyFeeConcessions()
}

package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import javax.inject.Inject

class GetMyFeeRefundsUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(): List<FeeRefund> = repository.getMyFeeRefunds()
}

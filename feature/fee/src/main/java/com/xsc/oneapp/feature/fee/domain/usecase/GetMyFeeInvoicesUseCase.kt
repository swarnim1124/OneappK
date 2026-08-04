package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.FeeInvoice
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import javax.inject.Inject

class GetMyFeeInvoicesUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(): List<FeeInvoice> = repository.getMyFeeInvoices()
}

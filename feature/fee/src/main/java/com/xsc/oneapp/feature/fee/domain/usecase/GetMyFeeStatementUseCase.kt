package com.xsc.oneapp.feature.fee.domain.usecase

import com.xsc.oneapp.feature.fee.domain.model.FeeStatement
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import javax.inject.Inject

/** Replaces GetMyFeeInvoicesUseCase: `feeInvoice:view` returns one statement object,
 * not a list of invoices (m_fees contract v1.3 §3.4). */
class GetMyFeeStatementUseCase @Inject constructor(
    private val repository: FeeRepository
) {
    suspend operator fun invoke(): FeeStatement = repository.getMyFeeStatement()
}

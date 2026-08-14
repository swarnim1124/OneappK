package com.xsc.oneapp.feature.fee.domain.model

/** m_fees contract §3.1: one line item inside `feeStructure/add`'s `components[]` -
 * request-construction only, there is no corresponding read model since `view`
 * returns the assembled structure, not its component breakdown. */
data class FeeStructureComponent(
    val headCode: String,
    val headName: String,
    val amount: Double,
    val isOptional: Boolean
)

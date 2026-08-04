package com.xsc.oneapp.feature.fee.data.network

/** Mirrors m_fees_api_contract.md (2026-07-30), confirmed by the user as the
 * definitive spec for request payloads, over an earlier narrative-style document
 * whose subMod values and several payload shapes turned out to be wrong (see
 * FeePlaceholder.kt for the specific discrepancies). Response field names for
 * `view` are separately confirmed - m_fees returns raw SQLAlchemy-model
 * dictionaries in `data`, same schema-less pattern as m_attendance/m_curriculum. */
object FeeEndpoint {
    const val MODULE = "m_fees"

    /** No "sm_" prefix, and doesn't always match the action name (e.g. subMod
     * "concession" pairs with action "feeConcession") - confirmed exact values
     * from the definitive contract, not guessed. */
    object SubModules {
        const val FEE_STRUCTURE = "feeStructure"
        const val FEE_ASSIGNMENT = "feeAssignment"
        const val CONCESSION = "concession"
        const val INVOICE = "invoice"
        const val PAYMENT = "payment"
        const val REFUND = "refund"
        const val PENALTY = "penalty"
    }

    object Actions {
        const val FEE_STRUCTURE = "feeStructure"
        const val FEE_ASSIGNMENT = "feeAssignment"
        const val FEE_CONCESSION = "feeConcession"
        const val FEE_INVOICE = "feeInvoice"
        const val FEE_PAYMENT = "feePayment"
        const val FEE_REFUND = "feeRefund"
        const val FEE_PENALTY = "feePenalty"
    }

    object ActionTypes {
        const val ADD = "add"
        const val VIEW = "view"
        const val UPDATE = "update"
        const val DELETE = "delete"
    }
}

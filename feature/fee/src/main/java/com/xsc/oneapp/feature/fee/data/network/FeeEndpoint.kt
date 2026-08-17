package com.xsc.oneapp.feature.fee.data.network

/**
 * Mirrors m_fees_api_contract.md v1.3.0 (2026-08-14).
 *
 * SubModule values were corrected here on 2026-08-14: they had been the bare forms
 * ("invoice", "payment", ...) taken from an earlier revision of this same document.
 * Every request example in v1.3 - and the canonical index in §2 - uses the `sm_`
 * prefixed form, so that is what is sent now. The index lists the bare name as an
 * accepted alias ("`sm_invoice` / `invoice`"), so this is the safer of the two:
 * the prefixed form is what the dispatcher's own examples use.
 */
object FeeEndpoint {
    const val MODULE = "m_fees"

    object SubModules {
        const val FEE_STRUCTURE = "sm_fee_structure"
        const val FEE_ASSIGNMENT = "sm_fee_assignment"
        const val CONCESSION = "sm_concession"
        const val INVOICE = "sm_invoice"
        const val PAYMENT = "sm_payment"
        const val REFUND = "sm_refund"
        const val PENALTY = "sm_penalty"
    }

    /** Action names do not always match the submodule name (subMod "sm_concession"
     * pairs with action "feeConcession") - exact values from the contract's §2 index. */
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

    /** `method` values accepted by feePayment:add (contract §3.5). */
    object PaymentMethods {
        const val CASH = "CASH"
        const val CHEQUE = "CHQ"
        const val ONLINE = "ONLINE"
    }

    /** `status` values accepted by feePayment:update (contract §3.5). */
    object PaymentStatus {
        const val COMPLETED = "COMPLETED"
        const val FAILED = "FAILED"
    }
}

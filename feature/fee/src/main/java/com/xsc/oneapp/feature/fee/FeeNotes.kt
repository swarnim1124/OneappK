package com.xsc.oneapp.feature.fee

// Scope decision (2026-07-30, explicit user choice, same as feature/timetable):
// build all 7 sub-modules - feeStructure, feeAssignment, concession, invoice,
// payment, refund, penalty - covering Institution Admin/Accounts-office and
// Student surfaces, gated dynamically by role at login.
//
// "Pay Now" is explicitly deferred: the contract's feePayment/add only records a
// payment that already happened (via transactionRef) - it doesn't describe how
// money actually moves (gateway SDK, redirect, UPI intent, etc.), so a working
// payment button needs that flow clarified first. View-only payment history is
// fine and already built.
//
// Two live contract-document discrepancies to remember:
//   1. Two documents exist for this module. The first (narrative BRS-style) had
//      wrong subMod values (e.g. "sm_fee_structure") and substantially different
//      payload shapes for feeConcession/feeInvoice/feePayment/feeRefund/
//      feePenalty. The second (Pydantic-schema-derived, "m_fees_api_contract.md")
//      is the confirmed-authoritative one for request payloads - see
//      FeeEndpoint.kt's subMod/action values, which come from that document only.
//   2. feePenalty looks like it conflates two concepts the docs never reconcile:
//      the definitive request document's add/update describes configuring an
//      institution-wide policy (gracePeriodDays, penaltyType, penaltyValue - no
//      student reference at all), but the confirmed view response is a specific
//      student's applied penalty record (stud_id, fee_asmt_id, amount,
//      applied_date - the outcome of a policy being applied). Read-side is built
//      correctly against the real response; add/update for penalty are NOT built
//      until this is clarified, since guessing here risks sending a request that
//      doesn't match either concept.
//
// Built so far: read/view data layer only (domain models, mappers, repository,
// use cases) for all 7 entities - same scope precedent as feature/curriculum's
// Course/Syllabus/Term. No add/update/delete, no screens, no role-gating yet.
internal object FeeNotes

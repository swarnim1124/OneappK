package com.xsc.oneapp.feature.fee

// Scope decision (2026-07-30, explicit user choice, same as feature/timetable):
// build all 7 sub-modules - feeStructure, feeAssignment, concession, invoice,
// payment, refund, penalty - covering Institution Admin/Accounts-office and
// Student surfaces, gated dynamically by role at login.
//
// ---------------------------------------------------------------------------
// 2026-08-14: realigned to m_fees_api_contract.md v1.3.0, and online payment built.
// ---------------------------------------------------------------------------
//
// What was wrong, and why the Invoices tab looked broken:
//
//   1. feeInvoice/view does not return invoice rows. It returns one computed
//      statement object per student - studentId / totalDebits / totalCredits /
//      outstandingBalance (contract §3.4). This module modelled it as a list of
//      ledger transactions keyed on stud_id, transaction_type_id, transaction_date
//      and reference_id, none of which exist in that response. JsonRowUtils.asRows
//      turns a lone object into a one-element list, so the tab rendered exactly one
//      card with every field showing "—". Now modelled as FeeStatement.
//
//   2. The same drift hit every other entity, just less visibly. v1.3's view
//      responses use camelCase business keys (paymentId, concessionId, penaltyValue,
//      penaltyDate, sanctionDate, receiptNumber) where this mapper expected raw ORM
//      column names (id, applied_date, transaction_reference, ...). Penalties showed
//      no amount, payments no reference, concessions no type or date. FeeMapper now
//      tries both key families per field - which one a deployment returns depends on
//      whether the action goes through a Pydantic response model or dumps the
//      SQLAlchemy row, and both are currently in flight.
//
//   3. subMod values were the bare forms ("invoice", "payment"). Every request
//      example in v1.3 uses the sm_-prefixed form; the §2 index lists the bare name
//      only as an alias. Now sends the prefixed form.
//
//   4. feePenalty/view was sent with inst_id only - that is the *write* side's
//      filter. §3.7's view example filters on studentId, so a student was seeing
//      either nothing or the whole institution's penalties.
//
// Online payment ("Pay Now", previously deferred) is now built, because §3.5
// answered the question that had blocked it: feePayment/add with method "ONLINE"
// returns a Razorpay order rather than posting to the ledger. Flow is
//   feePayment:add (ONLINE) -> Razorpay Checkout -> feePayment:update (COMPLETED).
//
// Two things about that flow to keep in mind:
//   - The contract's documented order id is "order_MOCK12345678". The backend is
//     still returning a placeholder, and Razorpay rejects a fabricated order id
//     outright, so PaymentOrder.isRealGatewayOrder detects that and the client opens
//     an amount-only Checkout instead (valid in test mode). When the backend starts
//     creating real orders this switches over on its own, no client change.
//   - There is no documented signature-verification action. The client's
//     feePayment:update is therefore advisory, not authoritative: server-side
//     verification of razorpay_signature (or a gateway webhook) is still needed
//     before this can be trusted for real money. A failed update is surfaced as
//     "paid, reconciling" rather than as a failed payment, because by that point
//     the card has been charged.
//
// Still open from the original contract read:
//   - feePenalty conflates an institution-wide policy (add/update: gracePeriodDays,
//     penaltyType, penaltyValue, no student reference) with a specific student's
//     applied penalty record (view). Read side is built against the real response;
//     add/update are still not built, since guessing risks sending a request that
//     matches neither concept.
//   - studentId is sent as the JWT's user id. The backend's studentId is
//     tb_student.id. They coincide on seeded dev data and are not the same column -
//     first thing to check if a real tenant's student sees an empty statement.
internal object FeeNotes

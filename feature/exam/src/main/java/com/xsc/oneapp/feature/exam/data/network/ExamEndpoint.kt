package com.xsc.oneapp.feature.exam.data.network

/** Mirrors EXAM_API_CONTRACT.md §2 "Canonical routes". Only the subset this module's
 * screens use is wired up so far; the rest of the route table is listed here so
 * later screens follow the exact same envelope pattern.
 *
 * sm_revaluation (confirmed 2026-07-31, gateway routing table -
 * m_exam_sm_revaluation.py's Revaluation handler class) groups 4 actions:
 * revaluationRequest, photocopyRequest, appeal, challengeRevaluation. Only
 * revaluationRequest and challengeRevaluation are modeled here -
 * photocopyRequest/view and appeal/view are confirmed-routed but have only ever
 * returned `data: []` (mock-scaffolded backend), so there's no real row to verify
 * a field shape against yet. Add REVALUATION_PHOTOCOPY/REVALUATION_APPEAL action
 * constants once real rows exist to map. */
object ExamEndpoint {
    const val MODULE = "m_exam"

    object SubModules {
        const val SCHEDULE = "sm_schedule"
        const val EXAM_CENTER = "sm_examCenter"
        const val HALL_TICKET = "sm_hallTicket"
        const val RESULTS = "sm_results"
        const val REVALUATION = "sm_revaluation"
    }

    object Actions {
        const val EXAM_SCHEDULE = "examSchedule"
        const val EXAM_SCHEDULE_PUBLICATION = "examSchedulePublication"
        const val EXAM_CENTRE = "examCentre"
        const val HALL_TICKET = "hallTicket"
        const val RESULT = "result"
        const val REVALUATION_REQUEST = "revaluationRequest"
        const val CHALLENGE_REVALUATION = "challengeRevaluation"
    }

    object ActionTypes {
        const val ADD = "add"
        const val VIEW = "view"
        const val UPDATE = "update"
        const val DELETE = "delete"
    }
}

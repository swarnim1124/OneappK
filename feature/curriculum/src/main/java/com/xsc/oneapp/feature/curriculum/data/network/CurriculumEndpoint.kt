package com.xsc.oneapp.feature.curriculum.data.network

/** Corrected against the real backend handlers (2026-07-31) - the earlier
 * contract doc's summary index table had hallucinated a standalone sm_term
 * submodule and a "curriculumTerm" action; neither exists in the dispatcher.
 * Per the OneApp dynamic dispatcher (xsc_liba_dispatcher.py), the action name
 * must exactly match a method on the backend submodule class. Confirmed mapping:
 *   sm_programme: programme
 *   sm_course: courseDefinition, courseRequisite, courseOutcome
 *   sm_curriculum: curriculumManagement (terms are a nested "terms" array inside
 *     this action's payload, not a separately viewable entity), curriculumApproval
 *   sm_enrollment: enrollment, semesterRegistration, electiveRegistration
 * Only programme/courseDefinition/curriculumManagement view calls are wired up so
 * far (courseRequisite, courseOutcome, curriculumApproval, and the whole
 * sm_enrollment submodule have no data-layer support yet - not requested). The
 * standalone Term entity/getTerms() call this replaced is gone entirely: there is
 * no endpoint that returns terms on their own, only nested inside
 * curriculumManagement's payload, and that response shape isn't confirmed yet. */
object CurriculumEndpoint {
    const val MODULE = "m_curriculum"

    object SubModules {
        const val PROGRAMME = "sm_programme"
        const val COURSE = "sm_course"
        const val CURRICULUM = "sm_curriculum"
    }

    object Actions {
        const val PROGRAMME = "programme"
        const val COURSE_DEFINITION = "courseDefinition"
        const val CURRICULUM_MANAGEMENT = "curriculumManagement"
    }

    object ActionTypes {
        const val ADD = "add"
        const val VIEW = "view"
        const val UPDATE = "update"
        const val DELETE = "delete"
    }
}

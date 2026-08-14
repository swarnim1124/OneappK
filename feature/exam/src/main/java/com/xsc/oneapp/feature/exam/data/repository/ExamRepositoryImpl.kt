package com.xsc.oneapp.feature.exam.data.repository

import com.google.gson.JsonElement
import com.xsc.oneapp.core.json.JsonRowUtils
import com.xsc.oneapp.feature.exam.data.mapper.toChallengeRevaluation
import com.xsc.oneapp.feature.exam.data.mapper.toEvaluationBundle
import com.xsc.oneapp.feature.exam.data.mapper.toExamAppeal
import com.xsc.oneapp.feature.exam.data.mapper.toExamCentre
import com.xsc.oneapp.feature.exam.data.mapper.toExamResult
import com.xsc.oneapp.feature.exam.data.mapper.toExamSchedule
import com.xsc.oneapp.feature.exam.data.mapper.toExternalEvaluation
import com.xsc.oneapp.feature.exam.data.mapper.toExternalExaminer
import com.xsc.oneapp.feature.exam.data.mapper.toExternalPaperSetting
import com.xsc.oneapp.feature.exam.data.mapper.toGrade
import com.xsc.oneapp.feature.exam.data.mapper.toHallTicket
import com.xsc.oneapp.feature.exam.data.mapper.toInternalExam
import com.xsc.oneapp.feature.exam.data.mapper.toInvigilatorAssignment
import com.xsc.oneapp.feature.exam.data.mapper.toMalpracticeCase
import com.xsc.oneapp.feature.exam.data.mapper.toMarksRecord
import com.xsc.oneapp.feature.exam.data.mapper.toPhotocopyRequest
import com.xsc.oneapp.feature.exam.data.mapper.toQuestionBankEntry
import com.xsc.oneapp.feature.exam.data.mapper.toQuestionPaper
import com.xsc.oneapp.feature.exam.data.mapper.toQuestionPaperSubmission
import com.xsc.oneapp.feature.exam.data.mapper.toReappearEligibility
import com.xsc.oneapp.feature.exam.data.mapper.toReappearExam
import com.xsc.oneapp.feature.exam.data.mapper.toResultApprovalStatus
import com.xsc.oneapp.feature.exam.data.mapper.toRevaluationRequest
import com.xsc.oneapp.feature.exam.data.mapper.toSeatingPlan
import com.xsc.oneapp.feature.exam.data.mapper.toStudentExamBlock
import com.xsc.oneapp.feature.exam.data.mapper.toSupplementaryExam
import com.xsc.oneapp.feature.exam.data.network.ExamEndpoint
import com.xsc.oneapp.feature.exam.domain.model.ChallengeRevaluation
import com.xsc.oneapp.feature.exam.domain.model.EvaluationBundle
import com.xsc.oneapp.feature.exam.domain.model.ExamAppeal
import com.xsc.oneapp.feature.exam.domain.model.ExamCentre
import com.xsc.oneapp.feature.exam.domain.model.ExamResult
import com.xsc.oneapp.feature.exam.domain.model.ExamSchedule
import com.xsc.oneapp.feature.exam.domain.model.ExternalEvaluation
import com.xsc.oneapp.feature.exam.domain.model.ExternalExaminer
import com.xsc.oneapp.feature.exam.domain.model.ExternalPaperSetting
import com.xsc.oneapp.feature.exam.domain.model.Grade
import com.xsc.oneapp.feature.exam.domain.model.HallTicket
import com.xsc.oneapp.feature.exam.domain.model.InternalExam
import com.xsc.oneapp.feature.exam.domain.model.InvigilatorAssignment
import com.xsc.oneapp.feature.exam.domain.model.MalpracticeCase
import com.xsc.oneapp.feature.exam.domain.model.MarksRecord
import com.xsc.oneapp.feature.exam.domain.model.PhotocopyRequest
import com.xsc.oneapp.feature.exam.domain.model.QuestionBankEntry
import com.xsc.oneapp.feature.exam.domain.model.QuestionPaper
import com.xsc.oneapp.feature.exam.domain.model.QuestionPaperSubmission
import com.xsc.oneapp.feature.exam.domain.model.ReappearEligibility
import com.xsc.oneapp.feature.exam.domain.model.ReappearExam
import com.xsc.oneapp.feature.exam.domain.model.ResultApprovalStatus
import com.xsc.oneapp.feature.exam.domain.model.RevaluationRequest
import com.xsc.oneapp.feature.exam.domain.model.SeatingPlan
import com.xsc.oneapp.feature.exam.domain.model.StudentExamBlock
import com.xsc.oneapp.feature.exam.domain.model.SupplementaryExam
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.network.APIClient
import com.xsc.sdk.network.APIError
import javax.inject.Inject

class ExamRepositoryImpl @Inject constructor(
    private val apiClient: APIClient,
    private val sessionManager: SessionManager
) : ExamRepository {

    /**
     * The single place this module decides what to put in a `studentId`/`studId`
     * payload field.
     *
     * It sends the session's user id, which is what every verified-working call in this
     * module and in :attendance already does. Note this is knowingly not the same
     * number as the student id: m_profile's personalDetail.view returns
     * `studentId` (sch_student.tb_stud.id) separately from `userId` (sch_iam.tb_user.id)
     * and they are never equal (confirmed 2026-07-31). The reads here still return the
     * right rows, which is only possible because the backend overrides a student
     * caller's supplied id with the subject of the JWT.
     *
     * That override is load-bearing for the write actions below in a way it never was
     * for a read: if it were ever removed, a revaluation or registration would be filed
     * against whichever student happens to own tb_stud.id == this user id. Kept in one
     * helper so confirming the backend's behaviour turns into a one-line change here
     * rather than an edit at every call site. See the note raised with the backend team.
     */
    private fun requireStudentId(): Any {
        val id = sessionManager.getUserId()
            ?: throw APIError.BusinessError("NO_ID", "No signed-in student for this request")
        return id.toLongOrNull() ?: id
    }

    /** Contract payloads show ids as bare JSON numbers, not strings. */
    private fun String.asIdPayload(): Any = toLongOrNull() ?: this

    private suspend fun viewRows(
        submodule: String,
        action: String,
        payload: Map<String, Any>
    ): List<com.google.gson.JsonObject> {
        val data = apiClient.request<JsonElement?>(
            module = ExamEndpoint.MODULE,
            submodule = submodule,
            action = action,
            actionType = ExamEndpoint.ActionTypes.VIEW,
            payload = payload
        )
        return JsonRowUtils.asRows(data)
    }

    private suspend fun add(submodule: String, action: String, payload: Map<String, Any>) {
        apiClient.request<JsonElement?>(
            module = ExamEndpoint.MODULE,
            submodule = submodule,
            action = action,
            actionType = ExamEndpoint.ActionTypes.ADD,
            payload = payload
        )
    }

    private suspend fun update(submodule: String, action: String, payload: Map<String, Any>) {
        apiClient.request<JsonElement?>(
            module = ExamEndpoint.MODULE,
            submodule = submodule,
            action = action,
            actionType = ExamEndpoint.ActionTypes.UPDATE,
            payload = payload
        )
    }

    private suspend fun delete(submodule: String, action: String, payload: Map<String, Any>) {
        apiClient.request<JsonElement?>(
            module = ExamEndpoint.MODULE,
            submodule = submodule,
            action = action,
            actionType = ExamEndpoint.ActionTypes.DELETE,
            payload = payload
        )
    }

    /** For the *Publication actions, whose entire purpose is the publish/hold/unpublish
     * tri-state - the contract documents these as the real action-specific verbs, not
     * CRUD aliases standing in for something else. */
    private suspend fun act(
        submodule: String,
        action: String,
        actionType: String,
        payload: Map<String, Any>
    ) {
        apiClient.request<JsonElement?>(
            module = ExamEndpoint.MODULE,
            submodule = submodule,
            action = action,
            actionType = actionType,
            payload = payload
        )
    }

    /** m_exam contract: sm_schedule/examSchedule/view - omitting scheduleId returns
     * every schedule for the institution. instId is included when the session has
     * one (defensive-optional, same pattern as attendance's shortage view) so
     * accounts predating institutionId capture don't break the call. */
    override suspend fun getExamSchedules(): List<ExamSchedule> {
        val payload = mutableMapOf<String, Any>()
        sessionManager.getInstitutionId()?.let { payload["instId"] = it }
        return viewRows(
            ExamEndpoint.SubModules.SCHEDULE,
            ExamEndpoint.Actions.EXAM_SCHEDULE,
            payload
        ).map { it.toExamSchedule() }
    }

    /** m_exam contract: sm_hallTicket/hallTicket/view - scheduleId is required. */
    override suspend fun getHallTicket(scheduleId: String): List<HallTicket> {
        val payload = mutableMapOf<String, Any>("scheduleId" to scheduleId.asIdPayload())
        sessionManager.getUserId()?.let { payload["studentId"] = it.toLongOrNull() ?: it }
        return viewRows(
            ExamEndpoint.SubModules.HALL_TICKET,
            ExamEndpoint.Actions.HALL_TICKET,
            payload
        ).map { it.toHallTicket() }
    }

    /** m_exam contract: sm_results/result/view - courseId is confirmed
     * optional/ignored (tb_result only stores per-schedule aggregate GPA/CGPA, no
     * course-level rows), so it's never sent. */
    override suspend fun getMyResults(): List<ExamResult> {
        val payload = mutableMapOf<String, Any>()
        sessionManager.getUserId()?.let { payload["studentId"] = it.toLongOrNull() ?: it }
        return viewRows(
            ExamEndpoint.SubModules.RESULTS,
            ExamEndpoint.Actions.RESULT,
            payload
        ).map { it.toExamResult() }
    }

    /** m_exam contract §4.3: sm_hallTicket/studentBlock/view - studentId required,
     * scheduleId optional. */
    override suspend fun getMyExamBlocks(scheduleId: String?): List<StudentExamBlock> {
        val payload = mutableMapOf<String, Any>("studentId" to requireStudentId())
        scheduleId?.let { payload["scheduleId"] = it.asIdPayload() }
        return viewRows(
            ExamEndpoint.SubModules.HALL_TICKET,
            ExamEndpoint.Actions.STUDENT_BLOCK,
            payload
        ).map { it.toStudentExamBlock() }
    }

    /** m_exam contract: sm_revaluation/revaluationRequest/view. */
    override suspend fun getMyRevaluationRequests(): List<RevaluationRequest> {
        val payload = mutableMapOf<String, Any>()
        sessionManager.getUserId()?.let { payload["studentId"] = it.toLongOrNull() ?: it }
        return viewRows(
            ExamEndpoint.SubModules.REVALUATION,
            ExamEndpoint.Actions.REVALUATION_REQUEST,
            payload
        ).map { it.toRevaluationRequest() }
    }

    /** m_exam contract §8.1: revaluationRequest/add requires studentId, scheduleId and
     * courseId. `reason` is not in the contract's required list but the view row
     * carries one, so it is sent only when the student actually typed something. */
    override suspend fun submitRevaluationRequest(
        scheduleId: String,
        courseId: String,
        reason: String?
    ) {
        val payload = mutableMapOf(
            "studentId" to requireStudentId(),
            "scheduleId" to scheduleId.asIdPayload(),
            "courseId" to courseId.asIdPayload()
        )
        reason?.takeIf { it.isNotBlank() }?.let { payload["reason"] = it }
        add(ExamEndpoint.SubModules.REVALUATION, ExamEndpoint.Actions.REVALUATION_REQUEST, payload)
    }

    /** m_exam contract §8.1: sm_revaluation/photocopyRequest/view. */
    override suspend fun getMyPhotocopyRequests(): List<PhotocopyRequest> {
        val payload = mutableMapOf<String, Any>()
        sessionManager.getUserId()?.let { payload["studentId"] = it.toLongOrNull() ?: it }
        return viewRows(
            ExamEndpoint.SubModules.REVALUATION,
            ExamEndpoint.Actions.PHOTOCOPY_REQUEST,
            payload
        ).map { it.toPhotocopyRequest() }
    }

    /** m_exam contract §8.1: photocopyRequest/add requires studentId, scheduleId, courseId. */
    override suspend fun submitPhotocopyRequest(scheduleId: String, courseId: String) {
        add(
            ExamEndpoint.SubModules.REVALUATION,
            ExamEndpoint.Actions.PHOTOCOPY_REQUEST,
            mapOf(
                "studentId" to requireStudentId(),
                "scheduleId" to scheduleId.asIdPayload(),
                "courseId" to courseId.asIdPayload()
            )
        )
    }

    /** m_exam contract §8.1: sm_revaluation/appeal/view. */
    override suspend fun getMyAppeals(): List<ExamAppeal> {
        val payload = mutableMapOf<String, Any>()
        sessionManager.getUserId()?.let { payload["studentId"] = it.toLongOrNull() ?: it }
        return viewRows(
            ExamEndpoint.SubModules.REVALUATION,
            ExamEndpoint.Actions.APPEAL,
            payload
        ).map { it.toExamAppeal() }
    }

    /** m_exam contract §8.1: appeal/add requires studentId, referenceId and reason.
     * referenceId is the revaluation request being appealed. */
    override suspend fun submitAppeal(referenceId: String, reason: String) {
        add(
            ExamEndpoint.SubModules.REVALUATION,
            ExamEndpoint.Actions.APPEAL,
            mapOf(
                "studentId" to requireStudentId(),
                "referenceId" to referenceId.asIdPayload(),
                "reason" to reason
            )
        )
    }

    /** m_exam contract: sm_revaluation/challengeRevaluation/view. */
    override suspend fun getMyChallengeRevaluations(): List<ChallengeRevaluation> {
        val payload = mutableMapOf<String, Any>()
        sessionManager.getUserId()?.let { payload["studentId"] = it.toLongOrNull() ?: it }
        return viewRows(
            ExamEndpoint.SubModules.REVALUATION,
            ExamEndpoint.Actions.CHALLENGE_REVALUATION,
            payload
        ).map { it.toChallengeRevaluation() }
    }

    /** m_exam contract §8.1: challengeRevaluation/add requires a prior regular
     * revaluation id - the contract is explicit that a challenge cannot be raised
     * without one, so the caller must supply it rather than it being optional here. */
    override suspend fun submitChallengeRevaluation(
        scheduleId: String,
        courseId: String,
        revaluationRequestId: String
    ) {
        add(
            ExamEndpoint.SubModules.REVALUATION,
            ExamEndpoint.Actions.CHALLENGE_REVALUATION,
            mapOf(
                "studentId" to requireStudentId(),
                "scheduleId" to scheduleId.asIdPayload(),
                "courseId" to courseId.asIdPayload(),
                "revaluationRequestId" to revaluationRequestId.asIdPayload()
            )
        )
    }

    /** m_exam contract §8.2: sm_supplementary/supplementaryExam/view. */
    override suspend fun getSupplementaryExams(): List<SupplementaryExam> {
        val payload = mutableMapOf<String, Any>()
        sessionManager.getInstitutionId()?.let { payload["inst_id"] = it }
        return viewRows(
            ExamEndpoint.SubModules.SUPPLEMENTARY,
            ExamEndpoint.Actions.SUPPLEMENTARY_EXAM,
            payload
        ).map { it.toSupplementaryExam() }
    }

    /** m_exam contract §8.2: supplementaryRegistration/add requires studentId,
     * supplementaryExamId and courseIds. */
    override suspend fun registerForSupplementaryExam(
        supplementaryExamId: String,
        courseIds: List<String>
    ) {
        add(
            ExamEndpoint.SubModules.SUPPLEMENTARY,
            ExamEndpoint.Actions.SUPPLEMENTARY_REGISTRATION,
            mapOf(
                "studentId" to requireStudentId(),
                "supplementaryExamId" to supplementaryExamId.asIdPayload(),
                "courseIds" to courseIds.map { it.asIdPayload() }
            )
        )
    }

    /** m_exam contract §8.3: sm_reappear/reappearExam/view. */
    override suspend fun getReappearExams(): List<ReappearExam> {
        val payload = mutableMapOf<String, Any>()
        sessionManager.getInstitutionId()?.let { payload["inst_id"] = it }
        return viewRows(
            ExamEndpoint.SubModules.REAPPEAR,
            ExamEndpoint.Actions.REAPPEAR_EXAM,
            payload
        ).map { it.toReappearExam() }
    }

    /** m_exam contract §8.3: reappearRegistration/add requires studentId,
     * reappearExamId and courseIds. */
    override suspend fun registerForReappearExam(
        reappearExamId: String,
        courseIds: List<String>
    ) {
        add(
            ExamEndpoint.SubModules.REAPPEAR,
            ExamEndpoint.Actions.REAPPEAR_REGISTRATION,
            mapOf(
                "studentId" to requireStudentId(),
                "reappearExamId" to reappearExamId.asIdPayload(),
                "courseIds" to courseIds.map { it.asIdPayload() }
            )
        )
    }

    /** m_exam contract §8.3: sm_reappear/reappearEligibility/view, scoped to the
     * signed-in student and optionally to one reappear session. */
    override suspend fun getMyReappearEligibility(
        reappearExamId: String?
    ): List<ReappearEligibility> {
        val payload = mutableMapOf<String, Any>("studentId" to requireStudentId())
        reappearExamId?.let { payload["reappearExamId"] = it.asIdPayload() }
        return viewRows(
            ExamEndpoint.SubModules.REAPPEAR,
            ExamEndpoint.Actions.REAPPEAR_ELIGIBILITY,
            payload
        ).map { it.toReappearEligibility() }
    }

    /** m_exam contract §8.4: sm_malpractice/malpracticeCase/view. Read-only for a
     * student - reporting and committee actions are staff-only. */
    override suspend fun getMyMalpracticeCases(): List<MalpracticeCase> {
        val payload = mutableMapOf<String, Any>("studentId" to requireStudentId())
        return viewRows(
            ExamEndpoint.SubModules.MALPRACTICE,
            ExamEndpoint.Actions.MALPRACTICE_CASE,
            payload
        ).map { it.toMalpracticeCase() }
    }

    // ==================== Admin: sm_examCenter ====================

    override suspend fun getExamCentres(): List<ExamCentre> {
        val payload = mutableMapOf<String, Any>()
        sessionManager.getInstitutionId()?.let { payload["instId"] = it }
        return viewRows(
            ExamEndpoint.SubModules.EXAM_CENTER,
            ExamEndpoint.Actions.EXAM_CENTRE,
            payload
        ).map { it.toExamCentre() }
    }

    override suspend fun addExamCentre(
        centreName: String,
        capacity: String,
        address: String?,
        courses: List<String>
    ) {
        val payload = mutableMapOf<String, Any>(
            "centreName" to centreName,
            "capacity" to (capacity.toIntOrNull() ?: capacity),
            "courses" to courses.map { it.asIdPayload() }
        )
        address?.takeIf { it.isNotBlank() }?.let { payload["address"] = it }
        add(ExamEndpoint.SubModules.EXAM_CENTER, ExamEndpoint.Actions.EXAM_CENTRE, payload)
    }

    override suspend fun updateExamCentre(
        centreId: String,
        centreName: String?,
        capacity: String?,
        address: String?,
        courses: List<String>?,
        status: String?
    ) {
        val payload = mutableMapOf<String, Any>("id" to centreId.asIdPayload())
        centreName?.let { payload["centreName"] = it }
        capacity?.let { payload["capacity"] = it.toIntOrNull() ?: it }
        address?.let { payload["address"] = it }
        courses?.let { payload["courses"] = it.map { c -> c.asIdPayload() } }
        status?.let { payload["status"] = it }
        update(ExamEndpoint.SubModules.EXAM_CENTER, ExamEndpoint.Actions.EXAM_CENTRE, payload)
    }

    override suspend fun deleteExamCentre(centreId: String) {
        delete(
            ExamEndpoint.SubModules.EXAM_CENTER,
            ExamEndpoint.Actions.EXAM_CENTRE,
            mapOf("id" to centreId.asIdPayload())
        )
    }

    // ==================== Admin: sm_schedule ====================

    /** Simplified from the contract's dynamic `sessions[]` builder to one session per
     * creation (documented scope simplification, consistent with Fee's structure
     * dialog simplifying `components[]` to one entry at a time). */
    override suspend fun addExamSchedule(
        name: String,
        examType: String,
        fromDate: String,
        toDate: String,
        sessionDate: String,
        session: String
    ) {
        add(
            ExamEndpoint.SubModules.SCHEDULE,
            ExamEndpoint.Actions.EXAM_SCHEDULE,
            mapOf(
                "examName" to name,
                "examType" to examType,
                "fromDate" to fromDate,
                "toDate" to toDate,
                "sessions" to listOf(mapOf("date" to sessionDate, "session" to session))
            )
        )
    }

    override suspend fun updateExamSchedule(
        scheduleId: String,
        name: String?,
        fromDate: String?,
        toDate: String?
    ) {
        val payload = mutableMapOf<String, Any>("id" to scheduleId.asIdPayload())
        name?.let { payload["examName"] = it }
        fromDate?.let { payload["fromDate"] = it }
        toDate?.let { payload["toDate"] = it }
        update(ExamEndpoint.SubModules.SCHEDULE, ExamEndpoint.Actions.EXAM_SCHEDULE, payload)
    }

    override suspend fun deleteExamSchedule(scheduleId: String) {
        delete(
            ExamEndpoint.SubModules.SCHEDULE,
            ExamEndpoint.Actions.EXAM_SCHEDULE,
            mapOf("id" to scheduleId.asIdPayload())
        )
    }

    override suspend fun publishExamSchedule(scheduleId: String) {
        act(
            ExamEndpoint.SubModules.SCHEDULE,
            ExamEndpoint.Actions.EXAM_SCHEDULE_PUBLICATION,
            ExamEndpoint.ActionTypes.PUBLISH,
            mapOf("scheduleId" to scheduleId.asIdPayload())
        )
    }

    override suspend fun holdExamSchedule(scheduleId: String) {
        act(
            ExamEndpoint.SubModules.SCHEDULE,
            ExamEndpoint.Actions.EXAM_SCHEDULE_PUBLICATION,
            ExamEndpoint.ActionTypes.HOLD,
            mapOf("scheduleId" to scheduleId.asIdPayload())
        )
    }

    override suspend fun unpublishExamSchedule(scheduleId: String) {
        act(
            ExamEndpoint.SubModules.SCHEDULE,
            ExamEndpoint.Actions.EXAM_SCHEDULE_PUBLICATION,
            ExamEndpoint.ActionTypes.UNPUBLISH,
            mapOf("scheduleId" to scheduleId.asIdPayload())
        )
    }

    // ==================== Admin: sm_seating ====================

    override suspend fun getSeatingPlans(scheduleId: String?): List<SeatingPlan> {
        val payload = mutableMapOf<String, Any>()
        scheduleId?.let { payload["scheduleId"] = it.asIdPayload() }
        return viewRows(
            ExamEndpoint.SubModules.SEATING,
            ExamEndpoint.Actions.SEATING_PLAN,
            payload
        ).map { it.toSeatingPlan() }
    }

    override suspend fun addSeatingPlan(
        scheduleId: String,
        venueId: String,
        courseId: String,
        studentId: String,
        seatNumber: String,
        allocationMethod: String?
    ) {
        val payload = mutableMapOf<String, Any>(
            "scheduleId" to scheduleId.asIdPayload(),
            "venueId" to venueId.asIdPayload(),
            "courseId" to courseId.asIdPayload(),
            "studentId" to studentId.asIdPayload(),
            "seatNumber" to seatNumber
        )
        allocationMethod?.let { payload["allocationMethod"] = it }
        add(ExamEndpoint.SubModules.SEATING, ExamEndpoint.Actions.SEATING_PLAN, payload)
    }

    override suspend fun updateSeatingPlan(id: String, seatNumber: String?, venueId: String?) {
        val payload = mutableMapOf<String, Any>("id" to id.asIdPayload())
        seatNumber?.let { payload["seatNumber"] = it }
        venueId?.let { payload["venueId"] = it.asIdPayload() }
        update(ExamEndpoint.SubModules.SEATING, ExamEndpoint.Actions.SEATING_PLAN, payload)
    }

    override suspend fun deleteSeatingPlan(id: String) {
        delete(
            ExamEndpoint.SubModules.SEATING,
            ExamEndpoint.Actions.SEATING_PLAN,
            mapOf("id" to id.asIdPayload())
        )
    }

    // ==================== Admin: sm_invigilation ====================

    override suspend fun getInvigilatorAssignments(scheduleId: String?): List<InvigilatorAssignment> {
        val payload = mutableMapOf<String, Any>()
        scheduleId?.let { payload["scheduleId"] = it.asIdPayload() }
        return viewRows(
            ExamEndpoint.SubModules.INVIGILATION,
            ExamEndpoint.Actions.INVIGILATOR_ASSIGNMENT,
            payload
        ).map { it.toInvigilatorAssignment() }
    }

    override suspend fun addInvigilatorAssignment(
        scheduleId: String,
        venueId: String,
        facultyId: String,
        dutyType: String
    ) {
        add(
            ExamEndpoint.SubModules.INVIGILATION,
            ExamEndpoint.Actions.INVIGILATOR_ASSIGNMENT,
            mapOf(
                "scheduleId" to scheduleId.asIdPayload(),
                "venueId" to venueId.asIdPayload(),
                "facultyId" to facultyId.asIdPayload(),
                "dutyType" to dutyType
            )
        )
    }

    override suspend fun updateInvigilatorAssignment(id: String, dutyType: String?, status: String?) {
        val payload = mutableMapOf<String, Any>("id" to id.asIdPayload())
        dutyType?.let { payload["dutyType"] = it }
        status?.let { payload["status"] = it }
        update(ExamEndpoint.SubModules.INVIGILATION, ExamEndpoint.Actions.INVIGILATOR_ASSIGNMENT, payload)
    }

    override suspend fun deleteInvigilatorAssignment(id: String) {
        delete(
            ExamEndpoint.SubModules.INVIGILATION,
            ExamEndpoint.Actions.INVIGILATOR_ASSIGNMENT,
            mapOf("id" to id.asIdPayload())
        )
    }

    // ==================== Admin: sm_hallTicket ====================

    override suspend fun generateHallTicket(
        scheduleId: String,
        studentId: String,
        venueDetails: String?,
        seatNumber: String?
    ) {
        val payload = mutableMapOf<String, Any>(
            "scheduleId" to scheduleId.asIdPayload(),
            "studentId" to studentId.asIdPayload()
        )
        venueDetails?.let { payload["venueDetails"] = it }
        seatNumber?.let { payload["seatNumber"] = it }
        add(ExamEndpoint.SubModules.HALL_TICKET, ExamEndpoint.Actions.HALL_TICKET, payload)
    }

    override suspend fun updateHallTicketAdmin(id: String, venueDetails: String?, seatNumber: String?) {
        val payload = mutableMapOf<String, Any>("id" to id.asIdPayload())
        venueDetails?.let { payload["venueDetails"] = it }
        seatNumber?.let { payload["seatNumber"] = it }
        update(ExamEndpoint.SubModules.HALL_TICKET, ExamEndpoint.Actions.HALL_TICKET, payload)
    }

    override suspend fun deleteHallTicketAdmin(id: String) {
        delete(
            ExamEndpoint.SubModules.HALL_TICKET,
            ExamEndpoint.Actions.HALL_TICKET,
            mapOf("id" to id.asIdPayload())
        )
    }

    override suspend fun publishHallTickets(scheduleId: String) {
        act(
            ExamEndpoint.SubModules.HALL_TICKET,
            ExamEndpoint.Actions.HALL_TICKET_PUBLICATION,
            ExamEndpoint.ActionTypes.PUBLISH,
            mapOf("scheduleId" to scheduleId.asIdPayload())
        )
    }

    override suspend fun holdHallTickets(scheduleId: String) {
        act(
            ExamEndpoint.SubModules.HALL_TICKET,
            ExamEndpoint.Actions.HALL_TICKET_PUBLICATION,
            ExamEndpoint.ActionTypes.HOLD,
            mapOf("scheduleId" to scheduleId.asIdPayload())
        )
    }

    override suspend fun unpublishHallTickets(scheduleId: String) {
        act(
            ExamEndpoint.SubModules.HALL_TICKET,
            ExamEndpoint.Actions.HALL_TICKET_PUBLICATION,
            ExamEndpoint.ActionTypes.UNPUBLISH,
            mapOf("scheduleId" to scheduleId.asIdPayload())
        )
    }

    override suspend fun getExamBlocksAdmin(scheduleId: String?): List<StudentExamBlock> {
        val payload = mutableMapOf<String, Any>()
        scheduleId?.let { payload["scheduleId"] = it.asIdPayload() }
        return viewRows(
            ExamEndpoint.SubModules.HALL_TICKET,
            ExamEndpoint.Actions.STUDENT_BLOCK,
            payload
        ).map { it.toStudentExamBlock() }
    }

    override suspend fun addStudentExamBlock(studentId: String, scheduleId: String, reason: String) {
        add(
            ExamEndpoint.SubModules.HALL_TICKET,
            ExamEndpoint.Actions.STUDENT_BLOCK,
            mapOf(
                "studentId" to studentId.asIdPayload(),
                "scheduleId" to scheduleId.asIdPayload(),
                "reason" to reason
            )
        )
    }

    override suspend fun updateStudentExamBlock(id: String, reason: String?, status: String?) {
        val payload = mutableMapOf<String, Any>("id" to id.asIdPayload())
        reason?.let { payload["reason"] = it }
        status?.let { payload["status"] = it }
        update(ExamEndpoint.SubModules.HALL_TICKET, ExamEndpoint.Actions.STUDENT_BLOCK, payload)
    }

    override suspend fun deleteStudentExamBlock(id: String) {
        delete(
            ExamEndpoint.SubModules.HALL_TICKET,
            ExamEndpoint.Actions.STUDENT_BLOCK,
            mapOf("id" to id.asIdPayload())
        )
    }

    // ==================== Admin: sm_questionPaper ====================

    override suspend fun getQuestionPapers(scheduleId: String?): List<QuestionPaper> {
        val payload = mutableMapOf<String, Any>()
        scheduleId?.let { payload["scheduleId"] = it.asIdPayload() }
        return viewRows(
            ExamEndpoint.SubModules.QUESTION_PAPER,
            ExamEndpoint.Actions.QUESTION_PAPER,
            payload
        ).map { it.toQuestionPaper() }
    }

    /** `content` is a plain multi-line text field - no file-upload SDK exists anywhere
     * in this app (documented scope simplification). `setBy` resolves from the
     * signed-in actor's session, never typed in. */
    override suspend fun addQuestionPaper(scheduleId: String, courseId: String, content: String) {
        add(
            ExamEndpoint.SubModules.QUESTION_PAPER,
            ExamEndpoint.Actions.QUESTION_PAPER,
            mapOf(
                "scheduleId" to scheduleId.asIdPayload(),
                "courseId" to courseId.asIdPayload(),
                "setBy" to requireStudentId(),
                "content" to content
            )
        )
    }

    override suspend fun updateQuestionPaper(id: String, content: String?, status: String?) {
        val payload = mutableMapOf<String, Any>("id" to id.asIdPayload())
        content?.let { payload["content"] = it }
        status?.let { payload["status"] = it }
        update(ExamEndpoint.SubModules.QUESTION_PAPER, ExamEndpoint.Actions.QUESTION_PAPER, payload)
    }

    override suspend fun deleteQuestionPaper(id: String) {
        delete(
            ExamEndpoint.SubModules.QUESTION_PAPER,
            ExamEndpoint.Actions.QUESTION_PAPER,
            mapOf("id" to id.asIdPayload())
        )
    }

    override suspend fun getQuestionPaperSubmissions(
        questionPaperId: String?
    ): List<QuestionPaperSubmission> {
        val payload = mutableMapOf<String, Any>()
        questionPaperId?.let { payload["questionPaperId"] = it.asIdPayload() }
        return viewRows(
            ExamEndpoint.SubModules.QUESTION_PAPER,
            ExamEndpoint.Actions.QUESTION_PAPER_SUBMISSION,
            payload
        ).map { it.toQuestionPaperSubmission() }
    }

    override suspend fun submitQuestionPaper(questionPaperId: String) {
        add(
            ExamEndpoint.SubModules.QUESTION_PAPER,
            ExamEndpoint.Actions.QUESTION_PAPER_SUBMISSION,
            mapOf("questionPaperId" to questionPaperId.asIdPayload())
        )
    }

    override suspend fun deleteQuestionPaperSubmission(id: String) {
        delete(
            ExamEndpoint.SubModules.QUESTION_PAPER,
            ExamEndpoint.Actions.QUESTION_PAPER_SUBMISSION,
            mapOf("id" to id.asIdPayload())
        )
    }

    override suspend fun approveQuestionPaper(questionPaperId: String) {
        add(
            ExamEndpoint.SubModules.QUESTION_PAPER,
            ExamEndpoint.Actions.QUESTION_PAPER_APPROVAL,
            mapOf("questionPaperId" to questionPaperId.asIdPayload(), "status" to "approved")
        )
    }

    override suspend fun rejectQuestionPaper(questionPaperId: String, reason: String?) {
        val payload = mutableMapOf<String, Any>(
            "questionPaperId" to questionPaperId.asIdPayload(),
            "status" to "rejected"
        )
        reason?.let { payload["reason"] = it }
        add(ExamEndpoint.SubModules.QUESTION_PAPER, ExamEndpoint.Actions.QUESTION_PAPER_APPROVAL, payload)
    }

    override suspend fun getQuestionBank(courseId: String?): List<QuestionBankEntry> {
        val payload = mutableMapOf<String, Any>()
        courseId?.let { payload["courseId"] = it.asIdPayload() }
        return viewRows(
            ExamEndpoint.SubModules.QUESTION_PAPER,
            ExamEndpoint.Actions.QUESTION_BANK,
            payload
        ).map { it.toQuestionBankEntry() }
    }

    override suspend fun addQuestionBankEntry(
        courseId: String,
        content: String,
        questionPaperId: String?
    ) {
        val payload = mutableMapOf<String, Any>(
            "courseId" to courseId.asIdPayload(),
            "content" to content
        )
        questionPaperId?.let { payload["questionPaperId"] = it.asIdPayload() }
        add(ExamEndpoint.SubModules.QUESTION_PAPER, ExamEndpoint.Actions.QUESTION_BANK, payload)
    }

    override suspend fun updateQuestionBankEntry(id: String, content: String?) {
        val payload = mutableMapOf<String, Any>("id" to id.asIdPayload())
        content?.let { payload["content"] = it }
        update(ExamEndpoint.SubModules.QUESTION_PAPER, ExamEndpoint.Actions.QUESTION_BANK, payload)
    }

    override suspend fun deleteQuestionBankEntry(id: String) {
        delete(
            ExamEndpoint.SubModules.QUESTION_PAPER,
            ExamEndpoint.Actions.QUESTION_BANK,
            mapOf("id" to id.asIdPayload())
        )
    }

    // ==================== Admin: sm_externalExam ====================

    override suspend fun getExternalExaminers(): List<ExternalExaminer> {
        return viewRows(
            ExamEndpoint.SubModules.EXTERNAL_EXAM,
            ExamEndpoint.Actions.EXTERNAL_EXAMINER,
            emptyMap()
        ).map { it.toExternalExaminer() }
    }

    override suspend fun addExternalExaminer(
        examinerName: String,
        contactEmail: String?,
        contactPhone: String?,
        specialization: String?
    ) {
        val payload = mutableMapOf<String, Any>("examinerName" to examinerName)
        contactEmail?.let { payload["contactEmail"] = it }
        contactPhone?.let { payload["contactPhone"] = it }
        specialization?.let { payload["specialization"] = it }
        add(ExamEndpoint.SubModules.EXTERNAL_EXAM, ExamEndpoint.Actions.EXTERNAL_EXAMINER, payload)
    }

    override suspend fun updateExternalExaminer(
        id: String,
        examinerName: String?,
        contactEmail: String?,
        contactPhone: String?,
        specialization: String?
    ) {
        val payload = mutableMapOf<String, Any>("id" to id.asIdPayload())
        examinerName?.let { payload["examinerName"] = it }
        contactEmail?.let { payload["contactEmail"] = it }
        contactPhone?.let { payload["contactPhone"] = it }
        specialization?.let { payload["specialization"] = it }
        update(ExamEndpoint.SubModules.EXTERNAL_EXAM, ExamEndpoint.Actions.EXTERNAL_EXAMINER, payload)
    }

    override suspend fun deleteExternalExaminer(id: String) {
        delete(
            ExamEndpoint.SubModules.EXTERNAL_EXAM,
            ExamEndpoint.Actions.EXTERNAL_EXAMINER,
            mapOf("id" to id.asIdPayload())
        )
    }

    override suspend fun getExternalPaperSettings(): List<ExternalPaperSetting> {
        return viewRows(
            ExamEndpoint.SubModules.EXTERNAL_EXAM,
            ExamEndpoint.Actions.EXTERNAL_PAPER_SETTING,
            emptyMap()
        ).map { it.toExternalPaperSetting() }
    }

    override suspend fun addExternalPaperSetting(examinerId: String, courseId: String, scheduleId: String) {
        add(
            ExamEndpoint.SubModules.EXTERNAL_EXAM,
            ExamEndpoint.Actions.EXTERNAL_PAPER_SETTING,
            mapOf(
                "examinerId" to examinerId.asIdPayload(),
                "courseId" to courseId.asIdPayload(),
                "scheduleId" to scheduleId.asIdPayload()
            )
        )
    }

    override suspend fun deleteExternalPaperSetting(id: String) {
        delete(
            ExamEndpoint.SubModules.EXTERNAL_EXAM,
            ExamEndpoint.Actions.EXTERNAL_PAPER_SETTING,
            mapOf("id" to id.asIdPayload())
        )
    }

    override suspend fun getExternalEvaluations(): List<ExternalEvaluation> {
        return viewRows(
            ExamEndpoint.SubModules.EXTERNAL_EXAM,
            ExamEndpoint.Actions.EXTERNAL_EVALUATION,
            emptyMap()
        ).map { it.toExternalEvaluation() }
    }

    override suspend fun addExternalEvaluation(
        examinerId: String,
        courseId: String,
        scheduleId: String,
        scriptBundleIds: List<String>
    ) {
        add(
            ExamEndpoint.SubModules.EXTERNAL_EXAM,
            ExamEndpoint.Actions.EXTERNAL_EVALUATION,
            mapOf(
                "examinerId" to examinerId.asIdPayload(),
                "courseId" to courseId.asIdPayload(),
                "scheduleId" to scheduleId.asIdPayload(),
                "scriptBundleIds" to scriptBundleIds.map { it.asIdPayload() }
            )
        )
    }

    override suspend fun updateExternalEvaluation(id: String, status: String?) {
        val payload = mutableMapOf<String, Any>("id" to id.asIdPayload())
        status?.let { payload["status"] = it }
        update(ExamEndpoint.SubModules.EXTERNAL_EXAM, ExamEndpoint.Actions.EXTERNAL_EVALUATION, payload)
    }

    override suspend fun deleteExternalEvaluation(id: String) {
        delete(
            ExamEndpoint.SubModules.EXTERNAL_EXAM,
            ExamEndpoint.Actions.EXTERNAL_EVALUATION,
            mapOf("id" to id.asIdPayload())
        )
    }

    // ==================== Admin: sm_internalExam ====================

    override suspend fun getInternalExams(courseOfferingId: String?): List<InternalExam> {
        val payload = mutableMapOf<String, Any>()
        courseOfferingId?.let { payload["courseOfferingId"] = it.asIdPayload() }
        return viewRows(
            ExamEndpoint.SubModules.INTERNAL_EXAM,
            ExamEndpoint.Actions.INTERNAL_EXAM,
            payload
        ).map { it.toInternalExam() }
    }

    override suspend fun addInternalExam(
        courseOfferingId: String,
        examName: String,
        examTypeId: String,
        examDate: String,
        totalMaxMarks: String
    ) {
        add(
            ExamEndpoint.SubModules.INTERNAL_EXAM,
            ExamEndpoint.Actions.INTERNAL_EXAM,
            mapOf(
                "courseOfferingId" to courseOfferingId.asIdPayload(),
                "examName" to examName,
                "examTypeId" to examTypeId.asIdPayload(),
                "examDate" to examDate,
                "totalMaxMarks" to (totalMaxMarks.toIntOrNull() ?: totalMaxMarks)
            )
        )
    }

    override suspend fun updateInternalExam(
        id: String,
        examName: String?,
        examDate: String?,
        totalMaxMarks: String?,
        status: String?
    ) {
        val payload = mutableMapOf<String, Any>("id" to id.asIdPayload())
        examName?.let { payload["examName"] = it }
        examDate?.let { payload["examDate"] = it }
        totalMaxMarks?.let { payload["totalMaxMarks"] = it.toIntOrNull() ?: it }
        status?.let { payload["status"] = it }
        update(ExamEndpoint.SubModules.INTERNAL_EXAM, ExamEndpoint.Actions.INTERNAL_EXAM, payload)
    }

    override suspend fun deleteInternalExam(id: String) {
        delete(
            ExamEndpoint.SubModules.INTERNAL_EXAM,
            ExamEndpoint.Actions.INTERNAL_EXAM,
            mapOf("id" to id.asIdPayload())
        )
    }

    override suspend fun getInternalMarksEntries(internalExamId: String?): List<MarksRecord> {
        val payload = mutableMapOf<String, Any>()
        internalExamId?.let { payload["internalExamId"] = it.asIdPayload() }
        return viewRows(
            ExamEndpoint.SubModules.INTERNAL_EXAM,
            ExamEndpoint.Actions.INTERNAL_MARKS_ENTRY,
            payload
        ).map { it.toMarksRecord() }
    }

    override suspend fun submitInternalMarksEntry(
        internalExamId: String,
        records: List<Pair<String, String>>
    ) {
        add(
            ExamEndpoint.SubModules.INTERNAL_EXAM,
            ExamEndpoint.Actions.INTERNAL_MARKS_ENTRY,
            mapOf(
                "internalExamId" to internalExamId.asIdPayload(),
                "records" to records.map { (studentId, marks) ->
                    mapOf("studentId" to studentId.asIdPayload(), "marks" to (marks.toDoubleOrNull() ?: marks))
                }
            )
        )
    }

    override suspend fun consolidateInternalMarks(courseOfferingId: String) {
        add(
            ExamEndpoint.SubModules.INTERNAL_EXAM,
            ExamEndpoint.Actions.INTERNAL_MARKS_CONSOLIDATION,
            mapOf("courseOfferingId" to courseOfferingId.asIdPayload())
        )
    }

    // ==================== Admin: sm_marks ====================

    override suspend fun getMarksEntries(scheduleId: String?, courseId: String?): List<MarksRecord> {
        val payload = mutableMapOf<String, Any>()
        scheduleId?.let { payload["scheduleId"] = it.asIdPayload() }
        courseId?.let { payload["courseId"] = it.asIdPayload() }
        return viewRows(
            ExamEndpoint.SubModules.MARKS,
            ExamEndpoint.Actions.MARKS_ENTRY,
            payload
        ).map { it.toMarksRecord() }
    }

    override suspend fun submitMarksEntry(
        scheduleId: String,
        courseId: String,
        records: List<Pair<String, String>>
    ) {
        add(
            ExamEndpoint.SubModules.MARKS,
            ExamEndpoint.Actions.MARKS_ENTRY,
            mapOf(
                "scheduleId" to scheduleId.asIdPayload(),
                "courseId" to courseId.asIdPayload(),
                "records" to records.map { (studentId, marks) ->
                    mapOf("studentId" to studentId.asIdPayload(), "marks" to (marks.toDoubleOrNull() ?: marks))
                }
            )
        )
    }

    override suspend fun verifyMarks(scheduleId: String, courseId: String) {
        add(
            ExamEndpoint.SubModules.MARKS,
            ExamEndpoint.Actions.MARKS_VERIFICATION,
            mapOf("scheduleId" to scheduleId.asIdPayload(), "courseId" to courseId.asIdPayload())
        )
    }

    override suspend fun getGrades(scheduleId: String?): List<Grade> {
        val payload = mutableMapOf<String, Any>()
        scheduleId?.let { payload["scheduleId"] = it.asIdPayload() }
        return viewRows(
            ExamEndpoint.SubModules.MARKS,
            ExamEndpoint.Actions.GRADE,
            payload
        ).map { it.toGrade() }
    }

    override suspend fun generateGrades(scheduleId: String, courseId: String) {
        add(
            ExamEndpoint.SubModules.MARKS,
            ExamEndpoint.Actions.GRADE,
            mapOf("scheduleId" to scheduleId.asIdPayload(), "courseId" to courseId.asIdPayload())
        )
    }

    override suspend fun lockGrades(scheduleId: String, courseId: String) {
        add(
            ExamEndpoint.SubModules.MARKS,
            ExamEndpoint.Actions.GRADE_LOCK,
            mapOf("scheduleId" to scheduleId.asIdPayload(), "courseId" to courseId.asIdPayload())
        )
    }

    // ==================== Admin: sm_evaluation ====================

    override suspend fun getEvaluationBundles(scheduleId: String?): List<EvaluationBundle> {
        val payload = mutableMapOf<String, Any>()
        scheduleId?.let { payload["scheduleId"] = it.asIdPayload() }
        return viewRows(
            ExamEndpoint.SubModules.EVALUATION,
            ExamEndpoint.Actions.EVALUATION_BUNDLE,
            payload
        ).map { it.toEvaluationBundle() }
    }

    override suspend fun addEvaluationBundle(
        scheduleId: String,
        courseId: String,
        evaluatorId: String,
        studentIds: List<String>
    ) {
        add(
            ExamEndpoint.SubModules.EVALUATION,
            ExamEndpoint.Actions.EVALUATION_BUNDLE,
            mapOf(
                "scheduleId" to scheduleId.asIdPayload(),
                "courseId" to courseId.asIdPayload(),
                "evaluatorId" to evaluatorId.asIdPayload(),
                "studentIds" to studentIds.map { it.asIdPayload() }
            )
        )
    }

    override suspend fun updateEvaluationBundle(id: String, status: String?) {
        val payload = mutableMapOf<String, Any>("id" to id.asIdPayload())
        status?.let { payload["status"] = it }
        update(ExamEndpoint.SubModules.EVALUATION, ExamEndpoint.Actions.EVALUATION_BUNDLE, payload)
    }

    override suspend fun deleteEvaluationBundle(id: String) {
        delete(
            ExamEndpoint.SubModules.EVALUATION,
            ExamEndpoint.Actions.EVALUATION_BUNDLE,
            mapOf("id" to id.asIdPayload())
        )
    }

    override suspend fun requestSecondValuation(scheduleId: String, courseId: String, studentId: String) {
        add(
            ExamEndpoint.SubModules.EVALUATION,
            ExamEndpoint.Actions.SECOND_VALUATION,
            mapOf(
                "scheduleId" to scheduleId.asIdPayload(),
                "courseId" to courseId.asIdPayload(),
                "studentId" to studentId.asIdPayload()
            )
        )
    }

    override suspend fun applyModeration(scheduleId: String, courseId: String, adjustment: String) {
        add(
            ExamEndpoint.SubModules.EVALUATION,
            ExamEndpoint.Actions.MODERATION,
            mapOf(
                "scheduleId" to scheduleId.asIdPayload(),
                "courseId" to courseId.asIdPayload(),
                "adjustment" to (adjustment.toDoubleOrNull() ?: adjustment)
            )
        )
    }

    // ==================== Admin: sm_results / sm_resultApproval ====================

    override suspend fun generateResults(scheduleId: String) {
        add(
            ExamEndpoint.SubModules.RESULTS,
            ExamEndpoint.Actions.RESULT,
            mapOf("scheduleId" to scheduleId.asIdPayload())
        )
    }

    /** The contract documents `resultApproval/add`'s response as free text -
     * `"Results approved at level 'ADMIN'. Chain complete: False"` - not a structured
     * row, so the returned status is best-effort mapped and falls back to what the
     * caller already knows when the response carries nothing further. */
    override suspend fun approveResults(
        scheduleId: String,
        level: String,
        remarks: String?
    ): ResultApprovalStatus {
        val payload = mutableMapOf<String, Any>(
            "scheduleId" to scheduleId.asIdPayload(),
            "level" to level
        )
        remarks?.let { payload["remarks"] = it }
        val data = apiClient.request<JsonElement?>(
            module = ExamEndpoint.MODULE,
            submodule = ExamEndpoint.SubModules.RESULT_APPROVAL,
            action = ExamEndpoint.Actions.RESULT_APPROVAL,
            actionType = ExamEndpoint.ActionTypes.ADD,
            payload = payload
        )
        val row = JsonRowUtils.asRows(data).firstOrNull()
        return row?.toResultApprovalStatus()
            ?: ResultApprovalStatus(scheduleId, level, null, remarks)
    }

    override suspend fun rejectResults(scheduleId: String, level: String, remarks: String?) {
        val payload = mutableMapOf<String, Any>(
            "scheduleId" to scheduleId.asIdPayload(),
            "level" to level
        )
        remarks?.let { payload["remarks"] = it }
        delete(ExamEndpoint.SubModules.RESULT_APPROVAL, ExamEndpoint.Actions.RESULT_APPROVAL, payload)
    }

    override suspend fun publishResults(scheduleId: String) {
        act(
            ExamEndpoint.SubModules.RESULT_APPROVAL,
            ExamEndpoint.Actions.RESULT_PUBLICATION,
            ExamEndpoint.ActionTypes.PUBLISH,
            mapOf("scheduleId" to scheduleId.asIdPayload())
        )
    }

    override suspend fun holdResultPublication(scheduleId: String) {
        act(
            ExamEndpoint.SubModules.RESULT_APPROVAL,
            ExamEndpoint.Actions.RESULT_PUBLICATION,
            ExamEndpoint.ActionTypes.HOLD,
            mapOf("scheduleId" to scheduleId.asIdPayload())
        )
    }

    override suspend fun unpublishResults(scheduleId: String) {
        act(
            ExamEndpoint.SubModules.RESULT_APPROVAL,
            ExamEndpoint.Actions.RESULT_PUBLICATION,
            ExamEndpoint.ActionTypes.UNPUBLISH,
            mapOf("scheduleId" to scheduleId.asIdPayload())
        )
    }

    // ==================== Admin actions folded into student screens ====================

    override suspend fun processRevaluation(revaluationRequestId: String, evaluatorId: String) {
        add(
            ExamEndpoint.SubModules.REVALUATION,
            ExamEndpoint.Actions.REVALUATION_PROCESSING,
            mapOf(
                "revaluationRequestId" to revaluationRequestId.asIdPayload(),
                "evaluatorId" to evaluatorId.asIdPayload()
            )
        )
    }

    override suspend fun createSupplementaryExam(
        name: String,
        fromDate: String,
        toDate: String,
        courseIds: List<String>
    ) {
        add(
            ExamEndpoint.SubModules.SUPPLEMENTARY,
            ExamEndpoint.Actions.SUPPLEMENTARY_EXAM,
            mapOf(
                "examName" to name,
                "fromDate" to fromDate,
                "toDate" to toDate,
                "courseIds" to courseIds.map { it.asIdPayload() }
            )
        )
    }

    override suspend fun createReappearExam(
        name: String,
        academicTermId: String,
        examStartDate: String,
        examEndDate: String,
        registrationStartDate: String,
        registrationEndDate: String,
        courseIds: List<String>
    ) {
        add(
            ExamEndpoint.SubModules.REAPPEAR,
            ExamEndpoint.Actions.REAPPEAR_EXAM,
            mapOf(
                "examName" to name,
                "academicTermId" to academicTermId.asIdPayload(),
                "examStartDate" to examStartDate,
                "examEndDate" to examEndDate,
                "registrationStartDate" to registrationStartDate,
                "registrationEndDate" to registrationEndDate,
                "courseIds" to courseIds.map { it.asIdPayload() }
            )
        )
    }

    override suspend fun setReappearEligibility(
        studentId: String,
        courseId: String,
        reappearExamId: String,
        isEligible: Boolean,
        reason: String?
    ) {
        val payload = mutableMapOf<String, Any>(
            "studentId" to studentId.asIdPayload(),
            "courseId" to courseId.asIdPayload(),
            "reappearExamId" to reappearExamId.asIdPayload(),
            "isEligible" to isEligible
        )
        reason?.let { payload["reason"] = it }
        add(ExamEndpoint.SubModules.REAPPEAR, ExamEndpoint.Actions.REAPPEAR_ELIGIBILITY, payload)
    }

    override suspend fun reportMalpracticeCase(studentId: String, scheduleId: String, description: String) {
        add(
            ExamEndpoint.SubModules.MALPRACTICE,
            ExamEndpoint.Actions.MALPRACTICE_CASE,
            mapOf(
                "studentId" to studentId.asIdPayload(),
                "scheduleId" to scheduleId.asIdPayload(),
                "description" to description,
                "reportedBy" to requireStudentId()
            )
        )
    }

    override suspend fun recordMalpracticeVerdict(
        caseId: String,
        committee: String?,
        hearingDate: String?,
        verdict: String
    ) {
        val payload = mutableMapOf<String, Any>(
            "id" to caseId.asIdPayload(),
            "verdict" to verdict
        )
        committee?.let { payload["committee"] = it }
        hearingDate?.let { payload["hearingDate"] = it }
        update(ExamEndpoint.SubModules.MALPRACTICE, ExamEndpoint.Actions.MALPRACTICE_CASE, payload)
    }

    override suspend fun dismissMalpracticeCase(caseId: String) {
        delete(
            ExamEndpoint.SubModules.MALPRACTICE,
            ExamEndpoint.Actions.MALPRACTICE_CASE,
            mapOf("id" to caseId.asIdPayload())
        )
    }
}

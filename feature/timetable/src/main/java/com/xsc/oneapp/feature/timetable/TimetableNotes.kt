package com.xsc.oneapp.feature.timetable

// Scope decision (2026-07-30, explicit user choice, same as feature/fee): build all
// 8 sub-modules - workingDay, timeSlot, academicCalendar, timetable, facultyAllocation,
// roomAllocation, substitution, timetableApproval - covering Registrar/Institution
// Admin, Department HOD, Timetable Coordinator, Academic Council/Dean, Faculty, and
// Student surfaces, gated dynamically by role at login.
//
// Contract confirmed 2026-07-31 (gateway routing table + real response examples) -
// see TimetableEndpoint.kt for the subMod/action mapping and each domain model's
// doc comment for its confirmed field shape. Read/view data layer built for all 8;
// no add/update/delete, no screens yet (matches feature/fee/feature/curriculum's
// same read-first precedent).
//
// Three things worth remembering from that confirmation:
//   1. Raw ID resolution: courseId/facId/roomId/timeSlotId on TimetableEntry are
//      never resolved to display names by this endpoint. roomId specifically has
//      no master-list endpoint in m_timetable at all - it lives in m_infra, which
//      isn't wired up anywhere in this app. Until it is, any UI showing a
//      TimetableEntry can only show the raw room ID, not a room name.
//   2. RBAC is permission-driven, not role-driven: the JWT's "permissions" array
//      (e.g. "timetable.timetable.view") is the real access-control signal, not
//      the role_code string. SessionManager.currentPermissions now decodes that
//      array (added 2026-07-31) alongside the existing currentRole. The confirmed
//      role_code seed values, for when currentRole is what's actually needed
//      (display, coarse role-based UI branching):
//        Registrar/Admin: "ADMIN", "director", "principal"
//        Timetable Coordinator: "timetable_coordinator"
//        Department HOD: "hod"
//        Academic Council/Dean: "academic_council_member", "dean"
//        Faculty: "TEACHER", "faculty"
//        Student: "STUDENT"
//      Actual per-screen permission gating (deciding which of the 8 sub-modules a
//      signed-in user's UI should show/allow) is not built yet - that's a
//      ViewModel/screen-level concern for whenever the screens themselves get
//      built, and should check currentPermissions, not currentRole.
//   3. academicCalendar/view returns a single proxy-metadata object, not a row
//      array - see AcademicCalendar.kt. It explicitly is not the real academic
//      calendar; it's timetable's local proxy for term data owned by the academic
//      structure module.
internal object TimetableNotes

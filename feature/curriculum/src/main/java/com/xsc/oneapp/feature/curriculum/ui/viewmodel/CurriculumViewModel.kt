package com.xsc.oneapp.feature.curriculum.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.result.SectionLoader
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.curriculum.domain.model.Course
import com.xsc.oneapp.feature.curriculum.domain.model.Programme
import com.xsc.oneapp.feature.curriculum.domain.model.Syllabus
import com.xsc.oneapp.feature.curriculum.domain.usecase.GetCoursesUseCase
import com.xsc.oneapp.feature.curriculum.domain.usecase.GetProgrammesUseCase
import com.xsc.oneapp.feature.curriculum.domain.usecase.GetSyllabusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * One [SectionLoader] per curriculum sub-module, matching the Timetable/Attendance/Fee
 * pattern (see `SectionLoader` kdoc).
 *
 * Previously this ViewModel only called [GetProgrammesUseCase] - [GetCoursesUseCase] and
 * [GetSyllabusUseCase] existed, were unit-tested, and were never invoked from any screen.
 * The Curriculum tab therefore only ever showed programmes, silently dropping the course
 * catalog and syllabus entries the backend already exposes.
 *
 * Courses and Syllabus are surfaced as independent sections rather than nested under a
 * given Programme: `Course` carries no `programmeId`/`progId` field and `Syllabus.programmeId`
 * is sourced from a different backend key (`prog_id`) than `Programme.code` (`prog_code`),
 * with no confirmed shared key between them (see CurriculumMapper.kt). Joining them client
 * side would be a guess; each section loads and fails independently instead, same as the
 * other multi-section screens in this app.
 */
@HiltViewModel
class CurriculumViewModel @Inject constructor(
    getProgrammesUseCase: GetProgrammesUseCase,
    getCoursesUseCase: GetCoursesUseCase,
    getSyllabusUseCase: GetSyllabusUseCase
) : ViewModel() {

    private val programmes = SectionLoader(viewModelScope) { getProgrammesUseCase() }
    private val courses = SectionLoader(viewModelScope) { getCoursesUseCase() }
    private val syllabus = SectionLoader(viewModelScope) { getSyllabusUseCase() }

    val programmesState: StateFlow<UiState<List<Programme>>> = programmes.state
    val coursesState: StateFlow<UiState<List<Course>>> = courses.state
    val syllabusState: StateFlow<UiState<List<Syllabus>>> = syllabus.state

    init {
        // Tab 0 (Programmes) is visible immediately on entry, so load it eagerly.
        // Courses/Syllabus load lazily via onTabSelected when the user actually opens them.
        programmes.loadOnce()
    }

    /** Indices match CurriculumScreen's TAB_TITLES order. */
    fun onTabSelected(index: Int) {
        when (index) {
            0 -> programmes.loadOnce()
            1 -> courses.loadOnce()
            2 -> syllabus.loadOnce()
        }
    }

    fun loadProgrammes() = programmes.reload()
    fun loadCourses() = courses.reload()
    fun loadSyllabus() = syllabus.reload()
}

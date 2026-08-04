package com.xsc.oneapp.feature.curriculum.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.curriculum.domain.model.Course
import com.xsc.oneapp.feature.curriculum.domain.model.Programme
import com.xsc.oneapp.feature.curriculum.domain.model.Syllabus
import com.xsc.oneapp.feature.curriculum.ui.viewmodel.CurriculumViewModel
import com.xsc.sdk.commonui.record.DetailText
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.sdk.commonui.record.RecordCard
import com.xsc.sdk.commonui.record.RecordRow
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.commonui.record.SectionChips
import com.xsc.sdk.commonui.record.StatusPill
import com.xsc.sdk.theme.LocalSpacing

private val TAB_TITLES = listOf("Programmes", "Courses", "Syllabus")

/**
 * Three independent sections - Programmes, Courses, Syllabus - matching the tab pattern
 * used by Fee/Timetable. Previously this screen only rendered Programmes; GetCoursesUseCase
 * and GetSyllabusUseCase were wired in the domain/data layers and unit-tested but never
 * called from any screen, so the course catalog and syllabus entries were unreachable in
 * the app. See CurriculumViewModel for why the sections aren't nested (no confirmed
 * Programme<->Course/Syllabus join key from the backend contract).
 */
@Composable
fun CurriculumScreen(
    onBack: () -> Unit,
    viewModel: CurriculumViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val programmesState by viewModel.programmesState.collectAsStateWithLifecycle()
    val coursesState by viewModel.coursesState.collectAsStateWithLifecycle()
    val syllabusState by viewModel.syllabusState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedTab) { viewModel.onTabSelected(selectedTab) }

    RecordScaffold(title = "Curriculum", onBack = onBack) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SectionChips(
                options = TAB_TITLES,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it }
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> ProgrammesTab(programmesState, viewModel::loadProgrammes)
                    1 -> CoursesTab(coursesState, viewModel::loadCourses)
                    2 -> SyllabusTab(syllabusState, viewModel::loadSyllabus)
                }
            }
        }
    }
}

@Composable
private fun ProgrammesTab(state: UiState<List<Programme>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No programmes published yet.")
        } else {
            ProgrammeList(state.data)
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

@Composable
private fun ProgrammeList(programmes: List<Programme>) {
    val spacing = LocalSpacing.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        // Keyed on the programme's own id where present. `hashCode()` on an
        // all-nullable model collides between rows that differ only in a null field,
        // which makes Compose reuse the wrong item state on update.
        items(programmes, key = { it.code ?: it.hashCode() }) { programme ->
            ResponsiveContent {
                RecordCard {
                    RecordRow(
                        icon = Icons.Default.School,
                        title = programme.name,
                        trailing = programme.code?.let { code -> { StatusPill(code) } }
                    )

                    val summary = listOfNotNull(
                        programme.totalTerms?.let { "$it terms" },
                        programme.totalCredits?.let { "$it credits" }
                    ).joinToString(" · ")

                    if (summary.isNotBlank()) {
                        DetailText(summary, modifier = Modifier.padding(top = spacing.md))
                    }
                }
            }
        }
    }
}

@Composable
private fun CoursesTab(state: UiState<List<Course>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No courses published yet.")
        } else {
            CourseList(state.data)
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

@Composable
private fun CourseList(courses: List<Course>) {
    val spacing = LocalSpacing.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        items(courses, key = { it.id ?: it.code ?: it.hashCode() }) { course ->
            ResponsiveContent {
                RecordCard {
                    RecordRow(
                        icon = Icons.Default.Book,
                        title = course.name ?: "Course",
                        trailing = course.code?.let { code -> { StatusPill(code) } }
                    )

                    val summary = listOfNotNull(
                        course.creditValue?.let { "$it credits" },
                        course.lectureHours?.let { "L $it" },
                        course.tutorialHours?.let { "T $it" },
                        course.practicalHours?.let { "P $it" }
                    ).joinToString(" · ")

                    if (summary.isNotBlank()) {
                        DetailText(summary, modifier = Modifier.padding(top = spacing.md))
                    }
                }
            }
        }
    }
}

@Composable
private fun SyllabusTab(state: UiState<List<Syllabus>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No syllabus entries published yet.")
        } else {
            SyllabusList(state.data)
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

@Composable
private fun SyllabusList(entries: List<Syllabus>) {
    val spacing = LocalSpacing.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        items(entries, key = { it.id ?: it.hashCode() }) { entry ->
            ResponsiveContent {
                RecordCard {
                    RecordRow(
                        icon = Icons.Default.MenuBook,
                        title = entry.name ?: "Syllabus entry",
                        trailing = entry.programmeId?.let { id -> { StatusPill(id) } }
                    )
                }
            }
        }
    }
}

package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ExamScheduleAdminUseCasesTest {

    @Test
    fun `AddExamScheduleUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery {
            repository.addExamSchedule("Midterm", "REGULAR", "2026-06-01", "2026-06-15", "2026-06-01", "MORNING")
        } just Runs

        AddExamScheduleUseCase(repository)("Midterm", "REGULAR", "2026-06-01", "2026-06-15", "2026-06-01", "MORNING")

        coVerify(exactly = 1) {
            repository.addExamSchedule("Midterm", "REGULAR", "2026-06-01", "2026-06-15", "2026-06-01", "MORNING")
        }
    }

    @Test
    fun `UpdateExamScheduleUseCase forwards scheduleId and updated fields`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.updateExamSchedule("1", "Midterm Exam", "2026-06-02", "2026-06-16") } just Runs

        UpdateExamScheduleUseCase(repository)("1", "Midterm Exam", "2026-06-02", "2026-06-16")

        coVerify(exactly = 1) { repository.updateExamSchedule("1", "Midterm Exam", "2026-06-02", "2026-06-16") }
    }

    @Test
    fun `DeleteExamScheduleUseCase forwards scheduleId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.deleteExamSchedule("1") } just Runs

        DeleteExamScheduleUseCase(repository)("1")

        coVerify(exactly = 1) { repository.deleteExamSchedule("1") }
    }

    @Test
    fun `PublishExamScheduleUseCase forwards scheduleId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.publishExamSchedule("1") } just Runs

        PublishExamScheduleUseCase(repository)("1")

        coVerify(exactly = 1) { repository.publishExamSchedule("1") }
    }

    @Test
    fun `HoldExamScheduleUseCase forwards scheduleId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.holdExamSchedule("1") } just Runs

        HoldExamScheduleUseCase(repository)("1")

        coVerify(exactly = 1) { repository.holdExamSchedule("1") }
    }

    @Test
    fun `UnpublishExamScheduleUseCase forwards scheduleId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.unpublishExamSchedule("1") } just Runs

        UnpublishExamScheduleUseCase(repository)("1")

        coVerify(exactly = 1) { repository.unpublishExamSchedule("1") }
    }
}

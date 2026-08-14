package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.QuestionBankEntry
import com.xsc.oneapp.feature.exam.domain.model.QuestionPaper
import com.xsc.oneapp.feature.exam.domain.model.QuestionPaperSubmission
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionPaperUseCasesTest {

    @Test
    fun `GetQuestionPapersUseCase returns papers for the given schedule`() = runTest {
        val repository = mockk<ExamRepository>()
        val papers = listOf(QuestionPaper("1", "1", "101", "50", "Answer all questions", "DRAFT"))
        coEvery { repository.getQuestionPapers("1") } returns papers

        val result = GetQuestionPapersUseCase(repository)("1")

        assertEquals(papers, result)
    }

    @Test
    fun `AddQuestionPaperUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.addQuestionPaper("1", "101", "Answer all questions") } just Runs

        AddQuestionPaperUseCase(repository)("1", "101", "Answer all questions")

        coVerify(exactly = 1) { repository.addQuestionPaper("1", "101", "Answer all questions") }
    }

    @Test
    fun `UpdateQuestionPaperUseCase forwards id and updated fields`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.updateQuestionPaper("1", "Answer any four questions", "SUBMITTED") } just Runs

        UpdateQuestionPaperUseCase(repository)("1", "Answer any four questions", "SUBMITTED")

        coVerify(exactly = 1) { repository.updateQuestionPaper("1", "Answer any four questions", "SUBMITTED") }
    }

    @Test
    fun `DeleteQuestionPaperUseCase forwards id`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.deleteQuestionPaper("1") } just Runs

        DeleteQuestionPaperUseCase(repository)("1")

        coVerify(exactly = 1) { repository.deleteQuestionPaper("1") }
    }

    @Test
    fun `GetQuestionPaperSubmissionsUseCase returns submissions for the given paper`() = runTest {
        val repository = mockk<ExamRepository>()
        val submissions = listOf(QuestionPaperSubmission("1", "1", "SUBMITTED", "2026-06-01"))
        coEvery { repository.getQuestionPaperSubmissions("1") } returns submissions

        val result = GetQuestionPaperSubmissionsUseCase(repository)("1")

        assertEquals(submissions, result)
    }

    @Test
    fun `SubmitQuestionPaperUseCase forwards questionPaperId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.submitQuestionPaper("1") } just Runs

        SubmitQuestionPaperUseCase(repository)("1")

        coVerify(exactly = 1) { repository.submitQuestionPaper("1") }
    }

    @Test
    fun `DeleteQuestionPaperSubmissionUseCase forwards id`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.deleteQuestionPaperSubmission("1") } just Runs

        DeleteQuestionPaperSubmissionUseCase(repository)("1")

        coVerify(exactly = 1) { repository.deleteQuestionPaperSubmission("1") }
    }

    @Test
    fun `ApproveQuestionPaperUseCase forwards questionPaperId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.approveQuestionPaper("1") } just Runs

        ApproveQuestionPaperUseCase(repository)("1")

        coVerify(exactly = 1) { repository.approveQuestionPaper("1") }
    }

    @Test
    fun `RejectQuestionPaperUseCase forwards questionPaperId and reason`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.rejectQuestionPaper("1", "Incomplete syllabus coverage") } just Runs

        RejectQuestionPaperUseCase(repository)("1", "Incomplete syllabus coverage")

        coVerify(exactly = 1) { repository.rejectQuestionPaper("1", "Incomplete syllabus coverage") }
    }

    @Test
    fun `GetQuestionBankUseCase returns entries for the given course`() = runTest {
        val repository = mockk<ExamRepository>()
        val entries = listOf(QuestionBankEntry("1", "101", "1", "Explain Newton's laws of motion"))
        coEvery { repository.getQuestionBank("101") } returns entries

        val result = GetQuestionBankUseCase(repository)("101")

        assertEquals(entries, result)
    }

    @Test
    fun `AddQuestionBankEntryUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.addQuestionBankEntry("101", "Explain Newton's laws of motion", "1") } just Runs

        AddQuestionBankEntryUseCase(repository)("101", "Explain Newton's laws of motion", "1")

        coVerify(exactly = 1) { repository.addQuestionBankEntry("101", "Explain Newton's laws of motion", "1") }
    }

    @Test
    fun `UpdateQuestionBankEntryUseCase forwards id and content`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.updateQuestionBankEntry("1", "Explain Newton's three laws of motion") } just Runs

        UpdateQuestionBankEntryUseCase(repository)("1", "Explain Newton's three laws of motion")

        coVerify(exactly = 1) { repository.updateQuestionBankEntry("1", "Explain Newton's three laws of motion") }
    }

    @Test
    fun `DeleteQuestionBankEntryUseCase forwards id`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.deleteQuestionBankEntry("1") } just Runs

        DeleteQuestionBankEntryUseCase(repository)("1")

        coVerify(exactly = 1) { repository.deleteQuestionBankEntry("1") }
    }
}

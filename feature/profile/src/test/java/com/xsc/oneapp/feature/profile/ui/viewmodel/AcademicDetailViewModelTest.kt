package com.xsc.oneapp.feature.profile.ui.viewmodel

import com.xsc.oneapp.feature.profile.domain.model.AcademicDetail
import com.xsc.oneapp.feature.profile.domain.usecase.GetAcademicDetailUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.UpdateAcademicIdentifiersUseCase
import com.xsc.oneapp.feature.profile.ui.state.AcademicDetailEvent
import com.xsc.oneapp.feature.profile.ui.state.AcademicDetailState
import com.xsc.sdk.network.APIError
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AcademicDetailViewModelTest {

    private lateinit var getAcademicDetailUseCase: GetAcademicDetailUseCase
    private lateinit var updateAcademicIdentifiersUseCase: UpdateAcademicIdentifiersUseCase

    private val academicDetail = AcademicDetail(
        identifiers = emptyList(), enrollmentNumber = "ENR-001", employeeCode = "", studentId = "77"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getAcademicDetailUseCase = mockk()
        updateAcademicIdentifiersUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = AcademicDetailViewModel(getAcademicDetailUseCase, updateAcademicIdentifiersUseCase)

    @Test
    fun `loading academic detail surfaces a Success state`() = runTest {
        coEvery { getAcademicDetailUseCase() } returns academicDetail
        val vm = viewModel()

        vm.onEvent(AcademicDetailEvent.LoadAcademicDetail)

        val state = vm.state.value as AcademicDetailState.Success
        assertEquals(academicDetail, state.academicDetail)
    }

    @Test
    fun `a business error from the use case maps to BusinessError state`() = runTest {
        coEvery { getAcademicDetailUseCase() } throws APIError.BusinessError("NOT_FOUND", "Not found")
        val vm = viewModel()

        vm.onEvent(AcademicDetailEvent.LoadAcademicDetail)

        assertTrue(vm.state.value is AcademicDetailState.BusinessError)
    }

    @Test
    fun `saving identifiers reloads the detail on success`() = runTest {
        coEvery { getAcademicDetailUseCase() } returns academicDetail
        coEvery { updateAcademicIdentifiersUseCase(idCardNumber = "ID-1", biometricId = "BIO-1") } just Runs
        val vm = viewModel()

        vm.onEvent(AcademicDetailEvent.SaveAcademicIdentifiers("ID-1", "BIO-1"))

        assertTrue(vm.state.value is AcademicDetailState.Success)
    }
}

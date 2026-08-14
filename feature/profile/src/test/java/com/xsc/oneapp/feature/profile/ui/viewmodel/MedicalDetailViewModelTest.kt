package com.xsc.oneapp.feature.profile.ui.viewmodel

import com.xsc.oneapp.feature.profile.domain.model.MedicalDetail
import com.xsc.oneapp.feature.profile.domain.usecase.GetMedicalDetailUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.UpdateMedicalDetailUseCase
import com.xsc.oneapp.feature.profile.ui.state.MedicalDetailEvent
import com.xsc.oneapp.feature.profile.ui.state.MedicalDetailState
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
class MedicalDetailViewModelTest {

    private lateinit var getMedicalDetailUseCase: GetMedicalDetailUseCase
    private lateinit var updateMedicalDetailUseCase: UpdateMedicalDetailUseCase

    private val medicalDetail = MedicalDetail(
        bloodGroupId = 1, bloodGroup = "A+", allergies = emptyList(), chronicConditions = emptyList(),
        medications = emptyList(), height = 170.0, weight = 65.0, disabilityTypeId = null,
        disabilityType = "", doctorName = "Dr. Smith", doctorContact = "9999999999",
        insurancePolicyNo = "POLICY-1"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getMedicalDetailUseCase = mockk()
        updateMedicalDetailUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = MedicalDetailViewModel(getMedicalDetailUseCase, updateMedicalDetailUseCase)

    @Test
    fun `loading a present medical detail surfaces a Success state`() = runTest {
        coEvery { getMedicalDetailUseCase(null) } returns medicalDetail
        val vm = viewModel()

        vm.onEvent(MedicalDetailEvent.LoadMedicalDetail)

        val state = vm.state.value as MedicalDetailState.Success
        assertEquals(medicalDetail, state.medicalDetail)
    }

    @Test
    fun `a null medical detail surfaces Empty state`() = runTest {
        coEvery { getMedicalDetailUseCase(null) } returns null
        val vm = viewModel()

        vm.onEvent(MedicalDetailEvent.LoadMedicalDetail)

        assertTrue(vm.state.value is MedicalDetailState.Empty)
    }

    @Test
    fun `saving reloads the detail on success`() = runTest {
        coEvery { getMedicalDetailUseCase(null) } returns medicalDetail
        coEvery { updateMedicalDetailUseCase(fieldsToUpdate = mapOf("weight" to 68.0)) } just Runs
        val vm = viewModel()

        vm.onEvent(MedicalDetailEvent.SaveMedicalDetail(mapOf("weight" to 68.0)))

        assertTrue(vm.state.value is MedicalDetailState.Success)
    }
}

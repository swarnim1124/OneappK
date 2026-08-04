package com.xsc.oneapp.feature.profile.ui.viewmodel

import com.xsc.oneapp.feature.profile.domain.model.FamilyDetail
import com.xsc.oneapp.feature.profile.domain.model.PersonalDetail
import com.xsc.oneapp.feature.profile.domain.usecase.DeleteFamilyDetailUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.GetFamilyDetailUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.GetPersonalDetailUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.UpdateFamilyDetailUseCase
import com.xsc.oneapp.feature.profile.ui.state.FamilyDetailEvent
import com.xsc.oneapp.feature.profile.ui.state.FamilyDetailState
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
class FamilyDetailViewModelTest {

    private lateinit var getFamilyDetailUseCase: GetFamilyDetailUseCase
    private lateinit var updateFamilyDetailUseCase: UpdateFamilyDetailUseCase
    private lateinit var deleteFamilyDetailUseCase: DeleteFamilyDetailUseCase
    private lateinit var getPersonalDetailUseCase: GetPersonalDetailUseCase

    private val familyDetail = FamilyDetail(
        id = 1, firstName = "Parent", middleName = "", lastName = "One", genderId = null, dob = null,
        mobileNo = "9999999999", email = "parent@oneapp.local", occupation = "Engineer", annualIncome = 0.0,
        photoDocId = null, statusId = 1, relationshipTypeId = 1, isPrimaryGuardian = true, isEmergencyContact = true
    )

    /** userId ("3") deliberately differs from studentId ("77") - proves the
     * ViewModel resolves the real studentId via personalDetail.view rather than
     * assuming they're the same (confirmed 2026-07-31 that they never are). */
    private fun personalDetail(studentId: String?) = PersonalDetail(
        userId = "3", studentId = studentId, email = "student@oneapp.local", alternateEmail = "", mobile = "9999999999",
        firstName = "Student", middleName = "", lastName = "One", dob = "2000-01-01", genderId = 1,
        photoDocId = null, nationalityId = null, primaryLangId = null, bloodGroupId = null,
        createdAt = "2026-01-01", updatedAt = "2026-01-01"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getFamilyDetailUseCase = mockk()
        updateFamilyDetailUseCase = mockk()
        deleteFamilyDetailUseCase = mockk()
        getPersonalDetailUseCase = mockk()
        coEvery { getPersonalDetailUseCase() } returns personalDetail(studentId = "77")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = FamilyDetailViewModel(
        getFamilyDetailUseCase,
        updateFamilyDetailUseCase,
        deleteFamilyDetailUseCase,
        getPersonalDetailUseCase
    )

    @Test
    fun `loading family detail resolves studentId from personalDetail, not the session's userId`() = runTest {
        coEvery { getFamilyDetailUseCase("77") } returns listOf(familyDetail)
        val vm = viewModel()

        vm.onEvent(FamilyDetailEvent.LoadFamilyDetail)

        val state = vm.state.value as FamilyDetailState.Success
        assertEquals(listOf(familyDetail), state.familyDetails)
    }

    @Test
    fun `an empty family list surfaces Empty state`() = runTest {
        coEvery { getFamilyDetailUseCase("77") } returns emptyList()
        val vm = viewModel()

        vm.onEvent(FamilyDetailEvent.LoadFamilyDetail)

        assertTrue(vm.state.value is FamilyDetailState.Empty)
    }

    @Test
    fun `no studentId on the account surfaces a BusinessError instead of calling the use case`() = runTest {
        coEvery { getPersonalDetailUseCase() } returns personalDetail(studentId = null)
        val vm = viewModel()

        vm.onEvent(FamilyDetailEvent.LoadFamilyDetail)

        assertTrue(vm.state.value is FamilyDetailState.BusinessError)
    }

    @Test
    fun `saving reloads the detail on success`() = runTest {
        coEvery { getFamilyDetailUseCase("77") } returns listOf(familyDetail)
        coEvery { updateFamilyDetailUseCase(1, mapOf("occupation" to "Doctor")) } just Runs
        val vm = viewModel()

        vm.onEvent(FamilyDetailEvent.SaveFamilyDetail(1, mapOf("occupation" to "Doctor")))

        assertTrue(vm.state.value is FamilyDetailState.Success)
    }

    @Test
    fun `deleting reloads the (now empty) detail list on success`() = runTest {
        coEvery { getFamilyDetailUseCase("77") } returns emptyList()
        coEvery { deleteFamilyDetailUseCase(1, "No longer a dependent") } just Runs
        val vm = viewModel()

        vm.onEvent(FamilyDetailEvent.DeleteFamilyDetail(1, "No longer a dependent"))

        assertTrue(vm.state.value is FamilyDetailState.Empty)
    }

    @Test
    fun `a failed delete surfaces a BusinessError instead of reloading`() = runTest {
        coEvery { deleteFamilyDetailUseCase(1, "reason") } throws APIError.BusinessError("NOT_FOUND", "Detail not found")
        val vm = viewModel()

        vm.onEvent(FamilyDetailEvent.DeleteFamilyDetail(1, "reason"))

        assertTrue(vm.state.value is FamilyDetailState.BusinessError)
    }
}

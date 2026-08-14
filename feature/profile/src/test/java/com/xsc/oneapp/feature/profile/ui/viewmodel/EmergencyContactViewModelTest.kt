package com.xsc.oneapp.feature.profile.ui.viewmodel

import com.xsc.oneapp.feature.profile.domain.model.EmergencyContact
import com.xsc.oneapp.feature.profile.domain.usecase.AddEmergencyContactUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.DeleteEmergencyContactUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.GetEmergencyContactUseCase
import com.xsc.oneapp.feature.profile.domain.usecase.UpdateEmergencyContactUseCase
import com.xsc.oneapp.feature.profile.ui.state.EmergencyContactEvent
import com.xsc.oneapp.feature.profile.ui.state.EmergencyContactState
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
class EmergencyContactViewModelTest {

    private lateinit var getEmergencyContactUseCase: GetEmergencyContactUseCase
    private lateinit var addEmergencyContactUseCase: AddEmergencyContactUseCase
    private lateinit var updateEmergencyContactUseCase: UpdateEmergencyContactUseCase
    private lateinit var deleteEmergencyContactUseCase: DeleteEmergencyContactUseCase

    private val contact = EmergencyContact(
        id = 1, firstName = "Jane", middleName = "", lastName = "Doe",
        mobile = "9999999999", email = "jane@oneapp.local", isPrimary = true, statusId = 1
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getEmergencyContactUseCase = mockk()
        addEmergencyContactUseCase = mockk()
        updateEmergencyContactUseCase = mockk()
        deleteEmergencyContactUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = EmergencyContactViewModel(
        getEmergencyContactUseCase,
        addEmergencyContactUseCase,
        updateEmergencyContactUseCase,
        deleteEmergencyContactUseCase
    )

    @Test
    fun `loading contacts surfaces a Success state`() = runTest {
        coEvery { getEmergencyContactUseCase(null) } returns listOf(contact)
        val vm = viewModel()

        vm.onEvent(EmergencyContactEvent.LoadEmergencyContacts)

        val state = vm.state.value as EmergencyContactState.Success
        assertEquals(listOf(contact), state.contacts)
    }

    @Test
    fun `no contacts surfaces Empty state`() = runTest {
        coEvery { getEmergencyContactUseCase(null) } returns emptyList()
        val vm = viewModel()

        vm.onEvent(EmergencyContactEvent.LoadEmergencyContacts)

        assertTrue(vm.state.value is EmergencyContactState.Empty)
    }

    @Test
    fun `a network error maps to NetworkError state`() = runTest {
        coEvery { getEmergencyContactUseCase(null) } throws APIError.NetworkError("timeout")
        val vm = viewModel()

        vm.onEvent(EmergencyContactEvent.LoadEmergencyContacts)

        assertTrue(vm.state.value is EmergencyContactState.NetworkError)
    }

    @Test
    fun `saving reloads contacts on success`() = runTest {
        coEvery { getEmergencyContactUseCase(null) } returns listOf(contact)
        coEvery { updateEmergencyContactUseCase(1, mapOf("mobile" to "8888888888")) } just Runs
        val vm = viewModel()

        vm.onEvent(EmergencyContactEvent.SaveEmergencyContact(1, mapOf("mobile" to "8888888888")))

        assertTrue(vm.state.value is EmergencyContactState.Success)
    }

    @Test
    fun `adding passes the entered fields straight through - repository resolves userId, not the ViewModel`() = runTest {
        coEvery { getEmergencyContactUseCase(null) } returns listOf(contact)
        coEvery { addEmergencyContactUseCase(mapOf("name" to "New Contact")) } just Runs
        val vm = viewModel()

        vm.onEvent(EmergencyContactEvent.AddEmergencyContact(mapOf("name" to "New Contact")))

        assertTrue(vm.state.value is EmergencyContactState.Success)
    }

    @Test
    fun `deleting reloads the (now empty) contact list on success`() = runTest {
        coEvery { getEmergencyContactUseCase(null) } returns emptyList()
        coEvery { deleteEmergencyContactUseCase(1) } just Runs
        val vm = viewModel()

        vm.onEvent(EmergencyContactEvent.DeleteEmergencyContact(1))

        assertTrue(vm.state.value is EmergencyContactState.Empty)
    }

    @Test
    fun `a failed delete surfaces a BusinessError instead of reloading`() = runTest {
        coEvery { deleteEmergencyContactUseCase(1) } throws APIError.BusinessError("NOT_FOUND", "Contact not found")
        val vm = viewModel()

        vm.onEvent(EmergencyContactEvent.DeleteEmergencyContact(1))

        assertTrue(vm.state.value is EmergencyContactState.BusinessError)
    }
}

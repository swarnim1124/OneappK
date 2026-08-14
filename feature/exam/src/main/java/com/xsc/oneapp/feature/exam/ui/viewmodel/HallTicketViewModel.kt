package com.xsc.oneapp.feature.exam.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.core.result.uiStateCatching
import com.xsc.oneapp.feature.exam.domain.model.HallTicket
import com.xsc.oneapp.feature.exam.domain.model.StudentExamBlock
import com.xsc.oneapp.feature.exam.domain.usecase.GetHallTicketUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetMyExamBlocksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The hall ticket plus any examination hold explaining its absence. */
data class HallTicketPage(
    val tickets: List<HallTicket>,
    val blocks: List<StudentExamBlock>
)

@HiltViewModel
class HallTicketViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getHallTicketUseCase: GetHallTicketUseCase,
    private val getMyExamBlocksUseCase: GetMyExamBlocksUseCase
) : ViewModel() {

    private val scheduleId: String = savedStateHandle.get<String>("scheduleId").orEmpty()

    private val _state = MutableStateFlow<UiState<HallTicketPage>>(UiState.Loading)
    val state: StateFlow<UiState<HallTicketPage>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = uiStateCatching(label = "hall ticket") {
                val tickets = getHallTicketUseCase(scheduleId)
                // Best-effort only. sm_hallTicket/studentBlock is a separate permission
                // from the ticket itself, so a student whose role can read one but not
                // the other must still get their hall ticket - a failure here degrades
                // to "no known block" rather than failing the whole screen.
                val blocks = runCatching { getMyExamBlocksUseCase(scheduleId) }.getOrDefault(emptyList())
                HallTicketPage(tickets = tickets, blocks = blocks)
            }
        }
    }
}

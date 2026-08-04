package com.xsc.oneapp.feature.exam.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.core.result.uiStateCatching
import com.xsc.oneapp.feature.exam.domain.model.HallTicket
import com.xsc.oneapp.feature.exam.domain.usecase.GetHallTicketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HallTicketViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getHallTicketUseCase: GetHallTicketUseCase
) : ViewModel() {

    private val scheduleId: String = savedStateHandle.get<String>("scheduleId").orEmpty()

    private val _state = MutableStateFlow<UiState<List<HallTicket>>>(UiState.Loading)
    val state: StateFlow<UiState<List<HallTicket>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = uiStateCatching { getHallTicketUseCase(scheduleId) }
        }
    }
}

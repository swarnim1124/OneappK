package com.xsc.oneapp.feature.exam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.core.result.uiStateCatching
import com.xsc.oneapp.feature.exam.domain.model.ExamSchedule
import com.xsc.oneapp.feature.exam.domain.usecase.GetExamSchedulesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val getExamSchedulesUseCase: GetExamSchedulesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<ExamSchedule>>>(UiState.Loading)
    val state: StateFlow<UiState<List<ExamSchedule>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = uiStateCatching { getExamSchedulesUseCase() }
        }
    }
}

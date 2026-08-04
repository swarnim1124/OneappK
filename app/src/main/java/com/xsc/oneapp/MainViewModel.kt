package com.xsc.oneapp

import androidx.lifecycle.ViewModel
import com.xsc.sdk.auth.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val sessionManager: SessionManager,
) : ViewModel() {
    private val _isDarkMode = MutableStateFlow(value = false)
    val isDarkMode = _isDarkMode.asStateFlow()

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }
}

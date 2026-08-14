package com.xsc.oneapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.registry.ModuleRegistry
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.core.result.uiStateCatching
import com.xsc.oneapp.feature.dashboard.domain.usecase.GetAccessibleModulesUseCase
import com.xsc.oneapp.navigation.Routes
import com.xsc.sdk.auth.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val moduleRegistry: ModuleRegistry,
    private val getAccessibleModulesUseCase: GetAccessibleModulesUseCase,
) : ViewModel() {
    private val _isDarkMode = MutableStateFlow(value = false)
    val isDarkMode = _isDarkMode.asStateFlow()

    /**
     * Which of [RootNavHost]'s internal routes the signed-in session can actually
     * reach, derived from the same `accessibleModules` response the dashboard tiles
     * already use - not a separate, hand-authored route-to-permission manifest,
     * since the client has no visibility into individual permission strings per
     * module (`accessibleModules` is already filtered server-side; it never returns
     * the permission_code it matched on). A backend-supplied module route (e.g.
     * "/fees") is normalised through [Routes.destinationFor] - the same function
     * dashboard tile taps already go through - so this set is always in the same
     * coordinate space RootNavHost's composables are keyed by.
     *
     * Exposed as [UiState] rather than a plain `Set<String>` so a route guard can
     * tell "still loading" apart from "loaded, and this route isn't in it" - the
     * dashboard tile a user tapped to get here was already accessible when they
     * tapped it, so a guard must never bounce someone before this has had a chance
     * to resolve.
     */
    private val _accessibleRoutes = MutableStateFlow<UiState<Set<String>>>(UiState.Loading)
    val accessibleRoutes: StateFlow<UiState<Set<String>>> = _accessibleRoutes.asStateFlow()

    init {
        loadAccessibleRoutes()
    }

    private fun loadAccessibleRoutes() {
        viewModelScope.launch {
            // Label mostly theoretical: DashboardRepositoryImpl.getAccessibleModules()
            // is deliberately fail-open (see its own kdoc) and never actually throws,
            // so this branch is rarely if ever exercised - kept labelled anyway for
            // the rare case something in the mapping itself throws unexpectedly.
            _accessibleRoutes.value = uiStateCatching(label = "accessible modules") {
                getAccessibleModulesUseCase()
                    .map { module -> Routes.destinationFor(module.route, moduleRegistry) }
                    .toSet()
            }
        }
    }

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }
}

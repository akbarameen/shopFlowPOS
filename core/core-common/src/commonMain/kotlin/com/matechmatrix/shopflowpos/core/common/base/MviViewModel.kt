package com.matechmatrix.shopflowpos.core.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel for all feature ViewModels.
 *
 * S = State    — the UI state snapshot (data class)
 * I = Intent   — actions sent from the UI to the ViewModel
 * E = Effect   — one-time events (navigate, show snackbar, etc.)
 *
 * Usage example:
 *   class PosViewModel : MviViewModel<PosState, PosIntent, PosEffect>(PosState()) {
 *       override suspend fun handleIntent(intent: PosIntent) { ... }
 *   }
 */
abstract class MviViewModel<S, I, E>(
    initialState: S
) : ViewModel() {

    // ─── State ────────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    protected val currentState: S get() = _state.value

    // ─── Effects (one-time events) ────────────────────────────────────────────
    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Called from the UI to dispatch an intent (user action).
     */
    fun onIntent(intent: I) {
        viewModelScope.launch {
            handleIntent(intent)
        }
    }

    // ─── Protected helpers ────────────────────────────────────────────────────

    /**
     * Override this in each ViewModel to handle intents.
     */
    protected abstract suspend fun handleIntent(intent: I)

    /**
     * Update the state. Pass a lambda that takes the current state
     * and returns the new state.
     *
     * Example:  setState { copy(isLoading = true) }
     */
    protected fun setState(reducer: S.() -> S) {
        _state.update { it.reducer() }
    }

    /**
     * Emit a one-time effect to the UI (navigation, snackbar, etc.).
     */
    protected fun setEffect(effect: E) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
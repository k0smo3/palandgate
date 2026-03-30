package me.csystems.gategarage.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import me.csystems.gategarage.core.palgate.PalGateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GateUiState {
    object Idle    : GateUiState()
    data class Loading(val message: String) : GateUiState()
    object Success : GateUiState()
    data class Error(val message: String) : GateUiState()
}

class GateViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<GateUiState>(GateUiState.Idle)
    val state: StateFlow<GateUiState> = _state.asStateFlow()

    fun openGate() {
        if (_state.value is GateUiState.Loading) return
        viewModelScope.launch(Dispatchers.IO) {
            val gateCreds = CredentialsStore(getApplication()).load()
            if (gateCreds == null) {
                _state.value = GateUiState.Error("Gate credentials not set — tap ⚙ to configure")
                return@launch
            }

            _state.value = GateUiState.Loading("Opening gate…")

            PalGateService(
                sessionToken = gateCreds.sessionToken,
                phoneNumber  = gateCreds.phoneNumber,
                tokenType    = gateCreds.tokenType,
                deviceId     = gateCreds.deviceId
            ).openGate().fold(
                onSuccess = { _state.value = GateUiState.Success },
                onFailure = { _state.value = GateUiState.Error("Gate: ${it.message}") }
            )
        }
    }

    fun resetState() { _state.value = GateUiState.Idle }
    fun refresh()    { _state.value = GateUiState.Idle }
}

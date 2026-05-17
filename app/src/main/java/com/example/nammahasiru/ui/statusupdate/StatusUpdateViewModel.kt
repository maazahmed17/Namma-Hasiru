package com.example.nammahasiru.ui.statusupdate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammahasiru.data.SaplingRepository
import com.example.nammahasiru.model.Sapling
import com.example.nammahasiru.model.SaplingStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StatusUpdateUiState(
    val sapling: Sapling? = null,
)

class StatusUpdateViewModel(
    private val saplingRepository: SaplingRepository,
    private val saplingId: Long,
) : ViewModel() {

    val uiState: StateFlow<StatusUpdateUiState> =
        saplingRepository.getSaplingStream(saplingId)
            .map { sapling -> StatusUpdateUiState(sapling = sapling) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = StatusUpdateUiState(),
            )

    fun saveStatus(status: String, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            val current = saplingRepository.getSaplingOnce(saplingId)
            if (current == null) {
                onFinished(false)
                return@launch
            }
            if (current.status != SaplingStatus.PENDING) {
                onFinished(true)
                return@launch
            }

            val result = runCatching {
                saplingRepository.updateSaplingSurvivalStatus(current, status)
            }
            onFinished(result.isSuccess)
        }
    }
}

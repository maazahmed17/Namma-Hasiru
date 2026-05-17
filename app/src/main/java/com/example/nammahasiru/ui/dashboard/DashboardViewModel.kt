package com.example.nammahasiru.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammahasiru.data.SaplingRepository
import com.example.nammahasiru.model.Sapling
import com.example.nammahasiru.model.SaplingStatus
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val saplingRepository: SaplingRepository,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> =
        saplingRepository.getAllSaplingsStream()
            .map { saplings -> DashboardUiState.fromSaplings(saplings) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DashboardUiState(),
            )
}

data class DashboardUiState(
    val survivalPercent: Int = 0,
    val totalPlanted: Int = 0,
    val alive: Int = 0,
    val pending: Int = 0,
    val died: Int = 0,
    val saplings: List<Sapling> = emptyList(),
) {
    companion object {
        fun fromSaplings(saplings: List<Sapling>): DashboardUiState {
            if (saplings.isEmpty()) {
                return DashboardUiState()
            }
            val alive = saplings.count { it.status == SaplingStatus.ALIVE }
            val pending = saplings.count { it.status == SaplingStatus.PENDING }
            val died = saplings.count { it.status == SaplingStatus.DIED }
            val survivalPercent = ((alive.toFloat() / saplings.size.toFloat()) * 100f).roundToInt()
            return DashboardUiState(
                survivalPercent = survivalPercent,
                totalPlanted = saplings.size,
                alive = alive,
                pending = pending,
                died = died,
                saplings = saplings,
            )
        }
    }
}

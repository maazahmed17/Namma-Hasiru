package com.example.nammahasiru.ui.treemap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammahasiru.data.SaplingRepository
import com.example.nammahasiru.model.Sapling
import com.example.nammahasiru.model.SaplingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class SurvivalFilter {
    ALL,
    ALIVE,
    PENDING,
    DIED,
}

data class TreeMapUiState(
    val saplings: List<Sapling> = emptyList(),
    val filter: SurvivalFilter = SurvivalFilter.ALL,
    val selectedSapling: Sapling? = null,
)

class TreeMapViewModel(
    private val saplingRepository: SaplingRepository,
) : ViewModel() {

    private val filter = MutableStateFlow(SurvivalFilter.ALL)
    private val selectedSapling = MutableStateFlow<Sapling?>(null)

    val uiState: StateFlow<TreeMapUiState> = combine(
        saplingRepository.getAllSaplingsStream(),
        filter,
        selectedSapling,
    ) { allSaplings, currentFilter, selected ->
        val filtered = when (currentFilter) {
            SurvivalFilter.ALL -> allSaplings
            SurvivalFilter.ALIVE -> allSaplings.filter { it.status == SaplingStatus.ALIVE }
            SurvivalFilter.PENDING -> allSaplings.filter { it.status == SaplingStatus.PENDING }
            SurvivalFilter.DIED -> allSaplings.filter { it.status == SaplingStatus.DIED }
        }
        TreeMapUiState(
            saplings = filtered,
            filter = currentFilter,
            selectedSapling = selected,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TreeMapUiState(),
    )

    fun setFilter(newFilter: SurvivalFilter) {
        filter.update { newFilter }
    }

    fun selectSapling(sapling: Sapling?) {
        selectedSapling.value = sapling
    }
}

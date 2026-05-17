package com.example.nammahasiru.ui.speciesguide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammahasiru.data.SaplingRepository
import com.example.nammahasiru.model.SaplingStatus
import com.example.nammahasiru.model.SpeciesCatalog
import com.example.nammahasiru.model.SpeciesStats
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class SpeciesGuideRow(
    val species: String,
    val survivalRatePercent: Int,
    val totalPlanted: Int,
    val badgeLabel: String,
)

data class SpeciesGuideUiState(
    val query: String = "",
    val rows: List<SpeciesGuideRow> = emptyList(),
    val useFallback: Boolean = false,
)

class SpeciesGuideViewModel(
    private val saplingRepository: SaplingRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<SpeciesGuideUiState> = combine(
        saplingRepository.getSpeciesStatsStream(),
        saplingRepository.getAllSaplingsStream(),
        query,
    ) { stats: List<SpeciesStats>, allSaplings, q ->
        val nonPendingCount = allSaplings.count { it.status != SaplingStatus.PENDING }
        val useFallback = nonPendingCount < 5

        val baseRows: List<SpeciesGuideRow> = if (useFallback) {
            SpeciesCatalog.SPECIES_NAMES
                .sortedByDescending { species -> SpeciesCatalog.FALLBACK_SURVIVAL_RATES[species] ?: 0f }
                .map { species ->
                    val rate = SpeciesCatalog.FALLBACK_SURVIVAL_RATES[species] ?: 0f
                    SpeciesGuideRow(
                        species = species,
                        survivalRatePercent = (rate * 100f).roundToInt(),
                        totalPlanted = 0,
                        badgeLabel = badgeForRate(rate),
                    )
                }
        } else {
            stats
                .sortedByDescending { it.survivalRate }
                .map { row ->
                    SpeciesGuideRow(
                        species = row.species,
                        survivalRatePercent = (row.survivalRate * 100f).roundToInt(),
                        totalPlanted = row.totalPlanted,
                        badgeLabel = badgeForRate(row.survivalRate),
                    )
                }
        }

        val filtered = if (q.isBlank()) {
            baseRows
        } else {
            baseRows.filter { it.species.contains(q, ignoreCase = true) }
        }

        SpeciesGuideUiState(
            query = q,
            rows = filtered,
            useFallback = useFallback,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SpeciesGuideUiState(),
    )

    fun onQueryChange(value: String) {
        query.update { value }
    }

    private fun badgeForRate(rate: Float): String {
        return when {
            rate > 0.75f -> "Highly Recommended"
            rate > 0.50f -> "Recommended"
            else -> "Moderate"
        }
    }
}

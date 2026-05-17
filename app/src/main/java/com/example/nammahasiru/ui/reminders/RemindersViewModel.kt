package com.example.nammahasiru.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammahasiru.data.SaplingRepository
import com.example.nammahasiru.model.Sapling
import com.example.nammahasiru.model.SaplingStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ReminderRow(
    val sapling: Sapling,
    val daysUntilCheck: Long,
    val checkDateLabel: String,
)

data class RemindersUiState(
    val rows: List<ReminderRow> = emptyList(),
)

class RemindersViewModel(
    private val saplingRepository: SaplingRepository,
) : ViewModel() {

    val uiState: StateFlow<RemindersUiState> =
        saplingRepository.getSaplingsByStatus(SaplingStatus.PENDING)
            .map { pending -> RemindersUiState(rows = pending.map { it.toReminderRow() }) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RemindersUiState(),
            )

    private fun Sapling.toReminderRow(): ReminderRow {
        val zone = ZoneId.systemDefault()
        val plantedInstant = runCatching { Instant.parse(plantingDate) }.getOrNull()
        val plantedDate = if (plantedInstant != null) {
            LocalDate.ofInstant(plantedInstant, zone)
        } else {
            LocalDate.now(zone)
        }
        val checkDate = plantedDate.plusDays(90)
        val today = LocalDate.now(zone)
        val daysUntil = ChronoUnit.DAYS.between(today, checkDate)
        val label = checkDate.toString()
        return ReminderRow(
            sapling = this,
            daysUntilCheck = daysUntil,
            checkDateLabel = label,
        )
    }
}

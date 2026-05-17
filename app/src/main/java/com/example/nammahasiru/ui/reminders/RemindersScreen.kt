package com.example.nammahasiru.ui.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nammahasiru.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RemindersViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Reminders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.rows.isEmpty()) {
                item {
                    Text(
                        text = "No pending survival checks right now.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                items(items = uiState.rows, key = { it.sapling.id }) { row ->
                    ReminderCard(row = row)
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(row: ReminderRow) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = row.sapling.species, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = "Planted: ${row.sapling.plantingDate}", style = MaterialTheme.typography.bodyMedium)
            val daysText = when {
                row.daysUntilCheck > 0 -> "Check due in ${row.daysUntilCheck} day(s) (on ${row.checkDateLabel})"
                row.daysUntilCheck == 0L -> "Check is due today (${row.checkDateLabel})"
                else -> "Check is overdue by ${-row.daysUntilCheck} day(s)"
            }
            Text(text = daysText, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

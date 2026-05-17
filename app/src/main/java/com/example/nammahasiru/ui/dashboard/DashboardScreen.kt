package com.example.nammahasiru.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nammahasiru.R
import com.example.nammahasiru.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNewPlant: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenSpecies: () -> Unit,
    onOpenReminders: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_bar_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Community Survival Score",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "${uiState.survivalPercent}%",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Alive / Total saplings",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(title = "Total", value = uiState.totalPlanted.toString(), modifier = Modifier.weight(1f))
                StatCard(title = "Alive", value = uiState.alive.toString(), modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(title = "Pending", value = uiState.pending.toString(), modifier = Modifier.weight(1f))
                StatCard(title = "Died", value = uiState.died.toString(), modifier = Modifier.weight(1f))
            }

            if (uiState.totalPlanted == 0) {
                Text(
                    text = "Plant your first tree to start tracking!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }

            Text(
                text = "Quick actions",
                style = MaterialTheme.typography.titleMedium,
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionButton(
                    text = "New Plant",
                    icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                    onClick = onNewPlant,
                )
                QuickActionButton(
                    text = "Tree Map",
                    icon = { Icon(imageVector = Icons.Filled.Place, contentDescription = null) },
                    onClick = onOpenMap,
                )
                QuickActionButton(
                    text = "Species Guide",
                    icon = { Icon(imageVector = Icons.Filled.List, contentDescription = null) },
                    onClick = onOpenSpecies,
                )
                QuickActionButton(
                    text = "Reminders",
                    icon = { Icon(imageVector = Icons.Filled.Notifications, contentDescription = null) },
                    onClick = onOpenReminders,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            Text(text = value, style = MaterialTheme.typography.headlineLarge)
        }
    }
}

@Composable
private fun QuickActionButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            icon()
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

package com.example.nammahasiru.ui.statusupdate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.nammahasiru.NammaHasiruApplication
import com.example.nammahasiru.model.SaplingStatus
import com.example.nammahasiru.ui.statusUpdateViewModelFactory
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusUpdateScreen(
    saplingId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val application = context.applicationContext as NammaHasiruApplication
    val viewModel: StatusUpdateViewModel = viewModel(
        key = saplingId.toString(),
        factory = statusUpdateViewModelFactory(application, saplingId),
    )
    val uiState by viewModel.uiState.collectAsState()
    var errorText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Update Status") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        val sapling = uiState.sapling
        if (sapling == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = "Sapling not found.", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) {
                    Text("Go back")
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AsyncImage(
                model = File(sapling.photoPath),
                contentDescription = "Planting photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Crop,
            )

            Text(text = sapling.species, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Planted: ${formatPlantingDate(sapling.plantingDate)}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Days since planting: ${daysSincePlanting(sapling.plantingDate)}",
                style = MaterialTheme.typography.bodyLarge,
            )

            errorText?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.saveStatus(SaplingStatus.ALIVE) { ok ->
                        if (ok) onBack() else errorText = "Could not save status. Try again."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text("Alive")
            }

            Button(
                onClick = {
                    viewModel.saveStatus(SaplingStatus.DIED) { ok ->
                        if (ok) onBack() else errorText = "Could not save status. Try again."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) {
                Text("Died")
            }
        }
    }
}

private fun daysSincePlanting(isoPlantingDate: String): Long {
    val zone = ZoneId.systemDefault()
    val plantedInstant = runCatching { Instant.parse(isoPlantingDate) }.getOrNull() ?: return 0L
    val plantedDate = LocalDate.ofInstant(plantedInstant, zone)
    val today = LocalDate.now(zone)
    return ChronoUnit.DAYS.between(plantedDate, today)
}

private fun formatPlantingDate(isoPlantingDate: String): String {
    val zone = ZoneId.systemDefault()
    val plantedInstant = runCatching { Instant.parse(isoPlantingDate) }.getOrNull() ?: return isoPlantingDate
    val zoned = plantedInstant.atZone(zone)
    return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(zoned)
}

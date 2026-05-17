package com.example.nammahasiru.ui.treemap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nammahasiru.model.Sapling
import com.example.nammahasiru.model.SaplingStatus
import com.example.nammahasiru.ui.AppViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeMapScreen(
    onUpdateStatus: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TreeMapViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(12.9716, 77.5946), 10f)
    }

    LaunchedEffect(uiState.saplings) {
        val saplings = uiState.saplings
        if (saplings.isEmpty()) return@LaunchedEffect
        val first = saplings.first()
        val update = CameraUpdateFactory.newLatLngZoom(
            LatLng(first.latitude, first.longitude),
            12f,
        )
        cameraPositionState.move(update)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Tree Map") },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = uiState.filter == SurvivalFilter.ALL,
                        onClick = { viewModel.setFilter(SurvivalFilter.ALL) },
                        label = { Text("All") },
                    )
                    FilterChip(
                        selected = uiState.filter == SurvivalFilter.ALIVE,
                        onClick = { viewModel.setFilter(SurvivalFilter.ALIVE) },
                        label = { Text("Alive") },
                    )
                    FilterChip(
                        selected = uiState.filter == SurvivalFilter.PENDING,
                        onClick = { viewModel.setFilter(SurvivalFilter.PENDING) },
                        label = { Text("Pending") },
                    )
                    FilterChip(
                        selected = uiState.filter == SurvivalFilter.DIED,
                        onClick = { viewModel.setFilter(SurvivalFilter.DIED) },
                        label = { Text("Died") },
                    )
                }

                GoogleMap(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = false),
                    uiSettings = MapUiSettings(zoomControlsEnabled = true),
                ) {
                    uiState.saplings.forEach { sapling ->
                        key(sapling.id) {
                            val markerState = remember {
                                MarkerState(position = LatLng(sapling.latitude, sapling.longitude))
                            }
                            LaunchedEffect(sapling.latitude, sapling.longitude) {
                                markerState.position = LatLng(sapling.latitude, sapling.longitude)
                            }
                            Marker(
                                state = markerState,
                                title = sapling.species,
                                snippet = sapling.status,
                                icon = BitmapDescriptorFactory.defaultMarker(hueForStatus(sapling.status)),
                                onClick = {
                                    viewModel.selectSapling(sapling)
                                    true
                                },
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = uiState.selectedSapling != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp),
            ) {
                val selected = uiState.selectedSapling ?: return@AnimatedVisibility
                SaplingBottomCard(
                    sapling = selected,
                    onDismiss = { viewModel.selectSapling(null) },
                    onUpdateStatus = {
                        onUpdateStatus(selected.id)
                        viewModel.selectSapling(null)
                    },
                )
            }
        }
    }
}

@Composable
private fun SaplingBottomCard(
    sapling: Sapling,
    onDismiss: () -> Unit,
    onUpdateStatus: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = sapling.species, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(text = "Planted: ${sapling.plantingDate}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: ${sapling.status}", style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Close")
                }
                Button(
                    onClick = onUpdateStatus,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Update Status")
                }
            }
        }
    }
}

private fun hueForStatus(status: String): Float {
    return when (status) {
        SaplingStatus.ALIVE -> BitmapDescriptorFactory.HUE_GREEN
        SaplingStatus.PENDING -> BitmapDescriptorFactory.HUE_ORANGE
        SaplingStatus.DIED -> BitmapDescriptorFactory.HUE_RED
        else -> BitmapDescriptorFactory.HUE_VIOLET
    }
}

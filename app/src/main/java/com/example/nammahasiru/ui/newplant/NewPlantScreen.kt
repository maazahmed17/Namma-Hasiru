package com.example.nammahasiru.ui.newplant

import android.Manifest
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nammahasiru.model.SpeciesCatalog
import com.example.nammahasiru.ui.AppViewModelProvider
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPlantScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewPlantViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }

    val speciesSuggestions = remember(uiState.speciesQuery) {
        val q = uiState.speciesQuery.trim()
        if (q.isBlank()) SpeciesCatalog.SPECIES_NAMES
        else SpeciesCatalog.SPECIES_NAMES.filter { it.contains(q, ignoreCase = true) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        val cameraOk = granted[Manifest.permission.CAMERA] == true
        val locationOk = (granted[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
            (granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        if (locationOk) {
            viewModel.refreshLocation()
        }
        if (!cameraOk) {
            // Camera permission denied; preview may be blank until granted.
        }
    }

    LaunchedEffect(Unit) {
        val permissions = buildList {
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= 33) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
        permissionLauncher.launch(permissions)
    }

    LaunchedEffect(uiState.saveComplete) {
        if (uiState.saveComplete) {
            viewModel.consumeSaveComplete()
            onSaved()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "New Plant") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            CameraPreview(
                lifecycleOwner = lifecycleOwner,
                onImageCaptureReady = { capture -> imageCapture = capture },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
            )

            Button(
                onClick = {
                    val capture = imageCapture ?: return@Button
                    val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        ?: context.filesDir
                    if (!baseDir.exists()) {
                        baseDir.mkdirs()
                    }
                    val file = File(baseDir, "sapling_${System.currentTimeMillis()}.jpg")
                    viewModel.capturePhoto(capture, file)
                },
                enabled = imageCapture != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Capture photo")
            }

            if (uiState.capturedPhotoPath != null) {
                Text(
                    text = "Photo saved",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Button(
                onClick = { viewModel.refreshLocation() },
                enabled = !uiState.locationLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = if (uiState.locationLoading) "Fetching GPS…" else "Refresh GPS")
            }

            if (uiState.latitude != null && uiState.longitude != null) {
                Text(
                    text = "Lat: ${uiState.latitude}, Lng: ${uiState.longitude}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            uiState.locationError?.let { err ->
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            OutlinedTextField(
                value = uiState.speciesQuery,
                onValueChange = {
                    viewModel.onSpeciesQueryChange(it)
                    menuExpanded = true
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Species name") },
                singleLine = true,
            )

            DropdownMenu(
                expanded = menuExpanded && speciesSuggestions.isNotEmpty(),
                onDismissRequest = { menuExpanded = false },
            ) {
                speciesSuggestions.take(8).forEach { species ->
                    DropdownMenuItem(
                        text = { Text(species) },
                        onClick = {
                            viewModel.onSpeciesSelected(species)
                            menuExpanded = false
                        },
                    )
                }
            }

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes (optional)") },
                supportingText = {
                    Text("${uiState.notes.length}/${NewPlantViewModel.MAX_NOTES_LENGTH}")
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                minLines = 3,
            )

            uiState.saveError?.let { err ->
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                onClick = { viewModel.saveSapling() },
                enabled = uiState.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                Text(text = if (uiState.saveInProgress) "Saving…" else "Save sapling")
            }
        }
    }
}

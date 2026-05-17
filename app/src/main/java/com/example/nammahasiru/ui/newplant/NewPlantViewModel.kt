package com.example.nammahasiru.ui.newplant

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import com.example.nammahasiru.data.LocationRepository
import com.example.nammahasiru.data.SaplingRepository
import com.example.nammahasiru.model.Sapling
import com.example.nammahasiru.model.SaplingStatus
import java.io.File
import java.time.Instant
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class NewPlantUiState(
    val speciesQuery: String = "",
    val selectedSpecies: String = "",
    val notes: String = "",
    val capturedPhotoPath: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationLoading: Boolean = false,
    val locationError: String? = null,
    val saveInProgress: Boolean = false,
    val saveComplete: Boolean = false,
    val saveError: String? = null,
) {
    val canSave: Boolean
        get() = capturedPhotoPath != null &&
            latitude != null &&
            longitude != null &&
            selectedSpecies.isNotBlank() &&
            !saveInProgress
}

class NewPlantViewModel(
    application: Application,
    private val saplingRepository: SaplingRepository,
    private val locationRepository: LocationRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(NewPlantUiState())
    val uiState: StateFlow<NewPlantUiState> = _uiState.asStateFlow()

    fun onSpeciesQueryChange(value: String) {
        _uiState.update { it.copy(speciesQuery = value) }
    }

    fun onSpeciesSelected(species: String) {
        _uiState.update { it.copy(selectedSpecies = species, speciesQuery = species) }
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value.take(MAX_NOTES_LENGTH)) }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(locationLoading = true, locationError = null) }
            if (!locationRepository.hasLocationPermission()) {
                _uiState.update {
                    it.copy(
                        locationLoading = false,
                        locationError = "Location permission is required to record GPS coordinates.",
                        latitude = null,
                        longitude = null,
                    )
                }
                return@launch
            }

            val location: Location? = locationRepository.getBestAvailableLocation()
            if (location == null) {
                _uiState.update {
                    it.copy(
                        locationLoading = false,
                        locationError = "GPS location is unavailable. Move outdoors, enable location, and try again.",
                        latitude = null,
                        longitude = null,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        locationLoading = false,
                        locationError = null,
                        latitude = location.latitude,
                        longitude = location.longitude,
                    )
                }
            }
        }
    }

    fun capturePhoto(imageCapture: ImageCapture, outputFile: File) {
        viewModelScope.launch {
            val path = withContext(Dispatchers.IO) {
                captureToFile(imageCapture, outputFile)
            }
            if (path == null) {
                _uiState.update { it.copy(saveError = "Could not capture photo. Please try again.") }
            } else {
                _uiState.update { it.copy(capturedPhotoPath = path, saveError = null) }
            }
        }
    }

    private suspend fun captureToFile(imageCapture: ImageCapture, outputFile: File): String? =
        suspendCoroutine { cont ->
            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
            val executor = Executors.newSingleThreadExecutor()
            imageCapture.takePicture(
                outputOptions,
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        cont.resume(outputFile.absolutePath)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        cont.resume(null)
                    }
                },
            )
        }

    fun saveSapling() {
        val state = _uiState.value
        if (!state.canSave) return
        val lat = state.latitude ?: return
        val lng = state.longitude ?: return
        val photo = state.capturedPhotoPath ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(saveInProgress = true, saveError = null) }
            try {
                val plantingDate = Instant.now().toString()
                val notesOrNull = state.notes.trim().ifBlank { null }
                val sapling = Sapling(
                    id = 0,
                    species = state.selectedSpecies.trim(),
                    latitude = lat,
                    longitude = lng,
                    plantingDate = plantingDate,
                    photoPath = photo,
                    status = SaplingStatus.PENDING,
                    updateDate = null,
                    growthPhotoPath = null,
                    notes = notesOrNull,
                    workerTag = "",
                )
                saplingRepository.insertSapling(sapling)
                _uiState.update { it.copy(saveInProgress = false, saveComplete = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        saveInProgress = false,
                        saveError = e.message ?: "Could not save sapling.",
                    )
                }
            }
        }
    }

    fun consumeSaveComplete() {
        _uiState.update { it.copy(saveComplete = false) }
    }

    companion object {
        const val MAX_NOTES_LENGTH: Int = 200
    }
}

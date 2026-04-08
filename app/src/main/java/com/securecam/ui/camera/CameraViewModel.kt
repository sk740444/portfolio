package com.securecam.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securecam.domain.model.LocationMetadata
import com.securecam.domain.repository.LocationRepository
import com.securecam.domain.usecase.GenerateForensicReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val generateForensicReportUseCase: GenerateForensicReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun setMode(mode: CaptureMode) {
        _uiState.value = _uiState.value.copy(mode = mode)
    }

    fun setRecording(isRecording: Boolean) {
        _uiState.value = _uiState.value.copy(isRecording = isRecording)
    }

    suspend fun fetchLocation(): LocationMetadata? {
        val location = locationRepository.getCurrentLocation()
        _uiState.value = _uiState.value.copy(
            locationStatus = if (location == null) "Location: Unavailable" else "Location: Acquired"
        )
        return location
    }

    fun processIntegrity(
        uri: android.net.Uri,
        fileName: String,
        relativePath: String,
        mediaType: com.securecam.domain.model.MediaType,
        timestampIso: String,
        location: LocationMetadata?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(hashStatus = "Hash: Generating", reportStatus = "Report: Generating")
            val result = generateForensicReportUseCase(uri, fileName, relativePath, mediaType, timestampIso, location)
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(hashStatus = "Hash: ${result.getOrThrow().take(16)}...", reportStatus = "Report: Saved")
            } else {
                _uiState.value.copy(hashStatus = "Hash: Failed", reportStatus = "Report: Failed")
            }
        }
    }
}

package com.example.nammahasiru.ui.profile

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammahasiru.data.SaplingRepository
import com.example.nammahasiru.model.SaplingStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val totalPlanted: Int = 0,
    val alive: Int = 0,
    val pending: Int = 0,
    val died: Int = 0,
    val appVersionLabel: String = "",
)

class ProfileViewModel(
    application: Application,
    private val saplingRepository: SaplingRepository,
) : AndroidViewModel(application) {

    val uiState: StateFlow<ProfileUiState> =
        saplingRepository.getAllSaplingsStream()
            .map { saplings ->
                ProfileUiState(
                    totalPlanted = saplings.size,
                    alive = saplings.count { it.status == SaplingStatus.ALIVE },
                    pending = saplings.count { it.status == SaplingStatus.PENDING },
                    died = saplings.count { it.status == SaplingStatus.DIED },
                    appVersionLabel = readVersionLabel(),
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ProfileUiState(appVersionLabel = readVersionLabel()),
            )

    private fun readVersionLabel(): String {
        return try {
            val pm: PackageManager = getApplication<Application>().packageManager
            val pkg = getApplication<Application>().packageName
            val info = if (Build.VERSION.SDK_INT >= 33) {
                pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(pkg, 0)
            }
            val versionName = info.versionName ?: "1.0"
            "Version $versionName"
        } catch (_: Exception) {
            "Version 1.0"
        }
    }
}

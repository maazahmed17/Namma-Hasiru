package com.example.nammahasiru.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.nammahasiru.NammaHasiruApplication
import com.example.nammahasiru.ui.dashboard.DashboardViewModel
import com.example.nammahasiru.ui.newplant.NewPlantViewModel
import com.example.nammahasiru.ui.profile.ProfileViewModel
import com.example.nammahasiru.ui.reminders.RemindersViewModel
import com.example.nammahasiru.ui.speciesguide.SpeciesGuideViewModel
import com.example.nammahasiru.ui.statusupdate.StatusUpdateViewModel
import com.example.nammahasiru.ui.treemap.TreeMapViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            DashboardViewModel(
                nammaHasiruApplication().container.saplingRepository,
            )
        }
        initializer {
            TreeMapViewModel(
                nammaHasiruApplication().container.saplingRepository,
            )
        }
        initializer {
            SpeciesGuideViewModel(
                nammaHasiruApplication().container.saplingRepository,
            )
        }
        initializer {
            RemindersViewModel(
                nammaHasiruApplication().container.saplingRepository,
            )
        }
        initializer {
            ProfileViewModel(
                nammaHasiruApplication(),
                nammaHasiruApplication().container.saplingRepository,
            )
        }
        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
            NewPlantViewModel(
                application,
                nammaHasiruApplication().container.saplingRepository,
                nammaHasiruApplication().container.locationRepository,
            )
        }
    }
}

fun CreationExtras.nammaHasiruApplication(): NammaHasiruApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NammaHasiruApplication)

fun statusUpdateViewModelFactory(
    application: NammaHasiruApplication,
    saplingId: Long,
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (!modelClass.isAssignableFrom(StatusUpdateViewModel::class.java)) {
                error("Invalid ViewModel type")
            }
            return StatusUpdateViewModel(
                application.container.saplingRepository,
                saplingId,
            ) as T
        }
    }
}

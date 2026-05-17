package com.example.nammahasiru.ui.navigation

object Routes {
    const val HOME: String = "home"
    const val MAP: String = "map"
    const val SPECIES: String = "species"
    const val PROFILE: String = "profile"
    const val NEW_PLANT: String = "new_plant"
    const val REMINDERS: String = "reminders"
    const val STATUS_UPDATE: String = "status_update"

    fun statusUpdate(saplingId: Long): String = "$STATUS_UPDATE/$saplingId"
}

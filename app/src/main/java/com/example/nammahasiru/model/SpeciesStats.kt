package com.example.nammahasiru.model

import androidx.room.DatabaseView

@DatabaseView(
    value = "SELECT species, " +
        "COUNT(*) AS totalPlanted, " +
        "SUM(CASE WHEN status = 'ALIVE' THEN 1 ELSE 0 END) AS totalAlive, " +
        "CASE WHEN COUNT(*) > 0 THEN " +
        "CAST(SUM(CASE WHEN status = 'ALIVE' THEN 1 ELSE 0 END) AS REAL) / COUNT(*) " +
        "ELSE 0 END AS survivalRate " +
        "FROM saplings GROUP BY species",
    viewName = "species_stats",
)
data class SpeciesStats(
    val species: String,
    val totalPlanted: Int,
    val totalAlive: Int,
    val survivalRate: Float,
)

package com.example.nammahasiru.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saplings")
data class Sapling(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val species: String,
    val latitude: Double,
    val longitude: Double,
    val plantingDate: String,
    val photoPath: String,
    val status: String,
    val updateDate: String? = null,
    val growthPhotoPath: String? = null,
    val notes: String? = null,
    val workerTag: String,
)

object SaplingStatus {
    const val PENDING = "PENDING"
    const val ALIVE = "ALIVE"
    const val DIED = "DIED"
}

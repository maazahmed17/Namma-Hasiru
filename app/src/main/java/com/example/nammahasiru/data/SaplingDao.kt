package com.example.nammahasiru.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nammahasiru.model.Sapling
import com.example.nammahasiru.model.SpeciesStats
import kotlinx.coroutines.flow.Flow

@Dao
interface SaplingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSapling(sapling: Sapling): Long

    @Update
    suspend fun updateSapling(sapling: Sapling)

    @Delete
    suspend fun deleteSapling(sapling: Sapling)

    @Query("SELECT * FROM saplings ORDER BY plantingDate DESC")
    fun getAllSaplings(): Flow<List<Sapling>>

    @Query("SELECT * FROM saplings WHERE id = :id")
    fun getSaplingById(id: Long): Flow<Sapling?>

    @Query("SELECT * FROM saplings WHERE id = :id LIMIT 1")
    suspend fun getSaplingByIdOnce(id: Long): Sapling?

    @Query("SELECT * FROM saplings WHERE status = :status ORDER BY plantingDate DESC")
    fun getSaplingsByStatus(status: String): Flow<List<Sapling>>

    @Query("SELECT * FROM species_stats ORDER BY survivalRate DESC, totalPlanted DESC")
    fun observeSpeciesStats(): Flow<List<SpeciesStats>>
}

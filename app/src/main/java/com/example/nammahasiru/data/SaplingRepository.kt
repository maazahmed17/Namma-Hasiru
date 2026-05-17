package com.example.nammahasiru.data

import androidx.room.withTransaction
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.nammahasiru.NammaHasiruDatabase
import com.example.nammahasiru.model.Sapling
import com.example.nammahasiru.model.SpeciesStats
import com.example.nammahasiru.worker.SaplingReminderWorker
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow

interface SaplingRepository {
    fun getAllSaplingsStream(): Flow<List<Sapling>>
    fun getSaplingStream(id: Long): Flow<Sapling?>
    fun getSaplingsByStatus(status: String): Flow<List<Sapling>>
    fun getSpeciesStatsStream(): Flow<List<SpeciesStats>>
    suspend fun insertSapling(sapling: Sapling): Long
    suspend fun updateSapling(sapling: Sapling)
    suspend fun deleteSapling(sapling: Sapling)
    suspend fun getSaplingOnce(id: Long): Sapling?
    suspend fun updateSaplingSurvivalStatus(sapling: Sapling, status: String)
}

class DefaultSaplingRepository(
    private val database: NammaHasiruDatabase,
    private val saplingDao: SaplingDao,
    private val workManager: WorkManager,
) : SaplingRepository {

    override fun getAllSaplingsStream(): Flow<List<Sapling>> = saplingDao.getAllSaplings()

    override fun getSaplingStream(id: Long): Flow<Sapling?> = saplingDao.getSaplingById(id)

    override fun getSaplingsByStatus(status: String): Flow<List<Sapling>> =
        saplingDao.getSaplingsByStatus(status)

    override fun getSpeciesStatsStream(): Flow<List<SpeciesStats>> = saplingDao.observeSpeciesStats()

    override suspend fun insertSapling(sapling: Sapling): Long {
        val id = database.withTransaction {
            val newId = saplingDao.insertSapling(
                sapling.copy(
                    id = 0,
                    workerTag = "",
                ),
            )
            saplingDao.updateSapling(
                sapling.copy(
                    id = newId,
                    workerTag = "reminder_$newId",
                ),
            )
            newId
        }
        scheduleReminderForSapling(id)
        return id
    }

    override suspend fun updateSapling(sapling: Sapling) {
        saplingDao.updateSapling(sapling)
    }

    override suspend fun deleteSapling(sapling: Sapling) {
        cancelReminderForSapling(sapling.id)
        saplingDao.deleteSapling(sapling)
    }

    override suspend fun getSaplingOnce(id: Long): Sapling? = saplingDao.getSaplingByIdOnce(id)

    override suspend fun updateSaplingSurvivalStatus(sapling: Sapling, status: String) {
        val nowIso = Instant.now().toString()
        saplingDao.updateSapling(
            sapling.copy(
                status = status,
                updateDate = nowIso,
            ),
        )
        cancelReminderForSapling(sapling.id)
    }

    private fun scheduleReminderForSapling(saplingId: Long) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val input = workDataOf(
            SaplingReminderWorker.KEY_SAPLING_ID to saplingId,
        )

        val request = OneTimeWorkRequestBuilder<SaplingReminderWorker>()
            .setInitialDelay(90, TimeUnit.DAYS)
            .setConstraints(constraints)
            .addTag("reminder_$saplingId")
            .setInputData(input)
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName(saplingId),
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    private fun cancelReminderForSapling(saplingId: Long) {
        workManager.cancelUniqueWork(uniqueWorkName(saplingId))
        workManager.cancelAllWorkByTag("reminder_$saplingId")
    }

    private fun uniqueWorkName(saplingId: Long): String = "sapling_reminder_$saplingId"
}

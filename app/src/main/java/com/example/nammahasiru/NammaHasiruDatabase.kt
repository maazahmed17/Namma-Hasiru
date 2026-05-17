package com.example.nammahasiru

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nammahasiru.data.SaplingDao
import com.example.nammahasiru.model.Sapling
import com.example.nammahasiru.model.SpeciesStats

@Database(
    entities = [Sapling::class],
    views = [SpeciesStats::class],
    version = 2,
    exportSchema = false,
)
abstract class NammaHasiruDatabase : RoomDatabase() {
    abstract fun saplingDao(): SaplingDao

    companion object {
        @Volatile
        private var Instance: NammaHasiruDatabase? = null

        fun getDatabase(context: Context): NammaHasiruDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    NammaHasiruDatabase::class.java,
                    "namma_hasiru_database",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

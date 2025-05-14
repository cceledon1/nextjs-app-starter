package com.passportscanner.database

import android.content.Context
import androidx.room.*
import com.passportscanner.models.ScanHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanHistory>>

    @Insert
    suspend fun insertScan(scan: ScanHistory)

    @Delete
    suspend fun deleteScan(scan: ScanHistory)

    @Query("DELETE FROM scan_history")
    suspend fun deleteAllScans()

    @Query("SELECT * FROM scan_history WHERE id = :id")
    suspend fun getScanById(id: Int): ScanHistory?
}

@Database(entities = [ScanHistory::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "passport_scanner_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class DatabaseRepository(private val scanHistoryDao: ScanHistoryDao) {
    val allScans: Flow<List<ScanHistory>> = scanHistoryDao.getAllScans()

    suspend fun insert(scan: ScanHistory) {
        scanHistoryDao.insertScan(scan)
    }

    suspend fun delete(scan: ScanHistory) {
        scanHistoryDao.deleteScan(scan)
    }

    suspend fun deleteAll() {
        scanHistoryDao.deleteAllScans()
    }

    suspend fun getScanById(id: Int): ScanHistory? {
        return scanHistoryDao.getScanById(id)
    }
}

package com.vinav.helmet.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vinav.helmet.model.RideHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface RideHistoryDao {
    @Query("SELECT * FROM ride_history ORDER BY timestamp DESC")
    fun getAllRides(): Flow<List<RideHistory>>

    @Query("SELECT * FROM ride_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastRide(): RideHistory?

    @Query("SELECT * FROM ride_history ORDER BY timestamp DESC LIMIT 10")
    fun getRecentRides(): Flow<List<RideHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ride: RideHistory): Long

    @Delete
    suspend fun delete(ride: RideHistory)

    @Query("DELETE FROM ride_history")
    suspend fun clearAll()
}

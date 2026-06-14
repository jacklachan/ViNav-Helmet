package com.vinav.helmet.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vinav.helmet.model.SavedPlace
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPlaceDao {
    @Query("SELECT * FROM saved_places ORDER BY lastUsedTimestamp DESC")
    fun getAllPlaces(): Flow<List<SavedPlace>>

    @Query("SELECT * FROM saved_places WHERE isHome = 1 LIMIT 1")
    suspend fun getHome(): SavedPlace?

    @Query("SELECT * FROM saved_places WHERE isHome = 1 LIMIT 1")
    fun getHomeFlow(): Flow<SavedPlace?>

    @Query("SELECT * FROM saved_places WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavorites(): Flow<List<SavedPlace>>

    @Query("SELECT * FROM saved_places WHERE isFavorite = 0 AND isHome = 0 ORDER BY lastUsedTimestamp DESC LIMIT 20")
    fun getRecent(): Flow<List<SavedPlace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(place: SavedPlace): Long

    @Update
    suspend fun update(place: SavedPlace)

    @Delete
    suspend fun delete(place: SavedPlace)

    @Query("UPDATE saved_places SET isHome = 0")
    suspend fun clearHome()

    @Query("DELETE FROM saved_places WHERE isFavorite = 0 AND isHome = 0")
    suspend fun clearRecent()
}

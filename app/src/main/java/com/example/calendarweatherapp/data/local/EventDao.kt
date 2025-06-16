package com.example.calendarweatherapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.calendarweatherapp.data.local.model.Event // Upewnij się, że ten import jest poprawny
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    // Metody, których może używać EventRepository (jeśli jest używany)
    @Query("SELECT * FROM weather_cache WHERE date = :dateToFilter")
    fun getEventsForDate(dateToFilter: Long): Flow<List<Event>>

    @Query("SELECT * FROM weather_cache ORDER BY date DESC")
    fun getAllEvents(): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weatherEvent: Event) // Ta metoda powinna być używana przez WeatherRepository jako insertWeather

    @Delete
    suspend fun deleteEvent(event: Event)

    // Metody używane przez WeatherRepository (z poprzedniej wersji kodu)
    // Upewnij się, że ta metoda jest obecna:
    @Query("SELECT * FROM weather_cache WHERE dateAndLocation = :dateAndLocation")
    suspend fun getWeatherByDateAndLocation(dateAndLocation: String): Event? // <-- TA METODA JEST POTRZEBNA

    @Query("DELETE FROM weather_cache WHERE lastUpdated < :timestamp")
    suspend fun deleteOldCache(timestamp: Long) // <-- TA METODA JEST POTRZEBNA


}
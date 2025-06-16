package com.example.calendarweatherapp.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

// This will represent a cached weather entry for a specific date and location
@Entity(tableName = "weather_cache")
data class Event( // Keeping name "Event" as per your structure, but it's for weather
    @PrimaryKey
    val dateAndLocation: String, // Composite key like "YYYY-MM-DD_lat_lon"
    val date: Long, // Store LocalDate as epochDay or String
    val tempMax: Double,
    val tempMin: Double,
    val weatherDescription: String,
    val weatherIcon: String,
    val hourlyForecastJson: String, // Store serialized list of hourly data
    val dailyForecastJson: String, // Store serialized list of daily data (for context beyond this date)
    val lastUpdated: Long = System.currentTimeMillis()
)
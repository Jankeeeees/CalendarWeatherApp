package com.example.calendarweather.data.repository

import com.example.calendarweatherapp.data.local.EventDao
import com.example.calendarweatherapp.data.local.model.Event

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Inspired by ToDoRepository.kt from Aplikację z listą zadań.txt
class EventRepository(private val eventDao: EventDao) {
    fun getEventsForDate(date: Long): Flow<List<Event>> = eventDao.getEventsForDate(date)

    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()

    suspend fun insertEvent(event: Event) {
        withContext(Dispatchers.IO) {
            eventDao.insertWeather(event)
        }
    }

    suspend fun deleteEvent(event: Event) {
        withContext(Dispatchers.IO) {
            eventDao.deleteEvent(event)
        }
    }
}
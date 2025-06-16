package com.example.calendarweatherapp.ui.viewmodel // Pakiet dla klas ViewModel i powiązanych narzędzi

import android.app.Application // Klasa bazowa dla obiektu Application, używana do uzyskania kontekstu aplikacji
import androidx.lifecycle.ViewModel // Bazowa klasa dla ViewModeli z biblioteki AndroidX Lifecycle
import androidx.lifecycle.ViewModelProvider // Interfejs fabryki do tworzenia instancji ViewModeli
import androidx.lifecycle.viewmodel.CreationExtras // Dodatkowe informacje przekazywane podczas tworzenia ViewModelu
import com.example.calendarweatherapp.MyCalendarWeatherApp // Własna klasa Application (potrzebna do rzutowania)
import com.example.calendarweatherapp.data.local.AppDatabase // Klasa reprezentująca bazę danych Room
import com.example.calendarweatherapp.data.remote.RetrofitClient // Obiekt dostarczający instancję klienta Retrofit
import com.example.calendarweatherapp.data.repository.WeatherRepository // Repozytorium danych pogodowych

/**
 * Obiekt singleton dostarczający fabrykę ([ViewModelProvider.Factory])
 * do tworzenia instancji ViewModeli w aplikacji.
 *
 * Jest to potrzebne, gdy ViewModele mają konstruktory z parametrami (zależnościami),
 * ponieważ domyślny mechanizm tworzenia ViewModeli nie potrafi ich obsłużyć.
 * Fabryka ta wie, jak stworzyć konkretne ViewModele, dostarczając im potrzebne zależności.
 */
object ViewModelFactoryProvider {

    /**
     * Funkcja dostarczająca skonfigurowaną fabrykę ViewModeli.
     * @param application Instancja klasy [Application], potrzebna do inicjalizacji zależności
     *                    takich jak baza danych czy repozytoria, które mogą wymagać kontekstu.
     * @return Obiekt implementujący [ViewModelProvider.Factory].
     */
    fun provideFactory(application: Application): ViewModelProvider.Factory =
        // Tworzenie anonimowego obiektu implementującego interfejs ViewModelProvider.Factory
        object : ViewModelProvider.Factory {

            /**
             * Metoda wywoływana przez system, gdy potrzebna jest nowa instancja ViewModelu.
             * @param modelClass Klasa ViewModelu, który ma zostać utworzony (np. MainViewModel::class.java).
             * @param extras Dodatkowe parametry, które mogą być użyte przy tworzeniu ViewModelu (np. SavedStateHandle).
             * @return Utworzona instancja ViewModelu typu T.
             * @throws IllegalArgumentException Jeśli fabryka nie wie, jak utworzyć ViewModel żądanej klasy.
             */
            @Suppress("UNCHECKED_CAST") // Tłumienie ostrzeżenia o niebezpiecznym rzutowaniu,
            // ponieważ sprawdzamy typ za pomocą isAssignableFrom.
            override fun <T : ViewModel> create(
                modelClass: Class<T>, // Klasa ViewModelu do stworzenia
                extras: CreationExtras // Dodatkowe dane (w tym przykładzie nieużywane bezpośrednio, ale są częścią API)
            ): T {
                // Sprawdzenie, czy żądana klasa ViewModelu to MainViewModel
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    // Tworzenie zależności potrzebnych dla MainViewModel:
                    // 1. Pobranie instancji DAO (Data Access Object) z bazy danych Room
                    val weatherDao = AppDatabase.getDatabase(application).eventDao() // eventDao to nazwa z Twojej struktury dla DAO pogodowego
                    // 2. Utworzenie instancji WeatherRepository, przekazując mu serwis API i DAO
                    val weatherRepository = WeatherRepository(RetrofitClient.apiService, weatherDao)
                    // 3. Utworzenie i zwrócenie instancji MainViewModel, przekazując repozytorium i obiekt Application
                    //    Rzutowanie `application as MyCalendarWeatherApp` jest potrzebne, jeśli MainViewModel oczekuje konkretnej implementacji Application.
                    return MainViewModel(weatherRepository, application as MyCalendarWeatherApp) as T // Rzutowanie na typ T
                }

                // Sekcja do dodawania logiki tworzenia innych ViewModeli, jeśli pojawią się w aplikacji.
                // Przykład dla zakomentowanego EventViewModel:
                // if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
                //     val eventDao = AppDatabase.getDatabase(application).eventDao()
                //     val eventRepository = EventRepository(eventDao) // Zakładając, że EventRepository istnieje i potrzebuje EventDao
                //     return EventViewModel(eventRepository) as T
                // }

                // Jeśli fabryka nie rozpoznaje żądanej klasy ViewModelu, rzuca wyjątek.
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
}
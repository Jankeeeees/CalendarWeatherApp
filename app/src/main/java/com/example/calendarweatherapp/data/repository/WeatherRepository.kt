package com.example.calendarweatherapp.data.repository // Warstwa abstrakcji danych pogodowych

import android.util.Log // Logowanie
import com.example.calendarweatherapp.data.local.EventDao // Dostęp do lokalnej bazy danych (cache)
import com.example.calendarweatherapp.data.local.model.Event // Model danych dla cache'u
import com.example.calendarweatherapp.data.remote.ApiService // Dostęp do zdalnego API
import com.example.calendarweatherapp.data.remote.model.GeocodingResponse // Model odpowiedzi dla geokodowania
import com.example.calendarweatherapp.data.remote.model.OneCallWeatherResponse // Model odpowiedzi dla danych pogodowych
import kotlinx.coroutines.Dispatchers // Określenie wątku dla operacji (IO - wejście/wyjście)
import kotlinx.coroutines.withContext // Wykonywanie kodu w określonym kontekście korutyny (np. na wątku IO)
import java.time.Instant // Praca z czasem
import java.time.LocalDate // Praca z datami
import java.time.ZoneId // Praca ze strefami czasowymi

/**
 * Repozytorium: Centralne miejsce zarządzania danymi pogodowymi.
 * Abstrahuje źródła danych (API, lokalna baza) od reszty aplikacji (np. ViewModeli).
 * Odpowiada za pobieranie danych, ich cachowanie i dostarczanie.
 */
class WeatherRepository(
    private val apiService: ApiService, // Zależność: Serwis do komunikacji z API (wstrzykiwana)
    private val eventDao: EventDao      // Zależność: DAO do operacji na lokalnej bazie (cache) (wstrzykiwana)
) {
    // Stałe dla domyślnych współrzędnych - uproszczenie, normalnie dynamiczne
    // private val defaultLat = 37.7749
    // private val defaultLon = -122.4194

    // Stała: czas ważności cache'u (tutaj: 1 godzina w milisekundach)
    private val CACHE_DURATION_MS = 60 * 60 * 1000L

    /**
     * Funkcja pomocnicza: Tworzy unikalny klucz dla wpisu w cache'u na podstawie daty i współrzędnych.
     */
    private fun getCacheKey(date: LocalDate, lat: Double, lon: Double): String {
        return "${date}_${lat}_${lon}"
    }

    /**
     * Funkcja zawieszająca (suspend): Pobiera współrzędne geograficzne dla podanej nazwy miasta.
     * Wykonywana na wątku IO (Dispatchers.IO).
     * @param cityName Nazwa miasta.
     * @return [GeocodingResponse] dla pierwszego znalezionego miasta lub null w przypadku błędu/braku wyników.
     */
    suspend fun getCoordinates(cityName: String): GeocodingResponse? {
        return withContext(Dispatchers.IO) { // Operacja sieciowa na wątku IO
            try {
                val response = apiService.getCoordinatesForCity(cityName) // Wywołanie metody z ApiService
                response.firstOrNull() // API zwraca listę, bierzemy pierwszy (najlepszy) wynik
            } catch (e: Exception) {
                Log.e("WeatherRepository", "Błąd pobierania współrzędnych dla $cityName", e)
                null // Zwróć null w przypadku błędu
            }
        }
    }

    /**
     * Funkcja zawieszająca (suspend): Pobiera dane pogodowe dla podanej daty i współrzędnych.
     * Najpierw sprawdza cache, jeśli dane są nieaktualne lub ich brak, pobiera z API.
     * Wynik z API jest cachowany.
     * Wykonywana na wątku IO (Dispatchers.IO).
     * @param date Żądana data.
     * @param lat Szerokość geograficzna.
     * @param lon Długość geograficzna.
     * @return [OneCallWeatherResponse] lub null w przypadku błędu.
     */
    suspend fun getWeatherDataForCoordinates(date: LocalDate, lat: Double, lon: Double): OneCallWeatherResponse? {
        return withContext(Dispatchers.IO) { // Operacje sieciowe i bazodanowe na wątku IO
            val cacheKey = getCacheKey(date, lat, lon) // Klucz do cache'u
            val cachedWeatherEvent = eventDao.getWeatherByDateAndLocation(cacheKey) // Sprawdzenie cache'u

            // Logika sprawdzania ważności cache'u
            if (cachedWeatherEvent != null && (System.currentTimeMillis() - cachedWeatherEvent.lastUpdated < CACHE_DURATION_MS)) {
                Log.d("WeatherRepository", "Używam danych z cache dla $date, $lat, $lon")
                // TODO: Implementacja deserializacji danych z `cachedWeatherEvent` do `OneCallWeatherResponse`
                // W tym szkielecie, jeśli jest w cache, na razie nie zwracamy, aby zawsze pobierać świeże dla prognozy.
                // Pełna implementacja wymagałaby konwersji.
            }

            try {
                Log.d("WeatherRepository", "Pobieram dane z API dla $date, $lat, $lon")
                val today = LocalDate.now(ZoneId.systemDefault())
                var response: OneCallWeatherResponse? // Zmienna na odpowiedź z API

                // Sprawdzenie, czy data jest w obsługiwanym przez OneCall API zakresie dla pełnej prognozy/historii
                if (date.isBefore(today.minusDays(5)) || date.isAfter(today.plusDays(7))) {
                    Log.w("WeatherRepository", "Data $date poza typowym zakresem OneCall API.")
                    if (date.isBefore(today.minusDays(5))) { // Jeśli data historyczna starsza niż 5 dni
                        Log.d("WeatherRepository", "Data zbyt odległa w przeszłości, wymagałaby logiki dla timemachine.")
                        // TODO: Opcjonalna implementacja pobierania danych z endpointu "timemachine"
                        // i mapowania wyniku `HistoricalWeatherResponse` na `OneCallWeatherResponse` (lub jego część).
                        // val historicalResponse = apiService.getHistoricalWeatherData(lat, lon, date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond())
                        // response = mapHistoricalToForecast(historicalResponse)
                        return@withContext null // Uproszczenie: Zwróć null dla dat spoza głównego zakresu OneCall
                    }
                    return@withContext null // Uproszczenie: Zwróć null
                }

                // Pobranie danych z API (aktualne, prognoza godzinowa, dzienna)
                response = apiService.getWeatherData(lat, lon) // Wywołanie metody z ApiService

                // Logika zapisu do cache'u, jeśli odpowiedź z API jest poprawna
                response?.current?.let { current -> // Jeśli są aktualne dane pogodowe
                    // Tworzenie obiektu Event (dla cache'u) na podstawie odpowiedzi z API
                    val weatherEvent = Event(
                        dateAndLocation = cacheKey,
                        date = date.toEpochDay(), // Data, dla której jest ten wpis
                        // Uproszczone pobieranie temp. max/min dla konkretnego dnia z prognozy dziennej
                        tempMax = response.daily?.find { dailyDate ->
                            LocalDate.ofInstant(Instant.ofEpochSecond(dailyDate.dt), ZoneId.systemDefault()) == date
                        }?.temp?.max ?: current.temp, // Jeśli nie ma w prognozie dziennej, użyj aktualnej
                        tempMin = response.daily?.find { dailyDate ->
                            LocalDate.ofInstant(Instant.ofEpochSecond(dailyDate.dt), ZoneId.systemDefault()) == date
                        }?.temp?.min ?: current.temp,
                        weatherDescription = current.weather.firstOrNull()?.description ?: "N/A",
                        weatherIcon = current.weather.firstOrNull()?.icon ?: "",
                        hourlyForecastJson = "", // TODO: Serializacja listy prognoz godzinowych do JSON (np. przy użyciu Moshi)
                        dailyForecastJson = ""   // TODO: Serializacja listy prognoz dziennych do JSON
                    )
                    eventDao.insertWeather(weatherEvent) // Zapis do bazy danych (cache)
                }
                return@withContext response // Zwróć odpowiedź z API
            } catch (e: Exception) {
                Log.e("WeatherRepository", "Błąd pobierania danych pogodowych", e)
                null // Zwróć null w przypadku błędu
            }
        }
    }

    /**
     * Funkcja zawieszająca (suspend): Czyści stare wpisy z cache'u.
     * Wykonywana na wątku IO (Dispatchers.IO).
     */
    suspend fun clearOldCache() {
        withContext(Dispatchers.IO) { // Operacja bazodanowa na wątku IO
            val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L) // Próg czasowy (7 dni)
            eventDao.deleteOldCache(oneWeekAgo) // Wywołanie metody DAO do usunięcia starych danych
        }
    }
}
package com.example.calendarweatherapp.data.remote // Definiuje pakiet, w którym znajduje się ten interfejs.
// Grupuje elementy związane ze zdalnym dostępem do danych (API).

import com.example.calendarweatherapp.BuildConfig // Importuje automatycznie wygenerowaną klasę BuildConfig.
// Pozwala na dostęp do zmiennych zdefiniowanych podczas procesu budowania,
// w tym przypadku do klucza API.
import com.example.calendarweatherapp.data.remote.model.GeocodingResponse // Importuje klasę modelu dla odpowiedzi Geocoding.
import com.example.calendarweatherapp.data.remote.model.HistoricalWeatherResponse // Importuje klasę modelu dla odpowiedzi danych historycznych.
import com.example.calendarweatherapp.data.remote.model.OneCallWeatherResponse // Importuje klasę modelu dla głównej odpowiedzi pogodowej.
import retrofit2.http.GET // Importuje adnotację @GET z biblioteki Retrofit.
// Oznacza, że metoda będzie wykonywać żądanie HTTP GET.
import retrofit2.http.Query // Importuje adnotację @Query z biblioteki Retrofit.
// Służy do dodawania parametrów zapytania do URL (np. ?lat=...&lon=...).

/**
 * Interfejs definiujący endpointy (punkty końcowe) API OpenWeatherMap, z którymi aplikacja będzie się komunikować.
 * Biblioteka Retrofit używa tego interfejsu do generowania implementacji kodu, który wykonuje rzeczywiste
 * żądania sieciowe HTTP. Każda metoda w interfejsie odpowiada jednemu konkretnemu żądaniu do API.
 */
interface ApiService {

    /**
     * Metoda do pobierania aktualnej pogody, prognozy godzinowej i dziennej za pomocą "One Call API 3.0".
     * Jest to funkcja zawieszająca (suspend), co oznacza, że może być wywoływana z korutyn
     * i nie blokuje głównego wątku aplikacji podczas oczekiwania na odpowiedź sieciową.
     *
     * @param lat Szerokość geograficzna lokalizacji.
     * @param lon Długość geograficzna lokalizacji.
     * @param exclude Opcjonalny parametr pozwalający wykluczyć części odpowiedzi (np. "minutely", "alerts").
     *                Wartość domyślna to "minutely,alerts".
     * @param units Opcjonalny parametr określający jednostki (np. "metric" dla Celsiusa, "imperial" dla Fahrenheita).
     *              Wartość domyślna to "metric".
     * @param apiKey Klucz API do autoryzacji żądania, pobierany z BuildConfig.
     * @return Obiekt [OneCallWeatherResponse] zawierający sparsowane dane pogodowe.
     */
    @GET("data/3.0/onecall") // Adnotacja @GET określa ścieżkę względną do bazowego URL dla tego endpointu.
    suspend fun getWeatherData(
        @Query("lat") lat: Double, // Adnotacja @Query("nazwa_parametru") mapuje argument metody na parametr URL.
        @Query("lon") lon: Double,
        @Query("exclude") exclude: String = "minutely,alerts",
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY // Klucz API jest przekazywany jako parametr zapytania.
    ): OneCallWeatherResponse // Typ zwracany przez metodę - Retrofit (z Moshi) automatycznie przekonwertuje odpowiedź JSON na ten obiekt.

    /**
     * Metoda do pobierania historycznych danych pogodowych dla konkretnej daty i godziny ("timemachine" endpoint).
     * Jest to funkcja zawieszająca (suspend).
     *
     * @param lat Szerokość geograficzna.
     * @param lon Długość geograficzna.
     * @param dt Unix timestamp (w sekundach) dla żądanej daty i godziny historycznej.
     * @param units Jednostki (domyślnie "metric").
     * @param apiKey Klucz API.
     * @return Obiekt [HistoricalWeatherResponse] zawierający historyczne dane pogodowe.
     */
    @GET("data/3.0/onecall/timemachine")
    suspend fun getHistoricalWeatherData(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("dt") dt: Long,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY
    ): HistoricalWeatherResponse

    /**
     * Metoda do pobierania współrzędnych geograficznych (szerokości i długości) dla podanej nazwy miasta.
     * Wykorzystuje "Geocoding API".
     * Jest to funkcja zawieszająca (suspend).
     *
     * @param cityName Nazwa miasta lub zapytanie lokalizacyjne (np. "London" lub "London,GB").
     * @param limit Opcjonalny parametr określający maksymalną liczbę zwracanych wyników (domyślnie 1).
     * @param apiKey Klucz API.
     * @return Lista obiektów [GeocodingResponse], ponieważ API może zwrócić wiele pasujących lokalizacji.
     *         Zazwyczaj interesuje nas pierwszy element tej listy.
     */
    @GET("geo/1.0/direct")
    suspend fun getCoordinatesForCity(
        @Query("q") cityName: String, // Parametr zapytania dla Geocoding API to "q".
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY
    ): List<GeocodingResponse> // API Geocoding zwraca tablicę (listę) wyników.
}
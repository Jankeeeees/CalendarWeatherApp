package com.example.calendarweatherapp.data.remote.model // Definiuje pakiet, w którym znajdują się te klasy.
// Grupuje klasy odpowiedzialne za model danych odpowiedzi z API.

import com.squareup.moshi.Json // Importuje adnotację @Json z biblioteki Moshi.
// Służy do mapowania nazw pól z JSON na nazwy pól w klasie Kotlin.
import com.squareup.moshi.JsonClass // Importuje adnotację @JsonClass z biblioteki Moshi.
// Informuje Moshi, że dla tej klasy ma być automatycznie wygenerowany adapter
// do konwersji między obiektem Kotlin a formatem JSON.

// Komentarz ogólny:
// Ten plik zawiera definicje klas danych (data classes) w Kotlinie,
// które odzwierciedlają strukturę odpowiedzi JSON otrzymywanych z API OpenWeatherMap.
// Biblioteka Moshi (w połączeniu z Retrofit) używa tych klas do automatycznego
// przekształcania (deserializacji) danych JSON na obiekty Kotlina, z którymi można
// łatwo pracować w aplikacji.

// Adnotacja @JsonClass(generateAdapter = true) jest kluczowa.
// Mówi ona Moshi, aby podczas kompilacji wygenerować kod (adapter) potrzebny
// do konwersji tej konkretnej klasy.

// Adnotacja @Json(name = "nazwa_z_json") pozwala na użycie innej nazwy pola w klasie Kotlin
// niż ta, która występuje w odpowiedzi JSON. Jeśli nazwy są takie same, ta adnotacja nie jest konieczna.

// Pola oznaczone znakiem zapytania (np. current: CurrentWeather?) oznaczają, że dane pole
// w odpowiedzi JSON może nie istnieć (być `null`). Moshi poprawnie obsłuży taki przypadek.


/**
 * Główna klasa odpowiedzi dla endpointu "One Call API" z OpenWeatherMap.
 * Zawiera szerokość i długość geograficzną, strefę czasową oraz zagnieżdżone obiekty
 * dla aktualnej pogody, prognozy godzinowej i dziennej.
 */
@JsonClass(generateAdapter = true)
data class OneCallWeatherResponse(
    @Json(name = "lat") val lat: Double, // Szerokość geograficzna
    @Json(name = "lon") val lon: Double, // Długość geograficzna
    @Json(name = "timezone") val timezone: String, // Nazwa strefy czasowej, np. "Europe/Warsaw"
    @Json(name = "current") val current: CurrentWeather?, // Aktualna pogoda (może być null)
    @Json(name = "hourly") val hourly: List<HourlyWeather>?, // Lista prognoz godzinowych (może być null lub pusta)
    @Json(name = "daily") val daily: List<DailyWeather>? // Lista prognoz dziennych (może być null lub pusta)
)

/**
 * Reprezentuje aktualne warunki pogodowe.
 */
@JsonClass(generateAdapter = true)
data class CurrentWeather(
    @Json(name = "dt") val dt: Long, // Czas danych, Unix timestamp, UTC
    @Json(name = "temp") val temp: Double, // Temperatura (w jednostkach z zapytania, np. Celsius)
    @Json(name = "feels_like") val feelsLike: Double, // Temperatura odczuwalna
    @Json(name = "pressure") val pressure: Int, // Ciśnienie atmosferyczne (hPa)
    @Json(name = "humidity") val humidity: Int, // Wilgotność (%)
    @Json(name = "uvi") val uvi: Double, // Indeks UV
    @Json(name = "wind_speed") val windSpeed: Double, // Prędkość wiatru (metry/sek lub mile/godz)
    @Json(name = "weather") val weather: List<WeatherDescription> // Lista opisów pogody (zwykle zawiera jeden element)
)

/**
 * Reprezentuje prognozę pogody na konkretną godzinę.
 */
@JsonClass(generateAdapter = true)
data class HourlyWeather(
    @Json(name = "dt") val dt: Long, // Czas prognozy, Unix timestamp, UTC
    @Json(name = "temp") val temp: Double, // Temperatura
    @Json(name = "feels_like") val feelsLike: Double, // Temperatura odczuwalna
    @Json(name = "pop") val probabilityOfPrecipitation: Double, // Prawdopodobieństwo opadów (wartość od 0 do 1)
    @Json(name = "weather") val weather: List<WeatherDescription> // Lista opisów pogody
)

/**
 * Reprezentuje prognozę pogody na konkretny dzień.
 */
@JsonClass(generateAdapter = true)
data class DailyWeather(
    @Json(name = "dt") val dt: Long, // Czas prognozy (początek dnia), Unix timestamp, UTC
    @Json(name = "summary") val summary: String, // Krótkie podsumowanie pogody na dany dzień
    @Json(name = "temp") val temp: DailyTemp, // Temperatury dzienne (min, max, rano, wieczór itd.)
    @Json(name = "feels_like") val feelsLike: FeelsLikeTemp, // Temperatury odczuwalne w różnych porach dnia
    @Json(name = "pop") val probabilityOfPrecipitation: Double, // Prawdopodobieństwo opadów
    @Json(name = "weather") val weather: List<WeatherDescription> // Lista opisów pogody
)

/**
 * Zawiera szczegółowy opis warunków pogodowych oraz kod ikony.
 */
@JsonClass(generateAdapter = true)
data class WeatherDescription(
    @Json(name = "id") val id: Int, // ID warunku pogodowego
    @Json(name = "main") val main: String, // Główna grupa warunków pogodowych (np. "Rain", "Clouds")
    @Json(name = "description") val description: String, // Dokładniejszy opis (np. "light rain", "few clouds")
    @Json(name = "icon") val icon: String // Kod ikony pogody (np. "01d", "10n")
)

/**
 * Temperatury w różnych porach dnia dla prognozy dziennej.
 */
@JsonClass(generateAdapter = true)
data class DailyTemp(
    @Json(name = "day") val day: Double, // Temperatura w ciągu dnia
    @Json(name = "min") val min: Double, // Minimalna temperatura dzienna
    @Json(name = "max") val max: Double, // Maksymalna temperatura dzienna
    @Json(name = "night") val night: Double, // Temperatura w nocy
    @Json(name = "eve") val eve: Double, // Temperatura wieczorem
    @Json(name = "morn") val morn: Double // Temperatura rano
)

/**
 * Temperatury odczuwalne w różnych porach dnia dla prognozy dziennej.
 */
@JsonClass(generateAdapter = true)
data class FeelsLikeTemp(
    @Json(name = "day") val day: Double, // Temperatura odczuwalna w ciągu dnia
    @Json(name = "night") val night: Double, // Temperatura odczuwalna w nocy
    @Json(name = "eve") val eve: Double, // Temperatura odczuwalna wieczorem
    @Json(name = "morn") val morn: Double // Temperatura odczuwalna rano
)


/**
 * Główna klasa odpowiedzi dla endpointu historycznych danych pogodowych ("timemachine").
 * Podobna do OneCallWeatherResponse, ale struktura danych godzinowych jest inna.
 */
@JsonClass(generateAdapter = true)
data class HistoricalWeatherResponse(
    @Json(name = "lat") val lat: Double,
    @Json(name = "lon") val lon: Double,
    @Json(name = "timezone") val timezone: String,
    // W odpowiedzi dla danych historycznych, klucz główny dla listy danych to "data"
    // i zawiera listę punktów danych, zazwyczaj jeden punkt na godzinę dla żądanego dnia.
    @Json(name = "data") val data: List<HistoricalDataPoint>?
)

/**
 * Reprezentuje pojedynczy punkt danych historycznych (zazwyczaj godzinowy).
 * Struktura jest bardzo podobna do CurrentWeather.
 */
@JsonClass(generateAdapter = true)
data class HistoricalDataPoint(
    @Json(name = "dt") val dt: Long,
    @Json(name = "temp") val temp: Double,
    @Json(name = "feels_like") val feelsLike: Double,
    @Json(name = "pressure") val pressure: Int,
    @Json(name = "humidity") val humidity: Int,
    @Json(name = "uvi") val uvi: Double,
    @Json(name = "wind_speed") val windSpeed: Double,
    @Json(name = "weather") val weather: List<WeatherDescription>
)

/**
 * Klasa reprezentująca odpowiedź z API Geocoding OpenWeatherMap.
 * Służy do konwersji nazwy miasta na współrzędne geograficzne (i odwrotnie).
 * W API odpowiedź jest listą, nawet jeśli znajdzie tylko jedno miasto.
 */
@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    @Json(name = "name") val name: String, // Nazwa znalezionej lokalizacji
    @Json(name = "lat") val lat: Double, // Szerokość geograficzna
    @Json(name = "lon") val lon: Double, // Długość geograficzna
    @Json(name = "country") val country: String?, // Kod kraju (np. "PL"), może być null
    @Json(name = "state") val state: String? // Nazwa stanu/regionu/województwa (jeśli dotyczy), może być null
)
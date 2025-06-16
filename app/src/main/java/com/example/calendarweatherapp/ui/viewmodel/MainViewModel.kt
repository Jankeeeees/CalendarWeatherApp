package com.example.calendarweatherapp.ui.viewmodel // Pakiet dla klas ViewModel

// Importy Android i bibliotek
import android.util.Log // Logowanie
import androidx.lifecycle.ViewModel // Bazowa klasa dla ViewModeli
import androidx.lifecycle.viewModelScope // Zakres korutyn powiązany z cyklem życia ViewModelu
import com.example.calendarweatherapp.MyCalendarWeatherApp // Klasa Application (może być używana np. do dostępu do zasobów globalnych)
import com.example.calendarweatherapp.data.remote.model.OneCallWeatherResponse // Model danych odpowiedzi pogodowej z API
import com.example.calendarweatherapp.data.remote.model.GeocodingResponse // Model danych odpowiedzi geokodowania z API
import com.example.calendarweatherapp.data.repository.WeatherRepository // Repozytorium do zarządzania danymi pogodowymi
import com.example.calendarweatherapp.data.util.UIState // Zapieczętowany interfejs do reprezentowania stanu UI
import kotlinx.coroutines.flow.MutableStateFlow // Mutowalny StateFlow do przechowywania i emitowania stanu
import kotlinx.coroutines.flow.StateFlow // Niemutowalny StateFlow (tylko do odczytu) udostępniany UI
import kotlinx.coroutines.flow.asStateFlow // Konwersja MutableStateFlow na StateFlow
import kotlinx.coroutines.launch // Uruchamianie korutyn
import java.time.LocalDate // Klasa do reprezentowania daty
import java.time.YearMonth // Klasa do reprezentowania miesiąca i roku
import java.time.Month // Klasa do reprezentowania miesiąca (jako enum)

/**
 * Główny ViewModel aplikacji.
 * Zarządza stanem UI dla ekranu kalendarza i pogody,
 * obsługuje logikę biznesową (pobieranie danych, nawigacja w kalendarzu, wyszukiwanie miast).
 * @param weatherRepository Repozytorium dostarczające dane pogodowe (wstrzykiwane).
 * @param application Instancja klasy Application (może być używana np. do zasobów string).
 */
class MainViewModel(
    private val weatherRepository: WeatherRepository,
    private val application: MyCalendarWeatherApp
) : ViewModel() {

    // Prywatny, mutowalny StateFlow dla aktualnie wyświetlanego miesiąca i roku w kalendarzu.
    // Inicjalizowany bieżącym miesiącem i rokiem.
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    // Publiczny, niemutowalny StateFlow udostępniany UI do obserwacji zmian miesiąca.
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    // Prywatny, mutowalny StateFlow dla aktualnie wybranej daty w kalendarzu.
    // Inicjalizowany bieżącą datą.
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    // Publiczny, niemutowalny StateFlow udostępniany UI.
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Prywatny, mutowalny StateFlow reprezentujący stan operacji pobierania danych pogodowych.
    // Używa zapieczętowanego interfejsu UIState (Idle, Loading, Success, Error).
    // Inicjalizowany stanem Idle.
    private val _weatherDataState = MutableStateFlow<UIState<OneCallWeatherResponse>>(UIState.Idle)
    // Publiczny, niemutowalny StateFlow dla stanu danych pogodowych.
    val weatherDataState: StateFlow<UIState<OneCallWeatherResponse>> = _weatherDataState.asStateFlow()

    // Prywatny, mutowalny StateFlow dla aktualnie używanych współrzędnych geograficznych (lat, lon).
    // Może być null, jeśli współrzędne nie zostały jeszcze ustalone.
    private val _currentCoordinates = MutableStateFlow<Pair<Double, Double>?>(null)
    // Publiczny, niemutowalny StateFlow dla współrzędnych.
    val currentCoordinates: StateFlow<Pair<Double, Double>?> = _currentCoordinates.asStateFlow()

    // Prywatny, mutowalny StateFlow dla tekstu wprowadzonego przez użytkownika w polu wyszukiwania miasta.
    // Inicjalizowany domyślną nazwą miasta.
    private val _cityNameInput = MutableStateFlow("San Francisco")
    // Publiczny, niemutowalny StateFlow dla nazwy miasta.
    val cityNameInput: StateFlow<String> = _cityNameInput.asStateFlow()

    // Blok inicjalizacyjny, wykonywany przy tworzeniu instancji ViewModelu.
    init {
        // Przy starcie, pobierz współrzędne dla domyślnego miasta, a następnie pogodę.
        fetchCoordinatesAndThenWeather(_cityNameInput.value)
        // Uruchom korutynę w viewModelScope do wyczyszczenia starego cache'u.
        viewModelScope.launch {
            weatherRepository.clearOldCache()
        }
    }

    /**
     * Aktualizuje wartość wprowadzonej przez użytkownika nazwy miasta.
     * @param name Nowa nazwa miasta.
     */
    fun updateCityNameInput(name: String) {
        _cityNameInput.value = name
    }

    /**
     * Rozpoczyna proces wyszukiwania współrzędnych dla aktualnie wprowadzonej nazwy miasta,
     * a następnie pobiera dla nich dane pogodowe.
     */
    fun searchCityAndFetchWeather() {
        fetchCoordinatesAndThenWeather(_cityNameInput.value)
    }

    /**
     * Prywatna funkcja: najpierw pobiera współrzędne dla podanego miasta,
     * a jeśli się powiedzie, następnie pobiera dane pogodowe.
     * @param cityName Nazwa miasta do wyszukania.
     */
    private fun fetchCoordinatesAndThenWeather(cityName: String) {
        if (cityName.isBlank()) { // Sprawdzenie, czy nazwa miasta nie jest pusta
            _weatherDataState.value = UIState.Error("City name cannot be empty.") // Ustawienie stanu błędu
            return
        }
        viewModelScope.launch { // Uruchomienie korutyny
            _weatherDataState.value = UIState.Loading // Ustawienie stanu ładowania na czas całego procesu
            Log.d("MainViewModel", "Fetching coordinates for city: $cityName")
            val coordinates = weatherRepository.getCoordinates(cityName) // Wywołanie repozytorium
            if (coordinates != null) { // Jeśli współrzędne zostały znalezione
                _currentCoordinates.value = Pair(coordinates.lat, coordinates.lon) // Zapisz współrzędne
                Log.d("MainViewModel", "Coordinates found: ${coordinates.lat}, ${coordinates.lon}. Fetching weather.")
                fetchWeatherForSelectedDateInternal() // Pobierz pogodę dla aktualnie wybranej daty i nowych współrzędnych
            } else { // Jeśli współrzędne nie zostały znalezione
                _weatherDataState.value = UIState.Error("Could not find city: $cityName") // Ustaw stan błędu
                _currentCoordinates.value = null // Zresetuj współrzędne
                Log.e("MainViewModel", "No coordinates found for $cityName")
            }
        }
    }

    /**
     * Ustawia nową wybraną datę i inicjuje pobieranie dla niej danych pogodowych.
     * @param date Nowo wybrana data.
     */
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date // Aktualizacja wybranej daty
        fetchWeatherForSelectedDateInternal() // Pobranie pogody dla nowej daty i aktualnych współrzędnych
    }

    /**
     * Przechodzi do następnego miesiąca w kalendarzu i aktualizuje wybraną datę.
     * Stara się zachować ten sam dzień miesiąca, a jeśli jest to niemożliwe (np. zmiana z 31 na luty),
     * wybiera ostatni dzień nowego miesiąca. Następnie pobiera pogodę.
     */
    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1) // Inkrementacja miesiąca
        val currentSelectedDay = _selectedDate.value.dayOfMonth // Zapamiętanie dnia
        var newSelectedDateCandidate = _currentMonth.value.atDay(currentSelectedDay) // Próba ustawienia tego samego dnia w nowym miesiącu
        // Sprawdzenie, czy ustawiony dzień faktycznie należy do nowego miesiąca (np. uniknięcie "31 lutego")
        if (newSelectedDateCandidate.month != _currentMonth.value.month) {
            newSelectedDateCandidate = _currentMonth.value.atEndOfMonth() // Jeśli nie, wybierz ostatni dzień nowego miesiąca
        }
        _selectedDate.value = newSelectedDateCandidate // Aktualizacja wybranej daty
        fetchWeatherForSelectedDateInternal() // Pobranie pogody
    }

    /**
     * Przechodzi do poprzedniego miesiąca w kalendarzu i aktualizuje wybraną datę.
     * Działa analogicznie do `nextMonth`.
     */
    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1) // Dekrementacja miesiąca
        val currentSelectedDay = _selectedDate.value.dayOfMonth
        var newSelectedDateCandidate = _currentMonth.value.atDay(currentSelectedDay)
        if (newSelectedDateCandidate.month != _currentMonth.value.month) {
            newSelectedDateCandidate = _currentMonth.value.atEndOfMonth()
        }
        _selectedDate.value = newSelectedDateCandidate
        fetchWeatherForSelectedDateInternal()
    }

    /**
     * Wewnętrzna funkcja do pobierania danych pogodowych dla aktualnie wybranej daty
     * i aktualnie zapisanych współrzędnych geograficznych.
     */
    private fun fetchWeatherForSelectedDateInternal() {
        val coords = _currentCoordinates.value // Pobranie aktualnych współrzędnych
        if (coords == null) { // Jeśli brak współrzędnych (np. miasto nie zostało jeszcze wyszukane)
            // Nie próbuj pobierać pogody, jeśli stan nie jest już błędem lub ładowaniem.
            if (_weatherDataState.value !is UIState.Error && _weatherDataState.value !is UIState.Loading) {
                _weatherDataState.value = UIState.Error("Please search for a city first.")
            }
            Log.d("MainViewModel", "Skipping weather fetch, no coordinates available.")
            return
        }

        viewModelScope.launch { // Uruchomienie korutyny
            // Ustaw stan ładowania tylko jeśli nie jest już ustawiony przez proces geokodowania.
            if (_weatherDataState.value !is UIState.Loading) {
                _weatherDataState.value = UIState.Loading
            }
            Log.d("MainViewModel", "Fetching weather for: ${_selectedDate.value} at ${coords.first}, ${coords.second}")
            try {
                // Wywołanie repozytorium w celu pobrania danych pogodowych dla danych współrzędnych i daty
                val weather = weatherRepository.getWeatherDataForCoordinates(_selectedDate.value, coords.first, coords.second)
                if (weather != null) { // Jeśli dane zostały pomyślnie pobrane
                    _weatherDataState.value = UIState.Success(weather) // Ustaw stan sukcesu z danymi
                } else { // Jeśli repozytorium zwróciło null (np. brak danych dla tej daty/lokalizacji)
                    _weatherDataState.value = UIState.Error("No weather data available for the selected date and location.")
                }
            } catch (e: Exception) { // Obsługa wyjątków (np. problem z siecią)
                _weatherDataState.value = UIState.Error(e.message ?: "An unknown error occurred") // Ustaw stan błędu
                Log.e("MainViewModel", "Error fetching weather", e)
            }
        }
    }

    /**
     * Publiczna funkcja do ręcznego odświeżenia danych pogodowych
     * dla aktualnie wybranej daty i współrzędnych.
     */
    fun refreshWeather() {
        fetchWeatherForSelectedDateInternal()
    }
}
package com.example.calendarweatherapp.ui.screens // Pakiet dla ekranów (głównych widoków) aplikacji

// Importy Android i Jetpack Compose
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // Do zapamiętywania stanu przewijania
import androidx.compose.foundation.text.KeyboardActions // Akcje dla klawiatury (np. "Search")
import androidx.compose.foundation.text.KeyboardOptions // Opcje klawiatury (np. typ akcji)
import androidx.compose.foundation.verticalScroll // Umożliwia pionowe przewijanie zawartości
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search // Ikona wyszukiwania
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // Do obserwowania StateFlow jako stanu Compose
import androidx.compose.runtime.getValue // Delegat do łatwego dostępu do wartości stanu
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi // Dla zaawansowanych API UI (np. SoftwareKeyboardController)
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Dostęp do kontekstu aplikacji
import androidx.compose.ui.platform.LocalSoftwareKeyboardController // Kontrola nad klawiaturą ekranową
import androidx.compose.ui.text.input.ImeAction // Typy akcji dla klawiatury (Input Method Editor)
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Funkcja do uzyskiwania instancji ViewModelu
import com.example.calendarweatherapp.MyCalendarWeatherApp // Klasa Application
import com.example.calendarweatherapp.data.util.UIState // Zapieczętowany interfejs do reprezentowania stanu UI
// Importy komponentów UI zdefiniowanych wcześniej
import com.example.calendarweatherapp.ui.composables.CalendarView
import com.example.calendarweatherapp.ui.composables.CurrentWeatherDisplay
import com.example.calendarweatherapp.ui.composables.DailyForecastDisplay
import com.example.calendarweatherapp.ui.composables.HourlyForecastDisplay
import com.example.calendarweatherapp.ui.composables.DailyForecastSummaryDisplay
import com.example.calendarweatherapp.ui.composables.HistoricalDaySummaryDisplay
// Importy ViewModelu i fabryki ViewModelu
import com.example.calendarweatherapp.ui.viewmodel.MainViewModel
import com.example.calendarweatherapp.ui.viewmodel.ViewModelFactoryProvider
// Importy Javy do pracy z datą i czasem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

/**
 * Główny ekran aplikacji wyświetlający kalendarz i informacje pogodowe.
 * @param modifier Modyfikator.
 * @param mainViewModel Instancja [MainViewModel] dostarczana przez fabrykę.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class) // Włączenie eksperymentalnych API
@Composable
fun CalendarWeatherScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = viewModel( // Uzyskanie instancji ViewModelu
        factory = ViewModelFactoryProvider.provideFactory( // Użycie własnej fabryki do stworzenia ViewModelu (np. z zależnościami)
            LocalContext.current.applicationContext as MyCalendarWeatherApp // Przekazanie kontekstu aplikacji do fabryki
        )
    )
) {
    // Obserwowanie stanów z ViewModelu jako stany Compose
    val currentMonth by mainViewModel.currentMonth.collectAsState() // Aktualnie wyświetlany miesiąc w kalendarzu
    val selectedDate by mainViewModel.selectedDate.collectAsState() // Aktualnie wybrana data w kalendarzu
    val weatherState by mainViewModel.weatherDataState.collectAsState() // Stan operacji pobierania danych pogodowych (Loading, Success, Error, Idle)
    val cityNameInput by mainViewModel.cityNameInput.collectAsState() // Tekst wprowadzony przez użytkownika w polu wyszukiwania miasta
    val keyboardController = LocalSoftwareKeyboardController.current // Kontroler do programowego zarządzania klawiaturą ekranową

    Scaffold( // Główny kontener struktury ekranu (Material Design)
        topBar = { // Definicja górnego paska aplikacji
            TopAppBar(
                title = { Text("Calendar & Weather") }, // Tytuł paska
                colors = TopAppBarDefaults.topAppBarColors( // Kolory paska
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        modifier = modifier // Zastosowanie modyfikatora przekazanego do funkcji
    ) { paddingValues -> // Wartości paddingu dostarczone przez Scaffold (uwzględniające np. TopAppBar)
        Column( // Główny kontener zawartości, elementy ułożone pionowo
            modifier = Modifier
                .padding(paddingValues) // Zastosowanie paddingu od Scaffold
                .fillMaxSize() // Wypełnienie całego dostępnego miejsca
                .verticalScroll(rememberScrollState()) // Umożliwienie przewijania całej kolumny
        ) {
            // Pole tekstowe do wprowadzania nazwy miasta
            OutlinedTextField(
                value = cityNameInput, // Aktualna wartość pola (ze stanu ViewModelu)
                onValueChange = { mainViewModel.updateCityNameInput(it) }, // Aktualizacja stanu w ViewModelu przy zmianie tekstu
                label = { Text("City Name") }, // Etykieta pola
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true, // Pole jednowierszowe
                trailingIcon = { // Ikona na końcu pola tekstowego
                    IconButton(onClick = { // Akcja po kliknięciu ikony wyszukiwania
                        keyboardController?.hide() // Ukrycie klawiatury
                        mainViewModel.searchCityAndFetchWeather() // Wywołanie funkcji w ViewModelu
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search City")
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), // Ustawienie akcji "Search" dla klawiatury
                keyboardActions = KeyboardActions(onSearch = { // Akcja po naciśnięciu przycisku "Search" na klawiaturze
                    keyboardController?.hide() // Ukrycie klawiatury
                    mainViewModel.searchCityAndFetchWeather() // Wywołanie funkcji w ViewModelu
                })
            )

            // Komponent widoku kalendarza
            CalendarView(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                onDateSelected = { date -> mainViewModel.selectDate(date) }, // Akcja po wybraniu daty
                onPreviousMonthClicked = { mainViewModel.previousMonth() }, // Akcja dla poprzedniego miesiąca
                onNextMonthClicked = { mainViewModel.nextMonth() },         // Akcja dla następnego miesiąca
                modifier = Modifier.fillMaxWidth()
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp)) // Linia oddzielająca kalendarz od pogody

            // Sekcja wyświetlania danych pogodowych w zależności od stanu (weatherState)
            when (val state = weatherState) {
                is UIState.Loading -> { // Stan ładowania
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator() // Wyświetlenie wskaźnika postępu
                    }
                }
                is UIState.Success -> { // Stan sukcesu - dane pogodowe dostępne
                    val weatherData = state.data // Rozpakowanie danych pogodowych
                    val today = LocalDate.now(ZoneId.systemDefault()) // Aktualna data systemowa

                    // ----- LOGIKA WYBORU DANYCH DO GŁÓWNEGO OKNA POGODOWEGO -----
                    // Wyświetlanie różnych komponentów w zależności od tego, czy wybrana data to dzisiaj, przeszłość czy przyszłość.
                    if (selectedDate.isEqual(today)) { // Jeśli wybrana data to dzisiaj
                        weatherData.current?.let { // Jeśli są dostępne aktualne dane pogodowe
                            CurrentWeatherDisplay(it, titlePrefix = "Now", modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    } else if (selectedDate.isBefore(today)) { // Jeśli wybrana data jest z przeszłości
                        // Znalezienie danych historycznych dla wybranego dnia w zwróconej prognozie dziennej (OneCall API zwraca też historię ostatnich 5 dni)
                        val historicalDailyData = weatherData.daily?.find { daily ->
                            LocalDate.ofInstant(Instant.ofEpochSecond(daily.dt), ZoneId.systemDefault()) == selectedDate
                        }
                        if (historicalDailyData != null) { // Jeśli znaleziono dane historyczne dla tego dnia
                            HistoricalDaySummaryDisplay(historicalDailyData, selectedDate, modifier = Modifier.padding(horizontal = 8.dp))
                        } else { // Jeśli brak szczegółowych danych historycznych (np. data zbyt odległa)
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    "Detailed historical data for ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} not available in current view.",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else { // selectedDate.isAfter(today) - Jeśli wybrana data jest z przyszłości
                        // Znalezienie prognozy dziennej dla wybranej daty
                        val forecastDailyData = weatherData.daily?.find { daily ->
                            LocalDate.ofInstant(Instant.ofEpochSecond(daily.dt), ZoneId.systemDefault()) == selectedDate
                        }
                        forecastDailyData?.let { // Jeśli znaleziono prognozę
                            DailyForecastSummaryDisplay(it, selectedDate, modifier = Modifier.padding(horizontal = 8.dp))
                        } ?: Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { // Jeśli brak prognozy
                            Text(
                                "Daily forecast summary for ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} not available.",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                    // ----- KONIEC LOGIKI WYBORU GŁÓWNEGO OKNA POGODOWEGO -----

                    // Prognoza godzinowa (zawsze od teraz na pewien okres, np. 48h z OneCall API)
                    // Można ją pokazywać warunkowo, np. tylko dla wybranej daty, jeśli jest to dzisiaj lub jutro.
                    if (selectedDate.isEqual(today) || selectedDate.isEqual(today.plusDays(1))) {
                        weatherData.hourly?.let { // Jeśli są dostępne dane godzinowe
                            HourlyForecastDisplay(it, modifier = Modifier.padding(horizontal = 0.dp))
                        }
                    }

                    // Prognoza dzienna (jako lista kolejnych dni)
                    weatherData.daily?.let { // Jeśli są dostępne dane dzienne
                        // Opcjonalne filtrowanie: pokaż prognozę od wybranej daty (jeśli jest w przyszłości) lub całą dostępną.
                        val relevantDailyForecast = it.filter { daily ->
                            !LocalDate.ofInstant(Instant.ofEpochSecond(daily.dt), ZoneId.systemDefault()).isBefore(selectedDate)
                        }
                        if (relevantDailyForecast.isNotEmpty()){
                            DailyForecastDisplay(relevantDailyForecast, modifier = Modifier.padding(horizontal = 0.dp))
                        }
                    }
                }
                is UIState.Error -> { // Stan błędu
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, // Komunikat błędu
                                modifier = Modifier.padding(bottom=8.dp))
                            Button(onClick = { // Przycisk ponowienia próby
                                // Logika ponowienia: jeśli są współrzędne, odśwież dla nich; jeśli jest nazwa miasta, wyszukaj; inaczej nic nie rób.
                                if (mainViewModel.currentCoordinates.value == null && cityNameInput.isNotBlank()) {
                                    mainViewModel.searchCityAndFetchWeather()
                                } else if (mainViewModel.currentCoordinates.value != null) {
                                    mainViewModel.refreshWeather()
                                } else {
                                    Log.d("RetryLogic", "Cannot retry without coordinates or city input.")
                                }
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is UIState.Idle -> { // Stan początkowy/bezczynności
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("Enter a city and select a date to see the weather.") // Zachęta dla użytkownika
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Odstęp na dole ekranu
        }
    }
}
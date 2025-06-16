package com.example.calendarweatherapp.ui.composables // Pakiet dla reużywalnych komponentów UI pogodowych

// Importy Android i Jetpack Compose
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow // Dla horyzontalnej, przewijanej listy (prognoza godzinowa)
import androidx.compose.foundation.lazy.items // Do iterowania po elementach w LazyRow/LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale // Określa, jak obrazek ma być skalowany
import androidx.compose.ui.platform.LocalContext // Dostęp do kontekstu aplikacji (np. dla Coil)
import androidx.compose.ui.res.painterResource // Ładowanie zasobów drawable (np. placeholderów)
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // Biblioteka Coil do asynchronicznego ładowania obrazów (np. ikon pogody z URL)
import coil.request.ImageRequest // Konfiguracja żądania obrazu dla Coil
import com.example.calendarweatherapp.R // Dostęp do zasobów aplikacji (np. drawable)
// Importy modeli danych pogodowych zdefiniowanych wcześniej
import com.example.calendarweatherapp.data.remote.model.CurrentWeather
import com.example.calendarweatherapp.data.remote.model.DailyWeather
import com.example.calendarweatherapp.data.remote.model.HourlyWeather
// Importy Javy do pracy z datą i czasem
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Komponent UI wyświetlający aktualne warunki pogodowe.
 * @param currentWeather Obiekt [CurrentWeather] z danymi.
 * @param modifier Modyfikator.
 * @param titlePrefix Prefiks tytułu (np. "Now", "Weather on").
 */
@Composable
fun CurrentWeatherDisplay(currentWeather: CurrentWeather, modifier: Modifier = Modifier, titlePrefix: String = "Now") {
    // Konwersja Unix timestamp (dt) na czytelny format czasu lokalnego
    val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(currentWeather.dt), ZoneId.systemDefault())
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm") // Format np. "16:08"

    Card(modifier = modifier.fillMaxWidth().padding(8.dp)) { // Karta jako kontener
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { // Elementy w kolumnie, wyśrodkowane
            Text( // Tytuł z czasem
                text = "$titlePrefix (${localDateTime.format(timeFormatter)})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center // Wyśrodkowanie tekstu
            )
            Spacer(Modifier.height(8.dp)) // Odstęp

            Row(verticalAlignment = Alignment.CenterVertically) { // Ikona i temperatura w jednym wierszu
                WeatherIcon(iconCode = currentWeather.weather.firstOrNull()?.icon) // Wyświetlenie ikony pogody
                Text( // Temperatura
                    text = "${currentWeather.temp}°C",
                    style = MaterialTheme.typography.headlineLarge, // Duży rozmiar czcionki
                    fontWeight = FontWeight.Bold
                )
            }
            Text( // Opis pogody (np. "Few clouds")
                text = currentWeather.weather.firstOrNull()?.description?.replaceFirstChar { // Pierwsza litera wielka
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                } ?: "N/A", // Jeśli brak opisu, wyświetl "N/A"
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            // Dodatkowe informacje pogodowe
            Text("Feels like: ${currentWeather.feelsLike}°C", style = MaterialTheme.typography.bodySmall)
            Text("Humidity: ${currentWeather.humidity}%", style = MaterialTheme.typography.bodySmall)
            Text("Wind: ${currentWeather.windSpeed} m/s", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * Komponent UI wyświetlający prognozę godzinową w postaci horyzontalnej, przewijanej listy.
 * @param hourlyWeather Lista obiektów [HourlyWeather].
 * @param modifier Modyfikator.
 */
@Composable
fun HourlyForecastDisplay(hourlyWeather: List<HourlyWeather>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 8.dp)) { // Kontener dla tytułu i listy
        Text("Hourly Forecast", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
        LazyRow( // Horyzontalna lista, renderuje tylko widoczne elementy (optymalizacja)
            contentPadding = PaddingValues(horizontal = 16.dp), // Wewnętrzny padding dla listy
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Odstęp między elementami listy
        ) {
            items(hourlyWeather.take(24)) { hourly -> // Wyświetl maksymalnie 24 pierwsze godziny prognozy
                HourlyForecastItem(hourly) // Komponent dla pojedynczej godziny
            }
        }
    }
}

/**
 * Komponent UI dla pojedynczego elementu prognozy godzinowej.
 * @param hourly Obiekt [HourlyWeather] z danymi dla konkretnej godziny.
 * @param modifier Modyfikator.
 */
@Composable
fun HourlyForecastItem(hourly: HourlyWeather, modifier: Modifier = Modifier) {
    val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(hourly.dt), ZoneId.systemDefault())
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Card(modifier = modifier) { // Karta dla pojedynczej godziny
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(localDateTime.format(timeFormatter), style = MaterialTheme.typography.labelSmall) // Czas
            WeatherIcon(iconCode = hourly.weather.firstOrNull()?.icon, size = 40.dp) // Ikona pogody (mniejsza)
            Text("${hourly.temp}°C", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) // Temperatura
            Text("Pop: ${(hourly.probabilityOfPrecipitation * 100).toInt()}%", style = MaterialTheme.typography.labelSmall) // Prawdopodobieństwo opadów (Pop)
        }
    }
}

/**
 * Komponent UI wyświetlający prognozę dzienną.
 * @param dailyWeather Lista obiektów [DailyWeather].
 * @param modifier Modyfikator.
 */
@Composable
fun DailyForecastDisplay(dailyWeather: List<DailyWeather>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text("Daily Forecast", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
        dailyWeather.forEach { daily -> // Pętla po dniach prognozy (można użyć LazyColumn, jeśli lista jest bardzo długa)
            DailyForecastItem(daily, Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) // Komponent dla pojedynczego dnia
        }
    }
}

/**
 * Komponent UI dla pojedynczego elementu prognozy dziennej.
 * @param daily Obiekt [DailyWeather] z danymi dla konkretnego dnia.
 * @param modifier Modyfikator.
 */
@Composable
fun DailyForecastItem(daily: DailyWeather, modifier: Modifier = Modifier) {
    val localDate = Instant.ofEpochSecond(daily.dt).atZone(ZoneId.systemDefault()).toLocalDate() // Konwersja timestamp na LocalDate
    val dayName = localDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()) // Skrócona nazwa dnia tygodnia (np. "Pon")

    Card(modifier = modifier.fillMaxWidth()) { // Karta dla pojedynczego dnia, zajmuje całą szerokość
        Row( // Elementy w wierszu
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically, // Wyrównanie w pionie
            horizontalArrangement = Arrangement.SpaceBetween // Rozmieszczenie z odstępami
        ) {
            Column(Modifier.weight(1.5f)) { // Kolumna na nazwę dnia i opis (zajmuje więcej miejsca)
                Text(if (localDate == LocalDate.now()) "Today" else dayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) // "Today" lub nazwa dnia
                Text(daily.weather.firstOrNull()?.description?.replaceFirstChar { it.titlecase(Locale.getDefault()) } ?: "", style = MaterialTheme.typography.labelSmall) // Opis pogody
            }
            WeatherIcon(iconCode = daily.weather.firstOrNull()?.icon, size = 40.dp, modifier = Modifier.weight(0.5f)) // Ikona pogody
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) { // Kolumna na temperatury i Pop (wyrównana do prawej)
                Text("${daily.temp.max}°C / ${daily.temp.min}°C", style = MaterialTheme.typography.bodyMedium) // Temp. max i min
                Text("Pop: ${(daily.probabilityOfPrecipitation * 100).toInt()}%", style = MaterialTheme.typography.labelSmall) // Prawdopodobieństwo opadów
            }
        }
    }
}

/**
 * Komponent UI do wyświetlania ikony pogody.
 * Ładuje obrazek z URL za pomocą biblioteki Coil.
 * @param iconCode Kod ikony z API OpenWeatherMap (np. "01d").
 * @param modifier Modyfikator.
 * @param size Rozmiar ikony.
 */
@Composable
fun WeatherIcon(iconCode: String?, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 50.dp) {
    if (iconCode.isNullOrEmpty()) { // Jeśli brak kodu ikony, wyświetl pustą przestrzeń
        Spacer(modifier = modifier.size(size))
        return
    }

    // Konstrukcja URL do obrazka ikony na serwerach OpenWeatherMap
    val imageUrl = "https://openweathermap.org/img/wn/${iconCode.lowercase()}@2x.png" // Użyj lowercase dla kodu ikony, zgodnie z niektórymi implementacjami

    AsyncImage( // Komponent Coil do asynchronicznego ładowania obrazu
        model = ImageRequest.Builder(LocalContext.current) // Budowanie żądania obrazu
            .data(imageUrl) // URL obrazu
            .crossfade(true) // Włączenie efektu płynnego przejścia przy ładowaniu
            .build(),
        contentDescription = "Weather icon: $iconCode", // Opis dla ułatwień dostępu
        modifier = modifier.size(size), // Ustawienie rozmiaru
        contentScale = ContentScale.Fit // Skalowanie obrazu, aby zmieścił się w całości
        // Usunięto placeholder i error drawable dla uproszczenia, można je dodać z R.drawable.*
    )
}

/**
 * Komponent UI wyświetlający podsumowanie prognozy dziennej (np. dla wybranego dnia w kalendarzu).
 * @param dailyWeather Obiekt [DailyWeather] z danymi.
 * @param date Data ([LocalDate]), dla której jest to podsumowanie.
 * @param modifier Modyfikator.
 */
@Composable
fun DailyForecastSummaryDisplay(
    dailyWeather: DailyWeather,
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    // Formatowanie nazwy dnia i miesiąca
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()) // Pełna nazwa dnia
    val monthName = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) // Pełna nazwa miesiąca

    Card(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text( // Wyświetlenie pełnej daty
                text = "$dayName, ${date.dayOfMonth} $monthName", // Np. "Poniedziałek, 2 Czerwiec"
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                WeatherIcon(iconCode = dailyWeather.weather.firstOrNull()?.icon)
                Text( // Średnia temperatura dzienna lub inna reprezentatywna
                    text = "${dailyWeather.temp.day}°C",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text( // Podsumowanie/opis pogody
                text = dailyWeather.summary.ifBlank { dailyWeather.weather.firstOrNull()?.description?.replaceFirstChar { it.titlecase(Locale.getDefault()) } ?: "N/A" }, // Jeśli summary jest puste, użyj description
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text("Max: ${dailyWeather.temp.max}°C, Min: ${dailyWeather.temp.min}°C", style = MaterialTheme.typography.bodySmall)
            Text("Precipitation: ${(dailyWeather.probabilityOfPrecipitation * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * Komponent UI wyświetlający podsumowanie historycznych danych pogodowych dla dnia.
 * Może być bardzo podobny do DailyForecastSummaryDisplay.
 * @param dailyWeather Obiekt [DailyWeather] (API OneCall zwraca dane historyczne w tej samej strukturze co prognozę dzienną, jeśli używamy go do historii ostatnich 5 dni).
 * @param date Data ([LocalDate]), dla której jest to podsumowanie.
 * @param modifier Modyfikator.
 */
@Composable
fun HistoricalDaySummaryDisplay(
    dailyWeather: DailyWeather,
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val monthName = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())

    Card(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text( // Tytuł wskazujący na dane historyczne
                text = "Weather on $dayName, ${date.dayOfMonth} $monthName",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                WeatherIcon(iconCode = dailyWeather.weather.firstOrNull()?.icon)
                Text(
                    text = "${dailyWeather.temp.day}°C", // Średnia dzienna temperatura
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text( // Opis
                text = dailyWeather.summary.ifBlank { dailyWeather.weather.firstOrNull()?.description?.replaceFirstChar { it.titlecase(Locale.getDefault()) } ?: "N/A" },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text("Max: ${dailyWeather.temp.max}°C, Min: ${dailyWeather.temp.min}°C", style = MaterialTheme.typography.bodySmall)
            // Można dodać inne specyficzne dane historyczne, jeśli API je dostarcza inaczej niż w prognozie
        }
    }
}
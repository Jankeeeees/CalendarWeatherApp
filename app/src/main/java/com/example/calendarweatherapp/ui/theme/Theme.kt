package com.example.calendarweatherapp.ui.theme // Pakiet dla elementów związanych z motywem (theme) aplikacji

// Importy Android i Jetpack Compose
import android.app.Activity // Dostęp do obiektu Activity (np. do modyfikacji okna)
import android.os.Build // Informacje o wersji systemu Android (np. do sprawdzania obsługi dynamicznych kolorów)
import androidx.compose.foundation.isSystemInDarkTheme // Funkcja sprawdzająca, czy aktualnie w systemie jest włączony tryb ciemny
import androidx.compose.material3.MaterialTheme // Główny komponent dostarczający motyw Material Design 3 do aplikacji Compose
import androidx.compose.material3.darkColorScheme // Funkcja tworząca schemat kolorów dla trybu ciemnego
import androidx.compose.material3.dynamicDarkColorScheme // Funkcja tworząca dynamiczny schemat kolorów dla trybu ciemnego (Android 12+)
import androidx.compose.material3.dynamicLightColorScheme // Funkcja tworząca dynamiczny schemat kolorów dla trybu jasnego (Android 12+)
import androidx.compose.material3.lightColorScheme // Funkcja tworząca schemat kolorów dla trybu jasnego
import androidx.compose.runtime.Composable // Adnotacja dla funkcji kompozycyjnych
import androidx.compose.runtime.SideEffect // Funkcja kompozycyjna do wykonywania operacji poza cyklem kompozycji (np. modyfikacja systemu)
import androidx.compose.ui.graphics.Color // Reprezentacja kolorów
import androidx.compose.ui.graphics.toArgb // Konwersja koloru Compose na format ARGB używany przez system Android
import androidx.compose.ui.platform.LocalContext // Dostęp do aktualnego kontekstu aplikacji
import androidx.compose.ui.platform.LocalView // Dostęp do aktualnego widoku Androida (View)
import androidx.core.view.WindowCompat // Narzędzia do kompatybilności związane z oknem aplikacji

// Definicja prywatnego schematu kolorów dla trybu ciemnego.
// Kolory (Purple80, PurpleGrey80 itd.) powinny być zdefiniowane w pliku Color.kt w tym samym pakiecie.
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,         // Główny kolor aplikacji (np. dla przycisków, akcentów)
    secondary = PurpleGrey80,   // Drugorzędny kolor aplikacji
    tertiary = Pink80,          // Trzeciorzędny kolor
    background = Color(0xFF1C1B1F), // Kolor tła dla większości ekranów
    surface = Color(0xFF1C1B1F),    // Kolor powierzchni dla komponentów takich jak karty, arkusze
    onPrimary = Purple40,       // Kolor tekstu/ikon na tle `primary`
    onSecondary = PurpleGrey40, // Kolor tekstu/ikon na tle `secondary`
    onTertiary = Pink40,        // Kolor tekstu/ikon na tle `tertiary`
    onBackground = Color(0xFFE6E1E5),// Kolor tekstu/ikon na tle `background`
    onSurface = Color(0xFFE6E1E5)    // Kolor tekstu/ikon na tle `surface`
)

// Definicja prywatnego schematu kolorów dla trybu jasnego.
// Kolory (SkyBlue, LightBlue itd.) powinny być zdefiniowane w pliku Color.kt.
private val LightColorScheme = lightColorScheme(
    primary = SkyBlue,          // Niestandardowy kolor podstawowy
    secondary = LightBlue,      // Niestandardowy kolor drugorzędny
    tertiary = Pink40,
    background = Color(0xFFFFFFFF), // Białe tło
    surface = Color(0xFFF0F4F8),    // Jasnoszary/błękitny kolor powierzchni
    onPrimary = White,          // Biały tekst na tle `primary`
    onSecondary = Black,        // Czarny tekst na tle `secondary`
    onTertiary = White,
    onBackground = Color(0xFF1C1B1F), // Ciemny tekst na jasnym tle
    onSurface = Color(0xFF1C1B1F)     // Ciemny tekst na jasnej powierzchni
)

/**
 * Główna funkcja kompozycyjna definiująca motyw aplikacji "CalendarWeatherAppTheme".
 * Otacza całą zawartość UI aplikacji, dostarczając jej zdefiniowany schemat kolorów, typografię itp.
 * @param darkTheme Flaga określająca, czy użyć motywu ciemnego (domyślnie pobierana z ustawień systemowych).
 * @param dynamicColor Flaga określająca, czy użyć dynamicznych kolorów (Material You, dostępne od Androida 12+).
 *                     Domyślnie wyłączone. Jeśli włączone, kolory są generowane na podstawie tapety użytkownika.
 * @param content Funkcja kompozycyjna zawierająca UI, które ma być ostylowane tym motywem.
 */
@Composable
fun CalendarWeatherAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Domyślnie sprawdza ustawienia systemowe dla trybu ciemnego
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit // Zawartość UI aplikacji
) {
    // Wybór odpowiedniego schematu kolorów w zależności od parametrów `dynamicColor` i `darkTheme`.
    val colorScheme = when {
        // Jeśli dynamiczne kolory są włączone i system to Android 12 (API S) lub nowszy:
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current // Pobranie aktualnego kontekstu
            // Wybór dynamicznego schematu (jasnego lub ciemnego) na podstawie `darkTheme`.
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Jeśli dynamiczne kolory są wyłączone, a włączony jest tryb ciemny:
        darkTheme -> DarkColorScheme
        // W pozostałych przypadkach (tryb jasny, bez dynamicznych kolorów):
        else -> LightColorScheme
    }

    // Modyfikacja wyglądu systemowego paska statusu, aby pasował do motywu aplikacji.
    val view = LocalView.current // Pobranie aktualnego widoku Androida.
    if (!view.isInEditMode) { // Sprawdzenie, czy kod nie jest wykonywany w trybie podglądu edytora (Preview).
        // `SideEffect` jest używany do wykonania kodu, który modyfikuje obiekty zarządzane przez system Android
        // (jak okno Activity) w sposób bezpieczny w cyklu życia Compose.
        SideEffect {
            val window = (view.context as Activity).window // Pobranie okna bieżącej aktywności.
            window.statusBarColor = colorScheme.primary.toArgb() // Ustawienie koloru tła paska statusu na kolor podstawowy motywu.
            // Ustawienie wyglądu ikon na pasku statusu (jasne lub ciemne) w zależności od tego, czy motyw jest ciemny.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Zastosowanie wybranego schematu kolorów i typografii do komponentu MaterialTheme.
    // MaterialTheme "otacza" całą zawartość aplikacji (`content`), przekazując jej zdefiniowany styl.
    MaterialTheme(
        colorScheme = colorScheme, // Przekazanie wybranego schematu kolorów.
        typography = Typography,   // Przekazanie zdefiniowanej typografii (obiekt `Typography` z pliku Type.kt).
        content = content          // Wyrenderowanie zawartości UI aplikacji z zastosowanym motywem.
    )
}
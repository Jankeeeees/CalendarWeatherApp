package com.example.calendarweatherapp.ui.composables // Pakiet dla reużywalnych komponentów UI (Composable funkcji)

// Importy standardowych komponentów i narzędzi Jetpack Compose
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons // Dostęp do predefiniowanych ikon Material Design
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft // Ikona strzałki w lewo (automatycznie lustrzana dla RTL)
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight // Ikona strzałki w prawo
import androidx.compose.material3.* // Komponenty Material Design 3 (np. Text, IconButton, Column, Row, Box)
import androidx.compose.runtime.Composable // Adnotacja oznaczająca funkcję jako komponent UI Jetpack Compose
import androidx.compose.ui.Alignment // Narzędzia do wyrównywania elementów
import androidx.compose.ui.Modifier // Obiekt do modyfikowania wyglądu i zachowania komponentów (padding, rozmiar, tło itp.)
import androidx.compose.ui.graphics.Color // Reprezentacja kolorów
import androidx.compose.ui.text.font.FontWeight // Określanie grubości czcionki
import androidx.compose.ui.text.style.TextAlign // Wyrównywanie tekstu
import androidx.compose.ui.unit.dp // Jednostka "density-independent pixels" do określania wymiarów
import androidx.compose.ui.unit.sp // Jednostka "scale-independent pixels" do określania rozmiaru tekstu

// Importy klas Javy do pracy z datą i czasem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter // Formatowanie daty/czasu do postaci tekstowej
import java.time.format.TextStyle // Styl wyświetlania nazw (np. dni tygodnia)
import java.util.Locale // Ustawienia regionalne (np. dla nazw dni tygodnia)

/**
 * Funkcja kompozycyjna wyświetlająca widok kalendarza miesięcznego.
 * @param currentMonth Aktualnie wyświetlany miesiąc i rok ([YearMonth]).
 * @param selectedDate Aktualnie wybrana data ([LocalDate]).
 * @param onDateSelected Funkcja zwrotna (lambda) wywoływana po kliknięciu na konkretny dzień. Przekazuje wybraną datę.
 * @param onPreviousMonthClicked Funkcja zwrotna wywoływana po kliknięciu przycisku poprzedniego miesiąca.
 * @param onNextMonthClicked Funkcja zwrotna wywoływana po kliknięciu przycisku następnego miesiąca.
 * @param modifier Modyfikator do dostosowania wyglądu tego komponentu.
 */
@Composable
fun CalendarView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonthClicked: () -> Unit,
    onNextMonthClicked: () -> Unit,
    modifier: Modifier = Modifier // Domyślny, pusty modyfikator
) {
    // Obliczenia potrzebne do wyrenderowania siatki kalendarza:
    val daysInMonth = currentMonth.lengthOfMonth() // Liczba dni w aktualnym miesiącu
    val firstDayOfMonth = currentMonth.atDay(1) // Pierwszy dzień aktualnego miesiąca (jako LocalDate)
    // Wartość dnia tygodnia dla pierwszego dnia miesiąca (np. 0 dla niedzieli, 1 dla poniedziałku itd., w zależności od Locale)
    // `% 7` jest używane do normalizacji, np. jeśli DayOfWeek.SUNDAY.value to 7, to 7 % 7 = 0.
    val firstDayOfWeekValue = firstDayOfMonth.dayOfWeek.value % 7

    // Przygotowanie listy skróconych nazw dni tygodnia (np. "N", "P", "W" lub "S", "M", "T")
    // na podstawie ustawień regionalnych urządzenia.
    val weekDays = DayOfWeek.entries.map { it.getDisplayName(TextStyle.NARROW, Locale.getDefault()) }


    Column(modifier = modifier.padding(16.dp)) { // Główny kontener; Column układa elementy pionowo. Padding wokół całego kalendarza.
        // Nagłówek z nazwą miesiąca, rokiem i przyciskami nawigacyjnymi
        Row( // Row układa elementy poziomo.
            modifier = Modifier.fillMaxWidth(), // Zajmuje całą dostępną szerokość.
            horizontalArrangement = Arrangement.SpaceBetween, // Rozmieszcza elementy: pierwszy na lewo, ostatni na prawo, reszta równomiernie pomiędzy.
            verticalAlignment = Alignment.CenterVertically // Wyrównuje elementy w pionie do środka.
        ) {
            IconButton(onClick = onPreviousMonthClicked) { // Przycisk z ikoną
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month") // Ikona strzałki w lewo.
            }
            Text( // Tekst wyświetlający nazwę miesiąca i rok.
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")), // Formatowanie daty, np. "Czerwiec 2025"
                style = MaterialTheme.typography.titleMedium, // Styl tekstu z motywu aplikacji.
                fontWeight = FontWeight.Bold // Pogrubiona czcionka.
            )
            IconButton(onClick = onNextMonthClicked) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month") // Ikona strzałki w prawo.
            }
        }
        Spacer(modifier = Modifier.height(16.dp)) // Pusta przestrzeń (odstęp) o wysokości 16dp.

        // Nagłówek z nazwami dni tygodnia
        Row(Modifier.fillMaxWidth()) {
            weekDays.forEach { day -> // Pętla po skróconych nazwach dni tygodnia.
                Text( // Wyświetlenie każdej nazwy dnia.
                    text = day,
                    textAlign = TextAlign.Center, // Wyrównanie tekstu do środka.
                    modifier = Modifier.weight(1f), // Każdy element zajmuje równą część dostępnej szerokości (1/7).
                    style = MaterialTheme.typography.bodySmall, // Mniejszy styl tekstu.
                    fontWeight = FontWeight.Medium // Średnia grubość czcionki.
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Siatka kalendarza z dniami miesiąca
        // val totalCells = 42 // Maksymalna liczba komórek (6 tygodni * 7 dni), nieużywane bezpośrednio w pętli poniżej
        Column { // Kontener dla wierszy (tygodni) kalendarza.
            // Obliczenie liczby wierszy (tygodni) potrzebnych do wyświetlenia wszystkich dni miesiąca,
            // uwzględniając przesunięcie pierwszego dnia. `+6` i dzielenie przez 7 to sposób na zaokrąglenie w górę.
            val rows = (firstDayOfWeekValue + daysInMonth + 6) / 7
            for (week in 0 until rows) { // Pętla po tygodniach.
                Row(Modifier.fillMaxWidth()) { // Wiersz reprezentujący tydzień.
                    for (dayOfWeekIndex in 0..6) { // Pętla po dniach w tygodniu (0 to np. Niedziela, 6 to Sobota).
                        // Obliczenie indeksu dnia w całej siatce (licząc od 0).
                        val dayIndexInGrid = week * 7 + dayOfWeekIndex
                        // Obliczenie numeru dnia miesiąca dla danej komórki.
                        // Uwzględnia przesunięcie pierwszego dnia miesiąca.
                        val dayOfMonth = dayIndexInGrid - firstDayOfWeekValue + 1

                        // Sprawdzenie, czy obliczony dzień miesiąca mieści się w zakresie dni aktualnego miesiąca.
                        if (dayOfMonth > 0 && dayOfMonth <= daysInMonth) {
                            val date = currentMonth.atDay(dayOfMonth) // Utworzenie obiektu LocalDate dla danego dnia.
                            CalendarDay( // Wywołanie funkcji kompozycyjnej dla pojedynczego dnia.
                                date = date,
                                isSelected = date == selectedDate, // Czy ten dzień jest aktualnie wybrany.
                                isCurrentMonth = true, // Ten dzień należy do aktualnie wyświetlanego miesiąca.
                                onClick = { onDateSelected(date) }, // Akcja po kliknięciu.
                                modifier = Modifier.weight(1f) // Komórka dnia zajmuje równą część szerokości wiersza.
                            )
                        } else {
                            // Pusta komórka dla dni spoza aktualnego miesiąca (aby zachować siatkę).
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f)) // Pusty Box zajmujący miejsce.
                        }
                    }
                }
            }
        }
    }
}

/**
 * Funkcja kompozycyjna wyświetlająca pojedynczy dzień w kalendarzu.
 * @param date Data ([LocalDate]) do wyświetlenia.
 * @param isSelected Czy ten dzień jest aktualnie zaznaczony.
 * @param isCurrentMonth Czy ten dzień należy do aktualnie wyświetlanego miesiąca (umożliwia inne stylowanie np. "szarych" dni z poprzedniego/następnego miesiąca, jeśli byłyby wyświetlane).
 * @param onClick Funkcja zwrotna wywoływana po kliknięciu na ten dzień.
 * @param modifier Modyfikator.
 */
@Composable
fun CalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    isCurrentMonth: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Określenie koloru tła w zależności od tego, czy dzień jest wybrany, czy należy do bieżącego miesiąca.
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer // Kolor dla zaznaczonego dnia.
    else if (!isCurrentMonth) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) // Kolor dla dni spoza miesiąca (lekko przezroczysty).
    else Color.Transparent // Brak tła dla zwykłych dni.

    // Określenie koloru tekstu.
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer // Kolor tekstu na zaznaczonym tle.
    else if (!isCurrentMonth) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) // Kolor tekstu dla dni spoza miesiąca.
    else MaterialTheme.colorScheme.onSurface // Domyślny kolor tekstu.

    // Modyfikator dodający obramowanie, jeśli dzień jest dniem dzisiejszym.
    val borderModifier = if (date == LocalDate.now()) Modifier.border(1.dp, MaterialTheme.colorScheme.primary) else Modifier

    Box( // Kontener dla pojedynczego dnia.
        modifier = modifier
            .aspectRatio(1f) // Utrzymuje kwadratowy kształt komórki.
            .padding(2.dp)   // Mały wewnętrzny odstęp.
            .then(borderModifier) // Zastosowanie modyfikatora obramowania (jeśli dotyczy).
            .background(backgroundColor) // Ustawienie tła.
            .clickable(enabled = isCurrentMonth, onClick = onClick), // Umożliwia kliknięcie tylko jeśli dzień należy do bieżącego miesiąca.
        contentAlignment = Alignment.Center // Wyśrodkowanie zawartości (numeru dnia) w komórce.
    ) {
        Text( // Wyświetlenie numeru dnia.
            text = date.dayOfMonth.toString(),
            color = textColor,
            textAlign = TextAlign.Center,
            fontSize = 14.sp // Rozmiar czcionki dla numeru dnia.
        )
    }
}
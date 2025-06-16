package com.example.calendarweatherapp.data.util // Pakiet dla klas pomocniczych (utility) związanych z danymi.

sealed interface UIState<out T> { // `out T` oznacza, że T jest używane tylko jako typ zwracany (kowariancja)

    /**
     * Reprezentuje stan sukcesu operacji.
     * Przechowuje pobrane lub przetworzone dane.
     * @param data Dane typu T, które zostały pomyślnie uzyskane.
     */
    data class Success<T>(val data: T) : UIState<T> // Dziedziczy po UIState<T>

    /**
     * Reprezentuje stan błędu operacji.
     * Przechowuje komunikat o błędzie do wyświetlenia użytkownikowi.
     * @param message Komunikat tekstowy opisujący błąd.
     */
    data class Error(val message: String) : UIState<Nothing>
    // Dziedziczy po UIState<Nothing>, ponieważ w przypadku błędu nie ma danych typu T.
    // `Nothing` to specjalny typ w Kotlinie, który nie ma żadnych instancji.

    /**
     * Reprezentuje stan ładowania lub przetwarzania danych.
     * Używany, aby UI mogło wyświetlić np. wskaźnik postępu.
     * Zdefiniowany jako `object`, ponieważ nie przechowuje żadnych dodatkowych danych
     * i wystarczy jedna instancja tego stanu.
     */
    object Loading : UIState<Nothing> // Dziedziczy po UIState<Nothing>

    /**
     * (Opcjonalny) Reprezentuje stan bezczynności lub stan początkowy.
     * Przydatny, gdy UI musi odróżnić sytuację, w której operacja jeszcze się nie rozpoczęła,
     * od sytuacji, w której np. nie ma danych po operacji (co mogłoby być `Success(emptyList())`).
     * Zdefiniowany jako `object`.
     */
    object Idle : UIState<Nothing> // Dziedziczy po UIState<Nothing>
}
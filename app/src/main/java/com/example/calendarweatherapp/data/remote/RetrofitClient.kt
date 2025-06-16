package com.example.calendarweatherapp.data.remote // Definiuje pakiet, w którym znajduje się ten obiekt.
// Grupuje elementy związane ze zdalnym dostępem do danych (API).

import com.squareup.moshi.Moshi // Importuje główną klasę biblioteki Moshi, używanej do parsowania JSON.
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory // Importuje fabrykę adapterów Moshi, która umożliwia pracę z klasami Kotlina (w tym data classes).
import okhttp3.OkHttpClient // Importuje klasę OkHttpClient, która jest klientem HTTP używanym przez Retrofit do wykonywania żądań sieciowych.
import okhttp3.logging.HttpLoggingInterceptor // Importuje interceptor (przechwytywacz) z OkHttp, który loguje szczegóły żądań i odpowiedzi HTTP.
import retrofit2.Retrofit // Importuje główną klasę biblioteki Retrofit.
import retrofit2.converter.moshi.MoshiConverterFactory // Importuje fabrykę konwerterów dla Moshi, która integruje Moshi z Retrofit.

/**
 * Obiekt singleton (`object`) o nazwie RetrofitClient.
 * Odpowiada za skonfigurowanie i dostarczenie instancji klienta Retrofit,
 * który będzie używany do komunikacji z API OpenWeatherMap.
 * Wzorzec singleton zapewnia, że w całej aplikacji istnieje tylko jedna instancja
 * tego klienta, co jest dobrą praktyką dla zarządzania zasobami sieciowymi.
 */
object RetrofitClient {
    // Prywatna stała przechowująca bazowy adres URL dla API OpenWeatherMap.
    // Wszystkie ścieżki zdefiniowane w interfejsie ApiService będą dołączane do tego adresu.
    private const val BASE_URL = "https://api.openweathermap.org/"

    // Prywatna właściwość `moshi` inicjalizowana leniwie (`by lazy`).
    // Oznacza to, że instancja Moshi zostanie utworzona tylko raz, przy pierwszym dostępie do tej właściwości.
    // Moshi jest biblioteką do serializacji/deserializacji JSON na obiekty Kotlina i odwrotnie.
    private val moshi: Moshi by lazy {
        Moshi.Builder() // Tworzenie instancji Moshi za pomocą wzorca Builder.
            .add(KotlinJsonAdapterFactory()) // Dodanie fabryki adapterów dla Kotlina, niezbędne do poprawnej pracy z data classes.
            .build() // Zbudowanie finalnej instancji Moshi.
    }

    // Prywatna właściwość `loggingInterceptor` inicjalizowana leniwie.
    // HttpLoggingInterceptor to narzędzie z biblioteki OkHttp, które pozwala na logowanie
    // szczegółów komunikacji HTTP (nagłówki, treść żądania/odpowiedzi).
    // Jest to bardzo przydatne podczas dewelopmentu i debugowania problemów z API.
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            // Ustawienie poziomu logowania.
            // Level.BODY loguje nagłówki i pełną treść żądań oraz odpowiedzi.
            // Level.BASIC loguje tylko podstawowe informacje (metoda, URL, kod odpowiedzi).
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // Prywatna właściwość `okHttpClient` inicjalizowana leniwie.
    // OkHttpClient to klient HTTP, który Retrofit wykorzystuje "pod spodem" do wysyłania żądań.
    // Możemy go skonfigurować, np. dodając interceptory.
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder() // Tworzenie instancji OkHttpClient za pomocą wzorca Builder.
            .addInterceptor(loggingInterceptor) // Dodanie skonfigurowanego wcześniej interceptora logującego.
            .build() // Zbudowanie finalnej instancji OkHttpClient.
    }

    // Prywatna właściwość `retrofit` inicjalizowana leniwie.
    // Jest to główna instancja biblioteki Retrofit.
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder() // Tworzenie instancji Retrofit za pomocą wzorca Builder.
            .baseUrl(BASE_URL) // Ustawienie bazowego adresu URL dla wszystkich żądań.
            .client(okHttpClient) // Ustawienie skonfigurowanego klienta OkHttpClient, który będzie używany do wykonywania żądań.
            .addConverterFactory(MoshiConverterFactory.create(moshi)) // Dodanie fabryki konwerterów.
            // Mówi Retrofit, aby używał Moshi (z naszą instancją `moshi`)
            // do konwersji odpowiedzi JSON na obiekty Kotlina (zdefiniowane w klasach modelu).
            .build() // Zbudowanie finalnej instancji Retrofit.
    }

    /**
     * Publiczna właściwość `apiService` inicjalizowana leniwie.
     * Dostarcza gotową do użycia implementację interfejsu `ApiService`.
     * Retrofit na podstawie definicji interfejsu `ApiService` (metod i adnotacji)
     * generuje konkretną implementację, która potrafi wykonywać żądania sieciowe.
     * Ta właściwość będzie używana w innych częściach aplikacji (np. w Repozytorium)
     * do wywoływania metod API.
     */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java) // Metoda `create` Retrofit generuje implementację podanego interfejsu.
    }
}
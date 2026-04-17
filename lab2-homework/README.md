# Kosmiczny Analizator Aktywności

Aplikacja pobiera dane z dwóch publicznych serwisów:
- kto jest teraz w kosmosie
- jakie starty rakiet są zaplanowane

Potem serwer robi proste obliczenia i pokazuje raport w HTML.

## Co robi aplikacja

- pokazuje statyczną stronę z formularzem
- przyjmuje dane z formularza
- wysyła zapytania do 2 zewnętrznych API
- liczy statystyki
- zwraca raport w postaci strony internetowej
- ma dokumentację Swagger pod adresem /api-docs

## Użyte technologie

- Node.js
- Express
- TypeScript
- Axios
- EJS
- Joi
- Swagger UI

## Zewnętrzne API

- Open Notify  
  https://api.open-notify.org/astros.json
- The Space Devs (Launch Library 2)  
  https://lldev.thespacedevs.com/2.2.0/launch/upcoming/

## Jak uruchomić projekt

1. Zainstaluj zależności:
npm install

2. Uruchom tryb developerski:
npm run dev

3. Otwórz w przeglądarce:
http://localhost:3000

4. Dokumentacja API:
http://localhost:3000/api-docs

## Endpoint API

POST /api/space-summary

Parametry formularza:
- daysLimit
- minAstronauts

Opis:
- daysLimit: ile dni brać do analizy startów
- minAstronauts: pokaż tylko statki, które mają co najmniej tylu astronautów

## Co jest liczone w raporcie

- liczba osób w kosmosie teraz
- liczba osób na każdym statku
- najczęstsza lokalizacja startów
- średnia liczba nadchodzących startów na dzień
- tabela z nadchodzącymi startami

## Ważna uwaga

Raport łączy dane z dwóch różnych momentów czasu:
- liczba astronautów to stan teraz
- starty rakiet to wydarzenia zaplanowane na przyszłość

To nie jest błąd, tylko celowe połączenie dwóch typów danych.

## Obsługa błędów i walidacja

- walidacja danych wejściowych przez Joi
- gdy dane wejściowe są złe, zwracany jest błąd 400
- gdy zewnętrzne API nie działa, zwracany jest błąd 502

## Bezpieczeństwo

- ograniczenie liczby zapytań do API
- podstawowe nagłówki bezpieczeństwa
- ukrycie informacji o technologii serwera
# MarolLink

Niezależna, lokalna aplikacja samochodowa na Androida (podejście clean-room na podstawie `PRD.md`), zaprojektowana pod poziomy ekran systemu multimedialnego auta — **bez zewnętrznych serwerów aktywacyjnych, kodów QR ani kont**.

## Najważniejsze funkcje
- **Pełny interfejs poziomy (landscape)** z dużymi, czytelnymi przyciskami do obsługi na postoju.
- **Lokalne działanie**: wszystkie ustawienia (`Ogólne`, `CarPlay`, `Android Auto`, `AutoKit`, `Panel`, `Informacje`), zapisane urządzenia oraz logi diagnostyczne przechowywane są wyłącznie w urządzeniu.
- **Kalibracja bezpiecznej strefy ekranu + siatka `20 dp`**, przypisywanie przycisków kierownicy, wykrywanie USB (w tym adapterów Carlinkit/AutoKit), wyszukiwanie Bluetooth oraz diagnostyka połączenia bezprzewodowego.
- **Automatyczne aktualizacje OTA (1 kliknięcie)** z GitHub Releases z weryfikacją sumy kontrolnej **SHA-256**.

## Jak wydać nową wersję (OTA na telefon / auto)
1. Zmień `VERSION_CODE` (np. na `2`) i `VERSION_NAME` (np. na `1.0.1`) w pliku `version.properties`.
2. Wyślij zmiany do gałęzi `main` na GitHuba (lub kliknij **Run workflow** w zakładce **Actions**).
3. GitHub Actions automatycznie zbuduje plik `.apk`, podpisze go Twoim stałym kluczem `.jks` z sekretów repozytorium, wyliczy `.sha256` i opublikuje wydanie w **GitHub Releases**.
4. Aplikacja w aucie wykryje nową wersję automatycznie (gdy ma internet) albo po kliknięciu **„Sprawdź aktualizacje”**.

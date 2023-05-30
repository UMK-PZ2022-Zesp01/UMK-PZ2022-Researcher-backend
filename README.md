# JustResearch

### Jest to repozytorium dla serwera backend aplikacji JustResearch.

#### Repozytorium front-end: [JustResearch (Frontend)](https://github.com/UMK-PZ2022-Zesp01/UMK-PZ2022-Researcher-frontend)


## Opis aplikacji

JustResearch to aplikacja stworzona z myślą o badaczach i osobach, które chcą wziąć udział w badaniach naukowych. Nasza aplikacja stawia na wygodę i intuicyjność użytkowania.


## Technologie
Aplikacja JustResearch (Backend) została stworzona przy użyciu następujących technologii:

- Kotlin
- Spring framework
- REST API
- MongoDB

## Funkcjonalności
Poniżej przedstawiamy podstawowe funkcjonalności oferowane przez aplikację JustResearch:

### Dla badaczy:

1. Autoryzacja użytkownika
2. Utworzenie nowego ogłoszenia o badaniu uwzględniając kryteria przynależności do grupy badanej
3. Zarządzanie utworzonymi badaniami
4. Śledzenie ilości zarejestrowanych na badanie użytkowników
5. Odpowiadanie na pytania użytkowników dotyczące badania

### Dla osób chcących wziąć udział w badaniach:

1. Autoryzacja użytkownika
2. Przeglądanie prowadzonych badań
3. Sortowanie i filtrowanie badań
4. Zadanie pytania autorowi badania na publicznym forum lub drogą mailową
5. Rejestracja na wybrane badania


## Wymagania techniczne

- Aplikacja backendowa może zostać uruchomiona przez środowisko uruchomieniowe [Java](https://www.java.com/pl/).

- Testy jednostkowe oraz integracyjne tworzą bazę danych lokalnie. Do ich przeprowadzenia jest potrzebny [Docker](https://docs.docker.com/get-docker/).

## Instrukcje instalacji oraz uruchamiania

Aby zainstalować i uruchomić aplikację JustResearch, wykonaj poniższe kroki:

1. Sklonuj repozytorium:
```
  git clone https://github.com/UMK-PZ2022-Zesp01/UMK-PZ2022-Researcher-backend.git
```

2. Przejdź do folderu projektu:
```
 cd UMK-PZ2022-Researcher-backend
```

3. Zbuduj aplikację:

```
  ./gradlew clean build
```

4. Aby uruchomić aplikację backendową należy przygotować i ustawić odpowienie zmienne środowiskowe:

- `ACCESS_TOKEN_SECRET` - wygenerowany kod potrzebny do poprawnego funkcjonowania tokenów dostępu,
- `MAIL` - adres e-mail podłączany do aplikacji. Będą z niego wysyłane maile weryfikujące konto,
- `MAIL_PASSWORD` - hasło potrzebne do uprawnień wysyłania maili z konta,
- `MONGO_DB_DB` - nazwa bazy MongoDB,
- `MONGO_DB_URI` - adres połączeniowy do bazy MongoDB (zaczynający się od `mongodb+srv://`...),
- `REFRESH_TOKEN_SECRET` - wygenerowany kod potrzebny do poprawnego funkcjonowania tokenów odświeżających,
- `VERIFICATION_TOKEN_SECRET` - wygenerowany kod potrzebny do poprawnego funkcjonowania tokenów weryfikacyjnych,
- `FRONT_URL` - adres URL do części front-endowej aplikacji (np. `https://justresearch.com/`),
- `SSL_KEY_STORE_PWD` - hasło do pliku z certyfikatem do poprawnego, szyfrowanego połączenia https
- `SSL_KEY_PWD` - hasło do pliku z certyfikatem do poprawnego, szyfrowanego połączenia https

5. Zbudowaną wcześniej aplikację backendową możemy uruchomić poleceniem:

```
java -jar justresearch.jar
```
___
Aplikacja backendowa **JustResearch** działa pod portem **8080**, a jej połączenie jest szyfrowane (**ssl**).
___

## Wsparcie techniczne

W aplikacji w panelu użytkownika znajduje się specjalna rubryka z możliwością zgłoszenia błędów/uwag do administratorów aplikacji. Można też skontaktować się mailowo: [researcher.pz2022@gmail.com](mailto://researcher.pz2022@gmail.com)

## Autorzy

* [Mateusz Maszkiewicz](https://github.com/mmaszkie) (opiekun zespołu)
* [Dawid Odolczyk](https://github.com/odolczykd) (kierownik zespołu, programista fullstack)
* [Michał Szczepański](https://github.com/RimbiBimbi1) (sekretarz zespołu, programista fullstack)
* [Paweł Osiński](https://github.com/Osik2000) (programista backend, tester)
* [Konrad Żyra](https://github.com/Zyrekk) (programista fullstack)
* [Jakub Farkasinszki](https://github.com/JJJayKob) (programista frontend)

____
JustResearch to projekt na zaliczenie przedmiotu **Programowanie Zespołowe**.

[Wydział Matematyki i Informatyki](https://www.mat.umk.pl/) - [Uniwersytet Mikołaja Kopernika](https://www.umk.pl) w Toruniu.
____


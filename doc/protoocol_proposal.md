# Client - Server protocol v2 proposal

**Operation name:** `login`

**Request body:**

```json
{
  "user_info": {
    "email": "john@mail.com",
    "password": "QWERTY",
    "team": "Avengers"
  },
  "device_info": {
    "deviceId": "",
    "androidVersion": 18,
    "deviceModel": ""
  }
}
```

* **user_info**: dane logującego się użytkownika:
  * **email**: adres e-mail, który trakujemy jako login
  * **password**: hasło do swojego konta
  * **team**: jeśli podana to użyje jej jako nazwę drużyny, jeżeli nie to pobierze ze serwera
* **device_info**: dane urządzenia
  * **androidVersion**: tylko numer wersji SDK, on mówi wszystko o wersji Androida,
  * **deviceId**: unikalny identyfikator urządzenia,
  * **deviceModel**: model urządzenia.
  
**Response body:**

```json
{
  "name": "John",
  "token": "token",
  "team": "Avengers",
  "settings": {
    "average": 30,
    "max": 120
  }
}
```

* **name**: nazwisko jakie podał podczas rejestracji lub potem ustawił na portalu CREDO,
* **token**: token sparowanego użądzenia, można unieważnić z poziomu
 portalu CREDO (wylogować urządzenie), wysyłanie danych i ping będą ten token zawierać,
 token będzie przechowywany w pamięci urządzenia do momentu wylogowania się,
* **team**: jeśli w request podana to taka sama jak w request,
 a jak nie podana to nazwa drużyny jaką ma obecnie przydzieloną na portalu CREDO.
* **settings**: można by przesłać tu jakieś ustawieni np. `max` i `average` specyficzne dla
logującego się modelu urządzenia
  
**Error:**

```json
{
  "error": "invalid_credentials",
  "message": "Invalid credentials"
}
```

* **error**: tag błędu,
* **message**: komunikat błędu po angielsku.

Jeżeli aplikacja ma taki tag błędu w bazie wielojęzyczności to
wyświetli komunikat z tej bazy na podstawie tego tagu, jeżeli nie
to wyświetli komunikat po angielsku z `message`.

Możliwe komunikaty błędów:
* `invalid_credentials` - zły login lub hasło.

## Register

**Operation name:** `register`

**Request body:**


```json
{
  "user_info": {
    "email": "john@mail.com",
    "name": "John",
    "password": "QWERTY",
    "team": "Avengers"
  },
  "device_info": {
    "deviceId": "",
    "androidVersion": 18,
    "deviceModel": ""
  }
}
```

* **user_info** - znaczenie pól jak w operacji `login`
* **device_info** - przy rejestracji również wysyłamy informację o urządzeniu

**Response body:**

Po poprawnej rejestraci serwer powinien zachować się tak jak po zalogowaniu się, czyli
utworzyć i przysłać token dla tego urządzenia.

```json
{
  "token": "token"
}
```

Możliwe komunikaty błędów:
* `account_exists` - taki email już jest w bazie danych

## Ping

**Operation name:** `ping`

```json
{
  "token": "token",
  "detection_start": 0,
  "battery": 80,
  "temperature": 40,
  "charging": true,
  "average": 0,
  "max": 0,
  "latitude": 0.0,
  "longitude": 0.0,
  "altitude": 0.0,
  "accuracy": 0.0,
  "provider": ""
}
```

Ping wysyłany podczas trwania detekcji aby poinformować serwer, że detekcja nadal trwa.
* **token** - token otrzymany przy logowaniu
* **detection_start** - timestamp (UNIX ms) kiedy rozpoczęto detekcję
* **battery** - poziom naładowania baterii
* **temperature** - temperatura odczytana z termometru w urządzeniu
* **charging** - telefon jest ładowany
* **average** - uśrednione `average` z kamery od ostatniego `ping`,
będziemy wiedzieć dlaczego nie wykrywa bo np. za słabo zasłonił kamerę
* **max** - uśrednione `max` z kamery od ostatniego `ping`, jeżeli dobrze zasłonił kamerę,
to może nie wykrywa bo `max` jest ustawiona na złą wartość
* **latitude** itd. - sprawa dyskusyjna, może niech to potwierdzi w ustawieniach, bo to oznacza
śledzenie gościa

Możliwe komunikaty błędów:
* `invalid_token` - token stracił ważność (wylogowano urządzenie)

## Detection

**Operation name:** `detection`

**Request body:**

```json
{
  "detection": {
    "id": 0,
    "frame_content": "",
    "timestamp": 0,
    "latitude": 0.0,
    "longitude": 0.0,
    "altitude": 0.0,
    "accuracy": 0.0,
    "provider": "",
    "width": 0,
    "height": 0
  },
  "user_info": {
    "token": "token",
    "team": "Avengers"
  },
  "device_info": {
    "deviceId": "",
    "androidVersion": "",
    "deviceModel": "",
    "settings": {
      "average": 30,
      "max": 120    
    }
  }
}
```

* **detection** - detection hit values
  * **id** - autoincremental value,
  * **frame_content** - cropped PNG encoded as BASE64 with detected hit,
  * **timestamp** - UNIX miliseconds timestamp of hit detection,
  * **latitude**, **longitude**, **altitude**, **accuracy** and **provider** - GPS status,
  * **width** - height of camera frame in pixels,
  * **height** - height of camera frame in pixels
* **user_info** - informacje o zalogowanym
  * **team** - na wypadek jak by w czasie detekcji zmienił drużynę, dlatego `team` przesyłamy
  razem z pomiarem,
* **device_info** - najważniejsze są `settings`, które użytkownik sobie może zmienić sam,
teoretycznie jeszcze tylko `androidVersion` może się zmienić jeżeli
użytkownik zaktualizuje Androida, ale może też być tak, że użytkownik sparuje telefon i
potem kupi sobie nowy, zrobi backup ustawień aplikacji i przeniesie na ten backup na nowy,
wtedy może być tak, że aplikacja będzie zalogowana bo będzie `token` ale już z innymi `deviceId` i
`deviceModel`

**Response body:**

Application checks only HTTP return code. When is not 200 then store hit in storage
and try send again later.

Możliwe komunikaty błędów:
* `invalid_token` - token stracił ważność (wylogowano urządzenie)


## Logout

**Operation name:** `logout`

**Request body:**

```json
{
  "token": "token"
}
```

Wylogowanie urządzenia. Powoduje unieważnienie tokenu na serwerze.


## Reset password

**Operation name:** `reset_password`

**Request body:**

```json
{
  "email": "email"
}
```

Serwer wyśle na podany adres e-mail link do resetowania hasła do konta.
 
 
## Detection start/stop

**Operation name:** `detection_state`

**Request body:**

```json
{
  "token": "token",
  "state": "start",
  "timestamp": 0,
  "latitude": 0.0,
  "longitude": 0.0,
  "altitude": 0.0,
  "accuracy": 0.0,
  "provider": "",
  "width": 0,
  "height": 0,
  "device_info": {
    "deviceId": "",
    "androidVersion": "",
    "deviceModel": "",
    "settings": {
      "average": 30,
      "max": 120    
    }
  }
}
```

Poinformowanie serwera o zmianie stanu na temat detekcji:
* **state** - gdy ma wartość `start` to rozpoczęto detekcję, `stop` to zakończono,
w czasie detekcji aplikacja wysyła co jakiś czas komunikaty `ping` więc w przypadku
braku komunikatu `stop` serwer może przyjąć ostatni `ping` za `stop`
* **timestamp** - czas (UNIX ms) w którym nastąpiła zmiana stanu na temat detekcji

Przesyłamy też wszystkie informacje o urządzeniu jak w `detection`, bo brak detekcji
to też ważna informacja dlaczego.

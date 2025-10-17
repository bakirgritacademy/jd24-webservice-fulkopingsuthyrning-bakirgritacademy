# Fulköping RentalHub – REST API för uthyrning av utrustning

Ett komplett REST API i Java/Spring Boot som hanterar uthyrning av utrustning (assets), kunder (customers) och bokningar (bookings). Projektet demonstrerar god praxis kring REST-design, validering, felhantering, säkerhet, dokumentation och datalager.

> **Kort:** 3 entiteter (Asset, Customer, Booking), full CRUD, dubbelbokningsskydd, global felhantering, API‑nyckel, Swagger, automatiskt DB‑schema med Hibernate, seed‑data, Postman-samling och instruktioner för körning/test.

---

## Innehåll
- [Mål & krav](#mål--krav)
- [Arkitektur & designval](#arkitektur--designval)
- [Datamodell (ER-diagram)](#datamodell-er-diagram)
- [Teknikstack & bibliotek](#teknikstack--bibliotek)
- [Komma igång](#komma-igång)
    - [Förkrav](#förkrav)
    - [Konfiguration](#konfiguration)
    - [Körning](#körning)
    - [Seed‑data (data.sql)](#seed-data-datasql)
- [API-dokumentation (Swagger)](#api-dokumentation-swagger)
- [Säkerhet (API-nyckel)](#säkerhet-api-nyckel)
- [Felhantering (GlobalExceptionHandler)](#felhantering-globalexceptionhandler)
- [DTO:er & serialisering](#dtoer--serialisering)
- [Endpoints & exempel](#endpoints--exempel)
    - [Assets](#assets)
    - [Customers](#customers)
    - [Bookings / Rentals](#bookings--rentals)
- [Dubbelbokning, transaktioner & samtidighet](#dubbelbokning-transaktioner--samtidighet)
- [Radering & beroenden](#radering--beroenden)
- [Testning (Postman)](#testning-postman)
- [VG-checklista](#vg-checklista)
- [Licens & författare](#licens--författare)

---

## Mål & krav

**Mål**
- Visa hur man bygger REST API:n med god praxis: resurser, HTTP-metoder, statuskoder.
- Validera indata, hantera fel på ett enhetligt sätt.
- Dokumentera med Swagger/OpenAPI.
- Demonstrera enkel autentisering (API-nyckel).

**Uppgiftskrav (sammanfattning)**
- Minst 3 entiteter: Asset (sak), Customer (person), Booking (relation).
- CRUD för alla entiteter.
- Validering av indata (ej tomma fält, rimlig logik).
- Felsvar med korrekta statuskoder (400, 404, 409, 500).
- JSON in/ut.
- Spring Boot 3+, Java 17+.
- Dokumentation via Swagger/OpenAPI.
- Git-repo med README.md (denna fil).
- För VG: enkel autentisering, förhindra dubbelbokning, hantera samtidighet, återanvändbar kod, tredjepartsbibliotek, Postman-samling.

---

## Arkitektur & designval

- **Lager**: Controller → Service → Repository → DB.
- **DTO:er**: Controllers exponerar *endast* DTO-objekt (AssetDTO, CustomerDTO, BookingDTO) för att undvika rekursiv serialisering och för att dölja interna fält.
- **Service**: `RentalCoordinatorService` kapslar in affärsregler: dubbelbokningsskydd, återlämning, statusövergångar.
- **Repository**: Spring Data JPA repositories med härledda metoder (t.ex. `existsByAsset_IdAndActiveTrue`).
- **Validering**: Bean Validation‑annoteringar på entiteter (t.ex. `@NotBlank`, `@Positive`).
- **Felhantering**: `GlobalExceptionHandler` returnerar enhetliga JSON-fel.
- **Säkerhet**: Enkel API‑nyckel i headern `X-API-KEY` via `ApiKeyFilter` + konfigurerad i Swagger.
- **Dokumentation**: Swagger via springdoc‑openapi, med “Authorize”-knapp för API‑nyckel.
- **DB-schema**: Hibernate skapar tabellerna automatiskt. `data.sql` seedar testdata.
- **Serialisering**: DTO bryter cirkulära referenser som annars kan ge “infinite recursion” eller för djupt dokument.

---

## Datamodell (ER-diagram)

```
+-------------+        1   *        +-------------+        *   1        +-------------+
|  Customer   |--------------------->|   Booking   |<---------------------|    Asset    |
+-------------+                      +-------------+                      +-------------+
| id (PK)     |                      | id (PK)     |                      | id (PK)     |
| firstName   |                      | startDate   |                      | assetName   |
| lastName    |                      | endDate     |                      | category    |
| email (UQ)  |                      | active      |                      | dailyRate   |
| phone       |                      | note        |                      | available   |
+-------------+                      | customer_id |                      +-------------+
                                     | asset_id    |
                                     +-------------+
```

- En **Customer** kan ha många **Booking**.
- En **Asset** kan ha många **Booking**.
- **Booking** kopplar samman **Asset** och **Customer**.

---

## Teknikstack & bibliotek

| Komponent | Version/exempel | Syfte |
|---|---|---|
| Java | 17 | Språk |
| Spring Boot | 3.5.x | Ramverk |
| Spring Web | – | REST/HTTP |
| Spring Data JPA | – | ORM mot DB |
| Hibernate | 6.x | JPA-implementation & DDL |
| MySQL | 8/9 (Connector/J 9.4) | Databas |
| Jakarta Validation | – | Bean Validation på entiteter |
| Lombok | – | Minskar boilerplate (getters/setters/builders) |
| springdoc-openapi | 2.x | Swagger UI & OpenAPI 3 |
| SLF4J | – | Loggning |


---

## Komma igång

### Förkrav
- Java 17+ installerat (`java -version`).
- Maven 3.9+ (`mvn -v`).
- MySQL igång lokalt med ett konto med rättigheter att skapa tabeller.

### Konfiguration

`src/main/resources/application.properties` (exempel):

```properties
# Datasource
spring.datasource.url=jdbc:mysql://localhost:3306/rentalhubdb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Init-script (data.sql) körs automatiskt
spring.sql.init.mode=always

# API-nyckel (läst av ApiKeyFilter)
app.api.key=fulkoping-secret-2025

# Springdoc OpenAPI (valfritt finlir)
springdoc.swagger-ui.path=/swagger-ui/index.html
springdoc.api-docs.path=/v3/api-docs
```

- `ddl-auto=create-drop` skapar tabeller vid uppstart och droppar vid nedstängning. För produktion: använd `validate`/`update`/migrations (Flyway).

### Körning

```bash
# I projektroten (där pom.xml ligger)
mvn clean spring-boot:run
# eller
mvn clean package
java -jar target/rentalhub-*.jar
```

### Seed‑data (data.sql)

`src/main/resources/data.sql` (ingår i projektet):

```sql
INSERT INTO rhub_customers (id, first_name, last_name, email, phone)
VALUES
    (1, 'Anna', 'Karlsson', 'anna.karlsson@example.com', '0701111111'),
    (2, 'Johan', 'Nilsson', 'johan.nilsson@example.com', '0702222222'),
    (3, 'Sara', 'Persson', 'sara.persson@example.com', '0703333333'),
    (4, 'Mikael', 'Berg', 'mikael.berg@example.com', '0704444444');

INSERT INTO rhub_assets (id, asset_name, category, daily_rate, available)
VALUES
    (1, 'Borrmaskin', 'Verktyg', 150.0, true),
    (2, 'Släpvagn', 'Fordon', 400.0, true),
    (3, 'Projektor', 'Elektronik', 250.0, true);

INSERT INTO rhub_bookings (id, asset_id, customer_id, start_date, end_date, active, note)
VALUES
    (1, 1, 1, '2025-10-01', NULL, true, 'För renovering');
```

> Körs automatiskt vid uppstart tack vare `spring.sql.init.mode=always`. Om du kör mot en befintlig databas, se till att tabellnamn/kolumner matchar entiteterna.

---

## API-dokumentation (Swagger)

- UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Projektet innehåller en Swagger‑konfiguration som:
- Sätter titel/metadata.
- Definierar API‑nyckel `X-API-KEY` som en Security Scheme.
- Visar “Authorize”-knapp i Swagger UI.

---

## Säkerhet (API-nyckel)

- `ApiKeyFilter` kräver headern `X-API-KEY` för **POST/PUT/DELETE**.
- `GET` och Swagger är öppna.
- Nyckeln konfigureras i `application.properties` med `app.api.key`.
- `SecurityConfig` kopplar in filtret och stänger av formulär/HTTP Basic.

Exempel med cURL:

```bash
# GET (öppet, ingen nyckel krävs)
curl -X GET "http://localhost:8080/api/assets" -H "accept: application/json"

# POST (kräver nyckel)
curl -X POST "http://localhost:8080/api/assets" \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: fulkoping-secret-2025" \
  -d '{"assetName":"Kaffemaskin","category":"Elektronik","dailyRate":120.0,"available":true}'
```

---

## Felhantering (GlobalExceptionHandler)

Alla fel returneras med enhetlig struktur (UTF‑8):

```json
{
  "timestamp": "2025-10-16T15:29:21.3781522",
  "status": 400,
  "error": "Bad Request",
  "message": "E-post krävs"
}
```

- `400` – valideringsfel/ogiltig begäran (`IllegalArgumentException`, `MethodArgumentNotValidException`).
- `404` – resurs saknas (`NoSuchElementException`).
- `409` – konflikt (t.ex. dubbelbokning/aktiv bokning finns).
- `500` – oväntat fel.

`ApiKeyFilter` svarar i JSON med UTF‑8 för `401 Unauthorized` när nyckel saknas/felaktig.

---

## DTO:er & serialisering

För att undvika rekursiva loopar (Asset → Booking → Asset → …) använder API:t DTO:er:
- `AssetDTO`: id, assetName, category, dailyRate, available, (ev. historik)
- `CustomerDTO`: id, firstName, lastName, email, phone, (ev. historik)
- `BookingDTO`: id, startDate, endDate, active, note, assetId/assetName, customerId/customerName

Controllers returnerar DTO:er utåt. Entities används internt i service/repository.

---

## Endpoints & exempel

### Assets

Base path: `/api/assets`

| Metod | Path | Beskrivning | Body | Svar |
|---|---|---|---|---|
| GET | `/` | Lista alla tillgångar | – | `200 OK` `List<AssetDTO>` |
| GET | `/available` | Lista tillgängliga tillgångar | – | `200 OK` `List<AssetDTO>` |
| GET | `/{id}` | Hämta en tillgång | – | `200 OK` `AssetDTO` / `404` |
| POST | `/` | Skapa ny tillgång | `AssetDTO` | `201 Created` `AssetDTO` / `400` |
| PUT | `/{id}` | Uppdatera befintlig tillgång | `AssetDTO` | `200 OK` `AssetDTO` / `404` |
| DELETE | `/{id}` | Ta bort tillgång | – | `204 No Content` / `409` om aktiv bokning |

Exempel (POST):
```bash
curl -X POST "http://localhost:8080/api/assets" \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: fulkoping-secret-2025" \
  -d '{"assetName":"Slagborr","category":"Verktyg","dailyRate":180.0,"available":true}'
```

### Customers

Base path: `/api/customers`

| Metod | Path | Beskrivning | Body | Svar |
|---|---|---|---|---|
| GET | `/` | Lista alla kunder | – | `200 OK` `List<CustomerDTO>` |
| GET | `/{id}` | Hämta kund | – | `200 OK` `CustomerDTO` / `404` |
| POST | `/` | Skapa kund | `CustomerDTO` | `201 Created` `CustomerDTO` / `400/409` |
| PUT | `/{id}` | Uppdatera kund | `CustomerDTO` | `200 OK` `CustomerDTO` / `404` |
| DELETE | `/{id}` | Ta bort kund | – | `204 No Content` / `409` om aktiv bokning |

Exempel (POST):
```bash
curl -X POST "http://localhost:8080/api/customers" \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: fulkoping-secret-2025" \
  -d '{"firstName":"Lina","lastName":"Sund","email":"lina.sund@example.com","phone":"0705555555"}'
```

### Bookings / Rentals

Base path: `/api/rentals`

| Metod | Path | Beskrivning | Body | Svar |
|---|---|---|---|---|
| POST | `/book/{assetId}/customer/{customerId}` | Skapa bokning | `BookingDTO` (valfritt: startDate, note) | `201 Created` `BookingDTO` / `409` dubbelbokning / `404` |
| PUT | `/return/{bookingId}` | Återlämna (avsluta) bokning | – | `200 OK` `BookingDTO` / `404/409` |
| GET | `/history/asset/{assetId}` | Historik per tillgång | – | `200 OK` `List<BookingDTO>` |
| GET | `/history/customer/{customerId}` | Historik per kund | – | `200 OK` `List<BookingDTO>` |
| GET | `/bookings` | Lista alla bokningar (om implementerad) | – | `200 OK` `List<BookingDTO>` |
| GET | `/bookings/{id}` | Hämta bokning (om implementerad) | – | `200 OK` `BookingDTO` / `404` |
| DELETE | `/bookings/{id}` | Ta bort bokning (om affärsregler tillåter) | – | `204 No Content` / `409` |

Exempel (skapa bokning):
```bash
curl -X POST "http://localhost:8080/api/rentals/book/1/customer/1" \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: fulkoping-secret-2025" \
  -d '{"note":"Weekendhyrning"}'
```

Exempel (återlämna):
```bash
curl -X PUT "http://localhost:8080/api/rentals/return/1" \
  -H "X-API-KEY: fulkoping-secret-2025"
```

---

## Dubbelbokning, transaktioner & samtidighet

- **Dubbelbokningsskydd** i `RentalCoordinatorService.registerBooking()`:
    - Kontrollerar `asset.isAvailable()`.
    - Kontrollerar `bookingRepo.existsByAsset_IdAndActiveTrue(assetId)`.
    - Sätter `asset.available = false` när bokningen skapas.
- **@Transactional** på metoder som skapar/avslutar bokning för atomära ändringar.
- Rekommenderat för produktion: databaslås/unik indexering eller tidsintervallskontroll om bokningar kan överlappa i datum.

---

## Radering & beroenden

- **Tillgång/Kund** får **inte** raderas om aktiv bokning finns → `409 Conflict`.
- Historiska bokningar kan behållas (rekommenderas) av spårbarhetsskäl. OrphanRemoval används inte för att inte tappa historik oavsiktligt.
- Controllers kontrollerar före DELETE via service/repo.

---

## Testning (Postman)

En Postman‑samling ingår i projektet (se projektroten). Den innehåller:
- Miljövariabel `baseUrl` (ex: `http://localhost:8080`).
- Förifylld header `X-API-KEY`.
- Exempel för samtliga endpoints.

Importera samlingen i Postman och kör anropen i ordning (list → create → update → delete, samt book → return).

---

## VG-checklista

- [x] Enkel autentisering via API‑nyckel (`X-API-KEY`) och Swagger “Authorize”.
- [x] Förhindra dubbelbokning: logik i service + repo‑metod `existsByAsset_IdAndActiveTrue`.
- [x] Hantera samtidighet: transaktioner och statusflagg `available` på Asset.
- [x] Tredjepartsbibliotek: Lombok, Hibernate Validator, springdoc‑openapi.
- [x] Återanvänd kod: central `RentalCoordinatorService`, gemensam felhantering, repositories.
- [x] Postman‑samling för testning.

---



## Licens & författare

- Författare: Fulköping RentalHub‑teamet.
- Kontakt: support@fulkoping-rentalhub.se

---

## Placering av README.md

Denna README.md ska ligga **i projektroten** (samma nivå som `pom.xml`). GitHub visar den då automatiskt på repositoriets startsida.

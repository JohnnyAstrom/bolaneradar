# BolåneRadar – Backend

Detta dokument beskriver backend-delen av BolåneRadar.

För en övergripande beskrivning av systemet, användarflöden och funktionalitet,
se projektets huvud-README i repository-roten.

Backend är byggd med Spring Boot och ansvarar för datainsamling, analys,
historik och API-exponering av svenska bolåneräntor samt Smart Ränte-testets
analysresultat till frontend.

---

# Funktionalitet

### Banker och räntor
- CRUD för banker
- Skapa och uppdatera bolåneräntor via admin-API
- Publika endpoints för:
  - räntehistorik
  - nuvarande räntor
  - bankintroduktion
  - bankdetaljer
  - jämförelser mellan banker
  - senaste uppdateringar
- Tydlig separation mellan admin-lager och publikt API

### Smart Ränte-test
- Publikt API för analys av användarens ränta
- Stöd för analys:
  - utan offert (jämförelse mot marknadens snitt)
  - med offerter (jämförelse mellan flera erbjudanden)
- Beräkning av:
  - marknadsavvikelse
  - årlig kostnadsskillnad
- Konsumentvänligt analysresultat anpassat för frontend

### Scraping av räntor
- Flera bank-specifika `BankScraper`-implementationer
- Central `ScraperService` som orkestrerar scraping
- Loggning via `RateUpdateLogService`
- Felhantering och dubblettkontroll
- Manuell scraping via admin-API
- E-postnotifiering vid fel

### Loggning & historik
- `RateUpdateLog` loggar varje scrapingkörning
- Publika och admin-endpoints för loggar
- Hämta senaste uppdatering per bank

### Schedulerade jobb
- **DatabaseBackupScheduler** – skapar dagliga `.dump`- och `.sql`-backuper
- **ScraperScheduler** – kör scraping automatiskt

### Säkerhet
- Basic Auth för alla admin-routes
- Alla GET-endpoints är publika
- POST / PUT / DELETE under `/api/**` kräver autentisering
- Swagger UI aktiverat med Basic Auth-stöd

### Tester
- Enhetstester (Mockito + JUnit 5)
- Integrationstester (MockMvc)
- Täckning av controllers, adminservices och scraperlogik

---


# Projektstruktur

```
backend/
 ├── controller/
 │    ├── api/                         # Publika API-endpoints
 │    │     ├── banks/                 # Bankinformation & historik
 │    │     ├── rates/                 # Aktuella räntor & jämförelser
 │    │     └── smartrate/             # Smart Ränte-test
 │    │
 │    └── admin/                       # Admin-API (skyddade endpoints)
 │          ├── banks/                 # Hantering av banker
 │          ├── rates/                 # Manuell hantering av räntor
 │          ├── scraper/               # Manuell scraping
 │          ├── logs/                  # Scraping-loggar
 │          └── dev/                   # Utvecklingshjälp (DEV-profil)
 │
 ├── service/
 │    ├── client/                      # Logik för publika vyer
 │    ├── smartrate/                   # Smart Ränte-test – analys
 │    │     ├── SmartRateAnalysisService
 │    │     ├── SmartRateMarketDataService
 │    │     └── model/
 │    ├── admin/                       # Adminlogik
 │    └── integration/
 │          └── scraper/
 │                ├── core/            # ScraperService & gemensam logik
 │                └── banks/           # Bank-specifika scrapers
 │
 ├── repository/                       # Databasåtkomst
 ├── entity/
 │    └── core/                        # Domänentiteter
 │
 ├── dto/
 │    ├── api/                         # DTO:er för publika API
 │    │     ├── banks/
 │    │     ├── rates/
 │    │     └── smartrate/
 │    ├── admin/                       # DTO:er för admin-API
 │    └── mapper/                      # Mapping mellan entiteter och DTO
 │
 ├── scheduler/                        # Schemalagda jobb
 ├── config/                           # Spring- & säkerhetskonfiguration
 ├── exception/                        # Centraliserad felhantering
 │
 └── BolaneradarBackendApplication.java

```

---

# Arkitektur

## Lagerindelning
- **Controller-lagret**
  - Exponerar HTTP-endpoints
  - Publika controllers under ```/api/**```
  - Admin controllers under ```/api/admin/**```
- **Service-lagret**
  - Innehåller all affärslogik
  - Uppdelat i:
    - ``service.client`` - data till frontend
    - ``service.smartrate`` - analyslogik och beslutsstöd
    - ``service.admin`` - interna funktioner
    - ``service.integration.scraper`` - extern datainsamling
- **Repository-lagret**
  - Ansvarar för all databasinteraktion
  - Anropas endast från service-lagret
- **DTO/Mapper-lager** 
  - Skyddar domänentiteter från direkt exponering
  - Uppdelad i ``dto.admin`` och ``dto.api``
- **Scheduler-lager**
  - Isolerar automatiserade jobb (scraping och backup)

---

# Scraping som integrationssubsystem

Scraping är ett centralt integrationssubsystem i BolåneRadar och ansvarar för
insamling av externa räntedata från banker.

Subsystemet är medvetet isolerat från övrig affärslogik och används endast via ```ScraperService```.

```java
public interface BankScraper {
  List<MortgageRate> scrapeRates(Bank bank) throws Exception;
}
```

## Principer

- Varje bank har sin egen scraperklass
- Varje scraper ansvarar för att:
  - hämta data från bankens räntesida
  - parsa HTML, tabeller eller API-format
  - omvandla datan till ``MortgageRate``
- Scraper-klasser innehåller ingen affärslogik

## ScraperService
``ScraperService`` orkestrerar hela scrapingflödet och ansvarar för:
- matchning av bank → rätt scraper
- felhantering
- dubblettkontroll
- beräkning av ``rateChange`` och ``lastChangedDate``
- persistens av nya räntor
- loggning och e-postnotifiering vid fel

# API-översikt

### Banker
```
GET /api/banks/intro/{bankKey}
GET /api/banks/details/{bankKey}
GET /api/banks/history/{bankKey}
```

### Räntor
```
GET /api/rates/current/{bankKey}
GET /api/rates/comparison
GET /api/rates/updates/latest
```

### Smart Ränte-test
```
POST /api/smartrate/analyze
```

### Admin
```
POST /api/admin/rates
POST /api/admin/scrape/all
POST /api/admin/scrape/{bankName}
GET  /api/admin/logs
GET  /api/admin/logs/latest
```

---

# Säkerhet
Backend använder en tydlig och enkel säkerhetsmodell baserad på Basic Authentication.

- Alla publika GET-endpoints är öppna
- Alla admin-endpoints kräver autentisering med rollen ``ROLE_ADMIN``
- POST/PUT/DELETE är alltid skyddade
- Sessions är stateless
- Säkerheten är konfigurerad i ``SecurityConfig``

---

# Swagger

- Swagger UI är aktiverat via OpenAPI-konfiguration
- Alla publika och admin-endpoints listas
- Admin-endpoints kan testas direkt via Swagger UI
- Inloggning sker via Basic Auth

```
http://localhost:8080/swagger-ui.html
```

# Scheduler-jobb

### ScraperScheduler
Kör scraping automatiskt (om aktiverad).

### DatabaseBackupScheduler
Tar dagliga PostgreSQL-backups i både .dump och .sql.

---

# Tester

### Enhetstester
- Service-lager
- Scraperlogik
- Hjälp- och stödtjänster


### Integrationstester (MockMvc)
- Publika controllers
- Adminflöden
- Scraper-endpoints

---

# Installation & Körning

### 1. Klona projektet
```
git clone https://github.com/JohnnyAstrom/bolaneradar
```

### 2. Kör PostgreSQL lokalt

Skapa en databas:
```
CREATE DATABASE bolaneradar;
```

### 3. Skapa `application.properties`

Utgå från:
```
src/main/resources/application-example.properties
```

### 4. Starta applikationen
```
mvn spring-boot:run
```

### 5. Öppna Swagger UI
```
http://localhost:8080/swagger-ui.html
```

---

# Backup-system

``DatabaseBackupScheduler`` använder ``pg_dump`` för att spara:
- `.dump` (binär)
- `.sql` (text)

Sparas i `/backups`.

---

# Licens

Detta projekt är licensierat under MIT-licensen.

Du är fri att använda, kopiera och modifiera koden för personligt eller utbildningssyfte. Eftersom projektet fortfarande utvecklas av mig privat kan licensen komma att ändras i framtiden.

Se `LICENSE`-filen för full licenstext.

---



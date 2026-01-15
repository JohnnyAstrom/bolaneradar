# BolåneRadar – Backend

Detta dokument beskriver backend-delen av BolåneRadar.

För en övergripande beskrivning av projektet, systemets syfte och användarflöden,
se huvud-README i repository-roten.

Backenden är byggd med **Java + Spring Boot** och ansvarar för:
- insamling och lagring av bolåneräntor
- historik och jämförelselogik
- Smart Ränte-testets analys
- exponering av ett publikt REST-API till frontend

---

## Funktionalitet

### Banker och räntor
- Hantering av banker och deras metadata
- Lagring av aktuella och historiska bolåneräntor
- Publika API-endpoints för:
  - bankintroduktion
  - bankdetaljer (”passar bäst för / mindre bra för”)
  - aktuella räntor
  - historisk ränteutveckling
  - jämförelser mellan banker
  - senaste ränteuppdateringar

All affärslogik ligger i service-lagret och exponeras via tydligt avgränsade API-endpoints.

---

### Smart Ränte-test
- Publikt API för analys av användarens ränta
- Stöd för två huvudflöden:
  - **utan offert** – jämförelse mot marknadens snittränta
  - **med offerter** – analys och rangordning av flera erbjudanden
- Beräknar bland annat:
  - marknadsavvikelse
  - relativ kostnadsnivå
  - rekommenderad åtgärd
- Resultatet är konsumentvänligt formaterat för direkt användning i frontend

---

### Scraping av räntor
- Varje bank har en egen `BankScraper`-implementation
- Central `ScraperService` ansvarar för:
  - matchning bank till scraper
  - felhantering
  - dubblettkontroll
  - beräkning av ränteförändring och senaste ändringsdatum
  - persistens av nya räntor
  - loggning och notifiering vid fel

Scraping är **helt frikopplad från användarflödet** och påverkar inte frontendens tillgänglighet.

---

### Loggning & historik
- Varje scrapingkörning loggas via `RateUpdateLog`
- Loggar innehåller:
  - tidsstämpel
  - status
  - antal importerade räntor
  - eventuella fel
- Publika och admin-endpoints för att hämta senaste uppdateringar

---

## Schemaläggning

Backenden innehåller **ingen intern schemaläggning för scraping** i produktion.

Automatisk scraping initieras **externt via GitHub Actions**, där schemalagda
arbetsflöden anropar backendens skyddade API-endpoints för datainhämtning.

Backenden fungerar därmed som ett **rent API och datalager**, medan:
- schemaläggning
- exekvering
- driftansvar

hanteras utanför applikationen.

Denna uppdelning:
- minskar komplexiteten i backend
- förenklar drift och felsökning
- gör lösningen enklare att testa och vidareutveckla

Utöver automatiserade körningar finns även stöd för:
- manuell scraping via skyddade admin-endpoints
- lokala körningar vid felsökning och verifiering

---

## Projektstruktur

```
backend/
 ├── controller/
 │    ├── api/                         # Publika API-endpoints
 │    │     ├── banks/                 # Bankinformation & historik
 │    │     ├── rates/                 # Aktuella räntor & marknadsjämförelser
 │    │     └── smartrate/             # Smart Ränte-test
 │    │
 │    └── admin/                       # Admin-API (skyddade endpoints)
 │          ├── banks/                 # Hantering av banker
 │          ├── rates/                 # Manuell hantering av räntor
 │          ├── scraper/               # Manuell scraping
 │          ├── logs/                  # Scraping-loggar
 │          └── dev/                   # Utvecklings- och felsökningsendpoints (DEV-profil)
 │
 ├── service/
 │    ├── client/                      # Data till frontend
 │    ├── smartrate/                   # Smart Ränte-test – analyslogik
 │    ├── admin/                       # Adminlogik
 │    └── integration/
 │          └── scraper/
 │                ├── core/            # ScraperService & gemensam logik
 │                └── banks/           # Bank-specifika scrapers
 │
 ├── repository/                       # Databasåtkomst
 ├── entity/                           # Domänentiteter                    
 │
 ├── dto/
 │    ├── api/                         # DTO:er för publika API
 │    ├── admin/                       # DTO:er för admin-API
 │    └── mapper/                      # Mapping mellan entiteter och DTO
 │
 ├── config/                           # Spring- & säkerhetskonfiguration
 ├── exception/                        # Centraliserad felhantering
 │
 └── BolaneradarBackendApplication.java

```

---

## Arkitekturöversikt

Backenden är uppbyggd enligt en lagerindelad arkitektur med tydlig separation av ansvar.

### Lager

- **Controller-lager**
  - Exponerar REST-endpoints
  - Publika endpoints under `/api/**`
  - Skyddade admin-endpoints under `/api/admin/**`

- **Service-lager**
  - Innehåller all affärs- och analyslogik
  - Uppdelat i:
    - `service.client` – data anpassad för frontend
    - `service.smartrate` – analys och beslutslogik
    - `service.admin` – interna och administrativa funktioner
    - `service.integration.scraper` – extern datainsamling

- **Repository-lager**
  - Ansvarar för all databasåtkomst
  - Anropas endast från service-lagret

- **DTO- och mapper-lager**
  - Separerar interna entiteter från API-kontrakt
  - Uppdelat i publika (`dto.api`) och administrativa (`dto.admin`) DTO:er

Automatisering och schemaläggning sker **utanför applikationen** och är därför
inte en del av backendens interna lagerstruktur.

---

## Scraping som integrationssubsystem

Scraping är ett isolerat integrationssubsystem som ansvarar för insamling av
externa räntedata från banker.

Subsystemet används endast via `ScraperService`.

```java
public interface BankScraper {
  List<MortgageRate> scrapeRates(Bank bank) throws Exception;
}
```

## Principer

- Varje bank har sin egen scraperklass
- Varje scraper ansvarar för att:
  - hämta data från bankens räntesida
  - parsa HTML eller strukturerad data
  - omvandla resultatet till `MortgageRate`
- Scraper-klasser innehåller ingen affärslogik

## ScraperService
``ScraperService`` orkestrerar hela scrapingflödet och ansvarar för:
- matchning av bank → rätt scraper
- felhantering
- dubblettkontroll
- beräkning av ``rateChange`` och ``lastChangedDate``
- persistens av nya räntor
- loggning och e-postnotifiering vid fel

---

## API-översikt

### Banker
```
GET /api/banks/{bankKey}/intro
GET /api/banks/{bankKey}/details
GET /api/banks/{bankKey}/info
```

### Räntor
```
GET /api/banks/{bankKey}/rates
GET /api/banks/{bankKey}/history/data
GET /api/banks/{bankKey}/history/available-terms
GET /api/rates/comparison
```

### Smart Ränte-test
```
POST /api/smartrate/test
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

## Säkerhet
Backend använder en tydlig och enkel säkerhetsmodell baserad på Basic Authentication.

- Alla publika GET-endpoints är öppna
- Alla admin-endpoints kräver autentisering med rollen ``ROLE_ADMIN``
- POST/PUT/DELETE är alltid skyddade
- Sessions är stateless
- Säkerheten är konfigurerad i ``SecurityConfig``

---

## Swagger

- Swagger UI är aktiverat via OpenAPI-konfiguration
- Alla publika och admin-endpoints listas
- Admin-endpoints kan testas direkt via Swagger UI
- Inloggning sker via Basic Auth

```
http://localhost:8080/swagger-ui.html
```

---

## Tester

### Enhetstester
- Service-lager
- Scraperlogik
- Hjälp- och stödtjänster


### Integrationstester (MockMvc)
- Publika controllers
- Adminflöden
- Scraper-endpoints

---

## Installation & Körning

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

# Licens

Detta projekt är licensierat under MIT-licensen.

Du är fri att använda, kopiera och modifiera koden för personligt eller utbildningssyfte. Eftersom projektet fortfarande utvecklas av mig privat kan licensen komma att ändras i framtiden.

Se `LICENSE`-filen för full licenstext.

---



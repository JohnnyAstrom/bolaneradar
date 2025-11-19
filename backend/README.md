# BolåneRadar - Backend

En komplett backend byggd med **Spring Boot**, ansvarig för att hämta, lagra, analysera och exponera svenska bolåneräntor. Projektet innehåller scraping, trendanalys, schedulerade jobb, e-postnotifieringar, databasbackup och ett omfattande API.

---

# Funktionalitet

### Hantering av banker och räntor  
- CRUD för banker  
- CRUD för bolåneräntor  
- DTO + Mapper-lager för säker exponering till frontend

### Analysfunktioner  
- Trendanalys mellan två datum  
- Trender över hela marknaden  
- Bank-specifik historik  
- Filtrering på datum, räntetyp och term

### Scraping av räntor  
- Flera `BankScraper`-implementationer  
- ScraperService som kör scraping för alla banker eller en specifik  
- Dubblettkontroll  
- RateChange + LastChangedDate-beräkning  
- Loggning av resultat  
- Mail-notifiering vid fel

### Loggning & Historik  
- RateUpdateLog loggar varje scrapinghändelse  
- API för att visa senaste loggar per bank eller alla loggar

### Schedulerade jobb  
- **DatabaseBackupScheduler** - skapar dagliga `.dump` + `.sql`-backup  
- **ScraperScheduler** - kör scraping varje dag

### Säkerhet  
- Basic Auth för POST/DELETE och admin routes  
- Alla GET-endpoints (förutom scrapers) är publika  
- Swagger UI aktiverat med Basic Auth-stöd

### Tester  
- Enhetstester (Mockito + JUnit 5)  
- Integrationstester (MockMvc)  
- Full täckning av controllers, services och scraperlogik  

---

# Projektstruktur

```
backend/
 ├── controller/
 │    ├── analytics/
 │    └── core/
 ├── service/
 │    ├── analytics/
 │    ├── core/
 │    └── integration/scraper/
 ├── repository/
 ├── entity/
 │    ├── analytics/
 │    └── core/
 ├── dto/
 │    ├── analytics/
 │    ├── core/
 │    └── mapper/
 ├── scheduler/
 ├── config/
 ├── exception/
 ├── BolaneradarBackendApplication.java
 ├── application.properties
 └── tests/
```

---

# Arkitektur

### Lagren är tydligt separerade:
- **Controller-lagret** tar emot HTTP-anrop  
- **Service-lagret** innehåller all affärslogik  
- **Repository-lagret** hanterar SQL och databasinteraktion  
- **DTO/Mapper-lager** skyddar entiteter  
- **Scheduler-lager** automatiserar dagliga jobb  
- **Scraper-lager** hämtar data externt  

---

# Datamodeller

### Bank
- id  
- name  
- website  
- mortgageRates (OneToMany)

### MortgageRate  
- bank (ManyToOne)  
- term (enum)  
- rateType (enum)  
- ratePercent  
- effectiveDate  
- rateChange  
- lastChangedDate

### RateUpdateLog  
- occurredAt  
- sourceName  
- importedCount  
- success  
- errorMessage  
- bank  
- durationMs  

### RateTrend (intern modell)  
Representerar hur en specifik ränta har förändrats mellan två mätta datum (t.ex. föregående och senaste värde).

---

# API-översikt

## Bank-endpoints
```
GET  /api/banks
GET  /api/banks/{id}
POST /api/banks
DELETE /api/banks/{id}
```

## Rate-endpoints
```
GET  /api/rates
GET  /api/rates/bank/{id}
POST /api/rates
```

## Analytics
```
GET /api/rates/analytics/trends
GET /api/rates/analytics/trends/range?from=DATE&to=DATE
GET /api/rates/analytics/history/bank/{id}
GET /api/rates/analytics/history/all-banks
```

## Loggar
```
GET /api/rates/updates
GET /api/rates/updates/bank/{id}
GET /api/rates/updates/latest
```

## Scraping (skyddade)
```
GET /api/scrape/all
GET /api/scrape/{bankName}
```

## Admin (skyddade)
```
POST   /api/admin/import-example
DELETE /api/admin/clear
DELETE /api/admin/delete-rates?bankName=X
```

---

# Säkerhet

- Basic Auth aktiverad  
- GET-anrop är öppna  
- POST/DELETE kräver `ROLE_ADMIN`  
- Swagger UI stöder Basic Auth direkt

`SecurityConfig` hanterar detta.

---

# Scheduler-jobb

### ScraperScheduler
Kör scraping 10:00 varje dag.

### DatabaseBackupScheduler
Tar backup av PostgreSQL-databasen:
- `.dump` - binär  
- `.sql` - läsbar  

Kör 09:50 dagligen.

---

# Scraper-arkitektur

BolåneRadar använder en flexibel scraping-arkitektur byggd kring ett gemensamt interface:

```java
public interface BankScraper {
    List<MortgageRate> scrapeRates(Bank bank) throws Exception;
}
```

Varje bank har sin egen scraperklass som implementerar detta interface.  
Det innebär att systemet enkelt kan byggas ut med fler banker utan att ändra kärnlogiken.

### Centrala principer  
- Varje scraper ansvarar själv för att:
  - hämta data från bankens räntesida  
  - parsa HTML, tabeller eller API-format  
  - omvandla datan till `MortgageRate`  

- Scraper-klasserna är helt frikopplade från resten av systemet.  
- ScraperService hanterar all logik efter att data hämtats.

### ScraperService - navet i scrapingfunktionaliteten  
`ScraperService` ansvarar för att:

- Matcha bank mot rätt scraper  
- Köra scrapern och hantera exceptions  
- Utföra dubblettkontroll  
- Räkna ut `rateChange` och `lastChangedDate`  
- Spara nya räntor  
- Logga scrapingresultat  
- Skicka e-post vid fel  

Denna struktur gör systemet robust även om varje bank har helt olika datakällor.

---

# Lägga till en ny scraper

Att lägga till stöd för en ny bank är enkelt och kräver inga ändringar i kärnsystemet.

### **1. Skapa en klass som implementerar `BankScraper`**
```java
@Service
public class NewBankScraper implements BankScraper {
    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws Exception {
        // Hämta och parsa data
    }
}
```

### **2. Markera den med `@Service`**  
Då plockas den automatiskt upp av Spring.

### **3. ScraperService hittar den automatiskt**  
Det enda kravet är att scrapern kan identifiera bankens namn korrekt.

### **4. Resten hanteras av systemet**  
Dubblettkontroll, logging, notifiering och sparning sker automatiskt.

---


# Tester

### Enhetstester
- All logik testas med Mockito
- BankService
- MortgageRateService
- RateAnalyticsService
- AdminDataService
- RateUpdateLogService
- ScraperService
- EmailService

### Integrationstester (MockMvc)
- Alla controllers  
- Full Basic Auth-testning  
- Full JSON-verifiering  
- End-to-end flöden

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

### 3. Skapa application.properties från mallen

### 3. Fyll i `application.properties`
Projektet innehåller en mallfil:
```
src/main/resources/application-example.properties
```
Gör så här:
#### Steg A: Kopiera mallen
```
cp src/main/resources/application-example.properties \
   src/main/resources/application.properties
```
#### Steg B: Fyll i dina egna värden
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bolaneradar
spring.datasource.username=your-db-username
spring.datasource.password=your-db-password

spring.mail.host=smtp.gmail.com
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

alert.email.to=recipient@example.com

admin.username=admin
admin.password=hemligt123
```
Viktigt:
application.properties ska INTE checkas in i GitHub, eftersom den innehåller lösenord.

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

`pg_dump.exe` används för att skapa:
- Daglig `.dump`
- Daglig `.sql`

Sparas i `/backups`.

---

# Swagger

Swagger (OpenAPI) är konfigurerat via `OpenApiConfig`.

Stödjer:
- Full dokumentation av endpoints  
- Basic Auth direkt i UI  

---

# Trendanalys

Systemet stödjer tre analyslägen:

### 1. Globala trender  
Analys av senaste två snapshots.

### 2. Per bank (snittränta)  
Bygger trender per bank med deras egna datum.

### 3. Intervall (range)  
Trender mellan två specifika datum.

---

# Licens

Detta projekt är licensierat under MIT-licensen.

Du är fri att använda, kopiera och modifiera koden för personligt eller utbildningssyfte.  
Eftersom projektet fortfarande utvecklas av mig privat kan licensen komma att ändras i framtiden.

Se `LICENSE`-filen för full licenstext.

---



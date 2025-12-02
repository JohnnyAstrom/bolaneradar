# BolåneRadar - Backend

En komplett backend byggd med **Spring Boot**, ansvarig för att hämta, lagra och exponera svenska bolåneräntor. Systemet innehåller scraping, historik, schedulerade jobb, uppdateringsloggar, admin-API och publika API-endpoints för frontend.

---

# Funktionalitet

### Hantering av banker och räntor  
- CRUD för banker  
- Skapa / uppdatera bolåneräntor via admin-API
- Publika endpoints för:
  - räntehistorik
  - nuvarande räntor
  - bankintroduktion
  - bankdetaljer
  - jämförelser mellan banker
  - senaste uppdateringar
- Tydlig separation mellan admin-lager och publikt API.

### Scraping av räntor  
- Flera `BankScraper`-implementationer  
- ScraperService som kör scraping för alla banker eller en specifik
- Loggning av resultat via RateUpdateLogService 
- Felhantering
- Dubblettkontroll
- Möjlighet att starta scraping manuellt via admin-API
- Mejlnotifiering vid fel

### Loggning & Historik  
- RateUpdateLog loggar varje scrapinghändelse 
- Publika och admin-endpoints för att se loggar
- Hämta senaste uppdatering per bank

### Schedulerade jobb  
- **DatabaseBackupScheduler** - skapar dagliga `.dump` + `.sql`-backup  
- **ScraperScheduler** - kör scraping varje dag

### Säkerhet  
- Basic Auth för alla admin-routes
- Alla GET-endpoints är publika
- POST/PUT/DELETE under /api/** kräver auth  
- Swagger UI aktiverat med Basic Auth-stöd

### Tester  
- Enhetstester (Mockito + JUnit 5)  
- Integrationstester (MockMvc)  
- Full täckning av controllers, adminservices och scraperlogik  

---

# Projektstruktur

```
backend/
 ├── controller/
 │    ├── admin/
 │    │     ├── banks/
 │    │     ├── rates/
 │    │     ├── scraper/
 │    │     ├── logs/
 │    │     └── dev/
 │    └── api/
 │          ├── banks/
 │          └── rates/
 │
 ├── service/
 │    ├── admin/
 │    │     ├── AdminDataService
 │    │     ├── MortgageRateAdminService
 │    │     └── RateUpdateLogService
 │    ├── client/
 │    │     ├── BankDetailsService
 │    │     ├── BankIntroService
 │    │     ├── BankRateReadService
 │    │     ├── BankHistoryService
 │    │     ├── BankKeyResolverService
 │    │     ├── LatestRatesService
 │    │     └── MortgageRateComparisonService
 │    └── integration/
 │          └── scraper/core/
 │
 ├── repository/
 ├── entity/
 │    └── core/
 │
 ├── dto/
 │    ├── admin/
 │    ├── api/
 │    └── mapper/
 │          ├── admin/
 │          └── api/
 │
 ├── scheduler/
 ├── config/
 ├── exception/
 └── BolaneradarBackendApplication.java

```

---

# Arkitektur

### Lagren är tydligt separerade:
- **Controller-lagret** tar emot HTTP-anrop och exponerar API:er
  - Publika controllers under ```/api/banks/**``` och ```/api/rates/**```
  - Admin controllers under ```/api/admin/**```
- **Service-lagret** innehåller all affärslogik
  - Uppdelat i separata domäner:
    - ``service.admin`` - hantering av adminfunktioner
    - ``service.client`` - data till frontendens publika vyer
    - ``service.integration.scraper`` - integrering mot scraperklasser
- **Repository-lagret** hanterar all databasinteraktion
  - Enbart ansvarigt för CRUD mot entiteterna
  - Anropas endast från service-lagret (ingen direkt access från controllers)
- **DTO/Mapper-lager** skyddar entiteter  
  - Uppdelad i ``dto.admin`` och ``dto.api``
- **Scheduler-lager** automatiserar jobb
  - Dagliga backups och scraping jobb
- **Scraper-lager** hämtar data externt  
  - Varje bank har egen scraperklass
  - Orkestreras av ``ScraperService``

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

---

# API-översikt

## Bank-endpoints (publika)
```
GET /api/banks/intro/{bankKey}
GET /api/banks/details/{bankKey}
GET /api/banks/history/{bankKey}
```

## Rate-endpoints (publika)
```
GET /api/rates/current/{bankKey}
GET /api/rates/comparison
GET /api/rates/updates/latest
```

## Admin - Rates
```
POST /api/admin/rates
```

## Admin - Logs
```
GET /api/admin/rates/updates
GET /api/admin/rates/updates/latest
```

## Admin – Scraper
```
POST /api/admin/scrape/all
POST /api/admin/scrape/{bankName}
```

## Admin – Development (endast DEV-profil)
```
POST   /api/admin/dev/import-example
DELETE /api/admin/dev/clear
DELETE /api/admin/dev/delete-rates?bankName=X
```

---

# Säkerhet

- Basic Auth aktiverad
- GET-anrop är publika
- POST/PUT/DELETE kräver ``ROLE_ADMIN``
- Swagger UI stöder Basic Auth direkt
- Stateless sessions
- Konfigurerat i ``SecurityConfig``

---

# Scheduler-jobb

### ScraperScheduler
Kör scraping automatiskt (om aktiverad).

### DatabaseBackupScheduler
Tar dagliga PostgreSQL-backups i både .dump och .sql.

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
- AdminDataServiceTest
- MortgageRateAdminServiceTest
- RateUpdateLogServiceTest
- ScraperServiceTest
- EmailServiceTest


### Integrationstester (MockMvc)
- AdminBankControllerIT
- AdminMortgageRateControllerIT 
- AdminRateUpdateLogControllerIT
- AdminScraperControllerIT
- AdminDevDataControllerIT

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

``DatabaseBackupScheduler`` använder ``pg_dump`` för att spara:
- `.dump` (binär)
- `.sql` (text)

Sparas i `/backups`.

---

# Swagger

Swagger genereras via OpenAPI-config och listar alla endpoints - både publika och admin-endpoints.
Admin-endpoints kan testas direkt i Swagger UI genom Basic Auth inloggning

---

# Historik & Visualisering av räntedata

Systemet innehåller stöd för att hämta och visualisera historiska räntevärden per bank.
Funktionaliteten består av:

- Historiska datapunkter per bank  
  - Via ``BankHistoryService``, som använder sparade räntor från databasen.
- Snitträntor och jämförelser
  - Beräknas på backend och används i frontendens grafer.
- Uppdateringsloggar
  - Visar när en bank senast uppdaterades och används för att indikera datans färskhet.

Analysfunktionaliteten är förenklad jämfört med tidigare versioner och fokuserar nu på att erbjuda stabil historik + visualisering, snarare än avancerade marknadstrender.

---

# Licens

Detta projekt är licensierat under MIT-licensen.

Du är fri att använda, kopiera och modifiera koden för personligt eller utbildningssyfte.  
Eftersom projektet fortfarande utvecklas av mig privat kan licensen komma att ändras i framtiden.

Se `LICENSE`-filen för full licenstext.

---



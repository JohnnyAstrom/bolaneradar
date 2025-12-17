# BolåneRadar

BolåneRadar är en fullstack-applikation för att jämföra svenska bolåneräntor,
visualisera historisk ränteutveckling och analysera hur din egen ränta står sig
mot marknaden.

Systemet kombinerar automatiserad insamling av räntedata, historik och
jämförelser med ett konsumentfokuserat **Smart Ränte-test** som hjälper
användare att förstå om deras ränta är bra, rimlig eller onödigt dyr.

Projektet är byggt som ett tydligt separerat backend-API och en frontend,
men utgör tillsammans en sammanhängande produkt.

---

# Vad kan jag göra med BolåneRadar?

Som användare kan du:

- Jämföra aktuella bolåneräntor mellan svenska banker
- Se historisk utveckling per bank och bindningstid
- Se när banker senast ändrade sina räntor
- Läsa bankpresentationer (introduktion, passar bäst för, mindre bra för)
- Analysera din egen ränta med Smart Ränte-testet
- Jämföra flera erbjudanden och se vilket som är bäst
- Få konsumentvänliga rekommendationer baserat på marknadsdata

---

# Funktionalitet

## Publika funktioner (användarvy)

### Räntedata & jämförelser
- Aktuella bolåneräntor per bank och bindningstid
- Jämförelse mellan banker
- Visning av snitträntor
- Indikering av senaste ränteändring

### Historik & visualisering
- Historiska snitträntor per bank
- Val av bindningstid i grafvy
- Visualisering av ränteutveckling över tid

### Bankinformation
- Bankintroduktioner och beskrivningar
- ”Passar bäst för” / ”Mindre bra för”
- Länkar till bankernas egna erbjudanden

### Smart Ränte-test
Smart Ränte-testet analyserar hur din ränta står sig mot marknaden.

Stöder två huvudsakliga flöden:
- **Ingen offert** – jämför din nuvarande ränta mot marknadens snitt
- **Med offerter** – analyserar och rangordnar flera erbjudanden

Analysen tar hänsyn till:
- Bindningstid
- Ränteform (rörlig / bunden)
- Marknadens genomsnitt
- Årlig kostnadsskillnad

Resultatet presenteras med:
- Färgkodad status
- Förklarande analystext
- Konkreta rekommendationer

---

## Datainsamling & administration

### Scraping av räntor
- Automatisk insamling av bolåneräntor
- Flera `BankScraper`-implementationer
- Manuell scraping via admin-API
- Felhantering och dubblettkontroll
- Mejlnotifiering vid fel

### Loggning & historik
- Loggning av varje scrapingkörning
- Visning av senaste uppdatering per bank
- Publika och admin-endpoints för loggar

### Schedulerade jobb
- **ScraperScheduler** – kör scraping automatiskt
- **DatabaseBackupScheduler** – dagliga databasbackuper (`.dump` + `.sql`)

---

# Arkitektur

BolåneRadar är byggt enligt en tydligt lagerindelad arkitektur.

## Översikt
- **Frontend** (React + TypeScript)
    - Konsumerar backendens publika API
    - Visar tabeller, grafer och SmartRate-resultat
- **Backend** (Spring Boot)
    - Ansvarar för datainsamling, analys och API-exponering
    - Innehåller all affärslogik och beräkningar

---

## Backend – lagerindelning

### Controller-lagret
- Tar emot HTTP-anrop och exponerar API:er
- Publika controllers under:
    - `/api/banks/**`
    - `/api/rates/**`
    - `/api/smartrate/**`
- Admin controllers under:
    - `/api/admin/**`

### Service-lagret
Innehåller all affärslogik, uppdelad per domän:
- `service.client` – data till frontend
- `service.admin` – adminfunktioner
- `service.smartrate` – analyslogik för Smart Ränte-testet
- `service.integration.scraper` – scraping och datainsamling

### Repository-lagret
- Ansvarar för all databasinteraktion
- Anropas endast från service-lagret

### DTO / Mapper-lager
- Skyddar entiteter från extern exponering
- Uppdelat i:
    - `dto.api`
    - `dto.admin`

### Scheduler-lager
- Automatiserar scraping och backup-jobb

---

# Scraper-arkitektur

Varje bank har sin egen scraperklass som implementerar ett gemensamt interface.
Detta gör att systemet enkelt kan byggas ut med fler banker utan att ändra
kärnlogiken.

## Grundprinciper

Varje scraper ansvarar själv för att:

- hämta data från bankens räntesida
- parsa HTML, tabeller eller API-format
- omvandla datan till `MortgageRate`

Scraper-klasserna är helt frikopplade från resten av systemet och innehåller
ingen affärslogik utanför själva datainsamlingen.

## ScraperService – navet i flödet

`ScraperService` orkestrerar hela scrapingprocessen och ansvarar för:

- matchning av bank → rätt scraper
- felhantering
- dubblettkontroll
- beräkning av `rateChange` och `lastChangedDate`
- loggning av resultat
- e-postnotifiering vid fel

Denna uppdelning gör systemet robust även när olika banker har helt olika
datakällor och format.

---

# Säkerhet

- Basic Auth för alla admin-routes
- Alla GET-endpoints är publika
- POST / PUT / DELETE kräver `ROLE_ADMIN`
- Stateless sessions
- Swagger UI stöder Basic Auth

---

# Tester

### Enhetstester
- Service-lagret
- Scraperlogik
- SmartRate-analys
- Hjälp- och stödtjänster

### Integrationstester (MockMvc)
- Controllers (publika + admin)
- Adminflöden
- Scraper-endpoints

---

# Installation & körning

### 1. Klona projektet
```
git clone https://github.com/JohnnyAstrom/bolaneradar
```
### 2. Starta PostgreSQL
```
CREATE DATABASE bolaneradar;
```

### 3. Skapa application.properties
```
src/main/resources/application-example.properties
```

### 4. Starta backend
```
mvn spring-boot:run
```

### 5. Swagger UI
```
http://localhost:8080/swagger-ui.html
```

# Licens

Detta projekt är licensierat under MIT-licensen.

Du är fri att använda, kopiera och modifiera koden för personligt eller utbildningssyfte.  
Eftersom projektet fortfarande utvecklas av mig privat kan licensen komma att ändras i framtiden.

Se `LICENSE`-filen för full licenstext.




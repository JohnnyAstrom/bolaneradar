# BolåneRadar

BolåneRadar är en fullstack-applikation för att jämföra svenska bolåneräntor,
visualisera historisk ränteutveckling och analysera hur din egen ränta står sig
mot marknaden.

Applikationen är utvecklad som ett examensarbete inom utbildningen
**Systemutvecklare Java & JavaScript** och har ett tydligt fokus på
neutralitet, transparens och konsumentnytta.

Projektet består av ett fristående backend-API och en frontend som tillsammans
utgör en sammanhängande produkt.

---

## Live-demo

Applikationen är driftsatt och tillgänglig här:

https://bolaneradar.onrender.com

---

## Vad kan jag göra med BolåneRadar?

Som användare kan du bland annat:

- Jämföra aktuella bolåneräntor mellan svenska banker
- Se när banker senast ändrade sina räntor
- Studera historisk ränteutveckling per bank och bindningstid
- Läsa bankpresentationer och fördjupad bankinformation
- Analysera din egen ränta med **Smart Ränte-testet**
- Jämföra flera erbjudanden och få konsumentvänliga rekommendationer

---

## Huvudfunktioner

### Räntedata & jämförelser
- Aktuella list- och snitträntor per bank
- Jämförelse mellan banker för vald bindningstid
- Indikering av ränteförändringar och senaste ändringsdatum

### Historik & visualisering
- Historiska snitträntor per bank
- Interaktiva grafer med val av bindningstid
- Tydlig visualisering av ränteutveckling över tid

### Bankinformation
- Bankintroduktioner och översikter
- ”Passar bäst för” / ”Mindre bra för”
- Länkar till bankernas egna erbjudanden

### Smart Ränte-test
Smart Ränte-testet hjälper användaren att förstå hur den egna räntan står sig
mot marknaden.

Stöder två huvudsakliga flöden:
- **Ingen offert** – jämförelse mot marknadens snittränta
- **Med offerter** – analys och rangordning av flera erbjudanden

Analysen tar hänsyn till bindningstid, ränteform och marknadsdata och presenteras
med tydlig status, förklarande text och konkreta rekommendationer.

---

## Datainsamling

BolåneRadar använder automatiserad datainsamling för att hämta bolåneräntor från
bankernas publika webbsidor.

- Varje bank har en egen scraperimplementation
- Datainsamlingen är frikopplad från användarflödet
- Räntedata lagras för både aktuell visning och historisk analys
- Fel loggas och kan notifieras via e-post

Schemaläggning av scraping sker externt (t.ex. via CI/CD), medan backenden
ansvarar för validering, lagring och analys av datan.

---

## Arkitekturöversikt

### Frontend
- React + TypeScript
- Konsumerar backendens publika REST-API
- Visar tabeller, grafer och analysresultat
- Ingen direkt åtkomst till databas eller externa källor

### Backend
- Java + Spring Boot
- Exponerar publika och skyddade API-endpoints
- Innehåller all affärslogik, analys och datavalidering
- Lagerindelad arkitektur (Controller, Service, Repository, DTO)

Frontend och backend kan utvecklas och driftsättas oberoende av varandra.

---

För mer detaljer om respektive delsystem, se:
- `backend/README.md`
- `frontend/README.md`

---

## Säkerhet

- Publika GET-endpoints för användarvy
- Skyddade admin-endpoints med Basic Auth
- Roller och behörighet hanteras i backend
- Stateless API-design

---

## Tester

- Enhetstester för service- och analyslogik
- Integrationstester för controllers och API-flöden
- Tester för scraping och automatiserade processer

---

# Installation & körning

### 1. Klona projektet
```
git clone https://github.com/JohnnyAstrom/bolaneradar
```
### 2. Starta backend
- Skapa databas (PostgreSQL)
- Konfigurera application.properties
- Starta med:

```
mvn spring-boot:run
```

### 3. Starta frontend
```
npm install
npm run dev
```

# Licens

Projektet är licensierat under MIT-licensen.

Koden får användas, modifieras och studeras för personligt och utbildningssyfte.
Projektet utvecklas privat och licensvillkor kan komma att justeras i framtiden.

Se `LICENSE`-filen för full licenstext.




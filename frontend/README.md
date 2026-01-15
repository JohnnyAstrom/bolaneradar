# BolåneRadar – Frontend

Detta dokument beskriver frontend-delen av BolåneRadar.

För en övergripande beskrivning av projektets syfte, funktionalitet och arkitektur,
se huvud-README i repository-roten.

Frontend är byggd med **React och TypeScript** och ansvarar för presentation,
användarinteraktion och visualisering av data som hämtas från backendens REST-API.

---

## Ansvar och avgränsning

Frontendens ansvar är att:
- presentera bolåneräntor på ett tydligt och lättförståeligt sätt
- visualisera historisk ränteutveckling
- guida användaren genom Smart Ränte-testet
- kommunicera med backend via ett publikt REST-API

Frontend:
- innehåller **ingen affärslogik**
- har **ingen direkt åtkomst till databas**
- utför **ingen datainsamling eller scraping**
- fungerar helt som en klient till backendens API

All analys, validering och databehandling sker i backend.

---

## Funktionalitet

Frontend tillhandahåller bland annat:

- Startsida med jämförelse av bolåneräntor mellan banker
- Visning av aktuella list- och snitträntor
- Grafer för historisk ränteutveckling
- Fördjupade banksidor med bankinformation
- Smart Ränte-test:
    - inmatning av användarens ränta
    - presentation av analysresultat och rekommendationer

All data hämtas dynamiskt från backendens API.

---

## Kommunikation med backend

Frontend kommunicerar med backend via HTTP-baserade REST-anrop.

- Dataformat: JSON
- Kommunikation sker via centraliserade API-anrop i `services`
- Backend förutsätts returnera färdigbearbetad och validerad data

Frontend gör inga antaganden om hur data har samlats in eller analyserats.

---

## Teknisk översikt

Frontend är uppbyggd med ett komponentbaserat arbetssätt där vyer och funktionalitet
delas upp i mindre, återanvändbara delar.

- **Ramverk:** React
- **Språk:** TypeScript
- **Byggverktyg:** Vite
- **Styling:** Tailwind CSS
- **Kodkvalitet:** ESLint
- **CSS-hantering:** PostCSS + Autoprefixer
- **Routing:** Klientside-routing med React Router
- **State:** Lokal tillståndshantering via React hooks
- **Internationellt stöd:** i18n med språkfiler (sv/en)

Applikationen körs helt i webbläsaren och kommunicerar med backend via
HTTP-baserade REST-anrop där data utbyts i JSON-format.

---

## Projektstruktur

Projektstrukturen är uppdelad efter ansvar snarare än tekniska lager.

```
frontend/
├── src/
│ ├── components/  # Återanvändbara UI-komponenter
│ ├── pages/       # Routade sidor och vyer
│ ├── hooks/       # Egna React-hooks
│ ├── services/    # API-klient mot backend
│ ├── config/      # Konfiguration och visningsdata
│ ├── i18n/        # Språkstöd
│ ├── types/       # TypeScript-typer (API-kontrakt)
│ ├── utils/       # Hjälpfunktioner
│ ├── App.tsx
│ └── main.tsx
│
├── public/
└── index.html
```

Strukturen är avsiktligt enkel och fokuserar på tydlig separation mellan:
- vyer
- presentation
- datahämtning
- konfiguration

---

## Miljöer och konfiguration

Frontend använder miljövariabler för att hantera:
- backendens base-URL
- skillnader mellan utvecklings- och produktionsmiljö

Ingen känslig logik eller hemligheter lagras i frontend.

---

## Installation & körning

### 1. Navigera till frontend-mappen
```bash
cd frontend
```

### 2. Installera beroenden
```bash
npm install
```

### 3. Starta utvecklingsserver
```bash
npm run dev
```

Frontend startar lokalt och kommunicerar med backend via konfigurerad API-URL.

---

## Bygg för produktion
```bash
npm run build
```

Bygget genererar statiska filer som kan distribueras via valfri webbserver
eller molnplattform.

---

## Licens

Frontend-delen omfattas av samma licens som övriga projektet.

Se `LICENSE` i repository-roten för fullständig licenstext.










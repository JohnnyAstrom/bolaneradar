# Landshypotek: felsökning och verifiering

## Grundorsak, 2026-09-29

`LandshypotekBankScraper.scrapeRates()` sökte tidigare efter
`#anchor-2607 table` (LISTRATE) och `#anchor-2595 h4` + `table`
(AVERAGERATE). Båda ID:n saknas i hämtad HTML. De villkorade blocken
hoppades därför över utan undantag, vilket gav en tom lista innan
`extractSimpleTable()` ens anropades. Inga XPath-uttryck används.

Den gamla URL:en `https://www.landshypotek.se/lana/bolanerantor/`
omdirigerar till <https://www.landshypotek.se/lana-till-bostad/bolanerantor/>.
Hämtningen gav HTTP 200 och innehöll tabellerna i rå HTML, i
`accordion-item > template`-komponenter. JavaScript behövs för gränssnittet,
men inte för att läsa räntorna med Jsoup. Cookie-samtycke behövdes inte för
den observerade hämtningen. Detta verifierar den lokala hämtningen, inte
att varje framtida GitHub-runner alltid får samma HTTP-svar.

Listräntorna ligger nu under `#anchor-16667`, snitträntorna under
`#anchor-16676`, med en `h3` som säger `Snitträntor Augusti 2026`.
Att bara byta ID:n räcker därför inte för snitträntor. Den gamla koden
läste dessutom rubriktexten utan att använda den: datumet sattes alltid
till föregående kalendermånad, vilket blir fel vid försenad publicering.

## Avgränsad fix

Endast Landshypoteks produktionsklass ändras. Tabeller identifieras via
beskrivande `caption`: listräntor för bolån respektive snitträntor för
bolån senaste månaden. Exakt en av varje krävs. Historiska snitträntor,
räntor efter rabatt, ränterabatter och räkneexempel väljs inte.

Kolumner identifieras med `Bindningstid` samt `Ränta`/`Snittränta`, vilket
utesluter `Effektiv ränta`. Snitträntans svenska månad och år läses från
tabellens egen sektionsrubrik och blir månadens första dag. Listräntor
behåller kördagens datum. `n/a` i snitträntetabellen betyder opublicerat
värde och hoppas över. Felaktiga strukturer, datum, värden och dubbla
bindningstider ger IOException, så en ofullständig körning inte lämnas
vidare till databasen. Gemensamma helpers och sparlogik är oförändrade.

INFO visar antal per räntetyp och snitträntemånad. DEBUG visar URL,
tabellantal, överhoppade `n/a` och varje objekts bank, typ, term, värde
och datum. Aktivera med:

```text
--logging.level.com.bolaneradar.backend.service.integration.scraper.bank.LandshypotekBankScraper=DEBUG
```

## Förväntade objekt från HTML-filen hämtad 2026-09-29

| term | LISTRATE | AVERAGERATE |
| --- | ---: | ---: |
| VARIABLE_3M | 3.09 | 2.61 |
| FIXED_1Y | 3.74 | 3.10 |
| FIXED_2Y | 4.05 | 3.27 |
| FIXED_3Y | 4.15 | 3.34 |
| FIXED_4Y | 4.25 | opublicerad, n/a |
| FIXED_5Y | 4.35 | opublicerad, n/a |

Totalt 10 objekt: 6 LISTRATE med kördagens datum och 4 AVERAGERATE med
`effective_date=2026-08-01`. HTML-fixturen är ett utdrag från den hämtade
sidan med både relevanta tabeller och tabeller som ska ignoreras.
Fixturetesterna kontrollerar exakta termer, typer, värden och datum.

## Verifiera utan databas

Kör i `backend`:

```powershell
mvn "-Dtest=LandshypotekBankScraperTest" test
mvn "-Dtest=LandshypotekBankScraperTest#liveDryRun" "-Dlandshypotek.live=true" test
```

Första kommandot använder sparad HTML och testar också ändrade CMS-ID:n,
flyttade kolumner, svenska månader, årsskifte/försenad publicering,
saknade/dubbla tabeller, ogiltiga värden och framtida datum.

Andra kommandot använder produktionshämtningen och skriver varje returnerat
objekt till konsolen. Ingen Spring-kontext startas, inget repository
anropas och ingen databasanslutning skapas. Aktuella räntor och månad kan
förstås ändras efter att fixturen hämtats.

## Verifiera i GitHub Actions och efter lagring

1. Kör testerna ovan och granska objekten innan ändringen distribueras.
2. När ändringen finns på vald branch: öppna Actions → **Daily mortgage
   rate scraping** → **Run workflow** och välj den branchen. Det befintliga
   jobbet bygger med `-DskipTests`, så det ersätter inte regressionstesterna.
3. Kontrollera `Landshypotek Bank: LISTRATE=..., AVERAGERATE=...` och
   `average_effective_date=...`. För detaljer, lägg DEBUG-argumentet ovan
   på jobbets befintliga `java -jar ... --mode=scrape`-kommando. Detta jobb
   kör och sparar samtliga banker. För enbart Landshypotek på en körande
   uppdaterad backend finns det autentiserade adminanropet
   `POST /api/admin/scrape/Landshypotek%20Bank`.
4. Kontrollera i Neon för rätt miljö:

```sql
SELECT rate_type, term, rate_percent, effective_date
FROM mortgage_rates
WHERE bank_id = 10
  AND (
    (rate_type = 'AVERAGERATE' AND effective_date = DATE '2026-08-01')
    OR (rate_type = 'LISTRATE' AND effective_date = CURRENT_DATE)
  )
ORDER BY rate_type, term, id;

SELECT rate_type, term, rate_percent, effective_date, COUNT(*)
FROM mortgage_rates
WHERE bank_id = 10 AND rate_type = 'AVERAGERATE'
  AND effective_date = DATE '2026-08-01'
GROUP BY rate_type, term, rate_percent, effective_date
HAVING COUNT(*) > 1;
```

Byt snitträntedatum till månad som livekörningen visar. Sista frågan ska
inte hitta exakta snitträntedubbletter. Befintlig `ScraperService` hoppar
över AVERAGERATE med samma bank, term, datum och värde; nya månaders
värden kan sparas även om räntan är oförändrad. LISTRATE har inte samma
dubblettfilter: flera manuella körningar samma dag kan skapa flera
snapshots. Använd därför testets livekörning för upprepade kontroller
innan en körning som sparar.

Ingen korrigering eller återfyllnad av historiska databasrader ingår.

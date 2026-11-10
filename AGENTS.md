# AGENTS.md

Pokyny pro AI asistenty (a lidi), kteří pracují na kódu Geokuku.

## Než začneš

**Přečti `doc/SLOVNIK.md`.** Geokuk má vlastní terminologii (kešoid, kachle × dlaždice, Mou, moumer, genotyp, alela, sklivec, bousek…) a vlastní jmenné konvence. Bez slovníku kód špatně pochopíš i pojmenuješ. Pojmy používej přesně v jeho významu, nové pojmy do něj doplň.

## O projektu

Geokuk je desktopová aplikace (Java, Swing) pro plánování geocachingových výletů nad mapami. Načítá keše a další kešoidy (GPX, databáze GeoGetu a GSAKu, fotky s GPS), zobrazuje je na mapových podkladech, filtruje, třídí do výletu a umožňuje kreslit cesty. Uživatelské rozhraní i doména jsou česky.

## Sestavení a spuštění

Vyžaduje JDK 25 a Maven.

```sh
mvn clean verify                          # překlad + testy
java -Xmx2g -jar target/geokuk-5.2.0.jar  # spuštění
```

- Testy: JUnit 4 a Mockito, `src/test/java`. Testovací data v `testData/`.
- Kód používá **Lombok** (`@Data`, `@Slf4j`…). IDE potřebuje Lombok plugin.
- Po každé změně spusť `mvn clean verify`. Swing UI testy pokrývají málo, změny v UI popiš, jak je ručně ověřit.

## Struktura

Zdroje jsou v `src/main/java/cz/geokuk`:

| Balík | Obsah |
|---|---|
| `core/program` | hlavní okno, menu (`Menu`), akce (`Akce`), složení aplikace (`Inicializator`), konstanty (`FConst`, `FPref`) |
| `core/coord`, `core/coordinates` | souřadnice (Mou, Wgs, UTM), zobrazení (`Coord`), pozice, výřez |
| `core/render` | export mapy (obrázek, KMZ, OziExplorer, tisk) |
| `core/*` ostatní | hledání, nápověda, online/offline, profil, vzhled |
| `plugins/kesoid` | kešoidy, waypointy, import (`importek`), genetika (`genetika`), ikony (`mapicon`), filtr, druhy kešoidů (`kind/*`) |
| `plugins/mapy` | mapové podklady, kachle, offliner |
| `plugins/cesty`, `plugins/vylety` | cesty a výlet |
| `plugins/*` ostatní | popisky, kruhy, obsazenost, mřížky, geocoding, domov a pojmenované body (`refbody`) |
| `framework` | vlastní infrastruktura: DI, události, modely, nastavení |
| `util/*` | obecný technický kód (turboware) |
| `api/mapicon` | kreslení ikon |

**Názvy `core`, `plugins` a `api` popisují původní záměr, ne skutečnost.** Geokuk měl být malá obecná aplikace nad mapami a vše v `plugins`, včetně samotných kešoidů, mělo jít přidávat a vyměňovat. Refaktoring se nedotáhl: `core` dnes závisí na pluginech, `plugins/kesoid` je centrum s cykly k dalším modulům a skutečnými pluginy jsou jen druhy kešoidů v `plugins/kesoid/kind/*`. Co který balík doopravdy obsahuje: `doc/SLOVNIK.md`, kapitola 5.

## Kam dávat nový kód

- **Obecný kód** (nezávislý na geocachingu) do `util`, anglicky, bez importu z `core`, `plugins` a `framework`.
- **Nový druh kešoidu** jako plugin do `plugins/kesoid/kind/<druh>` (rozhraní `KesoidPlugin`), podle vzoru existujících druhů.
- **Nová funkce** pokud možno jako samostatný modul v `plugins`, který závisí na jádru a na `plugins/kesoid`, ale ne naopak.
- **Nevytvářet nové závislosti** `core` → `plugins`, `framework` / `util` / `api` → doména, ani **nové cykly** mezi moduly. Kód tím nemá vzdalovat aplikaci od záměru vyměnitelných pluginů.
- **Do `plugins/kesoid/mvc` nic nepřidávat**, je to odkladiště. Novou třídu dej do balíku, kam věcně patří.
- Přesuny a přejmenování existujících balíků dělej jen jako samostatnou, předem domluvenou změnu. Rozbíjejí rozpracované větve ostatních.

## Architektura v kostce

- **Skládání aplikace:** `core/program/Inicializator` registruje modely, akce a pluginy do `framework/BeanBag`. Závislosti se injektují reflexí přes veřejné metody `inject(Typ)`. Nový model nebo akci je nutné v `Inicializator` (resp. `Akce`) zaregistrovat.
- **Události:** `framework/EventManager`. Odběratel má veřejnou metodu `onEvent(KonkretniEvent)`, shoda je podle přesné třídy události. Nový odběratel hned dostane poslední vyslanou událost daného typu. Události se vysílají jen z vlákna Swingu (EDT).
- **Modely** dědí z `framework/Model0`, změnu oznamují událostí (`fire`). UI na ni reaguje, nevolá model opakovaně.
- **Dlouhé operace** běží ve `SwingWorker` (`MySwingWorker0`), výsledek se do modelu předává na EDT.
- **Nastavení** se ukládá přes `framework/MyPreferences` (Java Preferences). Názvy klíčů jsou v `core/program/FPref`.
- **Druhy kešoidů** jsou pluginy (`plugins/kesoid/kind/*`, rozhraní `KesoidPlugin`) a hledají se reflexí.
- **Vlastnosti waypointů a ikony** řeší genetika: geny, alely, genotyp, fenotyp (viz slovník, kapitola 3.4 a 3.5).

## Pravidla pojmenování

- **Doména česky** (bez diakritiky v identifikátorech): `Kesoid`, `Cesta`, `Usek`, `Kachle`, `obtiznost`.
- **Obecný technický kód anglicky** (turboware): kód, který by šel použít v jiné aplikaci, nesmí záviset na doméně.
- **Názvy z cizích formátů** (GPX, Groundspeak) jen ve vrstvě, která formát čte nebo zapisuje.
- **Jeden pojem, jeden název.** Pozor hlavně na **kachle** (obrázek mapy ze serveru) × **dlaždice** (díl renderovaného výstupu).
- Komentáře a texty UI česky.
- Podrobnosti, konvence (`J`, `F`, `E`, přípona `0`, `xx`/`yy` pro Mou…) a místa, kde kód pravidla nedodržuje: `doc/SLOVNIK.md`, kapitoly 1 a 2.

## Na co si dát pozor

Tyto změny tiše rozbijí data nebo nastavení uživatelů. Dělej je jen záměrně a s převodem starých dat:

- **Klíče nastavení** v `FPref` a názvy vlastností tříd s `@Preferenceble`: přejmenování ztratí uložené nastavení.
- **Pořadí konstant `EVylet`:** do nastavení se ukládá `ordinal()`.
- **Id podkladu** (`name` v definici mapových podkladů): je klíčem diskové cache kachlí.
- **Formát souborů** `lovim.ggt`, `tedne.ggt`, dokumentu cest (GPX) a `geohome.ini`: jsou to data uživatele a GeoGetu.
- **Názvy souborů ikon** v sadách ikon: kódují alely, viz Icondef ve slovníku.

Dál platí:
- Na zastaralý a nedokončený kód (slovník, kapitola 9) nenavazuj. Například **grupa** je zastaralá, místo ní použij druh.
- Neprovádět hromadné přeformátování ani přejmenování mimo rozsah úkolu. Rozbije historii a ztíží review.
- Když opravuješ překlep v názvu, kolizi pojmů nebo zastaralý kód, uprav i slovník.

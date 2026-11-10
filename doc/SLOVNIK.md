# Slovník pojmů a jmenné konvence Geokuku

Geokuk má vlastní terminologii. Část pojmů je převzatá z geocachingu, část vznikla při vývoji a v běžné češtině nemá ekvivalent (kešoid, kachle, sklivec, genotyp waypointu…). Tento dokument je určen **vývojářům i AI asistentům**, kteří na Geokuku pracují. Vysvětluje:

1. pravidla, podle kterých se v Geokuku pojmenovává (čeština × angličtina),
2. jmenné konvence a zkratky, na které narazíte v kódu,
3. slovník doménových pojmů s odkazy na třídy,
4. obecný technický kód,
5. balíky, jejich skutečný obsah a původní architektonický záměr,
6. místa, kde se pojmy pletou nebo kde kód pravidla nedodržuje.

Při psaní nového kódu, komentářů, issues i commitů používejte pojmy přesně v tomto významu. Když zavádíte nový pojem, doplňte ho sem.

---

## 1. Pravidla pojmenování

### Doména česky, obecný kód anglicky

| Druh kódu | Jazyk | Příklad |
|---|---|---|
| **Doména**: vše, co se týká geocachingu, map, keší, výletů a cest | **česky** | `Kesoid`, `Cesta`, `Usek`, `Vylet`, `Kachle`, `Genotyp`, `Alela` |
| **Obecný technický kód**: tak obecný, že by šel bez úprav použít v úplně jiné aplikaci | **anglicky** | `EventManager`, `BoundingRect`, `ExceptionDumper`, `DirScanner` |
| **Cizí knihovny** | anglicky (jak jsou) | Swing, Guava, Jackson, SQLite JDBC |
| **Názvy z cizího formátu nebo standardu** (GPX, Groundspeak, GeoGet, WGS84, UTM) | v originále, ale **jen ve vrstvě, která formát čte nebo zapisuje** | `GpxWpt`, `Groundspeak.difficulty`, `Wgs`, `Utm` |

Jazyk názvu tak sám říká, kam kód patří. Česká třída je doména Geokuku. Anglická třída je obecný nástroj, který o geocachingu nic neví a nesmí na doméně záviset.

Doplňující pravidla:
- **Identifikátory bez diakritiky** (`Usek`, `Ukladanec`, `obtiznost`). Komentáře, javadoc a texty UI s diakritikou.
- **Komentáře a javadoc česky.** Starší anglické javadocy pocházejí od přispěvatelů a nemusí se přepisovat.
- **Doménový model je česky i tam, kde data přicházejí z cizího formátu.** Parser GPX čte `difficulty` a `terrain`, keš je má uložené jako obtížnost a terén.
- **Jeden pojem, jeden název.** Synonyma v kódu nepoužívat. Pozor zvlášť na kachle × dlaždice (kapitola 7).
- **Nový doménový pojem** se tvoří česky. Klidně jako neologismus (kapitola 2.3), hlavně ať je jednoznačný. Pak ho doplňte do tohoto slovníku.

### Turboware (tw)

Obecnému technickému kódu se říká **turboware**, zkráceně **tw**. Je to kód nezávislý na doméně, který jde beze změny použít v jiné aplikaci. Část obecného kódu Geokuku pochází z dřívější tw knihovny, proto na ni ještě odkazují některé komentáře v `util/*` (`@see cz.tconsult.tw…`). Označení tw není závazné. Platí jen pravidlo, že obecný kód je anglicky a nezávisí na doméně.

V Geokuku je obecný kód hlavně v balících `util/*` a `framework`.

### Jak důsledně Geokuk pravidla dodržuje

Kód vznikal od roku 2009 a podílelo se na něm víc lidí, proto pravidla nejsou dodržena všude. Při úpravách je dobré nesoulad postupně odstraňovat, ne ho rozšiřovat.

| Oblast | Dodržení | Odchylky |
|---|---|---|
| `util/exception`, `util/lang`, `util/process` | vysoké | žádné |
| `util/file` | vysoké | neprůhledné zkratky `KeFile`, `LamUrl` |
| `util/gui` | částečné | česky: `JBarvovyDvojSlider`, `MenuStrujce`, `MouseKonecListener`, `ERoh` |
| `util/index2d` (quadtree) | smíšené | `Ctverecnik`, `Splitenec`, `Indexator` vedle `Node`, `Sheet`, `BoundingRect` |
| `util/pocitadla` | česky | celý balík, přitom jde o obecné diagnostické metriky |
| `util/procak` | česky | `Procak`, `ProcakDispatcher`: obecný řetěz zpracovatelů |
| `framework` | smíšené | česky: `JPrekryvnik`, `JSlidePozadi`, `Onoff`, `Prefe`, `Dlg`, `FKurzory`. **Doména ve frameworku**: `PodkladMapSpecificModel0` (mapový podklad), `JPresCelePrekryvnik` (závisí na `core.coord`) |
| `api/mapicon` | vysoké | `Imagant` (česky), navíc importuje `plugins.kesoid.Repaintanger` |
| doména: cesty, výlety, kachle, genetika, mřížky, popisky | vysoké | |
| doména: druhy kešoidů `plugins/kesoid/kind/*` | nízké | anglicky `Waymark`, `Munzee`, `Photo`, `SimpleWaypoint`, `CzechGeodeticPoint`; balíky `kind`, `data`, `detail`, `mvc`, `mapicon` |
| doména: render | smíšené | `RenderModel`, `RenderSettings`, `RenderParams` vedle `Rendrovadlo`, `EStavRendrovani` |
| doména: nastavení pluginů | smíšené | `KruhySettings`, `PopiskySettings`, `ObsazenostSettings` |
| doména: entita `Kesoid`, `Kes`, `Wpt` | smíšené | anglicky `author`, `hidden`, `hint`, `difficulty`, `terrain`, `size`, `status` vedle `vztah`, `hodnoceni`, `znamka`, `nazev`. Existuje `getName()` i `getNazev()` |
| doména: výčty | smíšené | `EKesSize`, `EKesStatus`, `EKesType` anglicky, `EKesVztah` česky s hodnotou `NORMAL` |

Vlastnosti keše jsou v Geokuku pojmenované dvakrát: anglicky v entitě `Kes` (z GPX) a česky v genetice (`GEN_obtiznost`, `GEN_teren`, `GEN_velikost`). Podle pravidla patří do doménového modelu česká jména.

---

## 2. Jmenné konvence v kódu

### 2.1 Prefixy a přípony tříd

| Konvence | Význam | Příklad |
|---|---|---|
| prefix `J` | Swing komponenta | `JKachlovnik`, `JKesDetail` |
| prefix `F` | statická utilita, fasáda nebo konstanty | `FConst`, `FPref`, `FGeoKonvertor` |
| prefix `E` | enum | `EKesVztah`, `EVylet` |
| prefix `I` | rozhraní (jen část rozhraní ho má) | `IAtom`, `IImportBuilder` |
| prefix `A` | **dvojznačný**: potomek `Atom` (`ASada`), nebo hodnotový typ z tw (`ADate`, `AString0`) | |
| prefix `X` / `Y` | výjimka runtime / checked | `XChainRupture`, `YNeniPamet` |
| přípona `0` | abstraktní předek | `Model0`, `Action0`, `Hledac0`, `Misto0` |
| přípona `Action`, `Event`, `Model`, `SwingWorker`, `Plugin`, `PopiskyDef` | role třídy | `VyletAnoAction`, `PoziceChangedEvent` |

### 2.2 Proměnné a pole

| Konvence | Význam | Příklad |
|---|---|---|
| prefix `a` | parametr metody | `aEvent` |
| prefix `i` | instanční pole (jen ve starším kódu) | `iExceptionId` |
| `xx`, `yy` | souřadnice v **Mou** (na rozdíl od pixelových `x`, `y`) | `Mou.xx` |
| `mou…`, `wgs…` | hodnota v daném souřadnicovém systému | `mouJZ`, `moustred`, `mouNaPixl` |
| `moumer` | měřítko v bitech (viz Moumer) | |
| `soord`, `roord`, `moord` | Coord slidu, renderu, výřezu | `getSoord()` |
| světové strany | rohy a směry: `sz`, `sv`, `jz`, `jv` (severozápad…) | `KaLoc` (JZ roh), `ERoh.JV` |
| `PresCele` × `DoRohu` | hlavní mapa přes celé okno × miniatura v rohu | `JKachlovnikPresCele`, `JKachlovnikDoRohu` |
| `curta` | aktuální (vybraná) cesta | `CestyModel.curta` |

Prefixy `xx`, `mou…`, `soord` jsou **Apps Hungarian**: neříkají datový typ (ten je často stejný, `int` nebo `Coord`), ale význam hodnoty. Chrání tak před chybou typu „k Mou jsem přičetl pixely“. Při psaní nového kódu je dodržujte.

### 2.3 Neologismy

Geokuk tvoří názvy tříd jako česká slova s příponami **-ec, -adlo, -ník, -ant, -er, -ovač**. Jsou nestandardní, ale konzistentní a dobře zapamatovatelné.

| Přípona | Význam | Příklady |
|---|---|---|
| **-ec** | věc, se kterou se něco děje nebo která něčím je | Uchopenec (dá se uchopit), Nalezenec (výsledek hledání), Ukládanec (čeká na uložení), Splitenec (výsledek rozdělení), Sklivec (složení skel) |
| **-adlo** | nástroj, který něco dělá | Rendrovadlo, Malovadlo |
| **-ník** | kontejner nebo skladatel | Kachlovník (skládá kachle), Překryvník (skládá vrstvy), Čtverečník (uzel quadtree), Pozicovník |
| **-ant, -er, -ovač** | aktér, zpracovatel | Imagant, Kaputer, Kanceler, Repaintanger, Zperzistentňovač |
| složeniny | slepení dvou slov | Bousek (bod + úsek), Weikoid (waypoint + kešoid), Kepodr (kešoid + poddruh), Kaputer (kachle + computer), Repaintanger (repaint + rectangle) |

### 2.4 Pozůstatky refaktoringu

Některé podivné názvy jsou jen stopy přejmenování. Autor přejmenuje třídu a pak opravuje místa, kde překladač hlásí chybu. Například koncové `q` v `Poziceq` nic neznamená. Takové názvy lze při úpravách opravit.

---

## 3. Slovník doménových pojmů

Sloupec **V kódu** odkazuje na hlavní třídy a pole. Cesty balíků jsou relativní k `src/main/java/cz/geokuk`.

### 3.1 Souřadnice a zobrazení

| Pojem | Význam | V kódu |
|---|---|---|
| **Mou** | Vnitřní celočíselné souřadnice v Mercatorově projekci. 2^32 jednotek obepne rovník (cca 1 cm). Do Mou se převádí vše ostatní. | `Mou` (`xx`, `yy`) |
| **Moud** | Rozdíl (vektor) dvou Mou. | `Moud` |
| **Moumer** | Měřítko jako počet bitů: 0 = celá Země do 256 px, maximum 24. Odpovídá úrovni zoomu. | `moumer`, `Coord.MOUMER_0_BITS` |
| **Wgs** | Zeměpisné souřadnice WGS84 (lat, lon). Standard, název se ponechává. | `Wgs`, `Wgsd` (rozdíl) |
| **Místo** | Cokoli s polohou na zemi v libovolném souřadnicovém systému. | `Misto0`, `Mouable` |
| **Uchopenec** | Místo, které jde uchopit myší a na které se dá přilepit pozice (waypoint, bod cesty). | `Uchopenec` |
| **Coord** | Neměnný popis zobrazení: moumer, střed (moustred), velikost v px, natočení. | `Coord` |
| **Soord / Roord / Moord** | Coord slidu (vrstvy mapového okna) / renderovaného výstupu / výřezu. | `soord`, `roord`, `moord` |
| **Natočení** | Úhel otočení mapy proti severu. V Mercatoru je vždy 0. Volba renderu „Srovnat do severu“ je proto bez účinku, pravděpodobně pozůstatek map v S-JTSK. | `natoceni`, `isSrovnatDoSeveru` |
| **Výřez** | Právě zobrazená část mapy. Ukládá se, aby se po startu obnovila. | `VyrezModel` |
| **Pozice** | Uživatelem označený bod, zobrazený jako **záměrný kříž**. Může být prázdná. Přilepí se na uchopence (waypoint má prioritu 40–50, bod cesty 20–22) a pamatuje si původní Mou. | `PoziceModel`, `Poziceq` |
| **Záměrný kříž** | Grafické znázornění pozice: na hlavní mapě kříž se dvěma kroužky, v detailu v rohu pevný kříž uprostřed. | `JPozicovnikSlide`, `JZamernyKriz`, `OdstranKrizAction` |
| **Referenční bod** | Odvozená hodnota: **pozice, a pokud není, střed výřezu**. Od něj se počítá vzdálenost v hledání, geocodingu a renderu. | `HlidacReferencnihoBodu`, `ReferencniBodSeZmenilEvent` |
| **Domov** | Trvale uložené domácí souřadnice („Jsem doma…“, Ctrl+D „Přesunout domů“). Od nich se počítá vzdálenost a azimut v detailu keše. | `RefbodyModel.hc`, `TadyJsemDomaAction`, `BezDomuAction` |
| **Pojmenovaný bod** | Bod ze souboru `geohome.ini` v datovém adresáři GeoGetu (řádek `lat lon název`, CP1250), skok přes Alt+1 až Alt+9. **Není to referenční bod**, i když to tvrdí tooltip a název balíčku `refbody`. | `NaKonkretniBodAction`, `RefbodyModel` |
| **Mřížka** | Souřadnicová síť přes mapu: stupně (Dd, DdMm, DdMmSs) a UTM. Mřížky S-42 a JTSK nejsou dokončené: existují jen akce a modely, položky menu a slidy jsou zakomentované. | `plugins/mrizky`, `JMrizka*` |
| **Měřítkovník** | Grafické měřítko na mapě. | `JMeritkoSlide`, `MeritkovnikAction` |

Vzdálenost a azimut se počítají ze tří různých míst:
- stavový řádek: od **pozice** k myši,
- hledání: od **referenčního bodu**,
- detail keše: od **domova**.

### 3.2 Mapové podklady a export

| Pojem | Význam | V kódu |
|---|---|---|
| **Kachle** | Čtvercový obrázek mapy (256 px) z mapového serveru v pevné mřížce podle moumeru. Stahuje se, ukládá do cache, zobrazuje. **Není to dlaždice.** | `Ka`, `JKachle`, `plugins/mapy/kachle` |
| **Lokace kachle** | Moumer a souřadnice kachle v mřížce. | `KaLoc` |
| **Podklad** | Mapový zdroj kachlí, který uživatel volí v menu Mapy (OSM, mapy.cz, ortofoto…). Od 11/2019 je podklad totéž co typ kachle. Skládání vrstev (dekorace, popisky) bylo tehdy zrušeno. | `KaType`, `EKaType`, `ConfiguredKaType`, `MapyModel.podklad` |
| **Id podkladu** | Jméno podkladu (`name` v definici). Je zároveň klíčem v diskové cache kachlí, takže jeho přejmenování osiří uložené kachle. | `name` v JSON |
| **Definice podkladů** | JSON se šablonami URL zdrojů kachlí: uživatelský soubor, nebo výchozí `defaultMapDefinitions.utf-8.json`. | `MapyModel` |
| **Nastavení podle podkladu** | Nastavení kreseb nad mapou ukládané zvlášť pro každý podklad (např. jiné barvy popisků na letecké a turistické mapě). | `PodkladMapSpecificModel0` (potomci `KruhyModel`, `PopiskyModel`, `ObsazenostModel`) |
| **Kachlovník** | Vrstva skládající kachle do zobrazení. | `JKachlovnik*` |
| **Kaputer** | Výpočty vztahu kachlí a zobrazení (kachle + computer). | `Kaputer` |
| **Získávač kachlí** | Získání kachle z paměti, disku nebo sítě. | `KachleZiskavac`, `KachloDownloader` |
| **Záznam cache kachle** | Položka paměťové cache získávače: řídí získání jedné kachle a eviduje příjemce, kteří na ni čekají. | `KachleZiskavac.Kachlice` (vnitřní třída, stejnojmenný soubor `Kachlice.java` je prázdný) |
| **Priorita kachle** | `KACHLE` (zobrazení na mapě) × `STAHOVANI` (offliner, render). Stahování se brzdí, dokud čekají kachle k zobrazení. | `Priority` |
| **Kanceler** | Zrušení požadavku na kachli, která mezitím opustila výřez. | `Kanceler` |
| **Zástupná kachle** | Obrázek zobrazený místo chybějící kachle (offline, chyba stažení). | `KachloDownloader.EPraznyObrazek` |
| **Ukládanec** | Kachle ve frontě na uložení na disk. | `Ukladanec` |
| **Offliner** | Hromadné stažení kachlí oblasti pro offline použití. | `JKachleOflinerDialog`, `KachleOflinerAction` |
| **Online/offline režim** | Zda smí aplikace na síť. Řídí stahování kachlí, geocoding i kontrolu aktualizací. | `OnofflineModel` |
| **Render** | Export výřezu mapy do souboru: obrázek, KMZ (Google Earth), OziExplorer, tisk. | `core/render` |
| **Rendrovadlo** | Jádro renderování do obrázku. | `Rendrovadlo` |
| **Měřítko renderu** | Moumer, ve kterém se renderuje. Liší se od moumeru výřezu a určuje velikost výstupu. | `renderedMoumer` |
| **Ořezávací obdélník** | Rámeček oblasti renderu zobrazený na mapě během přípravy renderu. | `JRenderSlide` |
| **Dlaždice** | Díl, na který se rozdělí renderovaný výstup kvůli limitu velikosti (KMZ). **Není to kachle.** | `DlazdicovaMetrika`, `kmzMaxDlazdiceX` |
| **Kalibrační bod** | Dvojice pixel ↔ WGS zapisovaná do `.map` souboru pro OziExplorer. | `JKalibrBoduSpinner`, `RenderSettings` |
| **Papírové měřítko** | Poměr 1:N pro tisk, z něj se počítá DPI. | `RenderSettings.papiroveMeritko` |
| **Papírová metrika** | Volba formátu papíru (A0…) a orientace pro tisk, odvozená z papírového měřítka. | `PapirovaMetrika` |

### 3.3 Kešoidy

| Pojem | Význam | V kódu |
|---|---|---|
| **Kešoid** | Zobecněná keš: cokoli, co se hledá nebo zobrazuje jako objekt s waypointy. | `Kesoid` |
| **Druh kešoidu** | Keš (`gc`), waymark (`wm`), munzee (`mz`), fotka (`pic`), český geodetický bod (`gb`, CGP), obecný waypoint (`wp`). | `EKesoidKind`, `GEN_druhKesoidu`, `plugins/kesoid/kind/*` |
| **Kešoidový plugin** | Podpora jednoho druhu kešoidu: načítání, ikony, popisky, detail. | `KesoidPlugin`, `KesoidPluginManager` |
| **Waypoint** | Bod s polohou patřící ke kešoidu. Mezinárodní pojem geocachingu. | `Wpt` |
| **Hlavní × první waypoint** | **První waypoint** jsou úvodní (listingové) souřadnice keše. **Hlavní waypoint** je u keše final, pokud má neprázdné souřadnice, jinak první waypoint. Ostatní waypointy jsou **vedlejší**. | `getMainWpt`, `getFirstWpt`, `Kes.setMainWpt` |
| **Ručně přidaný waypoint** | Vedlejší waypoint přidaný uživatelem v GeoGetu (podle flagu GeoGet rozšíření). V tooltipu označen „+“. | `Wpt.rucnePridany` |
| **Nutný k luštění** | Vlastnost waypointu: hlavní, otázka nebo stage multiny. Stojí na ní volba filtru „Jen do terénu“. | `Wpt.nutnyKLusteni()` |
| **Pořadí kreslení** | Pořadí vrstev waypointů: ostatní, vedlejší waypoint keše, první, final (final nahoře). | `Wpt.EZOrder` |
| **Weikoid** | Společný předek kešoidu a waypointu (waypoint + kešoid). | `Weikoid0` |
| **Keš** | Geocachingová keš: typ, velikost, obtížnost, terén, hodnocení, známka, favority, BestOf. | `Kes` |
| **Vztah** | Vztah uživatele ke kešoidu: nehledané, nalezené, vlastní, neexistující, captured, deployed (munzee). | `EKesVztah` |
| **Stav** | Aktivní, deaktivovaná, archivovaná. | `EKesStatus` |
| **Hodnocení, BestOf, favority, známka** | Hodnocení z geocaching.cz a geocaching.com, přichází přes GeoGet. | `Kes`, `Gpxg` |
| **Nick** | Přezdívka uživatele na geocaching.com, určuje vlastní keše. | `GccomNick` |
| **Zdroj** | Soubor nebo databáze, odkud byly kešoidy načteny. Zobrazují se jako strom pod kořeny a dají se blokovat. | `InformaceOZdroji`, `KeFile` |
| **Kořen zdrojů** | Místo, odkud se zdroje skenují. Jsou tři: data Geokuku (rekurzivně: geokuk, gpx, zip, jpg…), GeoGet (`*.db3`), GSAK (`sqlite.db3`). | `Root`, `Root.Def`, `MultiNacitac` |
| **Blokovaný zdroj** | Zdroj, který uživatel vyřadil z načítání, i s celým podstromem. | `KesoidModel.blokovaneZdroje` |
| **Načítač** | Importér jednoho formátu: GPX, DB GeoGetu, DB GSAKu, metadata fotek, vlastní formát Geokuku. | `Nacitac*`, `GeogetLoader`, `GsakDbLoader` |
| **Procák** | Zpracovatel jednoho GPX waypointu do kešoidu. Procáky jednotlivých pluginů jsou řetězené. | `Procak`, `*GpxWptProcak`, `ProcakDispatcher` |
| **GeoGet rozšíření** | Data z `<gpxg:GeogetExtension>` a z databází GeoGetu a GSAKu: nalezení, flag ručního přidání, hodnocení, BestOf, favority, známka, výška, kraj, okres, uživatelské tagy `geokuk_*`. | `Gpxg` |
| **Kepodr** (kešoid poddruh) | Ve skutečnosti **druh popisky**: určuje šablonu a dostupné zástupky popisku na mapě. Deklaruje ho plugin. Hodnoty `kes`, `kesadwpt`, `waymark`, `munzee`, `cgp`, `photo`, `simplewaypoint`. Popisky se kreslí jen u hlavních waypointů, takže `kesadwpt` se neuplatní a prakticky kepodr = druh kešoidu. | `Kepodr`, `KesoidPlugin.getKepodrs()`, `PopiskyPatterns` |
| **Bag** | Množina všech načtených kešoidů s prostorovým indexem. | `KesBag` |

### 3.4 Genetika

Vlastnosti waypointů a jejich zobrazení popisuje Geokuk pomocí **genetické metafory**. Kdo zná základy genetiky, odvodí si vztahy mezi pojmy sám.

| Pojem | Význam | V kódu |
|---|---|---|
| **Genom** | Registr všech genů, alel a druhů. | `Genom` |
| **Gen** | Jedna vlastnost waypointu: sym (typ), postavení (hlavní × vedlejší), postavení k myši, druh, vztah, stav, výlet, cesty, velikost, obtížnost, terén… | `Gen` |
| **Alela** | Hodnota genu. Každý gen má **výchozí alelu**. Kvalifikované jméno má tvar `alela:gen` (`fnd:vztah`). | `Alela` |
| **Druh** | Určuje, které geny daný druh kešoidu má a na kterých lokusech. | `Druh` |
| **Lokus** | Pozice genu v DNA daného druhu. | `Druh.lokusy` |
| **Genotyp** | Neměnná, internovaná (sdílená) kombinace alel jednoho waypointu. | `Genotyp` |
| **DNA** | Pole alel genotypu indexované lokusem. | `Genotyp.dna` |
| **Fenotyp** | Projev genotypu navenek, tedy ikona. **Nefenotypované alely** se na ikoně neprojeví. | `jmenaNefenotypovanychAlel`, `JFenotypIkonyDialog` |
| **Uživatelské geny** | Geny a alely vzniklé za běhu z tagů `geokuk_*` GeoGetu. | `Nacitac0.PREFIX_USERDEFINOANYCH_GENU` |
| **Gen `vylet`, gen `cesty`** | Geny, kterými se výlet a cesty promítají do ikony: `lovime`, `ignoru`, výchozí `nevime` / `nacestejsou`, výchozí `mimocesticu`. | `Genom.GEN_vylet`, `GEN_cesty` |
| **Grupa** | Skupina alel symbolového genu podle prefixu názvu souboru ikony (`gc!`, `gcawp!`). **Zastaralé** („Grupy nechceme, použijeme druhy“), přesto ji používá toolbar a dialogy výběru ikon. Pro nový kód použijte druh. | `Grupa`, `GrupaSym` |
| **Zhasnutí** | **Úplné skrytí** waypointů s danou alelou, tj. přidání alely do množiny nechtěných alel filtru. Vstupy: menu „Zhasni“, ovladače alel na toolbaru, dialog Filtr (F2). | `ZhasniKeseUrciteAlelyAction`, `jmenaNechtenychAlel`, `Gen.vypsatelnyVeZhasinaci` |
| **Ovladače alel** | Zaškrtávátka zhasnutí na toolbaru. Seznam alel je pevný. | `JToolbarOvladaceAlel` |
| **Filtr** | Nechtěné alely + prahy (hodnocení, BestOf, favority, výlet, „jen do terénu“, „jen final“). | `KesoidFilter`, `FilterDefinition` |

| Genetika | Obecný IT pojem |
|---|---|
| gen | atribut, vlastnost |
| alela | hodnota výčtu |
| druh | schéma, typ |
| lokus | index v poli |
| genotyp | internovaná n-tice hodnot (flyweight) |
| fenotyp | zobrazení dat |

### 3.5 Ikony

Ikona waypointu je fenotyp: skládá se z vrstev podle alel genotypu.

| Pojem | Význam | V kódu |
|---|---|---|
| **Sada ikon** | Pojmenovaná sada grafiky ikon (adresář s definicemi). | `Sada`, `ASada` |
| **Sklo** | Jedna vrstva, ze kterých se skládá ikona (obrysy, čtverce, typy, zešednutí…). Definice ve `skla.txt`. | `Sklo`, `skloNormal`, `skloZvyrazneni` |
| **Vrstva skla** | Vyhledávací struktura ikon jednoho skla podle alel. | `Vrstva` |
| **Icondef** | Definice obrázku pro kombinaci alel. Alely jsou v názvu souboru. | `IconDef`, `IconDefNacitac` |
| **Sklivec** | Výsledná složená ikona jednoho genotypu (obrázky přes všechna skla). Cache genotyp → sklivec. | `Sklivec` |
| **Imagant** | Obrázek s posunem vůči středu. | `Imagant` |
| **Repaintanger** | Repaint + rectangle: **maximální** okraje, o které libovolná ikona sady přesahuje bod waypointu. Hodnota jen roste. Rozšiřuje oblast k překreslení i výběr waypointů při kreslení. | `Repaintanger` |

### 3.6 Výlet a cesty

**Výlet a cesty jsou dvě různé věci.** Výlet je dlouhodobé třídění kešoidů. Cesty jsou konkrétní trasy přes body. Ve verzi 3.0 se sloučily, verze 3.3.0 je znovu oddělila.

| Pojem | Význam | V kódu |
|---|---|---|
| **Výlet** | Rozdělení kešoidů na **ANO** (lovím), **NE** (ignoruji, vynechávám) a **NEVÍM** (nerozhodnuto, výchozí). | `Vylet`, `VyletModel` |
| **Stav ve výletu** | ANO / NE / NEVÍM jednoho kešoidu. Do ikony se promítá genem `vylet`. | `EVylet` |
| **Práh výletu** | Volba filtru: všechny / bez vynechaných / jen lovené. Používá tentýž enum `EVylet` (viz kapitola 7). | `FilterDefinition.prahVyletu` |
| **Seznam GGT** | Formát GeoGetu: prostý text, jeden kód kešoidu na řádek. | `Ggt`, `VyletPul` |
| **Půlka výletu** | Obsah jednoho souboru výletu: `lovim.ggt` = ANO, `tedne.ggt` = NE. NEVÍM se neukládá. | `VyletPul` |
| **Cesta** | Lomená čára přes body. | `Cesta` |
| **Vybraná cesta** | Cesta, ke které se přidávají body a na kterou míří akce z menu. Může být žádná. | `CestyModel.curta` |
| **Bod cesty** | Vrchol cesty. Může být navázán na waypoint. | `Bod` |
| **Připíchnutí bodu** | Navázání bodu cesty na waypoint: při kreslení přichycením, po načtení podle souřadnic. | `CestyZperzistentnovac.pripniNaWayponty` |
| **Úsek** | Spojnice dvou bodů cesty. Všechny úseky jsou přímé. | `Usek` |
| **Vzdušný úsek** | Úsek, který se nepočítá do délky cesty, kreslí se čárkovaně a v GPX rozděluje cestu na samostatné `trkseg`. Představuje přesun, který se nejde. | `Usek.vzdusny`, `PrepniVzdusnostUseku` |
| **Segment** | Souvislá část cesty mezi vzdušnými úseky, v GPX `trkseg`. | `Bod.isNaHraniciSegmentu` |
| **Bousek** | Bod nebo úsek (bod + úsek), společný předek. | `Bousek0` |
| **Blízký bousek** | Bod nebo úsek do 7 px od kurzoru, na který míří myš a kontextové menu. | `JCestySlide.blizkyBousek` |
| **Krajový bod** | Počáteční nebo koncový bod cesty. | `Bod.isKrajovy` |
| **Uzavřená (kruhová) cesta** | Cesta, jejíž první a poslední bod leží na stejném místě. | `Cesta.isKruh` |
| **Startocíl** | Krajový bod uzavřené cesty, který je startem i cílem. | `Bod.isStartocil` |
| **Jednobodová cesta** | Cesta s jediným bodem. Maže se akcí „Promazat jednobodové cesty“. | `Cesta` |
| **Dokument cest** | GPX soubor se všemi cestami. V UI a kódu se často nepřesně nazývá „výlet“. | `Doc` |
| **Zperzistentňovač** | Ukládání a načítání výletu a cest ze souborů. | `VyletovyZperzistentnovac`, `CestyZperzistentnovac` |

### 3.7 Mapové okno a kresby

| Pojem | Význam | V kódu |
|---|---|---|
| **Slide** | Průhledná vrstva mapového okna (kešoidy, cesty, popisky, mřížky, kachle…). | `JSlide0`, `JSingleSlide0` |
| **Překryvník** | Kontejner skládající slidy přes sebe. | `JPrekryvnik` |
| **Překryvník s rohama** | Kořen hlavního okna: mapa přes celou plochu a v rozích plovoucí panely (JZ detail v rohu, JV informace o keši). | `JSRohamaPrekryvnik`, `CornerLayoutManager` |
| **Detail v rohu** | Malá mapa vlevo dole s pevným moumerem 17 a ortofotem, centrovaná na pozici. Při kreslení cest sleduje kurzor. | `JDetailPrekryvnik`, `JKachlovnikDoRohu` |
| **Myšování** | Nejvyšší průhledný slide, který chytá myš a klávesy. | `JPresCeleMysovani`, `JDetailMysovani` |
| **Popisky** | Texty u waypointů podle šablony (Ctrl+F12). | `plugins/kesoidpopisky` |
| **Kruhy** | Zvýrazňovací kruhy kolem waypointů. | `plugins/kesoidkruhy` |
| **Obsazenost** | Kruhy o poloměru 161 m kolem **obsazujících waypointů** (final, stage multiny, tradiční keš, ne archivované): oblasti obsazené kešemi. | `JObsazenost`, `Wpt.obsazujeOblast()` |
| **Malovadlo** | Kreslení cest. | `Malovadlo` |
| **Stavový řádek** | Souřadnice myši, počty kešoidů, vzdálenost a azimut od pozice, počty ve výletu, měřítko. | `JStatusBar` |

### 3.8 Hledání a ostatní

| Pojem | Význam | V kódu |
|---|---|---|
| **Hledač, nálezenec** | Hledání (kešoidů podle textu, míst přes geocoding). Nálezenec je výsledek se vzdáleností a azimutem od referenčního bodu. | `Hledac0`, `Nalezenec0` |
| **Geocoding** | Hledání adres a míst (Ctrl+M). | `plugins/geocoding` |
| **Street View** | Otevření Google Street View v prohlížeči. Třída se jmenuje zavádějícím způsobem `OpenStreetViewAction`, s projektem OpenStreetView nesouvisí. | `OpenStreetViewAction` |
| **Umístění souborů** | Nastavitelné cesty k datům (kešoidy, kachle, render, GeoGet). | `*UmisteniSouboru` |
| **Zprávy uživatelům** | Novinky z `geokuk.cz/version.php`. Tentýž dotaz slouží i ke kontrole aktualizací. | `ZpravaUzivateli`, `ZkontrolovatAktualizaceSwingWorker` |
| **Profil nastavení** | Pojmenovaný podstrom nastavení vybraný parametrem `-Dprefroot`. Nedokončené. | `MyPreferences` |

---

## 4. Obecný technický kód (tw)

| Třídy | Význam |
|---|---|
| `util/index2d`: `Indexator`, `Ctverecnik`, `Sheet`, `Splitenec` | Quadtree: index, vnitřní uzel se 4 potomky (jz, jv, sz, sv), list, výsledek rozdělení. |
| `util/pocitadla`: `Pocitadlo`, `PocitadloMalo`, `PocitadloNula`, `PocitadloRoste`, `PocitadloSystem` | Diagnostické metriky zobrazené v servisním okně. Varianty mají stejné chování a liší se jen skupinou: má být malé (počty objektů, únik paměti), má být nula (fronty), stále roste (kumulativní počty událostí), hodnoty JVM. |
| `util/procak`: `Procak`, `ProcakDispatcher` | Řetěz zpracovatelů s více koly. |
| `util/file`: `KeFile`, `Root`, `FileAndTime`, `DirScanner` | Soubor vztažený ke kořenu skenování (include/exclude, hloubka) s časem změny. „Ke“ pravděpodobně znamená kešoid, obsah je ale obecný. |
| `util/file`: `LamUrl`, `MultiFolder`, `KeyTree` | Last-Modified URL. Překryvný strom souborů z resources a složek na disku (ikony), přípona `.remove` položku odebere. |
| `framework/Atom` | Otevřený výčet: pojmenované hodnoty internované za běhu (`Atom.valueOf`). |
| `util/lang`: `ADate`, `AString0`, `IAtom*` | Hodnotové typy převzaté z tw, v aplikaci se skoro nepoužívají. |
| `framework`: `BeanBag`, `Factory`, `BeanType`, `MultiInjection`, `AfterInjectInit`, `AfterEventReceiverRegistrationInit` | Vlastní DI kontejner nad reflexí: injekce přes metody `inject(X)`, inicializace ve čtyřech fázích. Aplikaci skládá `core/program/Inicializator`. |
| `framework`: `EventManager`, `Event0`, `Model0` | Publish/subscribe podle třídy události přes metody `onEvent(X)`. Odběratelé přes slabé reference. Nový odběratel hned dostane poslední událost. Událost se odesílá jen z EDT. |
| `framework`: `Onoff`, `OnoffEvent0`, `ToggleAction0` | Přepínač v modelu, který při změně vyšle událost. Akce se zaškrtnutím sdílená menu a tlačítkem. |
| `framework`: `MyPreferences`, `Prefe`, `@Preferenceble` | Nastavení v Java Preferences (na Windows registr), struktury přes JavaBeans. Volitelné zrcadlo do `geokuk-preferences.xml` vedle JARu. Jména klíčů drží `core/program/FPref`. |
| `framework/Dlg` | Dialogy ano/ne, chyba, info, přepsat soubor. |
| `util/gui`: `CornerLayoutManager`, `ERoh` | Layout: podklad přes celou plochu a komponenty v rozích. |
| `util/exception`: `AExcId`, `FExceptionDumper`, `JErrorDialog` | Výjimky s identifikátorem, dump do `%TEMP%/geokuk/excrep`, dialog „Přehled problémů“. |
| `core/program/JServiceFrame` | Servisní okno s počítadly (menu „Servis…“). |
| `core/lookandfeel`, `LafSupport` | Volba vzhledu Swingu a tématu (menu „Skin“). |

---

## 5. Balíky

### 5.1 Původní záměr: malé jádro a vyměnitelné pluginy

Geokuk měl být **obecná, relativně malá aplikace nad mapami**. Vše, co je v `plugins`, mělo jít přidávat a vyměňovat. Pluginem měly být i samotné keše (kešoidy), tedy jádro by o geocachingu vůbec nevědělo.

Refaktoring k tomuto cíli se **nepodařilo dotáhnout**. Názvy balíků (`core`, `plugins`, `api`) proto popisují záměr, ne skutečnost:

- **`core` závisí na pluginech.** `core/program` (hlavní okno, menu, `Inicializator`) importuje všech deset modulů v `plugins`, `core/coord` a `core/render` po čtyřech.
- **`plugins/kesoid` je centrum.** Závisí na něm skoro všechny ostatní moduly a on sám závisí na `cesty`, `vylety`, `kesoidpopisky`, `mapy` a `refbody`. S každým z nich tvoří cyklus.
- **`framework`, `util` a `api` závisí na doméně** (`framework` asi 37 importy z `core` a `plugins`, `api/mapicon` na `plugins/kesoid`).

Skutečně fungující pluginy jsou jen **druhy kešoidů** v `plugins/kesoid/kind/*`. Implementují `KesoidPlugin`, hledají se reflexí (`KesoidPluginManager`) a nový druh jde přidat bez zásahu do jádra. Je to zmenšený model toho, jak měla vypadat celá aplikace.

**Ostatní obsah `plugins` jsou moduly podle funkce**, ne vyměnitelné pluginy. Nový kód by ale neměl vzdálit aplikaci od původního záměru:

- nevytvářet nové závislosti `core` → `plugins`, `framework` → doména, `util` → doména,
- nevytvářet nové cykly mezi moduly,
- novou funkci, která jde udělat jako samostatný modul nad rozhraním, tak udělat.

### 5.2 Co který balík skutečně obsahuje

Cesty jsou relativní k `src/main/java/cz/geokuk`.

| Balík | Obsah | Poznámka |
|---|---|---|
| `core/program` | hlavní okno, menu (`Menu`), akce (`Akce`), složení aplikace (`Inicializator`), konstanty (`FConst`, `FPref`), stavový řádek | importuje všechny moduly |
| `core/coordinates` | **souřadnicové systémy**: Mou, Wgs, UTM, Mercator, převody | téměř synonymum s `coord`, obsah jiný |
| `core/coord` | **zobrazení a pohyb po mapě**: `Coord`, pozice, výřez, zoom, posun, záměrný kříž; navíc mapové akce (`OpenStreetViewAction`, `NastavMapuCeskaAction`) | |
| `core/render` | render (obrázek, KMZ, OziExplorer, tisk) | anglický název doménového balíku |
| `core/hledani` | obecný rámec hledání (`Hledac0`, `Nalezenec0`) | konkrétní hledání kešoidů je v `plugins/kesoid/hledani` se stejnými názvy tříd bez `0` |
| `core/napoveda` | nápověda, O programu, zprávy uživatelům, kontrola aktualizací | |
| `core/onoffline` | online/offline režim | přepínač `OnlineModeAction` je ale v `plugins/mapy/kachle` |
| `core/profile`, `core/lookandfeel` | ukládání nastavení k programu, vzhled (Skin) | |
| `framework` | infrastruktura: DI (`BeanBag`), události (`EventManager`), modely (`Model0`), nastavení, slidy a překryvníky | obsahuje i doménu (`PodkladMapSpecificModel0`) |
| `api/mapicon` | kreslení ikon (`Imagant`, `*Drawer`) | **není API**, závisí na `plugins/kesoid` |
| `img` | javový balík s `ImageLoader`, ne složka obrázků | obrázky jsou v resources |
| `util/*` | obecný technický kód (turboware) | testy `index2d` leží v balíku s překlepem `util/yndex2d` |
| `plugins/kesoid` | kešoidy, waypointy, `KesBag`, filtr (`KesoidFilter`, `FilterDefinition`), vykreslení (`JKesoidySlide`) | centrum aplikace |
| `plugins/kesoid/kind/*` | **skutečné pluginy**: druhy kešoidů (`kes`, `waymark`, `munzee`, `cgp`, `photo`, `simplewaypoint`) | |
| `plugins/kesoid/importek` | načítače (GPX, GeoGet, GSAK, fotky) | **`importek`**: `import` je v Javě klíčové slovo |
| `plugins/kesoid/genetika` | genom, geny, alely, genotyp | |
| `plugins/kesoid/mapicon` | sady ikon, skla, sklivce | |
| `plugins/kesoid/mvc` | **odkladiště** (36 souborů): akce, události, dialogy, `KesoidModel`, nick, parametry GSAKu, i `MapyUmisteniSouboru` | sem nic nového nepřidávat |
| `plugins/kesoid/filtr` | dialog filtru | třídy filtru samotného jsou v kořeni `plugins/kesoid` |
| `plugins/kesoid/detail` | detail kešoidu | |
| `plugins/kesoid/detailroh` | **detail v rohu** (malá mapa) a záměrný kříž | s kešoidy nesouvisí, jen tu leží |
| `plugins/kesoid/data` | jediný soubor `EKesoidKind` | |
| `plugins/kesoid/hledani` | textové hledání kešoidů, hledání souřadnic | |
| `plugins/kesoidpopisky`, `kesoidkruhy`, `kesoidobsazenost` | popisky, zvýrazňovací kruhy, obsazenost | moduly nad kešoidy, pojmenované prefixem místo vnoření |
| `plugins/mapy` | podklady (`MapyModel`), kachle (`kachle/data`, `kachle/gui`, `kachle/podklady`), offliner (`stahovac`) | |
| `plugins/cesty` | cesty: data (`data`), akce rozdělené podle objektu (`akce/bod`, `cesta`, `usek`, `doc`, `soubor`), ukládání | |
| `plugins/vylety` | výlet (ANO / NE / NEVÍM) | |
| `plugins/mrizky` | souřadnicové mřížky | |
| `plugins/refbody` | **domov a pojmenované body** | ne referenční bod (ten je v `HlidacReferencnihoBodu`), viz kapitola 7 |
| `plugins/geocoding` | hledání adres | |

### 5.3 Pojmenování balíků

- Doménové balíky jsou většinou česky (`cesty`, `vylety`, `mrizky`, `genetika`, `filtr`, `podklady`, `stahovac`, `detailroh`, `importek`). Anglicky jsou `coord`, `coordinates`, `render`, `program`, `profile`, `geocoding`, `onoffline` a v kešoidech `kind`, `data`, `detail`, `mvc`, `mapicon`. `refbody` je napůl.
- Organizace se mísí: někde podle vrstev uvnitř funkce (`cesty/akce` + `cesty/data`, `mapy/kachle/data|gui|podklady`), jinde ne.
- Pro nové balíky platí pravidla kapitoly 1: doménový balík česky, obecný anglicky.

## 6. Externí formáty a systémy

| Pojem | Význam |
|---|---|
| **GPX** | Standardní XML formát waypointů a tras. Geokuk ho čte (kešoidy) i zapisuje (dokument cest). |
| **Groundspeak** | Rozšíření GPX od geocaching.com s údaji o keši (`groundspeak:cache`). |
| **GeoGet** | Český program na správu keší. Geokuk čte jeho databázi (`*.db3`), GPX rozšíření `gpxg`, `geohome.ini`, zapisuje seznamy GGT a předává mu kešoidy přes schránku. |
| **GGT** | Seznam kódů kešoidů GeoGetu, jeden na řádek. |
| **GSAK** | Program na správu keší. Geokuk čte jeho databázi `sqlite.db3`. |
| **KMZ** | Zabalený KML pro Google Earth a navigace Garmin, výstup renderu. |
| **OziExplorer** | Program pro kalibrované mapy, výstup renderu (obrázek + `.map`). |
| **JTSK, S-42, UTM, MGRS** | Souřadnicové systémy pro zobrazení a zadávání souřadnic. |

---

## 7. Kolize a dvojznačnosti pojmů

Na tato místa si dejte pozor. Při úpravách kódu nebo textů UI je dobré je sjednocovat.

1. **Kachle × dlaždice.** V češtině synonyma, v Geokuku dvě různé věci. Ani samotný kód rozlišení nedodržuje: offliner („Postahovat dlaždice…“), počítadla („Downloadlé dlaždice“) a `KachleUrlBuilder` říkají *dlaždice* ve významu **kachle**.
2. **`EVylet` má dvě role:** stav kešoidu ve výletu a práh filtru. Jeho `toString()` vrací texty filtru, takže stav NE se zobrazí jako „Všechny“. Filtr porovnává `ordinal()`, který se ukládá do nastavení, proto se pořadí konstant nesmí měnit. `ECeystyVylet` je nepoužitý pokus o oddělení rolí.
3. **Výlet × dokument cest.** Dokument cest se v UI a kódu často nazývá „výlet“ (`FPref.VYLET_node`, „Můj výlet.gpx“, popisek `ExportujDoGgtAction`). Soubor `lovim.ggt` má dvě role: výlet ANO v `~/geokuk/` a výchozí cíl exportu cest v adresáři GeoGetu.
4. **Referenční bod × pojmenovaný bod.** Referenční bod je „pozice, jinak střed výřezu“. Balíček s domovem a pojmenovanými body se ale jmenuje `refbody` a tooltip pojmenovaného bodu říká „Přesune mapu na referenční bod“.
5. **Zhasnout** u kešoidů znamená skrýt podle alely, u mřížek (`ZhasniVsechnyMrizkyAction`) vypnout vrstvu.
6. **Prefix `A`:** potomek `Atom` × hodnotový typ.
7. **`KesoidFilter.isFiltered()`** vrací `true`, když waypoint filtrem **projde** a zůstane zobrazen.
8. **Fotka × obrázek.** Druh `pic` má třídu `Photo` a popisek „Fotka“, alela se ale zobrazuje jako „Obrázek“. Používejte **fotka**.
9. **Záměrný kříž.** Kód i UI používají „záměrný“, ne „zaměřovací“.
10. **Kepodr** slibuje obecné třídění waypointů („kešoid poddruh“), ale slouží jen k výběru šablony popisku.
11. **`DEFAULTNI_DOMACI_SOURADNICE`** je ve `VyrezModel` i v `RefbodyModel`. Ve `VyrezModel` slouží jako výchozí střed výřezu.
12. **`OpenStreetViewAction`** otevírá Google Street View.

---

## 8. Překlepy v názvech

Hodí se při hledání v kódu: když hledáte „Groundspeak“, konstanta se jmenuje `GROUNSPEAK_*`.

| Název v kódu | Správně |
|---|---|
| `ECeystyVylet` | ECestyVylet |
| `SouboeCestaAction0` | SouborCestaAction0 |
| `OpenStreatMapUrlBuilder` | OpenStreetMapUrlBuilder |
| `GeocodingBorowserXmlAction` | GeocodingBrowserXmlAction |
| `JNastavovecMeritka` | JNastavovacMeritka |
| `InformaceoZdrojichAction` | InformaceOZdrojichAction |
| `XZaknclovanoRazeni` | XZkanclovanoRazeni |
| `XRelativizeDubleDot` | XRelativizeDoubleDot |
| `KaType.isOverleay` | isOverlay |
| `Sada.getBigiestIconInsets` | getBiggestIconInsets |
| `Kes.setVztahx` | setVztah |
| `getFountTime` / `setFoundTime` | getFoundTime |
| `GROUNSPEAK_*` | GROUNDSPEAK_* |
| `Groundspeak.availaible` | available |
| `FiltrIkonyAction` (popisek „Fitr…“) | Filtr |
| `PreferencebleProperty` atd. | Preferenceable |
| balík testů `util/yndex2d` | `util/index2d` (testy tak leží v jiném balíku než testovaný kód a nevidí package-private třídy) |

---

## 9. Zastaralý a nedokončený kód

Na tento kód nenavazujte, nebo ho při úpravách dokončete či odstraňte.

- **Grupa** (`Grupa`, `GrupaSym`): označená jako zastaralá, má ji nahradit druh.
- **Kepodr a popisky**: přesun definic popisků do pluginů (12/2019) zůstal nedokončený. `PopiskyPatterns` má pro každý kepodr ručně psané pole, výchozí šablony pluginů se nepoužívají a třída `PopiskaDef` je nepoužitá.
- **Mřížky S-42 a JTSK**: jen akce a modely.
- **Profily nastavení**: `PreferencesProfileChangedEvent` se nikdy nevysílá.
- **Mrtvé třídy a pole**: prázdný soubor `Kachlice.java`, `Test1` v hlavním kódu, `EKesFilterStavove`, `ECeystyVylet`, `ConfigurableMapSourcesChangedEvent`, `ProfileUkladacSwingWorker`, `Imagant.computeInsets`, `Grupa.isOther()`, `KaType.isTransparent()`/`isOverleay()`, `Gpxg.czkraj`/`czokres`, `KesoidModel.jmenaAlelNaToolbaru` (ukládá se, nikdo nečte).
- **Duplicity**: čtení formátu GGT je dvakrát (`Ggt` a `VyletPul`), předání kešoidů do GeoGetu přes schránku také dvakrát.
- **Zastaralý komentář**: `genetika/package-info.java` tvrdí, že genetický engine ještě není zapojený. Používá se v celé aplikaci.
- **Porušení vrstev**: `framework/PodkladMapSpecificModel0` a `JPresCelePrekryvnik` závisí na doméně, `api/mapicon/Imagant` na `plugins.kesoid`, a `KeFile`, `DirScanner`, `Root` leží v `util/file`, ačkoli je používá jen import kešoidů.

---

## 10. Jak slovník udržovat

- Nový doménový pojem doplňte do příslušné tabulky kapitoly 3: český název, význam z pohledu uživatele a hlavní třídy.
- Nový obecný kód (anglicky) doplňte do kapitoly 4 jen tehdy, když jeho název sám nestačí.
- Když opravíte kolizi, překlep nebo dokončíte zastaralý kód, upravte kapitoly 7 až 9.
- Význam pojmu ověřujte v kódu, ne podle názvu třídy. Několik názvů v Geokuku slibuje něco jiného, než skutečně dělá (Kepodr, `OpenStreetViewAction`, `isFiltered`).

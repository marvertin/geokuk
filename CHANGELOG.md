# Změny

## 6.5.0

### Změny
- Plánovač challenge (Kešoidy > Challenge): v Plánovači challenge… se vybere
  challenge (šablony 360° od domova, GC4PCQB – Challenge - 360 degrees of the
  Czech Republic, GC56R49 – Challenge - 360 Degrees of Prague a Matice D/T) a
  zapne se. Pruh vlevo nahoře na mapě ukazuje počet
  chybějících sektorů 360° nebo políček D/T; po kliknutí na počet se ukáže
  seznam chybějících sektorů s kandidáty nebo chybějících políček D/T. Další
  chybějící F4 (Shift+F4 zpět), zobrazení jen kandidátů na mapě.
- Položka Kešoidy > Kruhy (Alt+F8) se jmenuje podle poloměru (Kruhy 1 km,
  Kruhy 161 m); dialog se jmenuje Nastavení kruhů.
- Kruhy (Alt+F8) mají pevný poloměr v metrech, výchozí 100 m; mění se v
  Nastavení kruhů. Zapnou i zvýrazňovací kruhy.
- Když jsou zapnuté kruhy (zvýrazňovací, v metrech, Obsazenost) a nejsou vidět,
  ve stavovém řádku se ukáže štítek Kruhy skryté; v bublině je u každé vrstvy
  důvod a co udělat. Když se kruhy v metrech kreslí jen částečně (nejbližší ke
  středu výřezu), je tam štítek Kruhy omezené, a když je zároveň něco skryté
  a něco omezené, Kruhy skryté a omezené.
- Po zkratkách F5, F8, Alt+F8, F9 a Ctrl+1 až Ctrl+4 se uprostřed dole nad
  měřítkem na chvíli ukáže krátká zpráva s názvem akce a stavem, třeba „Zdroj
  keší GeoGet – Vypnuto“ nebo „Kruhy v metrech – Zapnuto, 100 m“. Když se
  zapnuté kruhy nekreslí, je v ní i důvod a co udělat.
- Posuvníky kruhů Obsazenost 161 m jsou tenké a při najetí myší ukážou popisky
  a aktuální hodnotu.
- Velikost zvýrazňovacích kruhů se volí v pixelech nebo v metrech (od 1 m do
  50 km), průhlednost posuvníkem.
- Obsazenost 161 m (F9) je na nové instalaci vypnutá.
- GeoKuk má nové logo, ikonu a úvodní obrázek.
- Přenosná verze pro Windows při startu obnoví soubor geokuk.ico, takže ikonu
  vymění i aktualizace z programu.
- Okno Umístění souborů má výchozí velikost 900×700 a zdroje keší (GPX,
  GeoGet, GSAK, OpenSAK) jsou na jedné záložce, každý se svou klávesovou
  zkratkou.
- Zdroje keší GeoGet, GSAK a OpenSAK mají volbu Nové databáze načíst až po
  vybrání.
- Zdroj keší bez načtených souborů má v menu Kešoidy > Zdroje keší vypnutou
  položku; bublina u ní a krátká zpráva po Ctrl+1 až Ctrl+4 řeknou, že zdroj
  nemá načtené soubory a že jeho složku jde zkontrolovat v Soubor > Umístění
  souborů.
- Klávesa F5 (Kešoidy > Zobrazovat kešoidy) skryje a zase ukáže kešoidy včetně
  kruhů a popisků.
- Uživatelská mapa se zkratkou, kterou používá program (F4, F5, Ctrl+1 až
  Ctrl+4 a další) nebo jiná mapa, se odmítne; zkratku v jejím souboru stačí
  změnit nebo smazat.
- Nápověda > O programu ukazuje verzi, licenci, původní texty Martina Veverky
  a údaje o upravené verzi (GeoKuk community) s odkazy.
- Při prvním načítání mapy po spuštění je v mapě bílá plocha se slabým logem
  GeoKuk; body keší, popisky, kruhy a cesty se kreslí, až je načteno 40 %
  dlaždic výřezu.
- Offline mapy se kreslí tématem GeoKuk; původní téma je v Mapy > Téma
  offline mapy jako Mapsforge (původní).
- Varianta GeoKuk – detail v Mapy > Téma offline mapy ukazuje ve větších
  měřítkách další body pro orientaci (posedy, krmelce, stoly, ohniště, kameny,
  bunkry a další); parkoviště a zastávky ukáže o úroveň přiblížení dřív. Ikony
  jsou z projektu Osmic (CC0).
- Vydání obsahuje GeoKuk-tema.zip: téma GeoKuk a GeoKuk – detail pro aplikace
  s mapami ve formátu mapsforge (například c:geo, aDrake).
- Po aktualizaci programu se offline mapy v cache jednou vykreslí znovu.
- Správce offline map (menu Mapy nebo blok Mapy ve stavovém řádku) ukazuje
  stažené mapy, jejich obrysy v mapě a odkazy na weby s mapami.
- Mapy zemí a regionů Evropy, Kanárských ostrovů a Egypta a k nim trasy,
  vrstevnice a stínování terénu jde ve Správci offline map stáhnout; novější
  verzi stažené mapy nebo vrstvy stáhne tlačítko Aktualizovat.
- Správce offline map ukáže při stahování odhad zbývajícího času a mapu, která
  se nevejde na disk, nenechá stáhnout.
- Přerušené stahování program sám až třikrát zkusí znovu a naváže, kde skončilo.
- Tlačítko Stáhnout mapu a vrstvy stáhne mapu, trasy, vrstevnice a stínování
  naráz; Vybrat části… v nabídce na pravé tlačítko u země ukáže souhrn
  s velikostí a volným místem.
- Země stažená celá i po regionech se ve Správci offline map pozná a program
  nabídne smazat regiony, nebo celek.
- Průvodce nastavením map (Soubor > Průvodce nastavením map, při první
  instalaci se otevře sám) vybere podklad a vrstvy, stáhne, co chybí, a přepne
  mapu.
- Zdroje map jsou v mapě na jednom řádku vpravo dole; co se nevejde, je za
  ikonou ⓘ.
- Měřítková lišta je uprostřed dole, má aspoň 4 dílky a na konci délku s
  jednotkou.
- Nadmořská výška keše z databáze GeoGetu se čte až při zobrazení jejího
  detailu.
- Okno programu se vykreslí před dialogy a méně potřebné části se připravují
  až po startu.
- Dlaždice se načítají podle toho, co je vidět: nejdřív hlavní mapa a viditelné
  dlaždice od středu ven, ostatní se přeskočí. Online dlaždice se stahují podle
  téže priority.
- Posun a přibližování offline mapy méně zatěžují procesor.
- Paměťová cache dlaždic se přizpůsobí paměti programu.
- Dokud se dlaždice načítá, ukáže se její náhled z jiného měřítka.
- Načítání dlaždic ukazuje malé točící se kolečko vpravo nahoře v mapě.
- Přenosná verze pro Windows si při prvním ukončení připraví soubor pro další
  starty; první ukončení po instalaci proto trvá o několik sekund déle.
- GeoKuk načte databáze OpenSAKu se schématem do verze 28.
- Okno Najdi keš najde kešoid podle odkazu (coord.info, geocaching.com,
  Opencaching, Waymarking) nebo kódu (GC, WM, OC, OP, OZ…, také zastávku
  Adventure Labu GCxxxx-N); když kešoid s takovým ID neexistuje, hledá se zadaný
  text.
- Okno Najdi keš má výchozí velikost 950×700 (na malé obrazovce nejvýš 90 %
  plochy), jde měnit a sloupec Id se neořezává.
- Typy zdrojů keší jdou zapnout a vypnout klávesami Ctrl+1 (GPX) až Ctrl+4
  (OpenSAK), také z číselné klávesnice; zkratky jsou v menu Kešoidy > Zdroje
  keší.
- Nabídka pravého tlačítka na mapě otevře místo v Mapy.com (turistická mapa)
  a v Google Mapách; podnabídka Další mapy má OpenStreetMap a ČÚZK Geoprohlížeč
  (jen v ČR a jejím okolí), u kříže i StreetView. Značka se otevře v místě
  kurzoru, u kříže v místě kříže.
- Nová volba v menu Kešoidy „Přejít na kešoid ze schránky“ (výchozí vypnuto):
  když se do schránky zkopíruje kód kešoidu nebo odkaz s kódem a GeoKuk není
  aktivní okno, mapa se vycentruje na kešoid a otevře se jeho detail; okno se
  do popředí nepřenáší.
- Schránku program sleduje jen při zapnuté volbě a její obsah nezapisuje na
  disk; do logu se může dostat jen rozpoznaný kód kešoidu.
- Dálkové ovládání umí otevřít kešoid podle odkazu nebo kódu, i mimo aktuální
  filtr, a pokusit se přenést okno GeoKuku do popředí; podrobnosti jsou
  v README.
- GeoKuk používá novější knihovnu Guava s bezpečnostní opravou.
- Regiony stažené země jsou ve Správci offline map pod ní.
- Dole ve Správci offline map jsou přepínače Turistické a cyklotrasy a Stínování
  terénu.
- Když pro zobrazené místo chybí offline mapa, program nabídne její stažení
  (i s vrstvami a velikostí), u hranice i sousední mapy v záběru.
- Po změně jedné offline mapy nebo vrstvy se dlaždice v cache vykreslí znovu
  jen v oblasti kolem ní.
- Změna zvýraznění nebo sady ikon nenačítá znovu zdroje keší.
- Soubor > Optimalizace cache dlaždic… zmenší soubor s uloženými dlaždicemi;
  když je to potřeba, program ji nabídne sám při startu.
- Optimalizace cache dlaždic odstraní i dlaždice vykreslené starší verzí mapy
  nebo tématu; dlaždice jiných témat a vrstev zůstávají.
- Nové mapové podklady: DE topografická mapa (BKG), AT mapa (basemap.at)
  a CH topografická mapa (swisstopo); německou a švýcarskou mapu nejde
  hromadně stahovat do cache.
- Mapy > Téma offline mapy > Vrstvy tématu: u tématu s vrstvami (například
  paws, Biker, Motorider) lze zvolit styl a zapnout nebo vypnout jednotlivé
  vrstvy; ve výchozím stavu jsou zapnuté vrstvy, které zvolil autor tématu.
  Volba se pamatuje pro každé téma, u tématu paws je v menu odkaz na legendu.
- Podmenu Kešoidy > Zvýraznění ukáže barevný čtverec pod ikonou keší podle
  předvolby (Bez zvýraznění, Terén 4,5 a 5, Obtížnost 4,5 a 5) nebo vlastních
  pravidel.
- Stavy Disablovaná, Archivovaný a Není vyluštěno mají na liště nástrojů
  vlastní ikony; stejné ikony jsou ve Výběru filtru (F2), Výběru fenotypu (F6),
  Ladění ikon a v nabídce Zhasni.
- Přepínače stavů keše jsou na liště nástrojů za oddělovačem a všechny ikony na
  liště kromě druhů waypointů jsou stejně velké.
- Pojmenované výlety: ve stavovém řádku je jméno aktivního výletu a najetím
  myší se otevře seznam výletů, kde lze výlet založit, aktivovat, přejmenovat
  a smazat.
- Cesty mají azurovou barvu, vybraná cesta tmavý lem. Úseky vedené po cestě
  jsou plnou čarou, rovné čárkovaně a vzdušné tečkovaně.
- Panel cesty vpravo nahoře v mapě ukazuje délku vybrané cesty, počet bodů
  a keší a vzdušné úseky; vypnout jde křížkem nebo v Cesty > Panel cesty.
- Mapy > ČÚZK – Stínovaný reliéf: jemné stínování terénu České republiky přes
  jakoukoli mapu, i v Tisknout/Rendrovat…; jen s připojením k internetu, od
  podrobnějšího měřítka.
- Mapy > Turistické a cyklotrasy: turistické trasy a cyklotrasy přes jakoukoli
  mapu, i v Tisknout/Rendrovat…; kreslí se z offline map z osm.paws.cz nebo ze
  stažené vrstvy tras.
- Úseky cesty mohou vést po cestách a pěšinách z offline mapy (pěší trasa bez
  internetu); způsob vedení se přepíná v panelu cesty a v kontextovém menu
  úseku.
- Cesty > Přepočítat úseky po cestě zkusí znovu úseky, pro které se trasa
  nenašla.
- Úsek vedený po cestě se do GPX ukládá se všemi body.
- Nová instalace začíná offline mapou, když je ve složce offline map podrobná
  mapa, jinak ortofotem ČÚZK; stávajícím uživatelům se podklad nemění.
- Informace pro hlášení chyby rozliší uspání počítače od záseku programu.
- Informace pro hlášení chyby neobsahují z adres v logu parametry ani
  přihlašovací údaje.
- Informace pro hlášení chyby uvádějí údaje o offline mapách a počet
  zobrazených dlaždic od spuštění; okno jde měnit.
- Nápověda > Co je nového ukáže, co přinesly jednotlivé verze programu.
- Když se s programem chvíli nepracuje, předkreslí offline mapy pro oddálená
  měřítka a okolí zobrazeného místa do cache; při práci s programem počká.
- V Soubor > Zobrazení programu jde zvětšit ikony keší na mapě (1× až 5×)
  a písmo, symboly a čáry offline mapy až na 200 %.

### Opravy
- Zvýrazňovací kruhy (F8) a Kruhy (Alt+F8) zůstanou zapnuté i po přepnutí
  mapového podkladu.
- Změna velikosti nebo barvy v Nastavení kruhů kruhy nevypne.
- Obsazenost 161 m se kreslí jako jedna plocha jedné barvy, takže překryvy
  netmavnou; kreslí se při dostatečném přiblížení a nejvýš pro 20 000 keší
  ve výřezu.
- Soubor GPX s kešemi s vlastníkem zapsaným jako GUID (například Lab keše) se
  načte bez zbytečných varování v logu.
- Soubor s kešemi, který se právě kopíruje nebo ho drží jiný program, se načte,
  až je volný.
- Nastavení se při ukončení nebo restartu uloží spolehlivě.
- V detailu keše jsou datum nálezu a kód keše dobře čitelné, ikona vlastní keše
  má výraznější obrys.
- Přepsaný soubor s kešemi (GPX, zip, .geokuk) ve složce dat se načte znovu,
  i když v ní nepřibyl ani neubyl žádný soubor.
- Domácí souřadnice z GeoGetu a seznamy výletů se načtou i ze souboru v UTF-16;
  export GeoKuku (.geokuk) ve starším kódování Windows se načte s diakritikou.
- Zip s kešemi, který má jména souborů ve starším kódování Windows, se načte.
- Obrázek ikony v neznámém formátu ve vlastní sadě ikon program ohlásí
  a nespadne.
- Vlastní sada ikon s chybným číslem se načte s výchozí hodnotou.
- Jít > Adresa… vynechá výsledek bez platné polohy.
- Po změně nicku se keše z databáze, kterou zrovna drží GeoGet nebo GSAK,
  načtou znovu, až ji program pustí.
- Při ukončení programu na pomalém disku program počká, až se stažené dlaždice
  zapíšou do cache (nejvýš minutu).
- Úklid cache dlaždic pokračuje, i když některý soubor nejde smazat.
- Offline mapa zkopírovaná do složky za běhu se otevře až po dokopírování
  a mapa v okně se sama obnoví. Mapu, kterou chvíli nešlo číst, program za půl
  minuty zkusí znovu.
- Menu Téma offline mapy se otevře hned, i když jsou témata na pomalém disku.
- Po načtení keší program nečeká na vykreslení ikon na liště nástrojů.

Obsahuje i všechny opravy z 6.4.3.

## 6.4.3

### Opravy
- Domácí souřadnice z GeoGetu (geohome.ini) se načtou i ze souboru v UTF-8
  se značkou BOM; GeoKuk ho už nehlásí jako poškozený.

## 6.4.2

### Opravy
- Mapy > Postahovat dlaždice: stahování velkého výřezu (tisíce dlaždic)
  už nedojde paměť, stažené dlaždice se průběžně uvolňují.

## 6.4.1

### Opravy
- Mapy > Postahovat dlaždice: stahování víc než asi 110 dlaždic se už
  nezastaví, stáhnou se všechny.
- Geodetický bod (ČGP), jehož popis má souřadnice JTSK v nečekaném tvaru,
  už nezpůsobí chybu při načítání; načte se bez souřadnic JTSK.
- Téma offline mapy ze složky programu zůstane zvolené i po přesunu přenosného
  GeoKuku jinam.

## 6.4.0

### Změny
- Rychlejší načítání databází GeoGetu, GSAKu a OpenSAKu.
- Rychlejší zobrazení keší po načtení a po změně filtru.
- Rychlejší zapnutí a vypnutí zdroje, který se s ostatními nepřekrývá.
- Keše z databází GeoGetu, GSAKu a OpenSAKu zabírají méně paměti.
- Prostorový index načtených keší zabírá méně paměti.
- Odkazy keší a názvy jejich přídavných waypointů zabírají méně paměti.
- Měřítková lišta má dílky po 1, 2 a 5 a délku zhruba čtvrtiny okna, při
  posunu mapy už neskáče.
- Nový podklad Mapy > Offline mapa (klávesa V): vektorové mapy `.map`
  (mapsforge, například z osm.paws.cz) ze složky `data/offline-mapy` se
  vykreslují přímo v počítači, bez internetu. Více map ve složce (třeba ČR
  a SK) se kreslí naráz, mapu zkopírovanou do složky za běhu program sám
  načte; dokud se kopíruje, kreslí se ostatní mapy. Vykreslené dlaždice se
  ukládají do cache dlaždic; po změně mapy, tématu, ostrosti nebo písma v ní
  zůstávají jen tři poslední kombinace. Klávesu V teď používá Offline mapa;
  uživatelskou mapu se zkratkou V přenastavte.
- Zip s programem obsahuje v `data/offline-mapy` přehledovou mapu světa
  z dat Natural Earth (pevnina, moře, státní hranice, české názvy států),
  takže offline mapa ukáže celý svět i bez stažených map. Přenosný GeoKuk
  ji po aktualizaci doplní nebo obnoví na novou verzi; smazanou znovu
  nepřidá. V rohu mapy je u ní atribuce „Made with Natural Earth“.
- Vzhled offline mapy se volí v Mapy > Téma offline mapy: vestavěná témata,
  nebo téma stažené spolu s mapou (soubor .zip nebo .xml ve složce offline
  map). Atribuce v mapě uvádí i použité téma. Složku offline map lze změnit
  v Soubor > Umístění souborů na kartě Mapy. Složku `data/offline-mapy`
  program při startu založí.
- Symboly v tématech offline mapy nenačítají nic z internetu ani z místní
  sítě; symbol, který odkazuje mimo téma, se nevykreslí.
- Soubor > Zobrazení programu, rámeček Mapa: offline mapa Ostrá, nebo
  Rychlejší (dlaždice v menším rozlišení zvětší systém), a velikost písma
  a ikon na mapě 80–150 %. Změna se projeví hned.
- Dialogy Paměť programu, Zobrazení programu a Limity kreslení jsou
  stručnější, vysvětlení je v bublinách a tlačítko Nápověda otevře stránku
  wiki. Po změně paměti GeoKuk nabídne restart.
- Offline mapa s popisky ulic a cest se na podrobných zoomech vykresluje
  asi o čtvrtinu rychleji.
- Posun a změna měřítka offline mapy nečekají, až se na pozadí otevře mapa
  nebo načte téma.
- Offline mapa se vykresluje ve více vláknech (až 6 naráz podle počtu jader),
  mapu i téma otevírá předem na pozadí a vykreslené symboly tématu si ukládá,
  takže další spuštění otevře mapu i s velkým tématem během zlomku sekundy.
- Vykreslená dlaždice offline mapy se zobrazí hned; do cache se převádí
  a ukládá až na pozadí.
- Offline mapa po zobrazení celé obrazovky na pozadí předvykreslí dlaždice
  kolem okna, takže posun mapy o kousek je okamžitý. Posun nebo změna
  měřítka předvykreslování přeruší, aby nezdrželo viditelné dlaždice.
  Na počítači s méně než pěti jádry se nepoužívá.
- Na displeji se zvětšením (třeba 150 %) kreslí offline mapa dlaždice
  s odpovídajícím počtem pixelů, mapa je ostrá a písmo i značky mají
  stejnou velikost jako ostatní texty programu.
- Dlaždice všech map se načítají od středu okna.
- Po odstranění kříže z kontextového menu jde mapu dál přibližovat
  a oddalovat kolečkem myši a kliknutím do mapy postavit nový kříž.
- Informace pro hlášení chyby obsahují výkon kreslení: dobu překreslení
  mapy, nejdelší události programu a dobu získání dlaždic. Dálkové ovládání
  vrací stejná čísla příkazem `/vykon`. V beta kanálu lze v Nápovědě zapnout
  ukazatel výkonu v rohu mapy.
- Poslední události v informacích pro hlášení chyby neuvádějí kontextová
  menu jako otevřená okna a u okna Informace pro hlášení chyby jen jeho název.
- U zaseknutí programu delšího než sekundu uvádějí informace pro hlášení
  chyby i místo, kde program stál; celý výpis je v logu.
- Informace pro hlášení chyby uvádějí zvětšení, Direct3D a vzhled offline
  mapy, se kterými program běží, a v posledních událostech každé zaseknutí
  programu delší než sekundu. Výkon kreslení se měří až chvíli po zobrazení
  mapy, ať ho nezkreslí start, a tlačítkem Vynulovat měření jde začít znovu.
- Soubor `.map` pro OziExplorer se zapisuje v kódování Windows, takže
  OziExplorer přečte i jméno obrázku s diakritikou.
- Při přiblížení a oddálení offline mapy se místo prázdných dlaždic hned
  ukáže zvětšený nebo zmenšený výřez už nakreslených dlaždic a po
  dokreslení se nahradí ostrou.
- Opětovná volba už vybrané sady ikon ikony znovu nenačítá. Sada ikon se
  načte i tehdy, když některou její podsložku nejde přečíst.
- Soubor `.geokuk` s waypointem před první keší se načte bez hlášení chyb,
  takový waypoint se přeskočí.

### Vývoj
- Knihovna mapsforge 0.25.0 pro vykreslování offline map; její licence
  (GNU LGPL 3) je ve složce `licence` a v okně O programu.
- Měření vykreslování offline mapy (`vykon/mer.sh`, část `offline`).
- Diagnostika: nečitelná offline mapa se počítá jen jako chyba vykreslení,
  ne jako chyba čtení cache dlaždic.

## 6.3.0

### Změny
- Stavový řádek vpravo: přepínače zdrojů GPX, GeoGet, GSAK a OpenSAK se
  zaškrtávátkem a ikonou stavu (načteno, načítá se, zamčeno jiným
  programem, chyba). Zaškrtávátko má tři stavy; částečně zapnutý je zdroj,
  u kterého jsou některé položky vypnuté. Najetí na „Zdroje:“ ukáže tabulku
  všech souborů a databází s velikostí, počtem waypointů a stavem, najetí
  na název zdroje jen jeho položky. Stejnou tabulku ukazuje okno Přehled
  zdrojů. Klik na blok Zdroje už okno Přehled zdrojů neotevře. Po návratu na
  verzi 6.2.x se znovu zapnou typy zdrojů vypnuté ve verzi 6.3.0.
- Klik na soubor nebo databázi vypnutého typu v tabulce zdrojů zapne
  typ jen s touto položkou.
- Ctrl+klik (na macOS Cmd+klik) na položku v tabulce zdrojů načítá jen
  tuto položku jejího typu, na zaškrtávátko typu jen tento typ. Řádky
  tabulky mají kontextové menu s volbami načítání.
- Prázdná, neexistující nebo nečitelná datová složka GeoGetu, GSAKu nebo
  OpenSAKu se ve stavovém řádku u zdroje ukáže ikonou chyby s důvodem
  v bublině; stejný důvod je v tabulce zdrojů a v Přehledu zdrojů.
- Průběh načítání ve stavovém řádku ukazuje typ zdroje a cestu v jeho datové
  složce (např. „GSAK\Default\sqlite.db3“), plná cesta je v bublině.
- Soubor s cestami a počet waypointů a cest se ve stavovém řádku ukazují,
  jen když jsou nějaké cesty. Výlet otevřený při startu stavový řádek
  neposune.
- Zdroj jde zapnout či vypnout i během načítání keší a Přehled zdrojů jde
  otevřít také během něj. Přepnutí načte znovu jen změněné zdroje a ty, které
  se s nimi překrývají (stejné keše ve více zdrojích); ostatní zůstanou
  načtené. Přehled zdrojů se otevírá rychleji a po novém načtení ukazuje
  aktuální stav.
- Keš, která je ve více zdrojích, se zobrazí ze zdroje s nejnovějšími daty.
- Zapnutí databáze, která byla od spuštění vypnutá a překrývá se s jinými zdroji,
  ji přečte jen jednou.
- Keše z databází OpenSAKu: v Umístění souborů na záložce OpenSAK
  zapněte datovou složku OpenSAKu, načte se každá databáze `.db` v ní.
- Kešoidy > Limity kreslení…: posuvníky pro nejvyšší počet ikon
  (výchozí 90 000) a teček (výchozí 300 000) ve výřezu. Limit ikon platí
  i pro popisky a zvýrazňovací kruhy; nad limitem teček se nic nekreslí
  a stavový řádek to ohlásí. Nejnižší nastavitelný limit teček je 60 000.
- Rychlejší načítání keší a menší spotřeba paměti u velkých dat, i při
  přenačtení keší. Kreslení teček při velkém počtu keší potřebuje výrazně
  méně paměti, posun mapy je plynulejší.
- Rychlejší načítání map z cache na počítačích se 4 a více jádry.
- Počítače s 16 GB paměti a více dostanou automaticky pro program 4 GB.
  Když paměť na načtení keší nestačí, GeoKuk to oznámí a už zobrazená
  data zůstanou.
- Paměť uvolněná po vypnutí velkého zdroje keší vrátí program systému během
  několika sekund.
- Aktualizace: už stažená nová verze se nestahuje znovu a při chybě
  výměny se vrátí původní verze. Nová verze s neplatným číslem verze nebo
  bez údaje o nejnižší Javě se nenabídne ani neinstaluje.
- Windows: okno GeoKuku se spojí s ikonou připnutou na hlavním panelu
  (ze zástupce GeoKuku), místo druhé ikony. Při prvním spuštění GeoKuk
  nabídne zástupce v nabídce Start, aby šel připnout na hlavní panel.
- Když do datové složky nejde zapisovat, log a soubor dálkového ovládání
  jsou ve složce `.geokuk` v domovské složce. Hláška o nezapisovatelné cizí
  složce říká, že patří jinému uživateli.
- Po pádu programu se mapa zobrazuje hned, kontrola cache dlaždic běží
  na pozadí. Prázdný soubor nastavení (po výpadku proudu) se při startu
  bere jako chybějící, bez hlášení; zbylé dočasné soubory
  `nastaveni.xml.*.tmp` se uklidí.
- Poškozená cache dlaždic se odloží stranou (uchovají se dvě poslední
  kopie); když se odložit nepodaří, původní cache se nesmaže.
- Uživatelské mapy: soubor uložený jinak než v UTF-8 se ohlásí s radou,
  soubory `._*` z macOS se přeskočí a hláška při startu vyjmenuje jen
  mapy, které se nezobrazí. Ukázka `osm.mapa` je bez vlastní hlavičky
  User-Agent.
- Na oddálené mapě jsou další waypointy keše (parkoviště, stage) šedé.
- Stavový řádek se na široké obrazovce vejde na jeden řádek, v užším okně
  se zalomí do více řádků; výlet zůstává vpravo dole.
- Nápověda (F1 a tlačítka Nápověda v dialozích) otevírá stránky
  uživatelské wiki na GitHubu.
- Mapy: nové podklady ČR ortofoto a ČR Základní topografická mapa
  z otevřených dat ČÚZK (© ČÚZK, CC BY 4.0). Mimo území České republiky
  zůstane mapa prázdná.
- Mapy: nový podklad SR ortofoto ze ZBGIS (© GKÚ Bratislava, NLC,
  CC BY 4.0). Mimo území Slovenska zůstane mapa prázdná.
- Licence GeoKuku, použitých knihoven a mapových podkladů jsou ve složce
  `licence` (v zipu i v repu). Nápověda > O programu ukazuje i licence
  mapových podkladů.
- Mapy.cz, OpenStreetMap a Waymarked Trails už nejde stahovat hromadně
  do cache (Mapy > Postahovat dlaždice), ani jako uživatelskou mapu
  s hromadne=ano; zobrazené dlaždice se do cache ukládají dál.
- Položky v menu Soubor, Jít, Kešoidy, Cesty a Nápověda jsou seskupené
  podle účelu a oddělené čarami; zkratky i umístění v menu zůstávají.
- Soubor > Zobrazení programu: zvětšení podle Windows a hardwarové
  vykreslování (Direct3D) jdou vypnout, když je program pomalý nebo seká.
  Platí od příštího spuštění, GeoKuk nabídne restart.
- Ikona zdroje, který se právě načítá, se ve stavovém řádku i v tabulce
  zdrojů točí.

### Opravy
- Cesta bez bodů (třeba z GPX s prázdným `trkseg`) už nezpůsobí chybové hlášení
  při každé změně cest; „Promazat jednobodové cesty“ ji smaže.
- Hláška „GeoKuk už běží“ při druhém spuštění je vždy vidět nad ostatními okny.
- macOS: změna vzhledu (Skin) z výchozího vzhledu systému už nezpůsobí chybové
  hlášení při přepnutí do jiného okna.
- Databáze zamčená jiným programem (třeba otevřeným GeoGetem) nezdrží
  ostatní zdroje: ty se načtou hned a zamčená, jakmile ji program pustí.
  Zapnutí či vypnutí zdroje se projeví i tehdy, když je jiná databáze
  zamčená. Změna Umístění souborů zruší rozběhnuté načítání a začne znovu.
- Databáze GeoGetu nebo GSAKu, které chybí sloupec nutný pro načtení
  keší, se nenačte a program řekne, který sloupec chybí; ostatní zdroje
  se načtou.
- Když z databáze GeoGetu, GSAKu nebo OpenSAKu nejde přečíst většina
  keší, GeoKuk na to upozorní.
- Změna vzhledu (Skin) se projeví i v otevřených dialozích.
- Nečitelné nastavení, které drží jiný program, už nesmaže dříve
  odložený soubor `nastaveni.xml.vadne`. Hláška řekne, že se změny při
  tomto spuštění neuloží.
- Chyba při vytváření zástupce se zobrazí se správnou diakritikou.
- Program spuštěný ze složky, kam nejde zapisovat, se ukončí bez
  dalšího dotazu.
- Final tradiční keše na stejném místě jako keš se už neukazuje na mapě
  jako samostatný bod.
- Databáze GSAKu ve složce zadané přes symbolický odkaz se po vybrání
  znovu nezablokuje.
- GSAK, „Načítat až po vybrání“: složka, která byla chvíli nedostupná
  a vrátila se během načítání, už nezablokuje vybrané databáze.
- Neodpovídající síťový disk s datovou složkou už nezdržuje ovládání
  programu po každém načtení keší.
- Neodpovídající datová složka GeoGetu nezdrží start programu; referenční
  body z GeoGetu se v menu Jít objeví, až se přečtou.
- Prázdný soubor `.geokuk` ve složce s kešemi nezpůsobí chybové
  hlášení.
- Umístění souborů: neexistující nebo nečitelná datová složka GeoGetu,
  GSAKu nebo OpenSAKu se při uložení ohlásí a nezaloží. Aktivní složka bez
  databází už nevyvolá chybové hlášení.
- Umístění souborů: složka na neodpovídajícím síťovém disku nezablokuje
  uložení; po třech sekundách se ohlásí, že neodpovídá.
- Uživatelská mapa, která je odkazem na jiný soubor nebo je větší než
  64 kB, se nenačte; hláška o neznámé vlastnosti ukáže jen její začátek.
  Hodnota hlavičky s řídicím znakem (třeba `\r`) se odmítne s chybou při
  startu.
- Ukázky uživatelských map ve složce `data/mapy-priklady` se obnoví i při
  aktualizaci z programu.
- Když se nová verze po aktualizaci nespustí, spustí se předchozí verze
  a GeoKuk na to upozorní.
- Když se GeoKuk spustí ve chvíli, kdy se předchozí spuštění právě
  ukončuje, nemohou omylem běžet dva GeoKuky nad stejnými daty.
- Panel nástrojů má stálou výšku, mapa se po načtení keší neposune.
  Vyšší ikony (například symboly Waymarků) jsou na panelu zmenšené.
- Dlaždice mapy se při rychlém posouvání nestahují dvakrát.
- Kalibrační body v souboru .map pro OziExplorer leží přesně v rozměrech
  obrázku a dlaždice KMZ pokryjí celý obrázek až k okraji.
- Když do stejné cache map zapisuje jiný spuštěný GeoKuk, dlaždice se
  stáhnou bez hlášení chyby čtení.
- Nastavení popisků: ukázka písma je v barvě písma a podkladu, náhled
  barev s černými čtverci zmizel.
- Hromadné stahování dlaždic při vypnutém „Mapy > Ukládat mapy“ vyzve
  k jeho zapnutí.
- Postahovat dlaždice po přiblížení mapy nestahuje měřítka nad
  nabízeným rozmezím.
- Odkaz, který nevede na webovou stránku, ukáže okno s adresou.
- Když programu dojde paměť při načítání nebo jiné práci na pozadí,
  GeoKuk poradí, jak paměť zvýšit, a ukončí se.
- Hláška o chybě při práci na pozadí neukazuje jméno třídy Javy.
- Hlášení chyby správně zobrazí text se znaky `<`, `>` a `&` (například
  ze jména souboru).
- Export cest do GGT u cest bez keší ohlásí, že cesty žádnou keš
  neobsahují, a prázdný soubor nevytvoří.

### Vývoj
- Odstraněna knihovna SwingX.
- Sestavení hlídá nová varování překladače, nezachycené výjimky v testech
  a nové nálezy SpotBugs; souhrn běhu ukazuje pokrytí testy.
- Zkouška zipu pro Windows kontroluje i jeho obsah a to, že po startu
  vznikne složka `data/mapy`.
- Ruční běh Smoke spouští jednotkové testy i na macOS.
- Týdenní kontrola schématu databáze OpenSAKu.
- Workflow Velká data: zkouška celého programu nad velkým GPX a velkými
  databázemi GeoGetu a GSAKu, spouští se ručně.

## 6.2.1

### Opravy
- Nad 200 000 waypointů na mapě se tečky nekreslí a zobrazí se hlášení
  o překročeném limitu.

## 6.2.0

### Změny
- Přenosný GeoKuk: nic se neinstaluje, program si všechno ukládá do
  složky `data` vedle sebe: nastavení (`nastaveni.xml`), uživatelské mapy
  (`mapy`), cache map (`cache`), cesty, ikony
  (`ikony/moje`, `ikony/ostatni`), výlety (`vylety`), log a chybová
  hlášení (`log`) i dočasné soubory Javy (`tmp`). Rozhoduje jen to,
  odkud se spustí, složku jde přesunout i s daty. Nastavení už není
  v registru Windows, při prvním spuštění se odtud převezme. Volba
  „Ukládat nastavení k programu“ odpadla.
- Data starší verze ve složce `%USERPROFILE%\geokuk` (keše z GPX,
  výlety, cesty, ikony, dlaždice map) GeoKuk nečte a umístění složek
  z jejího nastavení nepřevezme: keše čte z `data/gpx`, rendruje do
  `data/render` a GeoGet a GSAK má vypnuté. Data jdou zkopírovat do
  složky `data`, postup je v README v oddílu Přechod ze starší verze.
- Umístění souborů: relativní cesta, třeba `data/gpx`, se počítá od
  složky GeoKuk; volba „Relativně k umístění programu“ odpadla. Cesta
  uvnitř složky GeoKuk se ukazuje jako `${GeoKuk}/data/gpx` a při
  přesunu složky se posune s ní, pod ní je výsledná cesta.
- Ve Windows se verze 6.0.0 aktualizuje sama, ale bez přibalené Javy
  a další verze už sama nenainstaluje. Pro automatické aktualizace
  stáhněte `GeoKuk-windows.zip` a data přeneste podle README.
- Pro Windows je ke stažení `GeoKuk-windows.zip` s přibalenou Javou,
  program a Java jsou v podsložce `program`. Poprvé se spustí
  `GeoKuk-prvni-spusteni.cmd`, GeoKuk pak vedle vytvoří zástupce
  `GeoKuk.lnk`, který jde zkopírovat na plochu nebo připnout. Po
  přesunu složky se zástupce ve složce i jeho kopie na ploše
  a v nabídce Start opraví. Soubor > Vytvořit zástupce přidá
  GeoKuk do nabídky Start nebo na plochu. Spouštěč `geokuk.cmd` odpadl.
- Spouštěč `start.jar` nainstaluje staženou novou verzi a zvolí paměť
  (polovina paměti počítače, 1 až 3 GB), změnit ji jde v Soubor >
  Paměť programu. Po stažení nové verze GeoKuk nabídne restart: uloží
  se jako při Soubor > Konec a spustí se znovu už v nové verzi, i ve
  složce, do které nejde zapisovat. Když spouštěč nemůže staženou verzi
  nainstalovat (soubor drží jiný program), spustí dosavadní verzi a novou
  zkusí nainstalovat příště.
- GeoKuk upozorní, když do své složky nemůže zapisovat (třeba v Program
  Files), nebo když je ve složce synchronizované přes OneDrive, Dropbox
  nebo Google Disk. Ve složce bez práva zápisu se keše z GeoGetu
  a GSAKu načtou, mapa se zobrazí (dlaždice zůstanou jen v paměti),
  log a chybová hlášení jsou v `%TEMP%\GeoKuk\log` a dočasné soubory
  v dočasné složce systému.
- Když nová verze potřebuje novější Javu, než je přibalená, nebo je
  k dispozici zip s novější Javou, GeoKuk nabídne stažení nového zipu.
  Když nejde zjistit, jakou Javu nová verze potřebuje, neinstaluje se.
- Nápověda > Nabízet testovací verze (beta) zapíná beta kanál. Dokud
  je zapnutý, je verze vidět vpravo v menu. Po vypnutí nabídne ruční kontrola
  aktualizací přechod na poslední stabilní verzi.
- „Připomenout za týden“ (i zavření dialogu nové verze) odloží nabídku
  té verze o 7 dní. Novější verze se nabídne hned.
- „Aktualizovat“ na Linuxu a macOS novou verzi rovnou
  nainstaluje místo jaru, ze kterého GeoKuk běží; kde to nejde, otevře
  stránku nabízené verze, u bety tedy betu.
- Na oddálené mapě (zoom 12 a menší) jsou keše barevné tečky podle typu:
  tradiční zelené, multi žluté, mystery, letterbox a wherigo tmavě
  modré, virtuální, webcam a earthcache bílé, eventy červené, Lab keše
  světle tyrkysové, ostatní typy keší modré a objekty, které nejsou
  keše, šedé. Nalezené jsou menší, vlastní mají tmavý obrys, neaktivní
  a archivované jsou světlejší. Velikost teček se řídí hustotou keší.
  Tečky se vykreslí i u statisíců keší, limit 30 000 waypointů na mapě pro ně
  neplatí. Kešoidy > Tečky při oddálení / Vždy ikony / Vždy tečky.
- Zvýrazňovací kruhy a popisky se nekreslí, když by se na mapě slily
  do jedné plochy; kruhy obsazenosti, když mají poloměr pod 3 pixely.
  Popisek keše pod myší se při posunu mapy schová a na zoomu 7 a menším
  se nezobrazuje.
- Mapy > Zobrazit všechny keše nastaví mapu tak, aby byly vidět všechny
  zobrazené keše. Po prvním načtení dat se to stane samo, když ve výřezu
  žádná keš není.
- Uživatelské mapy: vlastní mapové podklady ve složce `data/mapy`, kterou
  GeoKuk založí při startu, každá mapa v samostatném souboru
  `<označení>.mapa`, v menu Mapy ve skupině „Uživatelské mapy“. Dvě
  uživatelské mapy se stejným názvem v menu nebo stejnou zkratkou se
  nezobrazí a GeoKuk je ohlásí při startu, stejně jako mapu s neznámou
  proměnnou v adrese (třeba `{s}`). Název vestavěné mapy uživatelská mapa
  mít smí, zkratku vestavěné mapy nebo akce programu (třeba F3 nebo
  Ctrl+S) ne. Hlavičky mapy (`hlavicka.*`) se při přesměrování na jiný
  server neposílají. Ukázky jsou
  v `priklady/mapy`, zip pro Windows je obsahuje ve složce
  `data/mapy-priklady`.
- Mapy mají atribuci (Mapy.cz, OpenStreetMap, Freemap Slovakia) vpravo
  dole v mapě.
- Hledání adresy (Jít > Adresa) a návrh názvu souboru u rendru podle
  místa používají Nominatim nad daty OpenStreetMap. Hledá se klávesou
  Enter nebo tlačítkem Hledat, souřadnice v poli najdou adresu místa.
  Položka „Geocoding“ v kontextovém menu mapy je nahrazena položkou
  „Na OpenStreetMap...“, která místo otevře v prohlížeči.
- Hlášení chyb: „Zadat problém“ i „Informace pro hlášení chyby“ nejdřív
  ukážou informace o programu, tlačítko „Nahlásit na GitHubu“ pak otevře
  nové issue, kde jsou předvyplněné. „Informace pro hlášení chyby“ obsahují cestu
  k logu a jeho posledních 50 řádků, servisní hodnoty ze servisního okna
  a posledních 100 událostí: spuštěné položky menu s cestou v menu,
  klávesovou zkratkou a stavem přepínače, otevřená a zavřená okna
  s textem hlášek, souhrnně práci s mapou (podklad, měřítko, počet
  posunů, bez polohy) a načítání keší.
- Dálkové ovládání: Soubor > Dálkové ovládání (nebo parametr
  `--ovladani[=port]`, výchozí port 48321) povolí jiným programům na
  tomto počítači ovládat GeoKuk přes HTTP – přesunout mapu, přepnout
  podklad, vybrat keš, znovu načíst keše a zjistit stav. Požadavky
  z webových stránek program odmítá. Když ovládání spustit nejde, třeba
  kvůli obsazenému portu, hlášení řekne proč.
- V Umístění souborů zůstaly k nastavení jen složky s daty jiných
  programů (keše z GPX, GeoGet, GSAK, výstupy rendru); ostatní ukazuje
  záložka Program.
- O programu ukáže licenci a seznam použitých knihoven s jejich
  licencemi. Program i zip pro Windows obsahují soubory `LICENSE`
  a `THIRD-PARTY.txt`.
- Písmo popisků keší se vybírá v novém panelu: rodina písma, řez
  (obyčejné, tučné, kurzíva, tučná kurzíva), velikost a náhled.

### Opravy
- Nová verze se stahuje jen jednou najednou a po stažení se ověří
  soubor na disku, takže se nenainstaluje neúplný program.
- Tlačítka dialogů (Ano, Ne, Zrušit) a dialog pro výběr souboru jsou
  česky. Položka menu Kešoidy > Filtr... má správný název.
- Opravené bublinové nápovědy, které popisovaly jinou funkci (mřížky,
  Uzavřít cestu, Ladění ikon, Lovím, Přidat do cesty, Zobrazení na webu
  a další), překlepy v oknech Postahovat dlaždice a Tisknout/Rendrovat
  a v detailu keše a nápovědy k filtrům BestOf a Favorit. Program se v textech
  jmenuje jednotně GeoKuk, u souborů s cestami se mluví o cestách, ne
  o výletu, a okna Umístění souborů a Tisknout/Rendrovat se jmenují
  stejně jako položky menu.
- Přehled problémů ukazuje u každého problému, co se dělo a proč to
  selhalo; sloupce se jmenují Č. a Hlášení. Stavový řádek bez načtených
  zdrojů neukazuje čas a bez vybrané pozice ukazuje pomlčku. Vzdálenost
  a směr od pozice ukazuje, až když je myš nad mapou.
- Hlášení v Přehledu problémů jsou česky a srozumitelně, opravené
  překlepy a oslovení v nápovědách tlačítek.
- Přehled problémů jde po zavření znovu otevřít v Nápověda > Přehled
  problémů.
- Hledání adresy v režimu offline hned ohlásí, že potřebuje připojení
  (Mapy > Online), a nezůstane na „Hledá se ...“.
- Po nedostatku paměti při rendrování přijde jen hláška o paměti, bez
  „Rendrování bylo přerušeno uživatelem“.
- Kontextové menu záměrného kříže nabízí Vystředit na kříž.
- „Načítat až po vybrání“ u GSAKu: databáze, kterou GeoKuk ještě
  neviděl (nová v GSAKu, po zapnutí GSAKu nebo změně jeho složky), se
  načte až po vybrání v Přehledu zdrojů.
- Dialog nové verze má titulek „Nová verze programu“ a čitelný text
  a při souběhu automatické a ruční kontroly aktualizací se ukáže jen
  jednou.
- Načtení keší z databáze GeoGetu nebo GSAKu nečte popisy keší, takže
  nemusí projít celou databázi včetně dlouhých listingů. Hint se načte
  až po kliknutí na Hint v detailu keše. Hodnocení, známka,
  BestOf, favority, nadmořská výška a vlastní hodnoty z databáze GeoGetu
  a GSAK se přenesou ke keším.
- Databáze GeoGetu nebo GSAKu, do které ten program právě zapisuje
  (třeba import), nezdrží při startu načtení ostatních zdrojů a načte se
  po dokončení zápisu. Do té doby zůstanou zobrazené dříve načtené keše,
  Přehled zdrojů u ní ukazuje „čeká na dokončení zápisu“ a další změny
  zdrojů (nový soubor, Znovu načíst, zapnutí zdroje) se projeví až po
  dokončení zápisu. Poškozenou databázi nebo soubor, který není databáze,
  program ohlásí česky.
- Databáze GeoGetu a GSAKu se otevírají jen pro čtení, GeoKuk v jejich
  složce nic nezaloží ani nezmění.
- Keše se načtou i z databáze starší verze GeoGetu nebo GSAKu, které
  chybí některé sloupce nebo tabulky, i když jsou poškozené jen tagy
  nebo popisy, a i keš GeoGetu bez autora. Vadný waypoint nebo záznam
  se přeskočí, ostatní keše se načtou.
- Databáze GSAKu s nestandardním řazením nezastaví načítání ostatních
  dat dialogem, ohlásí se v přehledu problémů. Okno nečeká, než doběhne
  procházení velké datové složky.
- GSAK: prázdné vlastní hodnoty se nezobrazují jako tagy.
- GSAK: kód keše z vlastních hodnot se nebere jako tag. Databáze GSAKu
  s vlastními hodnotami tak zabírá výrazně méně paměti.
- Aktivní datová složka GeoGetu nebo GSAKu bez databází se ohlásí.
  Uložení v Umístění souborů nezakládá složky u neaktivních položek.
- Druhé spuštění GeoKuku nad stejnou složkou `data` jen oznámí, že
  GeoKuk už běží, a skončí; dvě instance by si přepisovaly nastavení
  a výlety.
- Nastavení s neobvyklými znaky (třeba řídicími znaky ze schránky) se
  uloží a znovu načte; dřív se přestalo ukládat nebo se při dalším
  spuštění celé zahodilo. Nečitelný soubor nastavení, který nejde odložit
  stranou, program nepřepíše.
- Poškozená fotka (EXIF), na které se čtení zacyklí nebo mu dojde
  paměť, už neukončí načítání ostatních souborů s kešemi.
- Databáze GeoGetu nebo GSAKu s nedokončeným zápisem (program spadl
  při importu) se ohlásí s radou otevřít ji v GeoGetu nebo GSAKu.
- Výlety a cesty se ukládají v UTF-8; soubory ze starší verze
  v kódování Windows se načtou správně, i s diakritikou v názvech
  fotek a waypointů.
- GPX: keše ve jmenném prostoru Groundspeak `cache/1/0/2` (GPX 1.1) se
  načtou jako keše. Keš s nesmyslnými souřadnicemi (NaN), výškou
  s čárkou, bez názvu nebo s nesmyslnou obtížností či terénem nezahodí
  zbytek souboru. Keš bez nápovědy nepřebírá typ a název z logů
  a travel bugů.
- GPX soubor nemůže při načtení číst jiné soubory ani volat adresy
  na internetu (externí entity a DTD se nenačítají).
- Načtené soubory GPX, `.geokuk` a fotky nezůstávají ve Windows zamčené.
- Chybný soubor v datové složce už nezpůsobí opakované načítání dat
  a chybová hlášení dokola; nečitelná nebo zacyklená složka se přeskočí.
  Složka cest a složky ikon uvnitř datové složky se neprocházejí jako
  zdroj keší.
- Chybějící nebo neúplná vybraná sada ikon (smazaná složka, chybí
  `skla.txt`) se nahradí sadou Standard;
  dřív se kvůli ní nenačetly žádné keše.
- Pruh „Indexování“ při načítání keší ukazuje průběh.
- Zavřené hledání keší už nedrží v paměti keše z předchozího načtení.
- Hledání keší s regulárním výrazem: `\D`, `\W`, `\S` a `\Q…\E` fungují
  podle očekávání.
- Otevření filtru (F2) nad daty, ve kterých nejsou nalezené, archivované
  nebo jiné skryté keše, z filtru nevyhodí jejich skrytí. Keše bez
  hodnocení, BestOf nebo favoritů filtr podle těchto prahů neskryje.
- Kešoidy > Implicitní výběr (Alt+F2) vrátí filtr i zobrazené typy keší
  na výchozí hodnoty. Kešoidy > Jednotkové kruhy (Alt+F8) jsou v menu.
- Stahování mapových dlaždic má časový limit (celkem 60 s na dlaždici),
  takže pomalý nebo neodpovídající server nezastaví mapu. Neúplně
  stažená nebo useknutá dlaždice se nepoužije, nevezme z cache a stáhne
  se znovu; dlaždici, kterou server pošle celou, program nezahodí.
- Dlaždice, kterou se nepodařilo stáhnout (výpadek sítě), se zkusí
  stáhnout znovu s rostoucím odstupem (nejdéle po 5 minutách), ne při
  každém překreslení mapy,
  a místo technického výpisu ukazuje srozumitelný důvod. Bez připojení
  k internetu se chyby stahování hlásí do logu souhrnně.
- Hromadné stahování dlaždic ukazuje průběh, počet chyb a konec, jde
  zastavit a samo přestane, když server mapy stahování omezí
  (HTTP 429 nebo 503).
- Stažené dlaždice se uloží do diskové cache i při rychlém posouvání
  a zoomování a během souvislého čtení z cache, dlaždice z posledních
  sekund i při ukončení programu.
  Zdravá cache se neodkládá jako poškozená a opakovaná chyba čtení
  z cache se do logu hlásí souhrnně.
- Při rychlém posouvání mapy se nezobrazí cizí dlaždice a souběžný
  zápis cache map nepoškodí.
- Ve filtru se stav aktivních keší jmenuje „Aktivní“.
- Oddíl Výstup v okně Tisknout/Rendrovat se vejde i s dlouhou výstupní složkou:
  složka se zkrátí uprostřed, celá je v bublině nápovědy.
- Zrušené nebo neúspěšné rendrování nesmaže obrázek a kalibraci `.map`
  z dřívějšího rendrování; rendr obrázku kalibrace pro Ozi nemaže vůbec.
- Poškozená cache mapových dlaždic se odloží stranou (jen jednou a ta
  poškozená) a založí znovu. Když cache nejde použít (třeba odpojený
  disk), program na to jednou upozorní.
- Cache mapových dlaždic poškozená plným diskem nebo pádem programu
  uprostřed zápisu se odloží a založí znovu, i když se poškození
  ukáže až při čtení nebo zápisu dlaždic. Po pádu programu se cache při
  příštím startu zkontroluje. Když na disku zbývá méně než 100 MB,
  dlaždice se do cache neukládají a program na to jednou upozorní.
- Mapové dlaždice v paměti mají strop, při dlouhé práci s mapou paměť
  neroste.
- Dlaždice se stáhnou i ze serveru, který adresu `http://` přesměruje
  na `https://`.
- Stahování mapových dlaždic se všem serverům představuje jako GeoKuk
  s verzí, bez adresy webu. OpenStreetMap se načítá přes https a nejde
  ji hromadně stahovat do cache.
- Zvýrazňovací kruhy, popisky a obsazenost se po dorazení dlaždice
  kreslí jen v jejím místě, ne v celém okně. Kreslení obsazenosti
  nezdržuje zjišťování typu waypointu.
- Chyba při výpočtu ikon keší nezastaví jejich kreslení a do přehledu
  problémů se zapíše jen jednou.
- Toolbar s přepínači ikon má stálou výšku, mapa se po načtení keší
  (třeba Munzee nebo databáze) neposouvá.
- Zoom na obdélník (Shift + tažení), na keš, cestu, cesty a výlet
  vybere správné měřítko; dřív mapu oddálil o čtyři měřítka.
- Při oddálení, kdy se svět v okně opakuje, se nevykreslí všechny keše
  najednou; výřez se v takovém měřítku bere jako celý svět. Při měřítku
  v tisících kilometrů nepřetékají souřadnice, takže tažení mapy,
  přiblížení a zoom do obdélníku míří tam, kam mají.
- Čáry UTM mřížky jsou na správném místě i u okraje zóny, převod z UTM
  na zeměpisné souřadnice byl až o 7 metrů posunutý k jihu. Převod na
  UTM určuje správně pásmo, souřadnice na jižní polokouli se převádějí
  zpět správně.
- Souřadnice ve stupních a minutách (i vteřinách v mřížce a rendru) se
  zaokrouhlují správně, místo například 49°60.000 se ukáže 50°00.000.
- Souřadnice zadané mimo rozsah (šířka nad 90°, délka nad 180°, minuty
  nebo vteřiny od 60) program odmítne, místo aby přeskočil jinam.
- Dialog rendru ukazuje rozměr terénu ve skutečných kilometrech, ne
  desetinásobek. Měřítko „1 : 0“ pro tisk a uložení mapy se neuplatní
  ani neuloží.
- Kalibrační body v souboru `.map` pro OziExplorer mají polokouli S a W,
  rendr západně od Greenwiche nebo na jižní polokouli sedí na mapě.
  Rendr pro OziExplorer ohlásí, když se soubor `.map` nepodaří zapsat
  (třeba na plný disk), a nenechá po sobě neúplné soubory.
- Rendr mapy nepřichází o dlaždice, které si najednou vyžádalo víc míst.
- Dlouhý záznam trasy (statisíce bodů) se kreslí rychleji: úseky mimo
  překreslovanou oblast a kratší než pixel se vynechají a body vybrané
  trasy těsně u sebe mají jednu značku.
- Cesty: plán trasy (`<rte>`) z GPX se načte jako cesta, soubor bez
  trasy se ohlásí. Vadný soubor při importu více souborů nezahodí cesty
  z ostatních. GPX, který nevytvořil GeoKuk (tracklog z GPS, Locusu),
  Uložit nepřepíše a nabídne Uložit jako, aby se neztratily výšky, časy
  a body. Cesta s názvem obsahujícím `&`, `<` nebo `%` se uloží a jde
  znovu otevřít. Nepovedené uložení nezmění soubor, ke kterému cesta
  patří. Uložení cesty nepřenačte všechna data a body cest se
  neukazují jako keše.
- Ukládání cest, nastavení a výletů zapíše soubor celý, nebo nechá
  původní beze změny; chybu zápisu (plný disk, disk jen pro čtení)
  program ohlásí a cesty zůstanou rozpracované, ne ztracené. Nastavení
  se ukládá i po neočekávané chybě při zápisu.
- Výlet (`lovim.ggt`, `tedne.ggt`): kódy keší, které zrovna nejsou
  v načtených datech, zůstanou v souboru; změny se zapisují v pořadí
  a při Konci se dopíšou. Když výlet nejde uložit, program to ohlásí
  a zkusí ho uložit znovu při další změně.
- Chyba při kopírování odkazů výletu nebo cesty do schránky se ohlásí.
- Program nastartuje i s poškozeným nastavením nebo s nesmyslnou
  uloženou hodnotou; poškozený soubor odloží stranou a upozorní na to.
  Dlouhé nastavení (seznamy souborů a podobně) po zkrácení nebo smazání
  nenechává zbytky, které se dřív přilepily k nové hodnotě.
- Při plném disku nebo nezapisovatelné složce TEMP se ikony a mapa
  načtou. Když nejde uložit nastavení, Konec nabídne ukončení bez
  uložení.
- Chybová hlášení (`data/log/chyby`) si drží jen posledních dvacet spuštění,
  starší se při startu smažou. Chyba při práci na pozadí je v přehledu
  problémů česky a s příčinou.
- Dialogy se vejdou nad hlavní panel Windows. Okno uložené na větším
  nebo odpojeném monitoru se při startu posune a zmenší, aby bylo celé
  vidět.
- Servisní okno (Soubor > Servis) má titulek, tlačítka Zavřít
  a Nápověda, zavírá se klávesou Esc, otevírá se uprostřed hlavního okna
  a ukazuje hodnoty hned po otevření; po zavření se hodnoty přestanou
  měřit.
- Dialog Nastavení popisků se vejde na obrazovku: barva písma a podkladu
  jsou na kartách a pole pro vzorky popisků jsou dost široká.
- „Listing do Geogetu“ (F3) funguje i u keší, které mají jen běžný odkaz
  na listing; do schránky se vloží ten. Klávesa F3 patří jen této akci.
- Menu Cesty má podtržené C a Skin I; položky v menu Výlety, Cesty
  a Kešoidy mají různá podtržená písmena, která jsou v jejich textu.
  Alt+I otevře menu Skin i s vybranou keší a neoznačí ji jako
  ignorovanou.
- Opravené překlepy a nesrozumitelné texty v menu, bublinách
  a hláškách.
  Hláška o nedostatku paměti radí, jak paměť zvýšit, hláška
  o nepoužitelné cache dlaždic radí zkontrolovat disk a práva ke složce.
- Písmeno napsané v otevřeném menu nebo do pole na liště (Hodnocení,
  BestOf, Favorit) nepřepne mapový podklad. Po výběru v seznamu Výlet
  na liště ovládají šipky a PageUp/PageDown dál mapu.
- Když nejde otevřít prohlížeč (nápověda, web), program ukáže adresu
  a nabídne ji zkopírovat. Nápověda z okna O programu otevře hlavní
  stránku nápovědy.
- Odkaz u keše nebo waypointu otevře jen webovou stránku (http, https),
  ne soubor. Značky HTML v kódech, názvech a autorech keší, názvech
  waypointů, cest a typů waypointů, v hintu, ve jménech souborů, v adresách
  z hledání adresy a v souborech uživatelských map se v bublině, detailu
  keše, hledání, nabídkách, výběru ikon, Přehledu zdrojů a hlášce při
  startu zobrazí jako text.
- Při pomalém startu programu se už neobjeví chyba při načítání ikon.

### Odstraněno
- Vrstva „Open cyclo“ (Thunderforest). Lze ji přidat jako uživatelskou
  mapu s vlastním klíčem, příklad je v `priklady/mapy/cyklo.mapa`.
- Položka nápovědy „Zprávy uživatelům“, která neměla odkud zprávy brát.
- Vrstva „Letecká Google“, náhled vlevo dole ukazuje leteckou mapu
  Mapy.cz.

### Vývoj
- Aktualizace knihoven se známými zranitelnostmi: guava 33.4.8,
  sqlite-jdbc 3.53.4.0, metadata-extractor 2.21.0, logback 1.3.16,
  v testech junit 4.13.2.
- Popis vydání na GitHubu se bere z tohoto souboru. Soubory vydání mají
  ověřitelný původ (`gh attestation verify`).
- SQLJet 1.1.15 (GPL 3 nebo novější).
- Program se vydává jako `geokuk.jar`, spouštěč `start.jar`,
  `java.properties` a zip pro Windows s Javou,
  konfigurace Launch4j je odstraněná.
- Smoke test celého programu nad falešným mapovým serverem (workflow
  Smoke, každou noc na `main`) a test kolizí klávesových zkratek.

## 6.0.0

Navazuje na verzi 5.1.1c Jiřího Bílka.

### Opravy
- Oddálení mapy na nejmenší měřítko už nevyhazuje chybové okno.
- Mapa při přibližování ke kurzoru neskáče jinam, ani na širokých
  monitorech.
- Mapu nejde oddálit víc, než kolik program spolehlivě zvládne.
- Mřížky se nezacyklí při přetočení mapy přes okraj světa.
- Program spuštěný z jaru pozná svou složku, takže funguje nastavení
  uložené u programu a cesty relativní k programu.

### Změny
- Nápověda → „Informace pro hlášení chyby“ ukáže verzi, prostředí,
  poslední události a chyby a umí je zkopírovat do schránky. Testovací
  a vývojová verze má číslo trvale vpravo v menu, kliknutím se otevře
  totéž.
- Ve Windows Geokuk vedle sebe vytvoří spouštěč `geokuk.cmd`, pokud
  chybí. Novou verzi Geokuk stáhne a spouštěč ji při dalším spuštění
  nainstaluje. Předchozí verze zůstane jako
  `geokuk.jar.bak`.
- Kontrola nové verze a stažení míří na GitHub Releases tohoto repa,
  „Zadat problém“ a webová stránka programu na jeho GitHub.
- V dialogu o nové verzi tlačítko „Připomenout za měsíc“ odloží další
  automatickou kontrolu o 30 dní.
- Ve zprávách uživatelům přibylo tlačítko „Označit vše jako přečtené“.
- Nová instalace má vypnuté datové složky GeoGet a GSAK, popisky keší
  a mřížky Dd a DdMmMmm. Uložené nastavení se nemění.

### Vývoj
- Build na JDK 21 přes Maven Wrapper, CI na GitHub Actions.
- Release obsahuje `geokuk.jar` a `geokuk.jar.sha256`. Verze v jaru
  se bere z tagu; tag s příponou (`v6.0.1-beta.1`) vydá testovací verzi
  jako pre-release. Geokuk ji nabídne, jen když vedle `geokuk.jar` leží
  soubor `beta`. Sestavení mimo tag mají verzi s příponou `-dev.<číslo
  běhu CI>` a v jaru je uložen commit.
- Odstraněn build přes Ant a NetBeans a zdroj webu geokuk.cz.

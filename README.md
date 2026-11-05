# GeoKuk

[![Build](https://github.com/geokuk-community/geokuk/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/geokuk-community/geokuk/actions/workflows/build.yml)
[![Licence: GPL v3](https://img.shields.io/badge/licence-GPL%20v3-blue.svg)](LICENSE)

**Plánování geovýletů na českých mapách.**

* podpora pro geocaching
* desktopový platformově nezávislý program (Java)
* online i offline režim

## Stažení

Aktuální verze je v [Releases](https://github.com/geokuk-community/geokuk/releases).
GeoKuk se neinstaluje: rozbalí se do složky a spouští se z ní. Všechno,
co si program ukládá, je ve složce `data` vedle něj, takže rozhoduje jen to,
odkud ho spustíte. Složku s programem můžete přesunout nebo zkopírovat
i s daty a nastavením.

### Windows

Stáhněte `GeoKuk-windows.zip` a rozbalte ho třeba do `C:\`. Zip obsahuje
složku `GeoKuk`, vznikne tedy `C:\GeoKuk`. Je v ní i Java, žádnou jinou není potřeba instalovat. Nerozbalujte ho do
`Program Files` (tam program nesmí zapisovat) ani do složky, kterou
synchronizuje OneDrive, Dropbox nebo Google Disk (pozor, OneDrive často
zálohuje i Plochu a Dokumenty).

Poprvé spusťte `GeoKuk-prvni-spusteni.cmd`. Windows se zeptají, jestli
soubor od neznámého vydavatele spustit; zvolte Spustit. Dotazu se
vyhnete, když před rozbalením ve vlastnostech staženého zipu zaškrtnete
Odblokovat. GeoKuk pak vedle vytvoří zástupce `GeoKuk.lnk`; dál
spouštějte jeho, klidně ho zkopírujte na plochu nebo připněte na hlavní
panel. Do nabídky Start ho přidá Soubor > Vytvořit zástupce. Když
složku přesunete, spusťte nejdřív znovu `GeoKuk-prvni-spusteni.cmd`,
zástupce ve složce i jeho kopie na ploše a v nabídce Start se opraví.
Do té doby zástupce hlásí chybu „Unable to access jarfile“.

Když GeoKuk najde novou verzi, nabídne ji. Po volbě Stáhnout novou
verzi ji stáhne a nabídne restart; jinak se nainstaluje při příštím
spuštění. Javu aktualizuje nový zip. Rozbalte ho
tam, kam ten první (třeba do `C:\`), přes stávající složku `GeoKuk`;
data a nastavení zůstanou.

### Linux a macOS

Stáhněte `geokuk.jar` do vlastní složky a spusťte ho `java -jar geokuk.jar`
(potřeba je Java 8 nebo novější). Když GeoKuk najde novou verzi,
nabídne ji. Po volbě Aktualizovat nahradí `geokuk.jar`
(předchozí zůstane jako `geokuk.jar.bak`) a nová verze se spustí při
příštím spuštění. Paměť programu a restart po aktualizaci jsou jen
v zipu pro Windows; paměť jde zadat parametrem Javy, třeba
`java -Xmx2g -jar geokuk.jar`.

### Složka s programem

```
GeoKuk
├── GeoKuk-prvni-spusteni.cmd    první spuštění ve Windows
├── GeoKuk.lnk                   zástupce, vytvoří ho GeoKuk
├── CTIMNE.txt
├── licence                      licence GeoKuku, knihoven a mapových podkladů
├── program
│   ├── geokuk.jar               program
│   ├── start.jar                spouštěč
│   ├── geokuk.ico
│   └── runtime                  Java
└── data                         všechno, co si GeoKuk ukládá
    ├── nastaveni.xml
    ├── mapy                     uživatelské mapy, co mapa to soubor *.mapa
    ├── mapy-priklady            ukázky uživatelských map
    ├── cache                    dlaždice map, lze smazat
    ├── offline-mapy             mapy .map pro podklad Offline mapa
    ├── gpx                      výchozí složka pro keše z GPX
    ├── cesty
    ├── ikony                    vlastní ikony (moje) a od jiných (ostatni)
    ├── vylety                   lovim.ggt a tedne.ggt
    ├── render
    └── log                      log a chybová hlášení
```

Keše načte GeoKuk ze souborů GPX, `.geokuk` a zip ve složce `data\gpx`;
načítání z databáze GeoGetu nebo GSAKu a jinou složku s kešemi zapnete
v Soubor > Umístění souborů. Cesta uvnitř složky GeoKuk se tam ukazuje
jako `${GeoKuk}/data/gpx` a při přesunu nebo přejmenování složky se
posune s ní, pod polem je výsledná cesta. Cesta mimo složku GeoKuk
zůstává pevná.

Paměť pro program zvolí spouštěč podle počítače (polovina paměti, 1 až
3 GB, od 16 GB paměti počítače 4 GB), změnit ji jde v Soubor > Paměť programu.

### Přechod ze starší verze

Nastavení se při prvním spuštění převezme samo, kromě umístění složek:
keše se čtou ze složky `data\gpx`, výstupy rendru jdou do `data\render`
a načítání z GeoGetu a GSAKu je vypnuté, zapnout ho jde v Soubor >
Umístění souborů. Data starší verze zůstanou beze změny ve složce
`%USERPROFILE%\geokuk` (na Linuxu a macOS `~/geokuk`), GeoKuk je odtud
nečte. Co chcete dál používat, zkopírujte do složky `data`:

- keše z GPX uložené přímo v `%USERPROFILE%\geokuk` (výchozí složka
  starší verze) do `data\gpx`, nebo jejich složku nastavte v Soubor >
  Umístění souborů (relativní cesta, třeba `data\gpx`, se počítá od
  složky GeoKuk),
- výlety `lovim.ggt` a `tedne.ggt` do `data\vylety`,
- cesty ze složky `cesty` do `data\cesty`,
- vlastní ikony z `imagesMy` do `data\ikony\moje` a ikony od jiných
  z `images3rdParty` do `data\ikony\ostatni`,
- dlaždice map z `prchave\kachle` do `data\cache`.

Ve Windows se verze 6.0.0 na novou verzi aktualizuje sama, ale jen
samotný program ve stávající složce, bez přibalené Javy. Další
aktualizace pak už sama nenainstaluje. Pro automatické aktualizace
a přibalenou Javu stáhněte `GeoKuk-windows.zip` a data přeneste podle
postupu výše.

## Uživatelské mapy

Vlastní mapové podklady se zadávají ve složce `data/mapy`, každá mapa
v samostatném souboru `<označení>.mapa`, a v menu Mapy jsou ve skupině
„Uživatelské mapy“. Popis a příklady jsou v [`priklady/mapy`](priklady/mapy);
GeoKuk je při každém startu uloží do složky `data/mapy-priklady`
a upravené ukázky tam přepíše. Mapu začnete používat zkopírováním jejího
souboru do `data/mapy` a upravujte ji až tam.

## Offline mapy

Podklad Mapy > Offline mapa (klávesa V) kreslí mapu v počítači
z vektorových map ve formátu mapsforge (soubory `.map`), bez internetu.

Mapy zemí a regionů Evropy a k nim turistické trasy, vrstevnice a stínování
terénu stáhnete přímo v GeoKuku: v menu Mapy > Správce offline map… nebo
v bloku Mapy vpravo dole ve stavovém řádku. Při první instalaci se otevře
Soubor > Průvodce nastavením map, který vybere podklad a vrstvy a stáhne, co
chybí. Mapy ke stažení jsou z [download.mapsforge.org](https://download.mapsforge.org/).

Seznam vrstev (trasy, vrstevnice, stínování) si program stahuje z projektu
[geokuk-maps](https://github.com/geokuk-community/geokuk-maps) na GitHubu,
nejvýš jednou za hodinu; dotaz nese jen verzi programu, ne nick ani jiné osobní údaje.

Vlastní soubory `.map` dejte do složky `data/offline-mapy` (program ji při
startu založí; jinou složku lze nastavit v Soubor > Umístění souborů na kartě
Mapy). Více map ve složce, třeba Česko a Slovensko, se kreslí naráz;
mapu zkopírovanou do složky za běhu program sám načte. Mapy lze získat
i z [osm.paws.cz](https://osm.paws.cz/) (česká a slovenská mapa
s turistickým značením) nebo z [OpenAndroMaps](https://www.openandromaps.org/downloads)
(mapy jsou jen pro soukromé použití).

Data map jsou z OpenStreetMap, © přispěvatelé OpenStreetMap, licence
[ODbL](https://www.openstreetmap.org/copyright); atribuci program kreslí
v rohu mapy.

Zip s programem obsahuje v `data/offline-mapy` přehledovou mapu světa
`prehled-svet-ne.map` (pevnina, moře, státní hranice a české názvy států),
takže offline mapa ukáže celý svět i bez stažených map. Je vytvořená
z dat Natural Earth, která jsou volným dílem (public domain); Made with
Natural Earth, [naturalearthdata.com](https://www.naturalearthdata.com/).
Soubor můžete smazat, program funguje i bez něj a znovu ho nepřidá;
jinak ho aktualizace programu udržuje v aktuální verzi. Přehledová mapa je
jen ve výchozí složce `data/offline-mapy` a jen v zipu s programem; ve
vlastní složce offline map nebo u samotného `geokuk.jar` ji nemáte,
soubor `prehled-svet-ne.map` lze zkopírovat ze zipu.

Vzhled mapy se volí v Mapy > Téma offline mapy. Vestavěná témata jsou
součástí programu. Jiné téma (soubor `.zip` nebo `.xml`, například paws
z osm.paws.cz) si stáhněte sami a dejte ho do složky offline map
neupravené, se souborem licence od jeho autora. Některá témata mají
nekomerční licenci (například CC BY-NC-SA); taková se smějí používat
a šířit jen v souladu s ní; GeoKuk je s programem nedodává.

### Vzhled offline map

Snímky ukazují offline mapu z dat OpenStreetMap (mapsforge) s vlastními
vrstvami GeoKuku (turistické a cyklotrasy, vrstevnice, stínování terénu)
a vlastním vzhledem, tématem GeoKuk: trasy v barvách značení, keše jako
ikony, v menším měřítku jako barevné tečky. Trasu výletu jde vést po
cestách, panel cesty ukazuje její délku a body.

![Přehled kraje s keškami jako tečkami](docs/geokuk-z12.png)

![Detail s ikonami keší a cestou po cestách](docs/geokuk-z14.png)

Mapová data © přispěvatelé OpenStreetMap ([ODbL](https://www.openstreetmap.org/copyright)),
Made with Natural Earth, vrstevnice Copernicus DEM GLO-30 © DLR/Airbus, ESA;
letecký snímek v rohu © Seznam.cz.

## Dálkové ovládání

Soubor > Dálkové ovládání (nebo parametr `--ovladani[=port]`, výchozí port
48321) povolí jiným programům na tomto počítači ovládat GeoKuk přes HTTP:

```
GET  http://127.0.0.1:48321/stav
POST http://127.0.0.1:48321/pozice?lat=50.08&lon=14.42&meritko=15
POST http://127.0.0.1:48321/kes?kod=GC12345
POST http://127.0.0.1:48321/popredi
POST http://127.0.0.1:48321/podklad?jmeno=TURIST_M
POST http://127.0.0.1:48321/prenacti
```

Příkaz `kes` vystředí mapu na kešoid a otevře jeho detail; kód může být i odkaz nebo
text s kódem (`https://coord.info/GC12345`). Okno programu se přitom přenese do popředí,
`&focus=ne` to vypne. Příkaz `popredi` přenese okno do popředí a vyžádá mu focus; Windows
programu na pozadí focus dát nemusí, pak okno jen bliká na hlavním panelu.

Požadavky z webových stránek program odmítá.

## Původ

GeoKuk napsal Martin Veverka se spoluautory. Toto repo navazuje na jeho
práci a přebírá opravy z dalších forků:

* [marvertin/geokuk](https://github.com/marvertin/geokuk): původní projekt,
  základem je větev `master` (commit `e4a5cbf`, 2020).
* [JiriBilek/geokuk](https://github.com/JiriBilek/geokuk): úpravy map
  Jiřího Bílka, větev `zmena_map_5.1` (verze 5.1.1c).

Převzaté commity mají původního autora a v popisu odkaz na zdrojový commit
(`cherry picked from commit …`). Z Bílkovy větve nejsou převzaté jen
sestavené jary v `releases/`. Co je nového oproti 5.1.1c, popisuje
[CHANGELOG](CHANGELOG.md).

GeoKuk je upravená verze od 2026, GeoKuk community; původní dílo © Martin
Veverka a spoluautoři.

## Poděkování

Martinu Veverkovi za GeoKuk, Danielu Stahrovi za spoluautorství, Jiřímu
Bílkovi za udržování map v chodu a všem dalším přispěvatelům z historie
projektu, mimo jiné Bohuslavu Roztočilovi, Petru Pitkovi a Petru
Schönmannovi.

Autorům aplikace aDrake pro Android za inspiraci: zobrazení keší na
oddálené mapě jako barevných teček podle typu vychází z ní. Jsou to
[LudekV](https://www.geocaching.com/p/?u=LudekV) (původní autor),
[Karas0911](https://www.geocaching.com/p/?u=Karas0911) (Trnkáči, sada ikon) a
[lnavrat](https://www.geocaching.com/p/?u=lnavrat), který pokračuje
ve vývoji.

## Build

```sh
./mvnw -B clean package
```

Stačí JDK 21, výsledek je v `target/`. Zip pro Windows s přibalenou Javou
staví workflow Build na Windows runneru.

Smoke test celého programu nad falešným mapovým serverem (projde mapu,
menu a otevřené dialogy) potřebuje displej, na Linuxu třeba přes xvfb:

```sh
xvfb-run -a -s "-screen 0 1400x900x24" ./mvnw -B -P smoke verify
```

Na GitHubu běží workflow Smoke každou noc na Linuxu, ručně ho jde
spustit pro Linux i Windows.

## Licence

[GNU GPL v3](LICENSE). Použité knihovny a jejich licence jsou
v [licence/THIRD-PARTY.txt](licence/THIRD-PARTY.txt), mapové podklady, jejich
licence a atribuce v [licence/MAPOVE-PODKLADY.txt](licence/MAPOVE-PODKLADY.txt).

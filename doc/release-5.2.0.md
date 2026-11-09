# Geokuk 5.2.0 — nové mapové zdroje a Java 25

Multiplatformní vydání pro **Windows, macOS a Linux**. Ve všech případech stáhněte stejný soubor **`geokuk-5.2.0.jar`** v sekci **Assets** níže.

## Co je nového

- Konfigurovatelné mapové zdroje načítané z JSON a aktualizované definice map.
- Opravy načítání mapových dlaždic a mapového náhledu, HTTPS pro výchozí OpenStreetMap.
- Přechod z Javy 8 na **Javu 25** a aktualizace knihoven.
- Opravy importu GeoGetu, aktualizované načítání pluginů a ikona Lab Cache.
- Mřížka zeměpisných souřadnic po celých stupních a další opravy od verze 5.1.0.

## Jak aplikaci spustit

### 1. Nainstalujte Javu 25

Nainstalujte **64bitovou Javu 25**, například [Eclipse Temurin JDK 25](https://adoptium.net/temurin/releases/?version=25). Vyberte svůj operační systém a procesor; na Macu Apple Silicon zvolte AArch64, na Intelu x64. Java 8, 17 ani 21 toto vydání nespustí.

### 2. Stáhněte aplikaci

V **Assets** stáhněte **`geokuk-5.2.0.jar`** a uložte jej do vlastní složky s právem zápisu, například `Geokuk` ve své domovské složce. JAR nerozbalujte. Soubory **Source code (zip)** a **Source code (tar.gz)** jsou zdrojové kódy pro vývojáře.

### 3. Spusťte JAR

Otevřete terminál ve složce se staženým JARem (ve Windows PowerShell, na Macu Terminál, na Linuxu svůj terminál) a zadejte:

```sh
java -version
java -Xmx2g -jar geokuk-5.2.0.jar
```

První příkaz musí ukázat Javu 25. Druhý spustí Geokuk a umožní mu využít až 2 GB paměti.

**macOS:** pokud máte více verzí Javy, můžete Javu 25 vybrat přímo:

```sh
"$(/usr/libexec/java_home -v 25)/bin/java" -Xmx2g -jar geokuk-5.2.0.jar
```

**Windows a Linux:** pokud `java -version` ukazuje starší Javu nebo příkaz neexistuje, nastavte Javu 25 do `PATH` a otevřete nový terminál. Můžete také použít úplnou cestu k programu `java` z instalace Javy 25. Na Linuxu je nutné grafické prostředí a desktopová Java, nikoli jen headless balíček.

## Poznámky k vydání

- Distribuuje se pouze multiplatformní **JAR**, bez přibalené Javy. Samostatný Windows EXE ani instalační balíčky pro Mac a Linux tentokrát nevydáváme.
- Ověřeno sestavení a 162 automatických testů na macOS ARM64 s Javou 25. Spuštění na Windows a Linuxu nebylo v rámci tohoto vydání ručně ověřeno.
- Některé starší mapové služby již nemusí být dostupné; v takovém případě vyberte jiný mapový podklad nebo upravte jeho definici. Mapy lze volit v nabídce aplikace.
- Chyby hlaste v [GitHub Issues](https://github.com/marvertin/geokuk/issues), ideálně s operačním systémem, výstupem `java -version` a chybou z terminálu.

**Úplný seznam změn:** https://github.com/marvertin/geokuk/compare/v5.1.0...v5.2.0

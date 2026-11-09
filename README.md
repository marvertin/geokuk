# Geokuk

Plánování geovýletů nad mapami, práce s kešemi a dalšími waypointy.
Desktopová aplikace pro Windows, macOS a Linux.

## Stažení a spuštění

1. Nainstalujte **Javu 25 (64bitovou)**, například [Eclipse Temurin JDK 25](https://adoptium.net/temurin/releases/?version=25), pro svůj systém a procesor. Na Macu zvolte Apple Silicon (AArch64) nebo Intel (x64) podle svého počítače.
2. V [nejnovějším vydání](https://github.com/marvertin/geokuk/releases/latest) rozbalte **Assets** a stáhněte `geokuk-5.2.0.jar`. Archivy **Source code** obsahují zdrojové kódy, nejsou hotovou aplikací.
3. Uložte JAR do vlastní složky, ve které můžete zapisovat, například `Geokuk` ve své domovské složce. JAR nerozbalujte.
4. V této složce otevřete terminál (na Windows PowerShell) a spusťte:

   ```sh
   java -version
   java -Xmx2g -jar geokuk-5.2.0.jar
   ```

První příkaz musí ukázat Javu 25. Pokud systém příkaz `java` nezná nebo ukazuje starší Javu, nastavte instalovanou Javu 25 do `PATH` a otevřete nový terminál. Případně použijte úplnou cestu k jejímu programu `java`.

Na macOS můžete Javu 25 vybrat přímo:

```sh
"$(/usr/libexec/java_home -v 25)/bin/java" -Xmx2g -jar geokuk-5.2.0.jar
```

Stejný JAR je určen pro všechny tři systémy. Java není přibalená. Linux vyžaduje grafické prostředí a Javu s podporou desktopových aplikací (nikoli pouze headless balíček). Verze 5.2.0 se distribuuje pouze jako JAR; samostatné EXE, DMG ani DEB nejsou součástí vydání. Spuštění z terminálu také zobrazí případnou chybu, kterou by dvojklik skryl.

## Sestavení ze zdrojových kódů

Vyžaduje JDK 25 a Maven:

```sh
mvn clean verify
java -Xmx2g -jar target/geokuk-5.2.0.jar
```

Soubor `target/geokuk-5.2.0.jar` obsahuje závislosti aplikace. `target/temporary.jar` není distribuční soubor.

Podrobnosti o používání najdete ve [wiki](https://github.com/marvertin/geokuk/wiki). Chyby a návrhy hlaste přes [GitHub Issues](https://github.com/marvertin/geokuk/issues).

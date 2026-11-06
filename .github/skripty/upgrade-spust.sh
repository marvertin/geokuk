#!/usr/bin/env bash
# Spustí jeden scénář zkoušky přechodu (viz test-upgradu.py) v CI. Použití: upgrade-spust.sh SCENAR
#   prechod           poslední ostrá verze (nebo $STARA) -> měřená verze
#   poskozena-cache   totéž, ale mezi verzemi se poškodí cache dlaždic a nechají se zbytky .vadna/.new
#   starsi-rada       poslední vydání starší řady (nebo $STARSI) -> měřená verze, bez internetu
#   novy-zip          jen měřená verze s čerstvě sestavenou přibalenou Javou (Windows) nebo Javou runneru: musí vzniknout archiv tříd
set -uo pipefail
SCENAR="$1"
export NAZEV_ZKOUSKY="Zkouška přechodu: $SCENAR"
export PYTHONIOENCODING=utf-8
NOVY_JAR="$(ls target/geokuk-[0-9]*.jar | head -1)"
KOREN="$RUNNER_TEMP/zkouska-$SCENAR"
mkdir -p "$KOREN"
if [ "$RUNNER_OS" = "Windows" ]; then PY=python; DISPLEJ=(); else PY=python3; DISPLEJ=(xvfb-run -a -s "-screen 0 1400x900x24"); fi

# Stáhne vydání $1 a vypíše argumenty --stary [--runtime]; Windows rozbalí zip s přibalenou Javou, jinde stáhne jary.
stahni() {
  local tag="$1" cil="$RUNNER_TEMP/vydani-$1"
  mkdir -p "$cil"
  if [ "$RUNNER_OS" = "Windows" ]; then
    gh release download "$tag" --repo "$GITHUB_REPOSITORY" --pattern GeoKuk-windows.zip --dir "$cil" --clobber >&2 || return 1
    pwsh -NoProfile -Command "Expand-Archive -Path '$cil/GeoKuk-windows.zip' -DestinationPath '$cil' -Force" >&2 || return 1
    echo "--stary $cil/GeoKuk/program/geokuk.jar $cil/GeoKuk/program/start.jar --runtime $cil/GeoKuk/program/runtime"
  else
    gh release download "$tag" --repo "$GITHUB_REPOSITORY" --pattern geokuk.jar --pattern start.jar --dir "$cil" --clobber >&2 || return 1
    echo "--stary $cil/geokuk.jar $cil/start.jar"
  fi
}

STABILNI="$(git tag --list 'v*' --sort=-v:refname | grep -v -- '-')"
POSLEDNI="$(echo "$STABILNI" | head -1)"
case "$SCENAR" in
  prechod|poskozena-cache)
    TAG="${STARA:-$POSLEDNI}"
    echo "Stará verze: $TAG"
    ARGS=$(stahni "$TAG") || exit 1
    [ "$SCENAR" = "poskozena-cache" ] && ARGS="$ARGS --poskodit-cache"
    ;;
  starsi-rada)
    SERIE="$(echo "$POSLEDNI" | sed -E 's/^v([0-9]+\.[0-9]+).*/\1/')"
    TAG="${STARSI:-$(echo "$STABILNI" | grep -v "^v$SERIE\." | head -1)}"
    echo "Poslední vydání starší řady než $SERIE: $TAG"
    [ -n "$TAG" ] || { echo "Starší řada nenalezena"; exit 1; }
    ARGS=$(stahni "$TAG") || exit 1
    ARGS="$ARGS --podklad TURIST_M --bez-site"
    ;;
  novy-zip)
    ARGS="--jen-novy"
    if [ "$RUNNER_OS" = "Windows" ]; then
      # Stejný jlink jako v build.yml, tedy i s archivem tříd Javy.
      MODULY="java.base,java.compiler,java.desktop,java.logging,java.management,java.naming,java.prefs,java.sql,java.xml,jdk.accessibility,jdk.charsets,jdk.crypto.ec,jdk.crypto.mscapi,jdk.httpserver,jdk.localedata,jdk.management,jdk.unsupported,jdk.zipfs"
      jlink --add-modules "$MODULY" --strip-debug --no-header-files --no-man-pages --generate-cds-archive --output "$RUNNER_TEMP/novy-runtime" || exit 1
      ARGS="$ARGS --runtime $RUNNER_TEMP/novy-runtime"
    fi
    ARGS="$ARGS --ocekavej-cds"
    ;;
  *) echo "Neznámý scénář $SCENAR"; exit 2 ;;
esac
# shellcheck disable=SC2086
"${DISPLEJ[@]}" "$PY" .github/skripty/test-upgradu.py "$KOREN" $ARGS --novy "$NOVY_JAR" target/temporary-start.jar --mapy src/test/resources/offline-mapy

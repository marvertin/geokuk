#!/bin/zsh
cd -- "${0:A:h}" || exit 1
# Java musí být na trvalém místě; /tmp se při restartu může vymazat.
geokuk_java="$HOME/Library/Application Support/GeoKuk/runtime/zulu8.98.0.111-ca-jdk8.0.512-macosx_aarch64/Contents/Home/bin/java"
if [[ ! -x "$geokuk_java" ]]; then
  print 'Chybí připravená Java 8. Požádej o obnovení prostředí pro spuštění GeoKuku.'
  read '?Stiskni Enter pro zavření.'
  exit 1
fi
"$geokuk_java" -Xmx2g -jar target/geokuk-5.1.0.jar
geokuk_status=$?
if (( geokuk_status != 0 )); then
  print "GeoKuk skončil s chybou ($geokuk_status)."
  read '?Stiskni Enter pro zavření.'
fi
exit $geokuk_status

#!/bin/zsh
cd -- "${0:A:h}" || exit 1
# Java musí být na trvalém místě; /tmp se při restartu může vymazat.
if ! geokuk_java_home=$(/usr/libexec/java_home -v 25 2>/dev/null); then
  print 'Chybí Java 25. Nainstaluj JDK 25 a spusť GeoKuk znovu.'
  read '?Stiskni Enter pro zavření.'
  exit 1
fi
geokuk_java="$geokuk_java_home/bin/java"
"$geokuk_java" -Xmx2g -jar target/geokuk-5.2.0.jar
geokuk_status=$?
if (( geokuk_status != 0 )); then
  print "GeoKuk skončil s chybou ($geokuk_status)."
  read '?Stiskni Enter pro zavření.'
fi
exit $geokuk_status

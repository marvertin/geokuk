#!/usr/bin/env python3
"""Zkouška přechodu ze starší verze: stará verze si vytvoří profil a cache, pak ji nahradí kandidát stejně jako aktualizace z programu.

  test-upgradu.py KOREN --stary geokuk.jar start.jar --novy geokuk.jar start.jar [--java java] [--runtime SLOZKA] [--mapy SLOZKA]

KOREN je prázdná složka, ve které vznikne GeoKuk/{program,data}. Postup:
1. program/ dostane starý geokuk.jar a start.jar (a případně přibalenou Javu --runtime), data/ nastavení bez kontroly aktualizací a offline mapu;
2. spustí se stará verze přes start.jar, přepne se na offline mapu (vznikne cache dlaždic), nastaví se poloha a program se ukončí přes menu;
3. nahradí se jako při aktualizaci: nový jar jako geokuk.jar.new, nový start.jar;
4. spustí se kandidát nad stejnými daty, ověří se nastavení, soubory v cache a log, ukončí se a spustí se ještě jednou.
Výsledek jde do výstupu a do souhrnu běhu; končí kódem 1, když se něco nepovedlo.
"""
import argparse
import json
import os
import re
import shutil
import socket
import subprocess
import sys
import time
import urllib.parse
import urllib.request
import zipfile

OKNO = "Otevřeno okno: GeoKuk"
NASTAVENI = ('<?xml version="1.0" encoding="UTF-8" standalone="no"?>'
             '<!DOCTYPE preferences SYSTEM "http://java.sun.com/dtd/preferences.dtd">'
             '<preferences EXTERNAL_XML_VERSION="1.0"><root type="user"><map/><node name="geokuk"><map/><node name="current"><map/>'
             '<node name="vseobecne"><map><entry key="nextUpdateCheckTimestamp" value="9223372036854775807"/></map></node></node></node></root></preferences>')

problemy = []
souhrn = []


def ocekavej(plati, popis):
    print(("OK: " if plati else "CHYBA: ") + popis, flush=True)
    if not plati:
        problemy.append(popis)


def info(radek):
    print(radek, flush=True)
    souhrn.append(radek)


def verze_jaru(jar):
    with zipfile.ZipFile(jar) as z:
        for r in z.read("META-INF/MANIFEST.MF").decode("utf-8", "replace").splitlines():
            if r.startswith("Geokuk-Version:"):
                return r.split(":", 1)[1].strip()
    return "?"


class Beh:
    """Jeden běh programu přes start.jar s dálkovým ovládáním."""

    def __init__(self, koren, java, nazev, env=None):
        self.koren, self.nazev = koren, nazev
        self.data = os.path.join(koren, "data")
        self.log = os.path.join(self.data, "log", "geokuk.log")
        self.ovladani = os.path.join(self.data, "ovladani.properties")
        try:
            os.remove(self.ovladani)
        except OSError:
            pass
        self.zacatek = time.perf_counter()
        self.proces = subprocess.Popen([java, "-XX:-UsePerfData", "-jar", os.path.join(koren, "program", "start.jar"), "--ovladani=0", "--ovladani-devel"], cwd=koren,
                                       stdout=open(os.path.join(koren, nazev + ".out"), "wb"), stderr=subprocess.STDOUT, env=env)
        self.port = self.token = None

    def pocet_oken_v_logu(self):
        try:
            with open(self.log, encoding="utf-8", errors="replace") as f:
                return f.read().count(OKNO)
        except OSError:
            return 0

    def cekej_na_okno(self, predtim, limit=120):
        konec = time.time() + limit
        while time.time() < konec:
            # Spouštěč start.jar program spustí a sám skončí, proto jeho konec nic neznamená; chyba spouštěče je nenulový kód.
            if self.proces.poll() not in (None, 0):
                return None
            if self.pocet_oken_v_logu() > predtim:
                self.start_ms = int((time.perf_counter() - self.zacatek) * 1000)
                return self.start_ms
            time.sleep(0.2)
        return None

    def pripoj_ovladani(self):
        konec = time.time() + 60
        while time.time() < konec:
            try:
                p = dict(r.split("=", 1) for r in open(self.ovladani, encoding="utf-8").read().splitlines() if "=" in r and not r.startswith("#"))
                self.port, self.token = p["port"], p["token"]
                return True
            except (OSError, KeyError):
                time.sleep(0.2)
        return False

    def volej(self, metoda, adresa, **parametry):
        url = "http://127.0.0.1:%s%s" % (self.port, adresa)
        if parametry:
            url += "?" + urllib.parse.urlencode(parametry)
        pozadavek = urllib.request.Request(url, method=metoda, headers={"Authorization": "Bearer " + self.token})
        with urllib.request.build_opener(urllib.request.ProxyHandler({})).open(pozadavek, timeout=60) as odpoved:
            text = odpoved.read().decode("utf-8", "replace")
        try:
            return json.loads(text)
        except ValueError:
            return text

    def bezi(self):
        try:
            socket.create_connection(("127.0.0.1", int(self.port)), timeout=2).close()
            return True
        except OSError:
            return False

    def ukonci(self):
        try:
            self.volej("POST", "/menu", cesta="Soubor > Konec")
        except Exception as e:  # ovladání se může zavřít dřív, než odpoví
            print("Konec přes menu: %s" % e)
        konec = time.time() + 60
        while time.time() < konec:
            if not self.bezi():
                time.sleep(3)  # nastavení a cache se dopisují při ukončení
                return True
            time.sleep(0.5)
        print("Program se přes menu neukončil, ukončuji ho signálem.")
        ukonci_zbytky(self.koren)
        time.sleep(5)
        return False


def konec_souboru(cesta, radku=60):
    try:
        with open(cesta, encoding="utf-8", errors="replace") as f:
            return "".join(f.readlines()[-radku:])
    except OSError as e:
        return "(nejde přečíst: %s)" % e


def diagnostika(beh):
    """Při neotevřeném okně vypíše, co se stalo: kód spouštěče, výstup a konec logu."""
    print("::group::Diagnostika %s (kód spouštěče %s)" % (beh.nazev, beh.proces.poll()))
    print("--- %s.out (posledních 60 řádků)" % beh.nazev)
    print(konec_souboru(os.path.join(beh.koren, beh.nazev + ".out")))
    print("--- geokuk.log (posledních 60 řádků)")
    print(konec_souboru(beh.log))
    print("::endgroup::", flush=True)


def ukonci_zbytky(koren):
    """Zabije procesy Javy, které zůstaly viset ve zkoušené složce."""
    try:
        if os.name == "nt":
            subprocess.run(["powershell", "-NoProfile", "-Command",
                            "Get-CimInstance Win32_Process | Where-Object { $_.Name -like 'java*' -and $_.CommandLine -like '*%s*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }" % koren.replace("'", "''")],
                           timeout=60)
        else:
            subprocess.run(["pkill", "-f", koren], timeout=60)
    except Exception as e:
        print("Úklid procesů: %s" % e)


def pole(stav, klic):
    return str(stav.get(klic)) if isinstance(stav, dict) else "?"


def soubory(slozka):
    vysledek = {}
    for koren, _, nazvy in os.walk(slozka):
        for n in nazvy:
            cesta = os.path.join(koren, n)
            vysledek[os.path.relpath(cesta, slozka).replace(os.sep, "/")] = os.path.getsize(cesta)
    return vysledek


def radky_logu(log, vzor):
    try:
        with open(log, encoding="utf-8", errors="replace") as f:
            return [r.rstrip() for r in f if re.search(vzor, r)]
    except OSError:
        return []


SITOVE_CHYBY = r"stahování dlaždice|UnknownHost|Connection refused|ConnectException|NoRouteToHost"


def main():
    # Windows má výchozí kódování konzole cp1252, české znaky by skript shodily.
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")
    a = argparse.ArgumentParser()
    a.add_argument("koren")
    a.add_argument("--stary", nargs=2, metavar=("GEOKUK_JAR", "START_JAR"), help="vydaná starší verze; bez ní se zkouší jen nová verze (--jen-novy)")
    a.add_argument("--novy", nargs=2, required=True, metavar=("GEOKUK_JAR", "START_JAR"))
    a.add_argument("--jen-novy", action="store_true", help="bez staré verze: nová verze dvakrát po sobě nad čistými daty")
    a.add_argument("--java", default="java")
    a.add_argument("--runtime")
    a.add_argument("--mapy", help="složka s offline mapou (soubory .map)")
    a.add_argument("--podklad", default="OFFLINE_MF", help="podklad, který stará verze nastaví (starší řady offline mapu nemají)")
    a.add_argument("--bez-site", action="store_true", help="program nesmí na internet (dlaždice online podkladu), chyby sítě se v logu nepočítají")
    a.add_argument("--poskodit-cache", action="store_true", help="mezi starou a novou verzí poškodit tiles.sqlite a nechat zbytky .vadna a .new")
    a.add_argument("--ocekavej-cds", action="store_true", help="po 2. startu musí být v data/cds hotový archiv tříd (runtime se základním archivem)")
    arg = a.parse_args()
    if not arg.jen_novy and not arg.stary:
        a.error("chybí --stary (nebo --jen-novy)")

    koren = os.path.abspath(os.path.join(arg.koren, "GeoKuk"))
    program, data = os.path.join(koren, "program"), os.path.join(koren, "data")
    os.makedirs(program)
    os.makedirs(os.path.join(data, "offline-mapy"))
    zacatecni = arg.novy if arg.jen_novy else arg.stary
    shutil.copy(zacatecni[0], os.path.join(program, "geokuk.jar"))
    shutil.copy(zacatecni[1], os.path.join(program, "start.jar"))
    java = arg.java
    if arg.runtime:
        shutil.copytree(arg.runtime, os.path.join(program, "runtime"))
        java = os.path.join(program, "runtime", "bin", "java.exe" if os.name == "nt" else "java")
    if arg.mapy:
        for f in os.listdir(arg.mapy):
            if f.endswith(".map"):
                shutil.copy(os.path.join(arg.mapy, f), os.path.join(data, "offline-mapy", f))
    with open(os.path.join(data, "nastaveni.xml"), "w", encoding="utf-8") as f:
        f.write(NASTAVENI)
    env = dict(os.environ)
    if arg.bez_site:
        proxy = "-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=9 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=9 -Dhttp.nonProxyHosts=localhost|127.0.0.1"
        env["JAVA_TOOL_OPTIONS"] = (env.get("JAVA_TOOL_OPTIONS", "") + " " + proxy).strip()
    stara_verze = "-" if arg.jen_novy else verze_jaru(arg.stary[0])
    nova_verze = verze_jaru(arg.novy[0])
    if not arg.jen_novy:
        info("| Stará verze | %s |" % stara_verze)
    info("| Nová verze | %s |" % nova_verze)
    verze_javy = [r for r in subprocess.run([java, "-version"], capture_output=True, text=True).stderr.splitlines() if "version" in r and "Picked up" not in r]
    info("| Java | %s |" % (verze_javy[0] if verze_javy else "?"))
    info("| Základní archiv Javy (classes.jsa) | %s |" % ("je" if any(os.path.isfile(os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(java))), d, "server", n)) for d in ("bin", "lib") for n in ("classes.jsa", "classes_nocoops.jsa")) else "není"))

    pred = {}
    if not arg.jen_novy:
        # 1. Stará verze vytvoří profil.
        stary = Beh(koren, java, "stary", env)
        ms = stary.cekej_na_okno(0)
        ocekavej(ms is not None, "stará verze %s otevřela okno" % stara_verze)
        if ms is None:
            diagnostika(stary)
            return konec()
        ocekavej(stary.pripoj_ovladani(), "stará verze: dálkové ovládání odpovídá")
        stary.volej("POST", "/podklad", jmeno=arg.podklad)
        stary.volej("POST", "/pozice", lat="50.003", lon="14.405", meritko="15")
        time.sleep(15)
        stav_stary = stary.volej("GET", "/stav")
        print("Stav staré verze: %s" % json.dumps(stav_stary, ensure_ascii=False))
        ocekavej(pole(stav_stary, "podklad") == arg.podklad, "stará verze má podklad %s (%s)" % (arg.podklad, pole(stav_stary, "podklad")))
        ocekavej(stary.ukonci(), "stará verze se ukončila sama přes menu")
        cache = {k: v for k, v in soubory(data).items() if k.startswith("cache/")}
        info("| Cache po staré verzi | %s |" % (", ".join("%s %d kB" % (k, v // 1024) for k, v in sorted(cache.items())) or "žádná"))
        ocekavej(any(k.endswith("tiles.sqlite") and v > 0 for k, v in cache.items()), "stará verze založila cache dlaždic")
        ocekavej(os.path.isfile(os.path.join(data, "nastaveni.xml")), "stará verze uložila nastavení")

        if arg.poskodit_cache:
            tiles = os.path.join(data, "cache", "tiles.sqlite")
            if os.path.isfile(tiles):
                with open(tiles, "r+b") as f:
                    f.write(b"\xff" * 4096)
            for nazev, kb in (("tiles.sqlite.vadna", 3072), ("tiles.sqlite.vadna.1", 1024), ("tiles.sqlite.new", 2048)):
                with open(os.path.join(data, "cache", nazev), "wb") as f:
                    f.write(os.urandom(kb * 1024))
            info("| Před přechodem | poškozený tiles.sqlite, zbylé tiles.sqlite.vadna, .vadna.1 a .new |")

        # 2. Výměna jako při aktualizaci z programu: nový jar jako .new, nový start.jar.
        shutil.copy(arg.novy[0], os.path.join(program, "geokuk.jar.new"))
        shutil.copy(arg.novy[1], os.path.join(program, "start.jar"))
        pred = soubory(data)

    # 3. Nová verze nad stejnými daty, dvakrát po sobě.
    for poradi in (1, 2):
        beh = Beh(koren, java, "novy%d" % poradi, env)
        okna_pred = beh.pocet_oken_v_logu()
        ms = beh.cekej_na_okno(okna_pred)
        ocekavej(ms is not None, "%d. start nové verze otevřel okno" % poradi)
        if ms is None:
            diagnostika(beh)
            break
        info("| %d. start nové verze: okno za | %d ms |" % (poradi, ms))
        ocekavej(beh.pripoj_ovladani(), "%d. start: dálkové ovládání odpovídá" % poradi)
        if arg.jen_novy and poradi == 1:
            beh.volej("POST", "/podklad", jmeno=arg.podklad)
            beh.volej("POST", "/pozice", lat="50.003", lon="14.405", meritko="15")
        time.sleep(20)
        stav = beh.volej("GET", "/stav")
        print("Stav %d. startu: %s" % (poradi, json.dumps(stav, ensure_ascii=False)))
        ocekavej(pole(stav, "verze") == nova_verze, "%d. start: běží verze %s (%s)" % (poradi, nova_verze, pole(stav, "verze")))
        if not arg.jen_novy or poradi == 2:
            ocekavej(pole(stav, "podklad") == arg.podklad, "%d. start: podklad %s z nastavení zůstal (%s)" % (poradi, arg.podklad, pole(stav, "podklad")))
        ocekavej(beh.ukonci(), "%d. start: program se ukončil sám přes menu" % poradi)
        if poradi == 1 and arg.ocekavej_cds:
            # Rozepsaný archiv mladší než 30 s se nepovyšuje, další start by ho nechal být.
            time.sleep(35)

    if not arg.jen_novy:
        ocekavej(verze_jaru(os.path.join(program, "geokuk.jar")) == nova_verze, "program/geokuk.jar je nová verze")
        ocekavej(not os.path.exists(os.path.join(program, "geokuk.jar.new")), "geokuk.jar.new po výměně nezůstal")
        ocekavej(os.path.isfile(os.path.join(program, "geokuk.jar.bak")), "starý jar zůstal jako geokuk.jar.bak")

    po = soubory(data)
    zbytky = sorted(k for k in po if re.search(r"\.(vadna(\.\d+)?|new|tmp)$", k) and k not in pred)
    zustalo = sorted(k for k in pred if re.search(r"\.(vadna(\.\d+)?|new|tmp)$", k) and k in po)
    if arg.poskodit_cache:
        info("| Zbylé soubory z přípravy po přechodu | %s |" % (", ".join(zustalo) or "žádné se nezachovaly"))
        info("| Nové soubory .vadna/.new/.tmp | %s |" % (", ".join(zbytky) or "žádné"))
        tiles = os.path.join(data, "cache", "tiles.sqlite")
        hlavicka = open(tiles, "rb").read(16) if os.path.isfile(tiles) else b""
        ocekavej(hlavicka == b"SQLite format 3\x00", "po přechodu je tiles.sqlite platná databáze (hlavička %r)" % hlavicka)
    else:
        ocekavej(not zbytky, "po přechodu nevznikly zbytky .vadna/.new/.tmp: %s" % (zbytky or "žádné"))
        info("| Zbytky .vadna/.new/.tmp | %s |" % (", ".join(zbytky) or "žádné"))
        cache_po = {k: v for k, v in po.items() if k.startswith("cache/")}
        info("| Cache po nové verzi | %s |" % (", ".join("%s %d kB" % (k, v // 1024) for k, v in sorted(cache_po.items())) or "žádná"))
        ocekavej(any(k.endswith("tiles.sqlite") and v > 0 for k, v in cache_po.items()), "cache dlaždic zůstala")
    cds = sorted(k for k in po if k.startswith("cds/"))
    info("| Archiv tříd (data/cds) | %s |" % (", ".join(cds) or "není (přibalená Java bez základního archivu, nebo první běh)"))
    if arg.ocekavej_cds:
        ocekavej(any(k.startswith("cds/geokuk-") and k.endswith(".jsa") for k in cds), "po 2. startu je v data/cds hotový archiv tříd geokuk-*.jsa")

    chyby = radky_logu(os.path.join(data, "log", "geokuk.log"), r"\bERROR\b|Exception")
    if arg.bez_site:
        chyby = [r for r in chyby if not re.search(SITOVE_CHYBY, r)]
    else:
        chyby = [r for r in chyby if "UnknownHost" not in r]
    if arg.poskodit_cache:
        info("| Chyby v logu při poškozené cache (očekávané) | %d |" % len(chyby))
        for r in chyby[:5]:
            print("  " + r[:220])
    else:
        ocekavej(not chyby, "v logu nejsou chyby (%d): %s" % (len(chyby), " | ".join(r[:200] for r in chyby[:5])))
    return konec()


def konec():
    vystup = os.environ.get("GITHUB_STEP_SUMMARY")
    if vystup:
        with open(vystup, "a", encoding="utf-8") as f:
            f.write("### %s\n| | |\n|---|---|\n" % os.environ.get("NAZEV_ZKOUSKY", "Zkouška přechodu ze starší verze") + "\n".join(souhrn) + "\n\n")
            f.write("**%s**\n" % ("V pořádku." if not problemy else "Chyby: " + "; ".join(problemy)))
    print("\nVÝSLEDEK: %s" % ("v pořádku" if not problemy else "%d chyb" % len(problemy)))
    return 1 if problemy else 0


if __name__ == "__main__":
    try:
        kod = main()
    finally:
        if len(sys.argv) > 1:
            ukonci_zbytky(os.path.abspath(os.path.join(sys.argv[1], "GeoKuk")))
    sys.exit(kod)

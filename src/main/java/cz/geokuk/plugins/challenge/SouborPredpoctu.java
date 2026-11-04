package cz.geokuk.plugins.challenge;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.CRC32;

import lombok.extern.slf4j.Slf4j;

/**
 * Soubor s předpočítanými klasifikacemi keší: hlavička (značka, verze formátu, klíč), počet záznamů, záznamy (otisk kódu keše, šířka a délka v miliontinách stupně,
 * klasifikace) a CRC32 všeho předchozího. Neplatný, poškozený nebo cizí (jiný klíč) soubor se čte jako nepřítomný.
 */
@Slf4j
final class SouborPredpoctu {

	private static final int ZNACKA = 0x474B5043; // GKPC
	private static final int FORMAT = 1;
	private static final int BAJTU_ZAZNAMU = 8 + 4 + 4 + 2;
	private static final int NEJVIC_ZAZNAMU = 20_000_000;

	/** Jméno souboru předpočtu; dočasný soubor má navíc {@code .new}. */
	static final java.util.regex.Pattern JMENO = java.util.regex.Pattern.compile("challenge-[0-9a-f]{8}-\\d+\\.bin");
	static final java.util.regex.Pattern JMENO_DOCASNE = java.util.regex.Pattern.compile("challenge-[0-9a-f]{8}-\\d+\\.bin\\.new");

	/** Tabulka záznamů s otevřeným adresováním podle otisku kódu keše. */
	static final class Tabulka {
		private long[] otisky;
		private int[] lat;
		private int[] lon;
		private short[] kod;
		private int pocet;

		Tabulka(final int ocekavanyPocet) {
			int kapacita = 16;
			while (kapacita < ocekavanyPocet / 0.65 + 1) {
				kapacita <<= 1;
			}
			otisky = new long[kapacita];
			lat = new int[kapacita];
			lon = new int[kapacita];
			kod = new short[kapacita];
		}

		int pocet() {
			return pocet;
		}

		private int misto(final long otisk) {
			final int maska = otisky.length - 1;
			int i = (int) (otisk ^ otisk >>> 32) & maska;
			while (otisky[i] != 0 && otisky[i] != otisk) {
				i = i + 1 & maska;
			}
			return i;
		}

		void vloz(final long otisk, final int mikroLat, final int mikroLon, final short klasifikace) {
			if ((pocet + 1) * 100L > otisky.length * 70L) {
				zvetsi();
			}
			final long o = otisk == 0 ? 1 : otisk;
			final int i = misto(o);
			if (otisky[i] == 0) {
				pocet++;
			}
			otisky[i] = o;
			lat[i] = mikroLat;
			lon[i] = mikroLon;
			kod[i] = klasifikace;
		}

		/** Klasifikace uložená pro keš s tímto otiskem a těmito souřadnicemi; jinak {@link Short#MIN_VALUE} jako „není“. */
		int najdi(final long otisk, final int mikroLat, final int mikroLon) {
			final long o = otisk == 0 ? 1 : otisk;
			final int i = misto(o);
			if (otisky[i] == o && lat[i] == mikroLat && lon[i] == mikroLon) {
				return kod[i];
			}
			return NENI;
		}

		static final int NENI = Integer.MIN_VALUE;

		private void zvetsi() {
			final Tabulka nova = new Tabulka(otisky.length);
			for (int i = 0; i < otisky.length; i++) {
				if (otisky[i] != 0) {
					nova.vloz(otisky[i], lat[i], lon[i], kod[i]);
				}
			}
			otisky = nova.otisky;
			lat = nova.lat;
			lon = nova.lon;
			kod = nova.kod;
		}

		private void zapis(final DataOutputStream out) throws IOException {
			for (int i = 0; i < otisky.length; i++) {
				if (otisky[i] != 0) {
					out.writeLong(otisky[i]);
					out.writeInt(lat[i]);
					out.writeInt(lon[i]);
					out.writeShort(kod[i]);
				}
			}
		}
	}

	private SouborPredpoctu() {}

	/** Otisk kódu keše (FNV-1a, 64 bitů). */
	static long otisk(final String id) {
		long h = 0xcbf29ce484222325L;
		for (int i = 0; i < id.length(); i++) {
			h ^= id.charAt(i);
			h *= 0x100000001b3L;
		}
		return h;
	}

	static int mikrostupne(final double stupne) {
		return (int) Math.round(stupne * 1e6);
	}

	/** Načte tabulku; null, když soubor není, je poškozený nebo patří k jinému klíči. */
	static Tabulka nacti(final Path soubor, final String klic) {
		if (!Files.isRegularFile(soubor)) {
			return null;
		}
		final CRC32 crc = new CRC32();
		try (DataInputStream in = new DataInputStream(new java.util.zip.CheckedInputStream(new BufferedInputStream(Files.newInputStream(soubor), 1 << 16), crc))) {
			if (in.readInt() != ZNACKA || in.readInt() != FORMAT) {
				return null;
			}
			if (!klic.equals(in.readUTF())) {
				return null;
			}
			final int pocet = in.readInt();
			// Velikost souboru musí odpovídat počtu záznamů, ještě než se pro ně cokoli alokuje; poškozená hlavička tak nemůže vyžádat obří tabulku.
			if (pocet < 0 || pocet > NEJVIC_ZAZNAMU || Files.size(soubor) != hlavicka(klic) + (long) pocet * BAJTU_ZAZNAMU + 4) {
				return null;
			}
			final Tabulka t = new Tabulka(pocet);
			for (int i = 0; i < pocet; i++) {
				final long o = in.readLong();
				final int la = in.readInt();
				final int lo = in.readInt();
				final short k = in.readShort();
				if (o == 0) {
					return null;
				}
				t.vloz(o, la, lo, k);
			}
			final long spocteno = crc.getValue();
			final int ulozeno = in.readInt();
			if ((int) spocteno != ulozeno || t.pocet != pocet) {
				return null;
			}
			return t;
		} catch (final EOFException e) {
			log.debug("Předpočet {} je useknutý", soubor);
			return null;
		} catch (final IOException e) {
			log.debug("Předpočet {} nejde přečíst: {}", soubor, e.toString());
			return null;
		}
	}

	private static long hlavicka(final String klic) {
		return 4 + 4 + 2 + delkaUtf(klic) + 4;
	}

	/** Počet bajtů, které zabere řetězec v {@link DataOutputStream#writeUTF}. */
	private static long delkaUtf(final String text) {
		long n = 0;
		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			n += c >= 1 && c <= 0x7F ? 1 : c <= 0x7FF ? 2 : 3;
		}
		return n;
	}

	/** Soubor začíná značkou a verzí formátu předpočtu, tedy je to opravdu soubor, který tento kód zapsal. */
	static boolean maZnacku(final Path soubor) {
		try (DataInputStream in = new DataInputStream(Files.newInputStream(soubor))) {
			return in.readInt() == ZNACKA && in.readInt() == FORMAT;
		} catch (final IOException e) {
			return false;
		}
	}

	/** Zapíše tabulku přes dočasný soubor a atomické přejmenování, ať poškozený zápis nezničí platný soubor. */
	static void zapis(final Path soubor, final String klic, final Tabulka tabulka) throws IOException {
		final Path rodic = soubor.toAbsolutePath().getParent();
		if (rodic != null) {
			Files.createDirectories(rodic);
		}
		final Path docasny = soubor.resolveSibling(soubor.getFileName() + ".new");
		final CRC32 crc = new CRC32();
		try (DataOutputStream out = new DataOutputStream(new java.util.zip.CheckedOutputStream(new BufferedOutputStream(Files.newOutputStream(docasny), 1 << 16), crc))) {
			out.writeInt(ZNACKA);
			out.writeInt(FORMAT);
			out.writeUTF(klic);
			out.writeInt(tabulka.pocet);
			tabulka.zapis(out);
			out.flush();
			out.writeInt((int) crc.getValue());
		} catch (final IOException e) {
			Files.deleteIfExists(docasny);
			throw e;
		}
		Files.move(docasny, soubor, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
	}
}

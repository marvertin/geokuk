package cz.geokuk.plugins.kesoid.importek;

import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;

/**
 * @author veverka
 *
 */
public class NacitacGeokuk extends NacitacInputStream0 {

	/**
	 *
	 */
	private static final String HLAVICKA = "*geokuk:exportversion=2";

	@Override
	protected void nacti(final InputStream aIstm, final String name, final IImportBuilder builder, final Future<?> future) throws IOException {
		final BufferedReader rdr = new BufferedReader(new InputStreamReader(aIstm, Charset.forName("UTF8")));

		String hlavicka = rdr.readLine();
		if (hlavicka.charAt(0) == 0xfeff) {
			hlavicka = hlavicka.substring(1);
		}
		System.out.println(Integer.toHexString(hlavicka.charAt(0)));
		if (!HLAVICKA.equals(hlavicka))
			throw new RuntimeException("Přečtena hlavička: \"" + hlavicka + "\", ale má tam být\"" + HLAVICKA + "\"");
		String line;
		int linenumber = 1; // máme přeci přečtnou hlavičku
		int ncaches = 0;
		int nwpts = 0;
		String jmenoAktualniKese = null;
		// AWptType xxxxxxxx = AWptType.TRAILHEAD;

		GpxWpt gpxwpt = null;
		while ((line = rdr.readLine()) != null) {
			if (future != null && future.isCancelled())
				return;
			try {
				linenumber++;
				line = line.trim();
				if (line.length() == 0) {
					continue;
				}
				final char cmd = line.charAt(0);
				final String[] pp = line.substring(1).split("\\|", 19);

				switch (cmd) {
				case ':': {
					gpxwpt = new GpxWpt();
					gpxwpt.groundspeak = new Groundspeak();
					jmenoAktualniKese = pp[0];
					gpxwpt.groundspeak.type = pp[1].intern();
					gpxwpt.groundspeak.container = pp[2];
					gpxwpt.groundspeak.difficulty = pp[3].intern();
					gpxwpt.groundspeak.terrain = pp[4].intern();
					gpxwpt.groundspeak.archived = Boolean.parseBoolean(pp[5]);
					gpxwpt.groundspeak.availaible = !Boolean.parseBoolean(pp[6]);
					gpxwpt.explicitneUrcenoVlastnictvi = Boolean.parseBoolean(pp[8]);
					gpxwpt.sym = Boolean.parseBoolean(pp[7]) ? KesoidImportBuilder.GEOCACHE_FOUND : KesoidImportBuilder.GEOCACHE;
					gpxwpt.groundspeak.owner = pp[9].trim().intern();
					gpxwpt.groundspeak.placedBy = pp[9].trim().intern();
					gpxwpt.time = pp[10];
					// kes.setCountry(pp[11].intern());
					// kes.setState(pp[12].intern());
					gpxwpt.gpxg.bestOf = parseCislo(pp[13]);
					gpxwpt.gpxg.hodnoceni = parseCislo(pp[14]);
					gpxwpt.gpxg.hodnoceniPocet = parseCislo(pp[15]);
					gpxwpt.gpxg.znamka = parseCislo(pp[16]);
					gpxwpt.groundspeak.encodedHints = pp[17];
					gpxwpt.link.href = pp[18];
					break;
				}
				case '-': {
					if (gpxwpt == null) {
						gpxwpt = new GpxWpt();
					}
					gpxwpt.name = pp[0] + jmenoAktualniKese.substring(2);
					if (gpxwpt.sym == null) {
						gpxwpt.sym = pp[1];
					}
					gpxwpt.wgs = new Wgs(Double.parseDouble(pp[2]), Double.parseDouble(pp[3]));
					final String nazev = pp.length > 4 ? pp[4] : "";
					gpxwpt.cmt = nazev;
					if (gpxwpt.groundspeak != null) {
						gpxwpt.groundspeak.name = nazev;
					}
					builder.addGpxWpt(gpxwpt);
					gpxwpt = null;
					nwpts++;
					break;
				}
				case '/':
					ncaches++;
					break;
				case '*': // ignorovat druhou hlavičku
					break;
				}
			} catch (final Exception e) {
				final String positionInfo = "ERORR on line=" + linenumber + " while reading cache " + jmenoAktualniKese;
				FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, positionInfo);
			}
		}
		System.out.println("Precteno: " + ncaches + " keší a " + nwpts + " waypointů.");
	}

	@Override
	boolean umiNacist(final ZipEntry zipEntry) {
		return umiNacist(zipEntry.getName());
	}

	@Override
	boolean umiNacist(final File file) {
		return umiNacist(file.getName());
	}

	private boolean umiNacist(final String resourceName) {
		return resourceName.toLowerCase().trim().endsWith(".geokuk");
	}
}

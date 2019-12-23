package cz.geokuk.plugins.cesty;

import java.io.*;
import java.util.*;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.core.program.FConst;
import cz.geokuk.plugins.cesty.data.*;
import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.importek.NacitacGpx;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CestyZperzistentnovac {


	private int smimCist;

	private final Updator updator = new Updator();

	public boolean smimCist() {
		return smimCist == 0;
	}

	List<Cesta> nacti(final List<File> files, final KesBag kesBag) {
		final List<Cesta> cesty = new ArrayList<>();
		for (final File file : files) {
			try {
				log.debug("Nacitam z: " + file);
				final String pureName = file.getName().toLowerCase();
				if (pureName.endsWith(".ggt")) {
					final Ggt ggt = loadGgt(file);
					final Cesta cesta = zbuildujCestuZGgt(ggt, kesBag);
					cesty.add(cesta);
				} else if (pureName.endsWith(".gpx")) {
					final DocImportBuilder builder = new DocImportBuilder();
					builder.init();
					final InputStream istm = new BufferedInputStream(new FileInputStream(file));
					final NacitacGpx nacitac = new NacitacGpx();
					nacitac.nacti(istm, file.toString(), builder, null);
					builder.done();
					cesty.addAll(builder.getCesty());
				}
			} catch (final Exception e) {
				throw new RuntimeException("Problém se souborem: \"" + file + "\"", e);
			}
		}
		pripniNaWayponty(cesty, kesBag);
		return cesty;
	}

	void pripniNaWayponty(final Iterable<Cesta> cesty, final KesBag kesBag) {
		for (final Cesta cesta : cesty) {
			for (final Bod bod : cesta.getBody()) {
				final Mou mou = bod.getMou();
				final Wpt wpt = najdiExtremneBlizouckyWpt(mou, kesBag);
				updator.setMouableButNoChange(bod, wpt != null ? wpt : mou);
				if (wpt != null) {
					wpt.invalidate();
				}
			}
		}
	}

	void zapisGgt(final Doc doc, final File file) {
		BufferedWriter wrt = null;
		smimCist++;
		final Set<String> exportovano = new HashSet<>();
		try {
			try {
				wrt = new BufferedWriter(new FileWriter(file));
				for (final Bod bod : doc.getBody()) {
					final Mouable mouable = bod.getMouable();
					if (mouable instanceof Wpt) {
						final Wpt wpt = (Wpt) mouable;
						zapisKdyzNeni(wrt, wpt.getName(), exportovano);
						zapisKdyzNeni(wrt, wpt.getKesoid().getIdentifier(), exportovano);
					}

				}
				wrt.close();
			} catch (final IOException e) {
				if (wrt != null) {
					try {
						wrt.close();
					} catch (final IOException e1) { // co s tím jiného
					}
				}
				throw new RuntimeException(e);
			}
		} finally {
			smimCist--;
		}
	}

	private Ggt loadGgt(final BufferedReader reader) throws IOException {
		String line;
		final Set<String> set = new HashSet<>();
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0) {
				continue;
			}
			set.add(line);
		}
		final Ggt vyletPul = new Ggt(set);
		return vyletPul;
	}

	private Ggt loadGgt(final File file) throws IOException {
		FileReader filere = null;
		try {
			filere = new FileReader(file);
			final BufferedReader br = new BufferedReader(filere);
			return loadGgt(br);
		} catch (final FileNotFoundException e) {
			// FExceptionDumper.dump(e, EExceptionSeverity.CATCHE, "Nacitani vyletu.");
			return new Ggt(new HashSet<String>());
		} finally {
			if (filere != null) {
				filere.close();
			}
		}
	}

	private Wpt najdiExtremneBlizouckyWpt(final Mou mou, final KesBag kesBag) {
		if (kesBag == null) {
			return null;
		}
		final Indexator<Wpt> indexator = kesBag.getIndexator();
		final BoundingRect br = new BoundingRect(mou.xx, mou.yy, mou.xx, mou.yy).rozsir(100);
		return indexator.bound(br).locateAnyOne().orElse(null);
	}

	private void zapisKdyzNeni(final BufferedWriter wrt, final String kod, final Set<String> exportovano) throws IOException {
		if (kod == null) {
			return;
		}
		if (exportovano.add(kod)) {
			wrt.write(String.format("%s%s", kod, FConst.NL));
			// } else {
			// wrt.write(String.format("NEBERU %s%s", kod, FConst.NL));
		}
	}

	private Cesta zbuildujCestuZGgt(final Ggt ggt, final KesBag kesBag) {
		final Cesta cesta = Cesta.create();
		if (kesBag != null) {
			for (final Kesoid kesoid : kesBag.getKesoidy()) {
				if (ggt.kesides.contains(kesoid.getIdentifier())) {
					updator.pridejNaMisto(cesta, kesoid.getMainWpt());
				}
			}
		}
		return cesta;
	}
}

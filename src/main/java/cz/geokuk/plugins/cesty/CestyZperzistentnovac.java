package cz.geokuk.plugins.cesty;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.core.program.FConst;
import cz.geokuk.plugins.cesty.data.Bod;
import cz.geokuk.plugins.cesty.data.Cesta;
import cz.geokuk.plugins.cesty.data.Doc;
import cz.geokuk.plugins.cesty.data.Updator;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.importek.NacitacGpx;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;
import cz.geokuk.util.index2d.Sheet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CestyZperzistentnovac {

	private static final Logger log = LogManager.getLogger(CestyZperzistentnovac.class.getSimpleName());

	private int smimCist;

	private final Updator updator = new Updator();

	private Ggt loadGgt(File file) throws IOException {
		FileReader filere = null;
		try {
			filere = new FileReader(file);
			BufferedReader br = new BufferedReader(filere);
			return loadGgt(br);
		} catch (FileNotFoundException e) {
			//FExceptionDumper.dump(e, EExceptionSeverity.CATCHE, "Nacitani vyletu.");
			return new Ggt(new HashSet<String>());
		} finally {
			if (filere != null) {
				filere.close();
			}
		}
	}

	private Ggt loadGgt(BufferedReader reader) throws IOException {
		String line;
		Set<String> set = new HashSet<>();
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0) {
				continue;
			}
			set.add(line);
		}
		Ggt vyletPul = new Ggt(set);
		return vyletPul;
	}


	void zapisGgt(Doc doc, File file) {
		BufferedWriter wrt = null;
		smimCist++;
		Set<String> exportovano = new HashSet<>();
		try {
			try {
				wrt = new BufferedWriter(new FileWriter(file));
				for (Bod bod : doc.getBody()) {
					Mouable mouable = bod.getMouable();
					if (mouable instanceof Wpt) {
						Wpt wpt = (Wpt) mouable;
						zapisKdyzNeni(wrt, wpt.getName(), exportovano);
						zapisKdyzNeni(wrt, wpt.getKesoid().getIdentifier(), exportovano);
					}

				}
				wrt.close();
			} catch (IOException e) {
				if (wrt != null) {
					try {
						wrt.close();
					} catch (IOException e1) { // co s tím jiného
					}
				}
				throw new RuntimeException(e);
			}
		} finally {
			smimCist--;
		}
	}

	private void zapisKdyzNeni(BufferedWriter wrt, String kod, Set<String> exportovano) throws IOException {
		if (kod == null) return;
		if (exportovano.add(kod)) {
			wrt.write(String.format("%s%s", kod, FConst.NL));
			//    } else {
			//      wrt.write(String.format("NEBERU %s%s", kod, FConst.NL));
		}
	}


	List<Cesta> nacti(List<File> files, KesBag kesBag) {
		List<Cesta> cesty = new ArrayList<>();
		for (File file : files) {
			try {
				log.debug("Nacitam z: " + file);
				String pureName = file.getName().toLowerCase();
				if (pureName.endsWith(".ggt")) {
					Ggt ggt = loadGgt(file);
					Cesta cesta = zbuildujCestuZGgt(ggt, kesBag);
					cesty.add(cesta);
				} else if (pureName.endsWith(".gpx")) {
					DocImportBuilder builder = new DocImportBuilder();
					InputStream istm = new BufferedInputStream(new FileInputStream(file));
					NacitacGpx nacitac = new NacitacGpx();
					nacitac.nacti(istm, file.toString(), builder, null);
					cesty.addAll(builder.getCesty());
				}
			} catch (Exception e) {
				throw new RuntimeException("Problém se souborem: \"" + file + "\"", e);
			}
		}
		pripniNaWayponty(cesty, kesBag);
		return cesty;
	}


	private Cesta zbuildujCestuZGgt(Ggt ggt, KesBag kesBag) {
		Cesta cesta = Cesta.create();
		if (kesBag != null) {
			for (Kesoid kesoid : kesBag.getKesoidy()) {
				if (ggt.kesides.contains(kesoid.getIdentifier())) {
					updator.pridejNaMisto(cesta, kesoid.getMainWpt());
				}
			}
		}
		return cesta;
	}

	void pripniNaWayponty(Iterable<Cesta> cesty, KesBag kesBag) {
		for (Cesta cesta : cesty) {
			for (Bod bod : cesta.getBody()) {
				Mou mou = bod.getMou();
				Wpt wpt = najdiExtremneBlizouckyWpt(mou, kesBag);
				updator.setMouableButNoChange(bod, wpt != null ? wpt : mou);
				if (wpt != null) {
					wpt.invalidate();
				}
			}
		}
	}


	private Wpt najdiExtremneBlizouckyWpt(Mou mou, KesBag kesBag) {
		if (kesBag == null) return null;
		Indexator<Wpt> indexator = kesBag.getIndexator();
		BoundingRect br = new BoundingRect(mou.xx, mou.yy, mou.xx, mou.yy).rozsir(100);
		Sheet<Wpt> sheet = indexator.locateAnyOne(br);
		if (sheet == null) return null;
		return sheet.get();
	}

	public boolean smimCist() {
		return smimCist == 0;
	}
}

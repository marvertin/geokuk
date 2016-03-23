package cz.geokuk.plugins.kesoid.mapicon;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.api.mapicon.Drawer0;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;

public class IconDefNacitac {

	/**
	 *
	 */

	private static final Logger		log	= LogManager.getLogger(IconDefNacitac.class.getSimpleName());

	private final String			jmenoSPriponou;
	// private IconDef iconDef;

	IkonDrawingProperties			idp;
	// TODO : The alelas should have a more generic name
	private static Pattern			pat	= Pattern.compile("([a-z0-9]+!)*([^_]*)((?:_[ěščřžýáíéóúůďťňĎŇŤŠČŘŽÝÁÍÉÚŮa-zA-z -]+)*)(_x-?[0-9]+)*(_y-?[0-9]+)*(_p[0-9])*\\.([a-z]+)");
	private final URL				url;

	private final Genom				genom;
	private final IkonNacitacSada	iIkonNacitacSada;

	public IconDefNacitac(Genom genom, String jmenoSPriponou, URL url, IkonNacitacSada aIkonNacitacSada) {
		this.jmenoSPriponou = jmenoSPriponou;
		this.url = url;
		this.genom = genom;
		iIkonNacitacSada = aIkonNacitacSada;
	}

	public IconDef loadIconDef(ImagantCache imagantCache) {
		try {
			return load(imagantCache);
		} catch (IOException e) {
			FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Selhalo čtení obrázku ikony, tak obrázek nemůžeme použít");
			log.error("Selhalo čtení obrázku ikony, tak obrázek nemůžeme použít", e);
			return null;
		}
	}

	private IconDef load(ImagantCache imagantCache) throws IOException {
		idp = new IkonDrawingProperties();
		idp.url = url;
		String machovanec = jmenoSPriponou.startsWith("_.") ? jmenoSPriponou.substring(1) : jmenoSPriponou;
		Matcher mat = pat.matcher(machovanec);
		if (!mat.matches()) {
			log.error("Jméno \"" + jmenoSPriponou + "\" nevyhovuje regulárnímu výrazu: " + pat);
			return null;
		}
		String grupaName = mat.group(1);

		String wptsym = odstranZazavorkovaneNesouboroviteZnaky(mat.group(2));

		Set<Alela> alely = nactiAlely(mat.group(3));
		Alela alelaSym = null;
		if (wptsym != null && wptsym.trim().length() > 0) {
			alelaSym = genom.alelaSym(wptsym, grupaName);
			alelaSym.getGrupa().setDisplayName(iIkonNacitacSada.getGroupDisplayName(grupaName));
			alely.add(alelaSym);
		}
		// rozmnoz(alely, sese);
		String sufix = mat.group(7);
		if (isProperties(sufix)) {
			nactiObrazekDefinovanyVPropertach();
		} else { // zkusíme to považovat za obrázek
			otestujAVydefinujSkutecnyObrazek(imagantCache);
		}

		idp.xoffset = zpracujNaOffsete(mat.group(4));
		idp.yoffset = zpracujNaOffsete(mat.group(5));
		int priorita = zpracujNaPrioritu(mat.group(6));

		IconDef iconDef = new IconDef();
		iconDef.setAlelyx(alely);
		iconDef.setAlelaSym(alelaSym); // nastavit, aby se podle ní dalo rychle filtrovat, může být i null, pak je to bez alely a filtruje se vždy.
		iconDef.idp = idp;
		iconDef.priorita = priorita;

		return iconDef;
	}

	/**
	 * @param aGroup
	 * @return
	 */
	private String odstranZazavorkovaneNesouboroviteZnaky(String s) {
		if (s.indexOf('[') < 0)
			return s;
		s = s.replace("[lomitko]", "/");
		s = s.replace("[hvezdicka]", "*");
		s = s.replace("[uvozovky]", "*");
		s = s.replace("[otaznik]", "*");
		s = s.replace("[dvojtecka]", "*");
		return s;
	}

	private Set<Alela> nactiAlely(String alelygroup) {
		Set<Alela> alely = new HashSet<>();
		for (String s : alelygroup.split("_")) {
			if (s.isEmpty()) {
				continue;
			}
			Alela alela;
			int pozminus = s.indexOf('-');
			if (pozminus < 0) { // žádné mínus, alela musí existovat
				alela = genom.seekAlela(s);
			} else {
				String alelaName = s.substring(pozminus + 1);
				String genName = s.substring(0, pozminus);
				alela = genom.alela(alelaName, genName);
				if (alela == null)
					continue;
			}
			alely.add(alela);
		}
		return alely;
	}

	private int zpracujNaPrioritu(String zadano) {
		if (zadano == null)
			return 5;
		return Integer.parseInt(zadano.substring(2));
	}

	private int zpracujNaOffsete(String zadano) {
		if (zadano == null)
			return 0;
		int zadanapozice = Integer.parseInt(zadano.substring(2));
		return zadanapozice;
	}

	private void nactiObrazekDefinovanyVPropertach() throws IOException {
		Properties prop = new Properties();
		prop.load(new BufferedInputStream(idp.url.openStream()));
		// idp.width = Integer.parseInt(prop.getProperty("width"));
		// idp.height= Integer.parseInt(prop.getProperty("height"));
		idp.properties = prop;
		String className = prop.getProperty("class");
		try {
			idp.vykreslovac = (Drawer0) Class.forName(className).newInstance();
			naplnVykreslovac(idp.vykreslovac);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void otestujAVydefinujSkutecnyObrazek(ImagantCache imagantCache) throws IOException {
		// @SuppressWarnings("unused") // jen pro kontrolu
		// BufferedImage bi = ImageIO.read(idp.url);
		// idp.width = bi.getWidth();
		// idp.height = bi.getHeight();
		idp.properties = new Properties();
		idp.vykreslovac = new DefaultVykreslovac(imagantCache);
		naplnVykreslovac(idp.vykreslovac);
	}

	/**
	 * @param aVykreslovac
	 */
	private void naplnVykreslovac(Drawer0 vykreslovac) {
		vykreslovac.setIdp(idp);
	}

	private boolean isProperties(String sufix) {
		return sufix.equals("properties");
	}

}

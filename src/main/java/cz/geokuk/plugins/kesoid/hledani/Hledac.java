/**
 *
 */
package cz.geokuk.plugins.kesoid.hledani;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.geokuk.core.hledani.Hledac0;
import cz.geokuk.core.hledani.HledaciPodminka0;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.util.lang.FUtil;

/**
 * @author Martin Veverka
 *
 */
public class Hledac extends Hledac0<Nalezenec> {

	private class Porovnavac {
		private final String vzorek;
		private final boolean regularniVyraz;

		private final Pattern pat;

		/**
		 * @param aVzorek
		 * @param aRegularniVyraz
		 */
		public Porovnavac(final String aVzorek, final boolean aRegularniVyraz) {
			super();
			vzorek = FUtil.cestinuPryc(aVzorek.toLowerCase());
			regularniVyraz = aRegularniVyraz;
			pat = regularniVyraz ? Pattern.compile(vzorek, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE) : null;
		}

		public Nalezenec porovnej(final String vCem) {
			if (regularniVyraz) {
				final Matcher mat = pat.matcher(FUtil.cestinuPryc(vCem));
				final boolean found = mat.find();
				if (found) {
					final Nalezenec nal = new Nalezenec();
					nal.setKdeNalezeno(vCem);
					nal.setPoc(mat.start());
					nal.setKon(mat.end());
					return nal;
				} else {
					return null;
				}
			} else {
				final int poz = FUtil.cestinuPryc(vCem.toLowerCase()).indexOf(vzorek);
				if (poz >= 0) {
					final Nalezenec nal = new Nalezenec();
					nal.setKdeNalezeno(vCem);
					nal.setPoc(poz);
					nal.setKon(poz + vzorek.length());
					return nal;
				} else {
					return null;
				}
			}
		}

	}

	private final KesBag kesBag;

	/**
	 * @param aKl
	 */
	public Hledac(final KesBag kesBag) {
		super();
		this.kesBag = kesBag;
	}

	@Override
	public List<Nalezenec> hledej(final HledaciPodminka0 podm) {
		System.out.println("Hledy, hledy, hledy: " + kesBag.getKesoidy().size() + " " + podm.getVzorek());
		final Porovnavac poro = new Porovnavac(podm.getVzorek(), ((HledaciPodminka) podm).isRegularniVyraz());
		final List<Nalezenec> list = new ArrayList<>();
		for (final Kesoid kesoid : kesBag.getKesoidy()) {
			if (getFuture() != null && getFuture().isCancelled()) {
				return null;
			}
			final String[] prohledavanci = kesoid.getProhledavanci();
			Nalezenec nal = null;
			for (final String prohledavanec : prohledavanci) {
				if (prohledavanec != null) {
					nal = poro.porovnej(prohledavanec);
					if (nal != null) {
						break;
					}
				}
			}
			if (nal != null) {
				nal.setKes(kesoid);
				if (nal.getPoc() == nal.getKon()) {
					nal.setKdeNalezeno(null);
				}
				list.add(nal);
			}
		}
		return list;
	}

	// public static void main(String[] args) {
	// System.out.println(FUtil.cestinuPryc("Příliš žluťoučký kůň úpěl ďábelské ódy"));
	// System.out.println(FUtil.cestinuPryc("Tady neni cestina"));
	// System.out.println(FUtil.cestinuPryc("Tady je čeština"));
	// }
}

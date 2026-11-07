package cz.geokuk.plugins.challenge;

/**
 * Co se na mapě zvýrazňuje podle výběru: sektor 360° nebo políčko D/T a keš, podle které se vybralo (jinak null). Neměnné; spočte se z výběru v modelu a z keše, kterou vybral program.
 */
final class VyberZvyrazneni {

	static final VyberZvyrazneni NIC = new VyberZvyrazneni(-1, null, null);

	/** Sektor 0 až 359, nebo -1. */
	final int sektor;
	/** Štítek políčka D/T („D3/T4“), nebo null. */
	final String stitek;
	/** Keš, podle které se vybralo (klik na mapě); null, když se vybral sektor nebo políčko jinak. */
	final VybranaKes kes;

	VyberZvyrazneni(final int sektor, final String stitek, final VybranaKes kes) {
		this.sektor = sektor;
		this.stitek = stitek;
		this.kes = kes;
	}

	boolean jeNic() {
		return sektor < 0 && stitek == null;
	}

	/**
	 * Výběr podle toho, co se změnilo naposledy: keš, kterou vybral program, nebo sektor či políčko z modelu (◀ ▶, seznam v popupu). U 360° bez nich platí sektor vybraného
	 * kandidáta {@code sektorKandidata}. Keš mimo challenge (nemá sektor nebo políčko) se přeskočí.
	 */
	static VyberZvyrazneni vyres(final PrehledChallenge prehled, final ChallengeModel model, final long casVyberuModelu, final int sektorKandidata) {
		final Challenge c = prehled.challenge();
		final VybranaKes kes = model.vybranaKes();
		final boolean kesNovejsi = kes != null && model.casVyberuKese() > casVyberuModelu;
		switch (c.typ()) {
		case SEKTORY_360: {
			if (kesNovejsi) {
				final int s = prehled.sektorBodu(kes.lat(), kes.lon());
				if (s >= 0) {
					return new VyberZvyrazneni(s, null, kes);
				}
			}
			final int s = model.vybranySektor(c);
			return s >= 0 ? new VyberZvyrazneni(s, null, null) : sektorKandidata >= 0 ? new VyberZvyrazneni(sektorKandidata, null, null) : NIC;
		}
		case DT_MATICE: {
			if (kesNovejsi) {
				final String s = prehled.stitekPolicka(kes.obtiznostPulek(), kes.terenPulek());
				if (s != null) {
					return new VyberZvyrazneni(-1, s, kes);
				}
			}
			final String s = model.vybranePolicko(c);
			return s != null ? new VyberZvyrazneni(-1, s, null) : NIC;
		}
		default:
			return NIC;
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof VyberZvyrazneni)) {
			return false;
		}
		final VyberZvyrazneni v = (VyberZvyrazneni) o;
		return sektor == v.sektor && java.util.Objects.equals(stitek, v.stitek) && java.util.Objects.equals(kes, v.kes);
	}

	@Override
	public int hashCode() {
		return sektor * 31 + (stitek == null ? 0 : stitek.hashCode());
	}
}

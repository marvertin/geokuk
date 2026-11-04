package cz.geokuk.plugins.challenge;

/** Stav předpočtu pro rozhraní: neběží, počítá {@code hotovo} z {@code celkem}, hotovo, zrušeno nebo chyba. */
public final class StavPredpoctu {

	public enum Faze {
		NEBEZI, POCITA, HOTOVO, ZRUSENO, CHYBA
	}

	public static final StavPredpoctu NEBEZI = new StavPredpoctu(Faze.NEBEZI, 0, 0, null);

	private final Faze faze;
	private final long hotovo;
	private final long celkem;
	private final String chyba;

	StavPredpoctu(final Faze faze, final long hotovo, final long celkem, final String chyba) {
		this.faze = faze;
		this.hotovo = hotovo;
		this.celkem = celkem;
		this.chyba = chyba;
	}

	public Faze faze() {
		return faze;
	}

	/** Kolik keší je zpracováno. */
	public long hotovo() {
		return hotovo;
	}

	/** Kolik keší se zpracuje celkem; 0, dokud není známo. */
	public long celkem() {
		return celkem;
	}

	/** Zpráva o chybě, jen ve fázi {@link Faze#CHYBA}. */
	public String chyba() {
		return chyba;
	}

	/** Podíl hotového 0 až 1; 1 ve fázi hotovo. */
	public double podil() {
		if (faze == Faze.HOTOVO) {
			return 1;
		}
		return celkem == 0 ? 0 : Math.min(1.0, (double) hotovo / celkem);
	}

	@Override
	public String toString() {
		return faze + " " + hotovo + "/" + celkem + (chyba == null ? "" : " " + chyba);
	}
}

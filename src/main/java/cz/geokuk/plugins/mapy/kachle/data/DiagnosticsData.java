package cz.geokuk.plugins.mapy.kachle.data;

/**
 * Sbírá postupně diagnostická data, je možné přibalovat. Ni nevyhodnocuje, tkže je rychlé, vyhonocuje až toString při vypisování.
 *
 * @author Martin Veverka
 *
 */
public class DiagnosticsData {
	public interface Listener {
		public void send(DiagnosticsData data, String faze);
	}

	private final DiagnosticsData parent;
	private final String nazev;
	private final Object dato;

	private final Listener listener;

	public static DiagnosticsData create(final String nazev, final Object dato, final Listener listener) {
		return new DiagnosticsData(null, nazev, dato, listener);
	}

	private DiagnosticsData(final DiagnosticsData parent, final String nazev, final Object dato, final Listener listener) {
		this.parent = parent;
		this.nazev = nazev;
		this.dato = dato;
		this.listener = listener;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DiagnosticsData other = (DiagnosticsData) obj;
		if (dato == null) {
			if (other.dato != null) {
				return false;
			}
		} else if (!dato.equals(other.dato)) {
			return false;
		}
		if (nazev == null) {
			if (other.nazev != null) {
				return false;
			}
		} else if (!nazev.equals(other.nazev)) {
			return false;
		}
		return true;
	}

	public Object getDato() {
		return dato;
	}

	public String getNazev() {
		return nazev;
	}

	public DiagnosticsData getParent() {
		return parent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (dato == null ? 0 : dato.hashCode());
		result = prime * result + (nazev == null ? 0 : nazev.hashCode());
		return result;
	}

	/**
	 * Pošle diagnostiku do listenera
	 *
	 * @param faze
	 */
	public void send(final String faze) {
		if (listener != null) {
			listener.send(this, faze);
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("{");
		addToString(sb);
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Přidá další položku do diagnostickýc dat.
	 *
	 * @param nazev
	 * @param dato
	 * @return
	 */
	public DiagnosticsData with(final String nazev, final Object dato) {
		return new DiagnosticsData(this, nazev, dato, listener);
	}

	private void addToString(final StringBuilder sb) {
		if (parent != null) {
			parent.addToString(sb);
			sb.append("; ");
		}
		if (nazev != null || dato != null) {
			if (nazev != null) {
				sb.append(nazev);
				sb.append("=");
			}
			sb.append(dato);
		}
	}
}
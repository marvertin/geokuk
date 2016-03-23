package cz.geokuk.plugins.mapy.kachle;

import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloMalo;

abstract class Ka0Req {

	private static Pocitadlo pocitadloInstanci = new PocitadloMalo("Počet instancí požadavků na dlaždice.", "Počítá, kolik existuje instancí " + Ka0Req.class.getName() + ".");

	private final Ka0 ka;

	private final Priority priorita;

	protected Ka0Req(final Ka0 ka, final Priority priorita) {
		super();
		this.ka = ka;
		this.priorita = priorita;
		pocitadloInstanci.inc();
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
		final Ka0Req other = (Ka0Req) obj;
		if (ka == null) {
			if (other.ka != null) {
				return false;
			}
		} else if (!ka.equals(other.ka)) {
			return false;
		}
		if (priorita != other.priorita) {
			return false;
		}
		return true;
	}

	public Ka0 getKa() {
		return ka;
	}

	public Priority getPriorita() {
		return priorita;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (ka == null ? 0 : ka.hashCode());
		result = prime * result + (priorita == null ? 0 : priorita.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return ka.toString();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		pocitadloInstanci.dec();
	}

}

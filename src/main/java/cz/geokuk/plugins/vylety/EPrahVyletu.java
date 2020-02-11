package cz.geokuk.plugins.vylety;

public enum EPrahVyletu {
	VSECHNY("Všechny"), BEZ_VYNECHANYCH("Bez vynechaných"), JEN_LOVENE("Jen lovené"),;

	private final String doKomboBoxu;

	EPrahVyletu(final String doKomboBoxu) {
		this.doKomboBoxu = doKomboBoxu;
	}

	@Override
	public String toString() {
		return doKomboBoxu;

	}
}

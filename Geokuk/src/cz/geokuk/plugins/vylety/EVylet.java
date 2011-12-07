package cz.geokuk.plugins.lovim;

public enum EVylet {
	NE ("Všechny"),
	NEVIM ("Bez vynechaných"),
	ANO ("Jen lovené"),
	;
	
	private final String doKomboBoxu;
	
	EVylet(String doKomboBoxu) {
		this.doKomboBoxu = doKomboBoxu;
	}

	public String toString() {
		return doKomboBoxu;
		
	}
}

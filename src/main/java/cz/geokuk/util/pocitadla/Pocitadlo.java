/**
 *
 */
package cz.geokuk.util.pocitadla;

import javax.swing.SwingUtilities;

/**
 * počítadlo čehokoli, ale jeho smyslem je počítat technické záležitosti, jak jsou dély front a podobně.
 *
 * Předpokládá se, že to budou singletony ve statických proměnných
 * 
 * @author veverka
 *
 */
public abstract class Pocitadlo {

	public static Callback	callback;

	private int				val;
	private final String	name;
	private final String	description;
	private final String	textovyPopisTypu;

	/**
	 * @param aName
	 * @param aDescription
	 */
	public Pocitadlo(String aName, String aDescription, String textovyPopisTypu) {
		super();
		name = aName;
		description = aDescription;
		this.textovyPopisTypu = textovyPopisTypu;
		SpravcePocitadel.register(this);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	public synchronized int get() {
		return val;
	}

	public synchronized void set(int val) {
		synchronized (this) {
			if (this.val == val)
				return;
			this.val = val;
		}
		if (callback != null) {
			// System.out.println("Pocitani");
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					// To řpiřazení zde musí být, neboď callback může být jiným vláknem kdykoli smazán
					Callback c = callback;
					if (c != null) {
						c.onChange();
					}
				}
			});
		}
	}

	public synchronized void add(int diff) {
		set(val + diff);
	}

	public synchronized void sub(int diff) {
		set(val - diff);
	}

	public synchronized void inc() {
		add(1);
	}

	public synchronized void dec() {
		sub(1);
	}

	public String getTextovyPopisTypu() {
		return textovyPopisTypu;
	}

	@Override
	public String toString() {
		return "Pocitadlo [val=" + val + ", name=" + name + "]";
	}

	public static interface Callback {
		public void onChange();
	}

	// @Override
	// public void finalize() {
	// System.out.println("Pocitadlo finalizováno");
	// }
}

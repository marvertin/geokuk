package cz.geokuk.plugins.mapy.kachle.podklady;

import cz.geokuk.plugins.mapy.kachle.data.Ka;
import cz.geokuk.plugins.mapy.kachle.podklady.KachleZiskavac.Kachlice;
import lombok.*;

/**
 * Ukládanec obsahuje mapové kachle, které se ukládají na disk.
 *
 * @author veverka
 *
 */
@Data
public class Ukladanec {

	private final Ka ka;

	/**
	 * Image držíme jen proto, aby před tím, než je kachle uložena na disk nedošlo k odstranění z paměti přes GC a pak se program nepokoušel data získávat z disku ještě před uložením.
	 */
	private final byte[] rawData;

	@Getter(AccessLevel.NONE)
	private final Kachlice __dummy__;

	public Ukladanec(final Ka ka, final byte[] data, final Kachlice kachlice) {
		this.ka = ka;
		rawData = data;
		__dummy__ = kachlice;
	}
}

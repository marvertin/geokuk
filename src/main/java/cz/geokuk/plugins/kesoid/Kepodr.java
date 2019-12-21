package cz.geokuk.plugins.kesoid;

import java.util.HashSet;
import java.util.Set;

import cz.geokuk.plugins.kesoid.genetika.Indexable;
import lombok.*;

/**
 * KEěoid PODdruh.
 * Každý kešoid může mít i poddruhy, to znamená různé druhy wayointů.
 * Každý kešoidový plugin může přijmout víc poddruhjů.
 * @author Martin
 *
 */
@AllArgsConstructor(access=AccessLevel.PRIVATE)
@ToString
public final class Kepodr implements Indexable {

	private static Set<String> values = new HashSet<>();

	private final int index;
	private final String value;


	/**
	 * Zřídí konstantu.
	 * @param Hodnota má význam jen pro toString(), jinak ne, ale aby se konstanty odlišily, musí být jedinečná.
	 * @return Zřízená kosntanta.
	 */
	public synchronized static Kepodr of(final String s) {
		if (! values.add(s)) {
			throw new RuntimeException("Duplicitni hodota " + s + " v " + values);
		}
		System.out.println(values);
		return new Kepodr(values.size() - 1, s);
	}

	@Override
	public int getIndex() {
		return index;
	}
}


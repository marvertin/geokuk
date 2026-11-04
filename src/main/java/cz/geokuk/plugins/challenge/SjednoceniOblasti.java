package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.CRC32;

/** Bod patří do oblasti, leží-li v kterékoli z daných oblastí (třeba Praha a dva okolní okresy). */
public final class SjednoceniOblasti implements Oblast {

	private final List<Oblast> oblasti;
	private final String verze;

	public SjednoceniOblasti(final List<? extends Oblast> oblasti) {
		if (oblasti.isEmpty()) {
			throw new IllegalArgumentException("Sjednocení nemá žádnou oblast");
		}
		this.oblasti = Collections.unmodifiableList(new ArrayList<>(oblasti));
		final CRC32 crc = new CRC32();
		for (final Oblast o : oblasti) {
			crc.update(o.verze().getBytes(java.nio.charset.StandardCharsets.UTF_8));
			crc.update(0);
		}
		verze = "U" + oblasti.size() + "/" + String.format("%08x", crc.getValue());
	}

	@Override
	public boolean obsahuje(final double lat, final double lon) {
		for (final Oblast o : oblasti) {
			if (o.obsahuje(lat, lon)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String verze() {
		return verze;
	}
}

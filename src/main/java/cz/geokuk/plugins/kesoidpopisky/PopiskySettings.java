package cz.geokuk.plugins.kesoidpopisky;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import cz.geokuk.framework.Copyable;
import cz.geokuk.framework.Preferenceble;
import cz.geokuk.plugins.kesoid.Kepodr;
import lombok.Data;

@Preferenceble
@Data
public class PopiskySettings implements Copyable<PopiskySettings> {

	public Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	public Color foreground = Color.BLACK;
	public Color background = new Color(255, 255, 255, 70);

	public int posuX = 0;
	public int posuY = 0;

	public Map<Kepodr, String> patterns2;

	public PopiskySettings(final  Map<Kepodr, String> defaultPatterns) {
		patterns2 = defaultPatterns;
	}

	public PopiskySettings() {}

	@Override
	public PopiskySettings copy() {
		try {
			final PopiskySettings klon = (PopiskySettings) super.clone();
			klon.setPatterns2(new HashMap<>(patterns2));
			return klon;
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}



}

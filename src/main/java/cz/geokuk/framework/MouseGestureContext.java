package cz.geokuk.framework;

import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.plugins.cesty.data.Bousek0;
import cz.geokuk.plugins.kesoid.Wpt;

public class MouseGestureContext {
	//
	// /**
	// * Změň kurzor jednorázově, voláno z mouseMove a mouseFrag
	// * @param cursor
	// */
	// void setMouseCursorOnce(Cursor cursor);
	//
	// /**
	// * Změň kurzor na nějaký, dokud nebude zrušeno.
	// * Voláno z mousePressed a mouseReleased
	// * @param cursor
	// */
	// void setMouseCursorTogle(Cursor cursor);
	//
	// void finish();
	public Wpt blizkyWpt;

	public Bousek0 blizkyBousek;

	public Mouable blizkaPozice;
}

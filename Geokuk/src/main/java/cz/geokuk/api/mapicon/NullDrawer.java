package cz.geokuk.api.mapicon;

import java.util.Deque;

public class NullDrawer extends Drawer0 {

	@Override
	public void draw(Deque<Imagant> imaganti) {
		imaganti.addFirst(null);
	}

}

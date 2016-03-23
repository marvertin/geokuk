/**
 *
 */
package cz.geokuk.core.program;

import java.awt.*;
import java.awt.event.*;

import javax.swing.JPopupMenu;
import javax.swing.event.MouseInputListener;

import cz.geokuk.core.coord.*;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.framework.FKurzory;
import cz.geokuk.framework.MouseGestureContext;

/**
 * Musí být úplně nahoře, je průhledná a chytá události myši a případně je distribuuje. Také zobrazuje myší kourzor.
 */
public final class JPresCeleMysovani extends JSingleSlide0 implements MouseInputListener, MouseWheelListener, KeyListener {

	private static final long	serialVersionUID	= 4979888007463850390L;

	private boolean				posouvameMapu;

	/**
	 * @param jKachlovnik
	 */
	JPresCeleMysovani() {
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);

	}

	private Point		bod;
	private VyrezModel	vyrezModel;
	private Point		cur;

	private PoziceModel	poziceModel;

	private MouseGestureContext ctx() {
		// protected void setMouseCursor(Cursor cursor) {
		// getMainFrame().setCursor(cursor);
		// }

		return new MouseGestureContext() {

		};
	}

	/**
	 * Invoked when a mouse button has been pressed on a component.
	 */
	@Override
	public void mousePressed(final MouseEvent e) {
		maybeShowPopup(e);
		bod = e.getPoint();
		final MouseGestureContext ctx = ctx();
		chain().mousePressed(e, ctx);
		if (!e.isConsumed()) {
			if (!e.isShiftDown()) {
				posouvameMapu = true;
				// skutecneNastavMysiKurzor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			}
		}
		// System.out.println("UDALOST " + e);
		// System.out.println("Kliknuti mysi v pozici: " + getCoord().getMouCur());
		requestFocus();
		skutecneNastavMysiKurzor(getMouseCursor(true));
	}

	@Override
	public Cursor getMouseCursor(final boolean pressed) {
		Cursor kurzor;
		if (posouvameMapu) {
			kurzor = FKurzory.POSOUVANI_MAPY;
		} else {
			kurzor = super.getMouseCursor(pressed);
		}
		if (kurzor == null) {
			kurzor = FKurzory.STANDARDNI_MAPOVY_KRIZ;
		}

		return kurzor;
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		maybeShowPopup(e);
		posouvameMapu = false;
		final MouseGestureContext ctx = ctx();
		chain().mouseReleased(e, ctx);
		skutecneNastavMysiKurzor(getMouseCursor(false));
		// zoomovaciObdelnik.mouseReleased(e);
	}

	/**
	 * @param aE
	 */
	private void maybeShowPopup(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			final JPopupMenu popup = new JPopupMenu();
			addPopouItems(popup, ctx());
			if (popup.getComponentCount() > 0) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}

	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		// posouvameMapu = false;
		if (posouvameMapu) {
			cur = e.getPoint();
			final int dx = cur.x - bod.x;
			final int dy = cur.y - bod.y;

			final Mou mou = getSoord().getMoustred();
			// Mou moustred = new Mou(mou.xx + -dx * getSoord().getPomer(), mou.yy + dy * getSoord().getPomer());
			final Mou moustred = mou.add(getSoord().transformShift(-dx, -dy));
			// getCoord().setMoustredNezadouci(moustred);
			vyrezModel.presunMapuNaMoustred(moustred);
			bod = cur;
		} else {
			final Point bodik = e.getPoint();
			final Mou mouCur = getSoord().transform(bodik);
			poziceModel.setMys(bodik, mouCur, getUpravenaMys());
		}
		final MouseGestureContext ctx = ctx();
		chain().mouseDragged(e, ctx);
		skutecneNastavMysiKurzor(getMouseCursor(true));

		// ctx.finish();
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		if ((e.getModifiers() & Event.CTRL_MASK) != 0) {
			// to je proto, abychom při stiskuném Ctrl, kdy chceme začít kreslit cestu, tak dostali fokus
			requestFocus();
		}
		cur = e.getPoint();
		final Mou mouCur = getSoord().transform(cur);
		// ХХSystem.out.println("MYSUJEME: " + cur + " --- " + mouCur + " - " + mouCur.toWgs());
		// System.out.printf("stred=%s=%s, jz=%s, sz=%s, jv=%s, sv=%s",
		// getSoord().getMoustred().toWgs(), getSoord().getMoustred(),
		// getSoord().getMouJZ().toWgs(),
		// getSoord().getMouSZ().toWgs(),
		// getSoord().getMouJV().toWgs(),
		// getSoord().getMouSV().toWgs()
		// );

		poziceModel.setMys(cur, mouCur, getUpravenaMys());
		// System.out.println("Souradnice: " + wgs);
		final MouseGestureContext ctx = ctx();
		chain().mouseMoved(e, ctx);
		skutecneNastavMysiKurzor(getMouseCursor(false));
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		final Mouable upravenaMys = getUpravenaMys();
		if (upravenaMys == null) {
			return; // to by nemělo nastat, protože pokud jsme nad slidem, tak musím mít myš, ale co kdyby
		}
		final int rotation = e.getWheelRotation();
		// System.out.println("Rotace: " + rotation);
		final int moumer = getSoord().getMoumer() - rotation;
		final Mou mou = upravenaMys.getMou();
		vyrezModel.zoomByGivenPoint(moumer, mou);
		requestFocus();

	}

	public void inject(final VyrezModel vyrezModel) {
		this.vyrezModel = vyrezModel;
	}

	public void inject(final PoziceModel poziceModel) {
		this.poziceModel = poziceModel;
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		chain().mouseClicked(e, ctx());
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		chain().mouseEntered(e, ctx());
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		poziceModel.setMys(null, null, null);
		chain().mouseExited(e, ctx());
	}

	private void skutecneNastavMysiKurzor(final Cursor cursor) {
		getParent().setCursor(cursor);
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			ctrlKeyPressed(ctx());
			skutecneNastavMysiKurzor(getMouseCursor(false));
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			ctrlKeyReleased(ctx());
			skutecneNastavMysiKurzor(getMouseCursor(false));
		}
	}

	@Override
	public void keyTyped(final KeyEvent e) {
		// Nezájem
	}

}
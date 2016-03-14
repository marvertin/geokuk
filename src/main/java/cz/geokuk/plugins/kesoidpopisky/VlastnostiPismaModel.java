package cz.geokuk.plugins.kesoidpopisky;

import java.awt.Color;
import java.awt.Font;
import java.util.EventListener;

import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import com.google.common.base.Objects;

/**
 * Swingovitý model (nikoli geokuí) vtýkající se vlastností písma, jenž se objevuje někde na mpapě.
 * @author veverka
 *
 */
public class VlastnostiPismaModel {
  private Font font= new Font(Font.SANS_SERIF, Font.PLAIN, 12);
  private Color foreground = Color.BLACK;
  private Color background = Color.WHITE;

  private int posuX = 0;
  private int posuY = 0;



  /**
   * The list of ChangeListeners for this model.  Subclasses may
   * store their own listeners here.
   */
  protected EventListenerList listenerList = new EventListenerList();


  /**
   * Adds a ChangeListener to the model's listener list.  The
   * ChangeListeners must be notified when the models value changes.
   *
   * @param l the ChangeListener to add
   * @see #removeChangeListener
   * @see SpinnerModel#addChangeListener
   */
  public void addChangeListener(final ChangeListener l) {
    listenerList.add(ChangeListener.class, l);
  }


  /**
   * Removes a ChangeListener from the model's listener list.
   *
   * @param l the ChangeListener to remove
   * @see #addChangeListener
   * @see SpinnerModel#removeChangeListener
   */
  public void removeChangeListener(final ChangeListener l) {
    listenerList.remove(ChangeListener.class, l);
  }


  /**
   * Returns an array of all the <code>ChangeListener</code>s added
   * to this AbstractSpinnerModel with addChangeListener().
   *
   * @return all of the <code>ChangeListener</code>s added or an empty
   *         array if no listeners have been added
   * @since 1.4
   */
  public ChangeListener[] getChangeListeners() {
    return listenerList.getListeners(ChangeListener.class);
  }

  /**
   * Return an array of all the listeners of the given type that
   * were added to this model.  For example to find all of the
   * ChangeListeners added to this model:
   * <pre>
   * myAbstractSpinnerModel.getListeners(ChangeListener.class);
   * </pre>
   *
   * @param listenerType the type of listeners to return, e.g. ChangeListener.class
   * @return all of the objects receiving <em>listenerType</em> notifications
   *         from this model
   */
  public <T extends EventListener> T[] getListeners(final Class<T> listenerType) {
    return listenerList.getListeners(listenerType);
  }

  /**
   * Run each ChangeListeners stateChanged() method.
   *
   * @see #setValue
   * @see EventListenerList
   */
  protected void fireStateChanged() {
    final Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -=2 ) {
      if (listeners[i] == ChangeListener.class) {
        ((ChangeListener)listeners[i+1]).stateChanged(new ChangeEvent(this));
      }
    }
  }

  public Font getFont() {
    return font;
  }
  public void setFont(final Font font) {
    if (! Objects.equal(this.font, font)) {
      this.font = font;
      fireStateChanged();
    }
  }
  public Color getForeground() {
    return foreground;
  }
  public void setForeground(final Color foreground) {
    if (! Objects.equal(this.foreground, foreground)) {
      this.foreground = foreground;
      fireStateChanged();
    }
  }
  public Color getBackground() {
    return background;
  }
  public void setBackground(final Color background) {
    if (! Objects.equal(this.background, background)) {
      this.background = background;
      fireStateChanged();
    }
  }
  public int getPosuX() {
    return posuX;
  }
  public void setPosuX(final int posuX) {
    if (posuX != this.posuX) {
      this.posuX = posuX;
      fireStateChanged();
    }
  }
  public int getPosuY() {
    return posuY;
  }
  public void setPosuY(final int posuY) {
    if (posuY != this.posuY) {
      this.posuY = posuY;
      fireStateChanged();
    }
  }

}
package cz.geokuk.plugins.kesoid.kind;

import java.util.*;

import javax.swing.Action;
import javax.swing.JComponent;

import cz.geokuk.framework.Action0;
import cz.geokuk.framework.BeanBag;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mvc.TiskniNaGcComAction;
import cz.geokuk.plugins.kesoid.mvc.ZobrazNaGcComAction;


/**
 * Přináší některé defaulty pro kešoid pluginy, aby pluginy nemusely implemetnovat nepotřebné věci.
 * Tento předek je nepovinný a nesmí obsahovat nic víc, než defaultní triviání implemtnace.
 * @author veverka
 *
 */
public abstract class DefaultKesoidPlugin0 implements KesoidPlugin {

	/**
	 * Defaultně nepřidáváme na toolbar žádné komponenty.
	 */
	@Override
	public List<JComponent> getSpecificToolbarComponents() {
		return Collections.emptyList();
	}

	/**
	 * Defaultně nepřidáváme žádné menu akce.
	 */
	@Override
	public List<Action> getSpecificKesoidMenuActions() {
		return Collections.emptyList();
	}

	/** Defaultně neregistrujeme nic */
	@Override
	public void registerSingletons(final BeanBag bb) {}


	/**
	 * Defaultně propustíme všechno.
	 */
	@Override
	public boolean filter(final Wpt wpt) {
		return true;
	}


	@Override
	public WptSumarizer getWptSumarizer() {
		return WptSumarizer.EMPTY;
	}

	@Override
	public int getPolomerObsazenosti(final Wpt wpt) {
		return 0;
	}

	@Override
	public List<Action0> getPopupMenuActions(final Wpt wpt) {
		return Arrays.asList(
				new ZobrazNaGcComAction(wpt),
				new TiskniNaGcComAction(wpt)
				);
	}

}

package cz.geokuk.plugins.kesoid.kind;

import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;


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

}

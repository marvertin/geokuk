package cz.geokuk.plugins.kesoid.mvc;


import cz.geokuk.framework.ToggleAction0;
import cz.geokuk.plugins.kesoid.FilterDefinition;
import cz.geokuk.plugins.kesoid.filtr.FilterDefinitionChangedEvent;


public class JenDoTerenuUNenalezenychAction extends ToggleAction0  {

	private static final long serialVersionUID = -7547868179813232769L;
	private KesoidModel model;

	//  moaOldCheck("Jen final u NEnalezených",     null,   "U ještě nenalezených vyluštěných mysterek a multin zhasne úvodní souřadnice a všechny stage." +
	//  " Zobrazí jen finálku, parkoviště a trailheady, takže vidíte jen to, co budete potřebovat při odlovu.");
	//  kesoidModel.getFilter().jenFinal.zabutonuj(moaOld.getModel());
	//
	//  moaOldCheck("Jen final u nalezených", null, "U nalezenýc a mých keší skrýt všechny waypointy kromě kromě finálního smailíka.");
	//  kesoidModel.getFilter().jenJedenUNalezenych.zabutonuj(moaOld.getModel());
	//
	//

	public JenDoTerenuUNenalezenychAction() {
		super("Jen něco u NEnalezených");
		putValue(SHORT_DESCRIPTION, "U ještě nenalezených vyluštěných mysterek a multin zhasne úvodní souřadnice a všechny stage.");
	}

	public void onEvent(FilterDefinitionChangedEvent event) {
		setSelected(event.getFilterDefinition().isJenDoTerenuUNenalezenych());
	}

	@Override
	protected void onSlectedChange(boolean onoff) {
		FilterDefinition definition = model.getDefinition();
		definition.setJenDoTerenuUNenalezenych(onoff);
		model.setDefinition(definition);
	}

	public void inject (KesoidModel model) {
		this.model = model;
	}



}

package cz.geokuk.plugins.kesoid.kind.kes;

import java.util.*;

import javax.swing.Action;
import javax.swing.JComponent;

import com.google.common.collect.ImmutableSet;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Action0;
import cz.geokuk.framework.BeanBag;
import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;
import cz.geokuk.plugins.kesoid.genetika.IndexMap;
import cz.geokuk.plugins.kesoid.importek.WptReceiver;
import cz.geokuk.plugins.kesoid.kind.*;

public class KesPlugin implements KesoidPlugin {

	private static final int POLOMER_OBSAZENOSTI = 161;



	public static final Kepodr KES = Kepodr.of("kes");
	public static final Kepodr KESADWPT = Kepodr.of("kesadwpt");

	public static final String PREF_KES_PLUGIN_node = FPref.KESOID_PLUGIN_node + "/kes";

	private final KesFilterModel kesFilterModel = new KesFilterModel();
	private final KesWptSumarizer kesWptSumarizer = new KesWptSumarizer(this);

	private final IndexMap<Kepodr, PopiskyDefBuilder0> map = new IndexMap<>();
	{
		map.put(KES, new KesPopiskyDef().doInit());
		map.put(KESADWPT, new KesAddWptPopiskyDef().doInit());
	}
	@Override
	public GpxWptProcak createGpxWptProcak(final GpxToWptContext ctx, final GpxToWptBuilder builder, final WptReceiver wpts) {
		return new KesGpxWptProcak(ctx, builder, wpts);
	}

	@Override
	public JKesoidDetail0 createDetail() {
		return new JKesDetail();
	}

	@Override
	public Set<Kepodr> getKepodrs() {
		return ImmutableSet.of(KES, KESADWPT);
	}

	@Override
	public PopiskyDef getPopiskyDef(final Kepodr kepodr) {
		return map.get(kepodr).build();
	}

	@Override
	public int getOrder() {
		return 2000;
	}

	@Override
	public List<JComponent> getSpecificToolbarComponents() {
		return Arrays.asList(new JVybiracHodnoceni(), new JVybiracBestOf(),new JVybiracFavorit());
	}

	@Override
	public List<Action> getSpecificKesoidMenuActions() {
		return Arrays.asList(new JenDoTerenuUNenalezenychAction(), new JenFinalUNalezenychAction());
	}


	@Override
	public void registerSingletons(final BeanBag bb) {
		bb.registerSigleton(kesFilterModel);
		bb.registerSigleton(kesWptSumarizer);
	}

	@Override
	public boolean filter(final Wpt wpt) {
		return kesFilterModel.getKesFilter().filter(wpt);
	}


	@Override
	public WptSumarizer getWptSumarizer() {
		return kesWptSumarizer;
	}

	@Override
	public int getPolomerObsazenosti(final Wpt wpt) {
		if (wpt.getStatus() == EWptStatus.ARCHIVED) {
			return 0;
		}
		final EKesWptType type = EKesWptType.decode(wpt.getSym());
		return type == EKesWptType.FINAL_LOCATION || type == EKesWptType.STAGES_OF_A_MULTICACHE || Wpti.TRADITIONAL_CACHE.equals(wpt.getSym()) ? POLOMER_OBSAZENOSTI : 0;
	}

	@Override
	public List<Action0> getPopupMenuActions(final Wpt wpt) {
		return Arrays.asList(
				new ZobrazNaGcComAction(wpt),
				new TiskniNaGcComAction(wpt)
				);
	}
}

package cz.geokuk.plugins.kesoid.kind;

import java.util.List;

import com.google.common.collect.Lists;

import cz.geokuk.plugins.kesoid.kind.cgp.CgpPlugin;
import cz.geokuk.plugins.kesoid.kind.kes.KesPlugin;
import cz.geokuk.plugins.kesoid.kind.munzee.MunzeePlugin;
import cz.geokuk.plugins.kesoid.kind.photo.PhotoPlugin;
import cz.geokuk.plugins.kesoid.kind.waymark.WaymarkPlugin;
import lombok.Getter;

/**
 * Manager kesouidových pluginů.
 * Je to vstupní bod pro práci s pluginy.
 * KesoidPluginManager se injektuje těm, kteří potřebujídělat něco, co je specifické pro jednotlivé druhy kešoidů.
 * Je snaha specifikum kešoidů soustředit v jednom balíku.
 * @author Martin
 *
 */
public class KesoidPluginManager {

	@Getter
	private  final MunzeePlugin munzeePlugin = new MunzeePlugin();

	@Getter
	private  final WaymarkPlugin waymarkPlugin = new WaymarkPlugin();

	@Getter
	private  final CgpPlugin cgpLugin = new CgpPlugin();

	@Getter
	private  final PhotoPlugin photoPlugin = new PhotoPlugin();

	@Getter
	private final KesPlugin kesPlugin = new KesPlugin();

	@Getter
	private final List<KesoidPlugin> plugins
	= Lists.newArrayList(
			kesPlugin,
			photoPlugin,
			cgpLugin,
			waymarkPlugin,
			munzeePlugin
			);


}

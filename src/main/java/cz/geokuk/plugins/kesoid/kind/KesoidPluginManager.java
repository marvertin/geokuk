package cz.geokuk.plugins.kesoid.kind;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JComponent;

import org.reflections.Reflections;

import cz.geokuk.framework.Factory;
import cz.geokuk.plugins.kesoid.Kepodr;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;
import cz.geokuk.plugins.kesoid.importek.GpxWpt;
import cz.geokuk.plugins.kesoid.kind.simplewaypoint.SimpleWaypointGpxWptProcak;
import cz.geokuk.util.procak.ProcakDispatcher;
import lombok.*;

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
	private final List<KesoidPlugin> plugins;
	private Factory factory;

	public KesoidPluginManager() {
		System.out.println("Found kesoid plugins:");
		plugins = new Reflections(this.getClass()).getSubTypesOf(KesoidPlugin.class).stream()
				.filter(cls -> ! Modifier.isAbstract(cls.getModifiers()))
				.map(this::newInstance)
				.sorted( (p1, p2) -> p1.getOrder() - p2.getOrder())
				.peek(plugin -> System.out.println("   " + plugin.getClass().getName()))
				.collect(Collectors.toList());
		System.out.println("Found " + plugins.size() + " kesoid plugins total.");
	}

	@SneakyThrows
	private KesoidPlugin newInstance(final Class<? extends KesoidPlugin> clazz) {
		return clazz.newInstance();
	}

	/**
	 * Zřídíme procák dispatchera, který bude jednotlivými poskytovateli publikovat waypointy do buldera.
	 * @param builder
	 * @return
	 */
	public ProcakDispatcher<GpxWpt> createGpxWptProcakDispatcher(final GpxToWptContext ctx, final GpxToWptBuilder builder) {
		return new ProcakDispatcher<>(plugins.stream()
				.map(plugin -> plugin.createGpxWptProcak(ctx,
						(gpxwpt, kepodr) -> {
							final Wpt wpt = builder.createWpt(gpxwpt, kepodr);
							wpt.setKesoidPlugin(plugin);
							return wpt;
						}))
				.filter(Objects::nonNull)
				.collect(Collectors.toList()),

				new SimpleWaypointGpxWptProcak(ctx,
						(gpxwpt, kepodr) -> {
							final Wpt wpt = builder.createWpt(gpxwpt, kepodr);
							wpt.setKesoidPlugin(sinkPlugin());
							return wpt;
						})

				);

	}

	/**
	 * Poslední z pluginů je sink plugin, do kterého se nasype to, co jiní nechtěli.
	 * @return
	 */
	private KesoidPlugin sinkPlugin() {
		return plugins.get(plugins.size() - 1);
	}

	/**
	 * Vyrobí všechna okna do dolního rohu pro kešoidy.
	 * @return
	 */
	public Map<KesoidPlugin, JKesoidDetail0> createKesoidDetails() {
		return plugins.stream()
				.collect(Collectors.toMap(Function.identity(), kesoidPlugin -> factory.init(kesoidPlugin.createDetail())));

	}

	public void inject(final Factory factory) {
		this.factory = factory;
	}

	/**
	 * Vrátí objekty poskytující popisky map pro každý z kepodrů.
	 * @return
	 */
	public Map<Kepodr, PopiskyDef> getPopisekDefMap() {
		return podplugins().collect(
				Collectors.toMap(kp -> kp.getKepodr(), kp -> kp.getPlugin().getPopiskyDef(kp.kepodr))
				);
	}

	/**
	 * Z jednotlivých pluginů získá co dát na tollbar.
	 * @return
	 */
	public List<JComponent> getSpecificToolbarComponents() {
		return getPlugins().stream().flatMap(plugin -> plugin.getSpecificToolbarComponents().stream())
				.collect(Collectors.toList());
	}

	private Stream<KesoidPodplugin> podplugins() {
		return plugins.stream().flatMap(
				plugin -> plugin
				.getKepodrs().stream()
				.map(kepodr -> new KesoidPodplugin(plugin, kepodr))
				);
	}

	/** Přiřazení pluginu a poddruhu */
	@Data
	private static class KesoidPodplugin {
		final KesoidPlugin plugin;
		final Kepodr kepodr;
	}
}

package cz.geokuk.plugins.kesoid.importek;

import java.util.Arrays;
import java.util.Set;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Config0;
import cz.geokuk.framework.Prefe;

/**
 * Konfigurace pro GSAK Loader.
 *
 * @since ISSUE#48 [2016-04-01, Bohusz]
 * @todo Bylo by pěkné, kdyby se to dalo injektovat nějak přes ten BeanBag.
 * @todo Bylo by pěkné, kdyby se to dalo nějak optimalizovat, aby se data načítale jen jednou a pak při jejich případné změně.
 */
public class GsakConfig extends Config0 {
	public GsakConfig() {
		inject(new Prefe());
	}

	public Set<String> getCasNalezu() {
		return currPrefe().node(FPref.GSAK_node).getStringSet(FPref.GSAK_CAS_NALEZU_value, Arrays.asList("UserData"));
	}

	public Set<String> getCasNenalezu() {
		return currPrefe().node(FPref.GSAK_node).getStringSet(FPref.GSAK_CAS_NENALEZU_value, Arrays.asList("UserData"));
	}
}

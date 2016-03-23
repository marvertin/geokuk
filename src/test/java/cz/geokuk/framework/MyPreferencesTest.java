package cz.geokuk.framework;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.util.*;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import cz.geokuk.plugins.kesoid.mapicon.ASada;

/**
 * Unit tests for {@link MyPreferences}.
 *
 * TODO : add more tests
 */
@RunWith(JUnit4.class)
public class MyPreferencesTest {

	private MyPreferences preferences;

	@Before
	public void setUp() {
		preferences = new MyPreferences(new MockPreferences(null, ""));
	}

	@Test
	public void test_shortStringStorage() {
		preferences.put("@jhka", "FOOBAR@;\"");
		assertThat(preferences.get("@jhka", null)).isEqualTo("FOOBAR@;\"");
	}

	@Test
	public void test_longStringStorage() {
		String storingString = Strings.repeat("FOOBAR@;", 1000);
		preferences.put("@jhka", storingString);
		assertThat(preferences.get("@jhka", null)).isEqualTo(storingString);
	}

	@Test
	public void test_putFile() {
		File toStore = new File("/tmp/foobar");
		preferences.putFile("jhka", toStore);
		assertThat(preferences.getFile("jhka", null)).isEqualTo(toStore);
	}

	@Test
	public void test_putAtom() {
		Atom toStore = Atom.valueOf(ASada.class, "Standard");
		preferences.putAtom("jhka", toStore);
		assertThat(preferences.getAtom("jhka", null, ASada.class)).isEqualTo(toStore);
	}

	@Test
	public void test_putStringList() {
		List<String> toStore = ImmutableList.of("qwert", "asdfgh", "yxcvb", "12345", "@{}^<");
		preferences.putStringList("jhka", toStore);
		assertThat(preferences.getStringList("jhka", null)).isEqualTo(toStore);
	}

	@Test
	public void test_putStringSet() {
		Set<String> toStore = ImmutableSet.of("12345", "qwert", "asdfgh", "yxcvb", "12345", "@{}^<");
		preferences.putStringSet("jhka", toStore);
		assertThat((Iterable<String>) preferences.getStringSet("jhka", null)).isEqualTo(toStore);
	}

	class MockPreferences extends AbstractPreferences {

		private Map<String, String> storage = new HashMap<>();

		protected MockPreferences(AbstractPreferences parent, String name) {
			super(parent, name);
		}

		@Override
		protected void putSpi(String key, String value) {
			storage.put(key, value);
		}

		@Override
		protected String getSpi(String key) {
			return storage.get(key);
		}

		@Override
		protected void removeSpi(String key) {
			storage.remove(key);
		}

		@Override
		protected void removeNodeSpi() throws BackingStoreException {

		}

		@Override
		protected String[] keysSpi() throws BackingStoreException {
			return new String[0];
		}

		@Override
		protected String[] childrenNamesSpi() throws BackingStoreException {
			return new String[0];
		}

		@Override
		protected AbstractPreferences childSpi(String name) {
			return null;
		}

		@Override
		protected void syncSpi() throws BackingStoreException {

		}

		@Override
		protected void flushSpi() throws BackingStoreException {

		}
	}
}

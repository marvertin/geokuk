package cz.geokuk.plugins.kesoid;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import cz.geokuk.plugins.kesoid.data.EKesoidKind;
import cz.geokuk.plugins.kesoid.genetika.Alela;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;
import lombok.SneakyThrows;

public abstract class Kesoid0 extends Weikoid0 implements Cloneable, Kesoid {

	private static String URL_PREFIX_PRINT = "https://www.geocaching.com/seek/cdpf.aspx?guid=";
	private static String URL_PREFIX_SHOW = "https://www.geocaching.com/seek/cache_details.aspx?guid=";


	private static String[] urlPrefixes = new String[] { "http://www.geocaching.com/seek/cache_details.aspx?guid=", "http://www.waymarking.com/waymarks/", "http://dataz.cuzk.cz/gu/ztl", };
	// protected static String URL_PREFIX_SHOW = "http://www.geocaching.com/seek/cache_details.aspx?guid=";

	/** Jednoznacna identifikace jako GC124X4 nebo WM4587 */
	private String identifier;

	private EWptVztah vztah = EWptVztah.NORMAL;
	private EWptStatus status = EWptStatus.ACTIVE;

	private String author;
	private String hidden;

	private String zbytekUrl;

	private Set<Alela> userDefinedAlelas;

	public void addWpt(final Wpti wpti) {
		if (wpti == null) {
			return;
		}
		//wpt.getKesoidPlugin(); // jen kontrola, zda tam je
		// najít konec
		Weikoid0 weik = this;
		while (weik.next instanceof Wpt) {
			weik = weik.next;
		}
		wpti.next = weik.next;
		weik.next = wpti;
	}

	public abstract Genotyp buildGenotyp(Genotyp g);

	public final Genotyp doBuildGenotyp(final Genotyp g0) {
		Genotyp g = buildGenotyp(g0);
		if (userDefinedAlelas != null) {
			for (final Alela alela : userDefinedAlelas) {
				assert alela != null;
				g = g.with(alela);
			}
		}
		return g;
	}

	@Override
	public String getAuthor() {
		return author;
	}

	/**
	 * @return the firstWpt
	 */
	public Wpt getFirstWpt() {
		if (next instanceof Wpt) {
			return (Wpt) next;
		} else {
			return null;
		}
	}

	@Override
	public String getHidden() {
		return hidden;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public abstract EKesoidKind getKesoidKind();

	@Override
	public String getNazev() {
		return getFirstWpt().getNazev();
	}

	@Override
	public String[] getProhledavanci() {
		return new String[] { getNazev(), getIdentifier() };
	}

	public File getSourceFile() {
		return null;
	}

	@Override
	public EWptStatus getStatus() {
		return status;
	}


	@Override
	@SneakyThrows
	public URL getUrl() {
		return new URL(_getUrl());
	}
	/**
	 * @return the url
	 */

	private String _getUrl() {
		if (zbytekUrl == null || zbytekUrl.length() == 0) {
			return zbytekUrl;
		}
		final char c = zbytekUrl.charAt(0);
		if (c == '-') {
			return zbytekUrl.substring(1);
		}
		final int index = c - '0';
		return urlPrefixes[index] + zbytekUrl.substring(1);
	}

	@Override
	public URL getUrlProPridaniDoSeznamuVGeogetu() {
		return null; // implicitně neumíme přidat
	}

	@Override
	public EWptVztah getVztah() {
		return vztah;
	}

	//////////////////////////////////////////////////////////////////
	@Override
	public Iterable<Wpt> getRelatedWpts() {
		return () -> new Iterator<Wpt>() {

			private Weikoid0 curwk = Kesoid0.this.next; // na první waypoint

			@Override
			public boolean hasNext() {
				return curwk instanceof Wpt;
			}

			@Override
			public Wpt next() {
				final Wpt result = (Wpt) curwk;
				curwk = curwk.next;
				return result;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}


	public abstract void prispejDoTooltipu(StringBuilder sb, Wpt wpt);


	public void setAuthor(final String author) {
		this.author = author.intern();
	}

	public void setHidden(final String hidden) {
		this.hidden = hidden;
	}

	public void setIdentifier(final String identifier) {
		this.identifier = identifier;
	}

	public void setStatus(final EWptStatus status) {
		this.status = status;
	}

	/**
	 * @param aUrl
	 *            the url to set
	 */
	public void setUrl(final String aUrl) {
		if (aUrl == null) {
			zbytekUrl = null;
			return;
		}
		// Řetězcová konkatenace zde provedená vytvoří nový řetězec, takže se nemusíme bát,
		// že podstringy všechno nesou
		char index = '0';
		for (final String prefix : urlPrefixes) {
			if (aUrl.startsWith(prefix)) {
				zbytekUrl = index + aUrl.substring(prefix.length());
				return;
			}
			index++;
		}
		zbytekUrl = '-' + aUrl; // nemá žádný prefix
	}

	public void setUserDefinedAlelas(final Set<Alela> userDefinedAlelas) {
		this.userDefinedAlelas = userDefinedAlelas;
	}

	public void setVztahx(final EWptVztah vztah) {
		this.vztah = vztah;
	}



	@Override
	public URL getUrlProOtevreniListinguVGeogetu() {
		try {
			final String urls = getUrl().toExternalForm();
			System.out.println(urls);
			System.out.println(URL_PREFIX_SHOW);
			if (urls.startsWith(URL_PREFIX_SHOW)) {
				return new URL(URL_PREFIX_PRINT + urls.substring(URL_PREFIX_SHOW.length()));
			} else {
				return null;
			}
		} catch (final MalformedURLException e) {
			return null;
		}
	}

}

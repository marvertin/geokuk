package cz.geokuk.plugins.kesoid;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Icon;

import cz.geokuk.plugins.kesoid.data.EKesoidKind;
import cz.geokuk.plugins.kesoid.genetika.Alela;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;

public abstract class Kesoid extends Weikoid0 implements Cloneable {

	private static String[] urlPrefixes = new String[] { "http://www.geocaching.com/seek/cache_details.aspx?guid=", "http://www.waymarking.com/waymarks/", "http://dataz.cuzk.cz/gu/ztl", };
	// protected static String URL_PREFIX_SHOW = "http://www.geocaching.com/seek/cache_details.aspx?guid=";

	/** Jednoznacna identifikace jako GC124X4 nebo WM4587 */
	private String identifier;

	private EKesVztah vztah = EKesVztah.NORMAL;
	private EKesStatus status = EKesStatus.ACTIVE;

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

	public String getHidden() {
		return hidden;
	}

	public String getIdentifier() {
		return identifier;
	}

	public abstract EKesoidKind getKesoidKind();

	public Wpt getMainWpt() {
		return getFirstWpt();
	}

	public String getNazev() {
		return getFirstWpt().getNazev();
	}

	public String[] getProhledavanci() {
		return new String[] { getNazev(), getIdentifier() };
	}

	public File getSourceFile() {
		return null;
	}

	public EKesStatus getStatus() {
		return status;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
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

	public abstract Icon getUrlIcon();

	public URL getUrlPrint() {
		return null;
	}

	public URL getUrlShow() {
		try {
			final String url = getUrl();
			return new URL(url);
		} catch (final MalformedURLException e) {
			return null;
		}
	}

	public EKesVztah getVztah() {
		return vztah;
	}

	//////////////////////////////////////////////////////////////////
	public Iterable<Wpt> getWpts() {
		return () -> new Iterator<Wpt>() {

			private Weikoid0 curwk = Kesoid.this.next; // na první waypoint

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

	public int getWptsCount() {
		int count = 0;
		for (@SuppressWarnings("unused")
		final Wpt wpt : getWpts()) {
			count++;
		}
		return count;

	}

	public abstract void prispejDoTooltipu(StringBuilder sb, Wpt wpt);

	public void promoteVztah(final EKesVztah vztah) {
		if (this.vztah == null) {
			this.vztah = vztah;
		}
		if (vztah.ordinal() > this.vztah.ordinal()) {
			this.vztah = vztah;
		}
	}

	public void setAuthor(final String author) {
		this.author = author.intern();
	}

	public void setHidden(final String hidden) {
		this.hidden = hidden;
	}

	public void setIdentifier(final String identifier) {
		this.identifier = identifier;
	}

	public void setStatus(final EKesStatus status) {
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

	public void setVztahx(final EKesVztah vztah) {
		this.vztah = vztah;
	}

}

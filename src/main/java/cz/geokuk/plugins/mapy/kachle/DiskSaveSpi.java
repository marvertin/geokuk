package cz.geokuk.plugins.mapy.kachle;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Umí zapsat sebe na disk jako kachli, tedy jako obrázek v nějakém vhodném formátu. Jedna z implementací má v sobě bytové pole;
 *
 * @author Martin Veverka
 *
 */
interface DiskSaveSpi {

	public void save(OutputStream ostm) throws IOException;

}

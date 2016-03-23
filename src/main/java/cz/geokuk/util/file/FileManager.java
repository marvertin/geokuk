package cz.geokuk.util.file;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * <pTřída souborového managera. Doplňuje vlastnosti třídy {@see java.io.File}. Tedy, to co na této třídě najdete, nehledejte v tomto file manageru. Nebude to tam. Třída bude postupně doplnována o metody dle požadavků. *
 * <p>
 * Nyní obsahuje pouze metody pro kopírování a přemísování souborů. Jsou zde zvlášt metody pro kopírování a přenost do souboru a do adresáře. Nemám rád řešení, kdy pokud je cíl adresář, je do něj šupnut soubor a pokud cíl neexistuje nebo je to soubor je považován za soubor. Pokud neexistuje cílový
 * adresář, není vytvářen, k tomu lze použít metodu File.mkdirs(). Příklad použití:
 *
 * <pre>
 *
 * FileManager fm = FileManager.getInstance(64 * 1024);
 * File zdroj = new File("/home/veverka/zdrojovysoubor.txt");
 * File cil = new File("/home/veverka/cilovysoubor.txt");
 * fm.copyFileToFile(zdroj, cil);
 *
 * </pre>
 *
 * <pre>
 * Pokud neexistuje cílový adresář, není vytvářen, k tomu lze použít metodu File.mkdirs().*. Příklad:
 *
 * File zdroj = new File("/home/veverka/zdrojovysoubor.txt");
 * File cil   = new File("/home/veverka/cilovysoubor.txt");
 * cil.getParent().mkdirs();
 * fm.copyFileToFile(zdroj, cil);
 *
 * </pre>
 *
 * Pro implemetnaci je využito přímo nové IO rozhraní java.nio, takže kopírování je nejrychlejší možné, které lze jednodušše v Javě realizovat. Pokud se jeví kopírování pomalé, insspirujte se touto implemetnací a použijte direct buffery, ale nejdříve si o tom něco přečtětě.
 *
 * @author Martin Veverka
 * @version 1.0
 */

public class FileManager {
	private static final int	MIN_BUFFER_SIZE	= 1024 * 4;
	private static final int	MAX_BUFFER_SIZE	= 1024 * 1024 * 32;

	private int					iBufferSize;

	/**
	 * Vytvoří isntanci file manageru. Parametrem je velikost bufferu, který bude použit při kopírování. Nutno vhodně zvolit podle velikosti kopírovaných souborů, jejich počtu, množství paměti, požadované odezvy.
	 *
	 * @param aBufferSize
	 * @return
	 */
	public static FileManager getInstance(final int aBufferSize) {
		final FileManager fm = new FileManager();
		fm.setBufferSize(aBufferSize);
		return fm;
	}

	private FileManager() {}

	/**
	 * Binárně zkopíruje soubor na do zadaného adresáře. Vlastní jméno souboru bude stejné. Čas vytvoření se zkopíruje také.
	 *
	 * @param aFrom
	 *            Zdrojový soubor
	 * @param aTo
	 *            Cílový adresář, pokud to není adresář, je hlášena chyba.
	 * @throws IOException
	 *             Při chybě
	 */
	public void copyFileToDir(final File aFrom, File aTo) throws IOException {
		if (!aTo.isDirectory()) {
			throw new IOException("Path " + aTo + " is not directory");
		}
		aTo = new File(aTo, aFrom.getName());
		_copy(aFrom, aTo);
	}

	/**
	 * Binárně zkopíruje soubor na jiné místo. Čas vytvoření se zkopíruje také.
	 *
	 * @param aFrom
	 *            Zdrojový soubor
	 * @param aTo
	 *            Cílový soubor, pokud je to adresář, je hlášena chyba.
	 * @throws IOException
	 *             Při chybě
	 */
	public void copyFileToFile(final File aFrom, final File aTo) throws IOException {
		if (aTo.isDirectory()) {
			throw new IOException("Path " + aTo + " is directory");
		}
		_copy(aFrom, aTo);
	}

	/**
	 * Kopíruje vstupní proud do souboru co možná nejefektivnějším způsobem.
	 *
	 * @param aIs
	 *            Vstupní proud, po zkopírování bude uzavřen.
	 * @param aOut
	 *            Soubor do něhož se kopíruje. Může, ale nemusí existovat, pokud neexistuje, musí existovat adresářová cesta k němu.
	 * @throws IOException
	 */
	public void copyInputStreamToFile(final InputStream aIs, final File aOut) throws IOException {
		final FileOutputStream out = new FileOutputStream(aOut);
		final FileChannel outc = out.getChannel();
		final ByteBuffer buffer = ByteBuffer.allocate(iBufferSize);
		final byte[] bb = new byte[iBufferSize];
		while (true) {
			final int len = aIs.read(bb);
			if (len <= 0) {
				break;
			}
			buffer.put(bb, 0, len);
			buffer.flip();
			outc.write(buffer);
			buffer.clear(); // Make room for the next read
		}
		outc.close();
		out.close();
		aIs.close();
	}

	/**
	 * Vrátí nastavenou velikost bufferu, která se používá pro kopírování.
	 *
	 * @return
	 */
	public int getBufferSize() {
		return iBufferSize;
	}

	/**
	 * Binárně přesune soubor na do zadaného adresáře. Vlastní jméno souboru bude stejné. Čas vytvoření se přesune také. Tato implementace využívá kopie a následného smazání, nespoléhejte však na to, třeba pozdější implementace budou využívat služeb filesystému a přesouvat přímo.
	 *
	 * @param aFrom
	 *            Zdrojový soubor
	 * @param aTo
	 *            Cílový adresář, pokud to není adresář, je hlášena chyba.
	 * @throws IOException
	 *             Při chybě
	 */
	public void moveFileToDir(final File aFrom, File aTo) throws IOException {
		if (!aTo.isDirectory()) {
			throw new IOException("Path " + aTo + " is not directory");
		}
		aTo = new File(aTo, aFrom.getName());
		_copy(aFrom, aTo);
		aFrom.delete();
	}

	/**
	 * Binárně přesune soubor na jiné místo. Čas vytvoření se přesune také. Tato implementace využívá kopie a následného smazání, nespoléhejte však na to, třeba pozdější implementace budou využívat služeb filesystému a přesouvat přímo.
	 *
	 * @param aFrom
	 *            Zdrojový soubor
	 * @param aTo
	 *            Cílový soubor, pokud je to adresář, je hlášena chyba.
	 * @throws IOException
	 *             Při chybě
	 */
	public void moveFileToFile(final File aFrom, final File aTo) throws IOException {
		if (aTo.isDirectory()) {
			throw new IOException("Path " + aTo + " is directory");
		}
		_copy(aFrom, aTo);
		aFrom.delete();
	}

	/**
	 * Obsah souboru vrácený jako pole bytů.
	 *
	 * @param aFile
	 *            Soubor, který má být čten.
	 * @param aEncoding
	 *            Kódování, pokud je null, nečte nic.
	 * @return
	 */
	public byte[] readWholeFileAsBytes(final File aFile) throws IOException {
		try (FileInputStream in = new FileInputStream(aFile)) {
			final FileChannel chan = in.getChannel();
			final ByteBuffer buffer = ByteBuffer.allocate((int) chan.size());
			buffer.clear();
			final int pocet = chan.read(buffer);
			if (pocet != chan.size()) {
				throw new RuntimeException("Z nejakeho duvodu bylo precteno jen " + pocet + " bytu, kdyz melo byt precteno" + chan.size());
			}
			buffer.flip();

			final byte vysl[] = new byte[(int) chan.size()];
			buffer.get(vysl);
			// if (pocet != chan.size()) throw new RuntimeException("Z nejakeho duvodu bylo zapsano jen " + pocet + " bytu, kdyz melo byt zapsano" + chan.size());
			buffer.clear();
			chan.close();
			return vysl;
		}
	}

	/**
	 * Přečte co nejrychleji celý soubor a vrátí ho jako řetězec.
	 *
	 * @param aFile
	 *            Soubor, který má být čten.
	 * @param aEncoding
	 *            Kódování, pokud je null, nečte nic.
	 * @return Obsah souborui vrácený jako řetězec.
	 */
	public String readWholeFileAsString(final File aFile) throws IOException {
		final byte[] bb = readWholeFileAsBytes(aFile);
		return new String(bb);
	}

	/**
	 * Přečte co nejrychleji celý soubor a vrátí ho jako řetězec.
	 *
	 * @param aFile
	 *            Soubor, který má být čten.
	 * @param aEncoding
	 *            Kódování, pokud je null, nečte nic.
	 * @return Obsah souborui vrácený jako řetězec.
	 */
	public String readWholeFileAsString(final File aFile, final String aCharSetName) throws IOException {
		final byte[] bb = readWholeFileAsBytes(aFile);
		return new String(bb, aCharSetName);
	}

	/**
	 * Nastaví velikost bufferu pro kopírování souborů. Pro jedno běžící kopírování bude vytvořen jeden buffer.
	 *
	 * @param aBufferSize
	 *            Velikost naastavovaného bufferu. Nejdříve bude upravena na rozumnou velikost, ani velké ani malé. A také zaokrouhlí na nějaký rozumný celý násobek.
	 */
	public void setBufferSize(int aBufferSize) {
		aBufferSize = (aBufferSize + MIN_BUFFER_SIZE - 1) / MIN_BUFFER_SIZE * MIN_BUFFER_SIZE;
		if (aBufferSize < MIN_BUFFER_SIZE) {
			aBufferSize = MIN_BUFFER_SIZE;
		}
		if (aBufferSize > MAX_BUFFER_SIZE) {
			aBufferSize = MAX_BUFFER_SIZE;
		}
		iBufferSize = aBufferSize;
	}

	/**
	 * Zapíše řetězec do zadaného souboru. Soubor přepíše.
	 *
	 * @param aFile
	 *            Soubor, do kterého se zapisuje.
	 * @param aString
	 *            Zapisovaný řetězec.
	 * @param aEncoding
	 *            Požadované kódování, null pokud defaultní.
	 * @throws IOException
	 */
	public void writeStringToFile(final File aFile, final String aString) throws IOException {
		final Writer wrt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(aFile)));
		wrt.write(aString);
		wrt.close();
	}

	/**
	 * Zapíše řetězec do zadaného souboru. Soubor přepíše.
	 *
	 * @param aFile
	 *            Soubor, do kterého se zapisuje.
	 * @param aString
	 *            Zapisovaný řetězec.
	 * @param aEncoding
	 *            Požadované kódování, null pokud defaultní.
	 * @throws IOException
	 */
	public void writeStringToFile(final File aFile, final String aString, final String aEncoding) throws IOException {
		final Writer wrt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(aFile), aEncoding));
		// System.out.p rintln(">>>>>>>>"+aString+"<<<<<<<<");
		wrt.write(aString);
		wrt.close();
	}

	///////////////////////////////////// privátní metody /////////////

	private void _copy(final File aIn, final File aOut) throws IOException {
		final FileInputStream in = new FileInputStream(aIn);
		final FileOutputStream out = new FileOutputStream(aOut);
		pumpFileChannels(in.getChannel(), out.getChannel());
		in.close();
		out.close();
		aOut.setLastModified(aIn.lastModified());
	}

	private void pumpFileChannels(final FileChannel inc, final FileChannel outc) throws IOException {
		final ByteBuffer buffer = ByteBuffer.allocate(iBufferSize);
		while (true) {
			final int ret = inc.read(buffer);
			if (ret == -1) {
				break;
			}
			buffer.flip();
			outc.write(buffer);
			buffer.clear(); // Make room for the next read
		}
		inc.close();
		outc.close();
	}

}
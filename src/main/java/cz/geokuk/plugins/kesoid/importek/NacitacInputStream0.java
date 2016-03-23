package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cz.geokuk.framework.ProgressModel;

public abstract class NacitacInputStream0 extends Nacitac0 {

	@Override
	protected final void nacti(File file, IImportBuilder builder, Future<?> future, ProgressModel progressModel) throws IOException {
		if (!umiNacist(file)) {
			throw new IllegalArgumentException("Cannot load file " + file);
		}
		nacti(wrapByProgressor(new FileInputStream(file), file.toString(), progressModel), file.toString(), builder, future);
	}

	@Override
	protected final void nacti(ZipFile zipFile, ZipEntry zipEntry, IImportBuilder builder, Future<?> future, ProgressModel progressModel)
			throws IOException {
		if (!umiNacist(zipEntry)) {
			throw new IllegalArgumentException("Cannot load zipped entry " + zipEntry + " from file " + zipFile);
		}
		String name = zipFile.getName() + "/" + zipEntry.getName();
		nacti(wrapByProgressor(zipFile.getInputStream(zipEntry), name, progressModel), name, builder, future);
	}


	protected abstract void nacti(InputStream aIstm, String name, IImportBuilder builder, Future<?> future) throws IOException;

}

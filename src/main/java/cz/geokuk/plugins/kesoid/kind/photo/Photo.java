package cz.geokuk.plugins.kesoid.kind.photo;

import java.io.File;

import cz.geokuk.plugins.kesoid.Kesoid0;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.data.EKesoidKind;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;

/**
 * Kesoid representing coordinates loaded from EXIF of a picture.
 */
public class Photo extends Kesoid0 {

	@Override
	public Genotyp buildGenotyp(final Genotyp genotyp) {
		final GenotypBuilderPhoto genotypBuilder = new GenotypBuilderPhoto(genotyp.getGenom());
		return genotypBuilder.build(this, genotyp);
	}

	@Override
	public EKesoidKind getKesoidKind() {
		return EKesoidKind.PHOTO;
	}

	@Override
	public File getSourceFile() {
		final File file = new File(getIdentifier());
		if (file.exists()) {
			return file;
		} else {
			return null;
		}
	}


	@Override
	public void prispejDoTooltipu(final StringBuilder sb, final Wpt wpt) {
		sb.append("<b>").append(wpt.getIdentifier()).append("<b>");
	}
}

package cz.geokuk.plugins.kesoid;

import java.io.File;

import javax.swing.Icon;

import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.data.EKesoidKind;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Jedinec;

/**
 * Kesoid representing coordinates loaded from EXIF of a picture.
 */
public class Photo extends Kesoid {

	@Override
	public void buildGenotyp(final Genom genom, final Jedinec genotyp) {
		final GenotypBuilderPhoto genotypBuilder = new GenotypBuilderPhoto(genom, genotyp);
		genotypBuilder.build(this);
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
	public Icon getUrlIcon() {
		return ImageLoader.seekResIcon("gccom.png");
	}

	@Override
	public void prispejDoTooltipu(final StringBuilder sb, final Wpt wpt) {
		sb.append("<b>").append(wpt.getName()).append("<b>");
	}
}

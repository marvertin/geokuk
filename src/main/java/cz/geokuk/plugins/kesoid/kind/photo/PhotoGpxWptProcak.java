package cz.geokuk.plugins.kesoid.kind.photo;

import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.importek.GpxWpt;
import cz.geokuk.plugins.kesoid.importek.WptReceiver;
import cz.geokuk.plugins.kesoid.kind.*;
import cz.geokuk.util.procak.EProcakResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PhotoGpxWptProcak implements GpxWptProcak {
	private static final String PIC = "pic";

	private final GpxToWptContext ctx;
	private final GpxToWptBuilder builder;
	private final WptReceiver wpts;


	@Override
	public EProcakResult process(final GpxWpt gpxwpt) {
		if (isPhoto(gpxwpt)) {
			wpts.expose(createPhoto(gpxwpt).getMainWpt());
			return EProcakResult.DONE;
		} else {
			return EProcakResult.NEVER;
		}

	}


	private boolean isPhoto(final GpxWpt gpxWpt) {
		return PIC.equals(gpxWpt.type);
	}


	private Photo createPhoto(final GpxWpt gpxwpt) {
		final Wpt wpt = builder.createWpt(gpxwpt, PhotoPlugin.PHOTO);
		wpt.setSym(gpxwpt.sym);

		final Photo photo = new Photo();
		photo.setIdentifier(gpxwpt.link.href);

		photo.addWpt(wpt);
		log.debug("photo: " + photo.getFirstWpt());
		photo.setUserDefinedAlelas(ctx.definujUzivatslskeAlely(gpxwpt));
		return photo;
	}

	@Override
	public void roundDone() {
		// TODO Auto-generated method stub

	}


	@Override
	public void allDone() {
		// TODO Auto-generated method stub

	}



}

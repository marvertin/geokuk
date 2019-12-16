package cz.geokuk.plugins.kesoid.detail;

import java.awt.Color;
import java.awt.Font;
import java.util.EnumMap;

import javax.swing.*;

import cz.geokuk.core.coord.PoziceChangedEvent;
import cz.geokuk.core.program.Akce;
import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.Factory;
import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.data.EKesoidKind;
import cz.geokuk.plugins.kesoid.kind.cgp.JCgpDetail;
import cz.geokuk.plugins.kesoid.kind.kes.JKesDetail;
import cz.geokuk.plugins.kesoid.kind.munzee.JMunzeeDetail;
import cz.geokuk.plugins.kesoid.kind.simplewaypoint.JSimpleWaypointDetail;
import cz.geokuk.plugins.kesoid.kind.waymark.JWaymarkDetail;
import cz.geokuk.plugins.kesoid.mapicon.IkonBag;
import cz.geokuk.plugins.kesoid.mvc.IkonyNactenyEvent;
import cz.geokuk.plugins.kesoid.mvc.ZobrazNaGcComAction;
import cz.geokuk.plugins.refbody.DomaciSouradniceSeZmenilyEvent;
import cz.geokuk.plugins.refbody.RefbodyModel;
import cz.geokuk.util.gui.JSmallPictureButton;

/**
 * Detailní informace o vybrané keši.
 *
 * @author Spikodrob
 *
 */
public class JKesoidDetailContainer extends JPanel implements AfterInjectInit {

	/**
	 *
	 */
	private static final long serialVersionUID = -3323887260932949747L;

	private Kesoid kesoid;

	private JLabel jKesoidCode;
	private JLabel jKesoidNazev;
	private JLabel jKesoidSym;

	private JLabel jWptName;
	private JLabel jWptNazev;
	private JLabel jWptSym;

	private JLabel jElevation;

	private JLabel jType;
	private JLabel jAuthor;
	private JLabel jHiddenTime;
	private JLabel jVztah;

	private JLabel jVzdalenost;
	private JLabel jAzimut;

	private JSmallPictureButton jOtevriUrl;
	private JSmallPictureButton vyletAnoButton;
	private JSmallPictureButton vyletNeButton;
	private JSmallPictureButton vyletNevimButton;

	private final EnumMap<EKesoidKind, JKesoidDetail0> jDetailyKesoidu = new EnumMap<>(EKesoidKind.class);

	private IkonBag ikonBag;

	private Wpt wpt;

	private JLabel jRucnePridany;

	private Akce akce;

	private Factory factory;

	private RefbodyModel refbodyModel;

	private static String formatuj(final String s, final EKesStatus status) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		if (status != EKesStatus.ACTIVE) {
			sb.append("<strike>");
		}
		if (status == EKesStatus.DISABLED) {
			sb.append("<font color=\"darkgray\">");
		}
		if (status == EKesStatus.ARCHIVED) {
			sb.append("<font color=\"red\">");
		}
		sb.append(s);
		if (status == EKesStatus.ARCHIVED) {
			sb.append("</font");
		}
		if (status == EKesStatus.DISABLED) {
			sb.append("</font");
		}
		if (status != EKesStatus.ACTIVE) {
			sb.append("</strike>");
		}
		return sb.toString();
	}

	private static Icon vztah(final EKesVztah vztah) {
		switch (vztah) {
		case FOUND:
			return ImageLoader.seekResIcon("kesvztah/found.gif");
		case OWN:
			return ImageLoader.seekResIcon("kesvztah/own.gif");
		case NORMAL:
			return null;
		case NOT:
			return null;
		default:
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.program.AfterInjectInit#initAfterInject()
	 */
	@Override
	public void initAfterInject() {
		initComponents();
	}

	public void inject(final Akce akce) {
		this.akce = akce;
	}

	public void inject(final Factory factory) {
		this.factory = factory;
	}

	public void inject(final RefbodyModel refbodyModel) {
		this.refbodyModel = refbodyModel;
	}

	public void onEvent(final DomaciSouradniceSeZmenilyEvent aEvent) {
		if (isVisible() && kesoid != null) {
			napln();
		}
	}

	public void onEvent(final IkonyNactenyEvent event) {
		ikonBag = event.getBag();

	}

	public void onEvent(final PoziceChangedEvent aEvent) {
		if (aEvent.poziceq.isNoPosition()) {
			setVisible(false);
		} else {
			wpt = aEvent.poziceq.getWpt();
			if (wpt == null) {
				setVisible(false);
			} else {
				final Kesoid k = wpt.getKesoid();
				if (kesoid == null || k.getKesoidKind() != kesoid.getKesoidKind()) {
					if (kesoid != null) {
						jDetailyKesoidu.get(kesoid.getKesoidKind()).setVisible(false);
					}
					kesoid = wpt.getKesoid();
					jDetailyKesoidu.get(kesoid.getKesoidKind()).setVisible(true);
				} else {
					kesoid = k;
				}
				jDetailyKesoidu.get(kesoid.getKesoidKind()).napln(wpt);

				if (kesoid.getUrlShow() != null) {
					jOtevriUrl.setAction(new ZobrazNaGcComAction(kesoid));
					jOtevriUrl.setVisible(true);
				} else {
					jOtevriUrl.setVisible(false);
				}
				jOtevriUrl.setText(null);
				napln();
				// boolean mameHint = kes.getHint() != null && ! kes.getHint().trim().isEmpty();
				// zobrazHint.setEnabled(mameHint);
				setVisible(true);
			}
		}
	}

	protected void napln() {
		jKesoidCode.setText(kesoid.getKesoidKind() == EKesoidKind.CGP ? wpt.getName() : kesoid.getIdentifier());
		jKesoidNazev.setText(formatuj(kesoid.getNazev(), kesoid.getStatus()));
		jKesoidSym.setText(kesoid.getFirstWpt().getSym());

		jWptName.setText(wpt.getName());
		jWptNazev.setText(formatuj(wpt.getNazev(), kesoid.getStatus()));
		jWptSym.setText(wpt.getSym());
		jRucnePridany.setText(wpt.isRucnePridany() ? "+" : "*");
		jRucnePridany.setToolTipText(wpt.isRucnePridany() ? "Waypoint byl ručně přidán v Geogetu nebo podobném programu." : "Waypoint byl obsažen v PQ");
		final int elevation = wpt.getElevation();
		jElevation.setText(elevation == 0 ? null : elevation + " m n. m.");
		jAuthor.setText(kesoid.getAuthor());
		jHiddenTime.setText(JKesoidDetail0.formatujDatum(kesoid.getHidden()));
		jVztah.setIcon(vztah(kesoid.getVztah()));
		if (ikonBag != null) {
			jType.setIcon(ikonBag.seekIkon(kesoid.getMainWpt().getGenotyp()));
		}
		jAzimut.setIcon(azimut(kesoid.getMainWpt()));
		jVzdalenost.setText(vzdalenost(kesoid.getMainWpt()));

		// jNoFirstWpt.setVisible(wpt != kesoid.getFirstWpt());
		// kesoid.getKesoidKind().getDetail().setVisible(true);

	}

	private Icon azimut(final Wpt wpt) {
		return Ikonizer.findSmerIcon(refbodyModel.getHc().azimut(wpt.getWgs()));
	}

	private void initComponents() {
		jKesoidCode = new JLabel();
		jKesoidNazev = new JLabel();
		jKesoidSym = new JLabel();

		jWptName = new JLabel();
		jWptNazev = new JLabel();
		jWptSym = new JLabel();

		jType = new JLabel();
		jAuthor = new JLabel();
		jHiddenTime = new JLabel();
		jVztah = new JLabel();

		jVzdalenost = new JLabel();
		jAzimut = new JLabel();
		jElevation = new JLabel();
		jElevation.setToolTipText("Nadmořská výška");

		jRucnePridany = new JLabel();

		jOtevriUrl = new JSmallPictureButton();

		vyletAnoButton = new JSmallPictureButton(ImageLoader.seekResIcon("x16/vylet/vyletAno.png"));
		vyletAnoButton.setAction(akce.vyletAnoAction);
		vyletAnoButton.setText(null);
		// vyletAnoButton.setPreferredSize(new Dimension(30,10));
		vyletNeButton = new JSmallPictureButton(ImageLoader.seekResIcon("x16/vylet/vyletNe.png"));
		vyletNeButton.setAction(akce.vyletNeAction);
		vyletNeButton.setText(null);
		vyletNevimButton = new JSmallPictureButton(ImageLoader.seekResIcon("x16/vylet/vyletNevim.png"));
		vyletNevimButton.setAction(akce.vyletNevimAction);
		vyletNevimButton.setText(null);

		jKesoidCode.setForeground(Color.RED);
		jKesoidNazev.setFont(jKesoidNazev.getFont().deriveFont(Font.BOLD));
		jKesoidSym.setFont(jKesoidSym.getFont().deriveFont(Font.ITALIC));
		final Box hlav = Box.createVerticalBox();
		add(hlav);

		{
			final Box box1 = Box.createHorizontalBox();
			box1.add(jKesoidNazev);
			box1.add(Box.createHorizontalStrut(10));
			hlav.add(box1);
		}

		{

			final Box box2a = Box.createVerticalBox();
			jVzdalenost.setAlignmentX(CENTER_ALIGNMENT);
			jAzimut.setAlignmentX(CENTER_ALIGNMENT);
			box2a.add(jVzdalenost);
			box2a.add(jAzimut);

			final Box box2b = Box.createVerticalBox();
			jKesoidSym.setAlignmentX(RIGHT_ALIGNMENT);
			jKesoidCode.setAlignmentX(RIGHT_ALIGNMENT);
			box2b.add(jKesoidCode);
			box2b.add(jKesoidSym);

			final Box box2 = Box.createHorizontalBox();
			box2.add(jType);
			box2.add(Box.createHorizontalStrut(5));
			box2.add(jVztah);
			box2.add(box2a);
			box2.add(Box.createGlue());
			box2.add(jElevation);
			box2.add(Box.createGlue());
			box2.add(box2b);
			box2.add(Box.createHorizontalStrut(10));
			hlav.add(box2);
		}

		final Box box9 = Box.createHorizontalBox();
		box9.add(Box.createGlue());
		box9.add(vyletAnoButton);
		box9.add(vyletNeButton);
		box9.add(vyletNevimButton);
		box9.add(Box.createHorizontalStrut(10));
		box9.add(jOtevriUrl);

		{
			final Box box8a = Box.createVerticalBox();
			jHiddenTime.setAlignmentX(LEFT_ALIGNMENT);
			jAuthor.setAlignmentX(LEFT_ALIGNMENT);
			box8a.add(jHiddenTime);
			box8a.add(jAuthor);

			final Box box8 = Box.createHorizontalBox();
			box8.add(box8a);
			box8.add(Box.createGlue());
			box8.add(box9);
			hlav.add(box8);
		}

		jDetailyKesoidu.put(EKesoidKind.CGP, factory.init(new JCgpDetail()));
		jDetailyKesoidu.put(EKesoidKind.KES, factory.init(new JKesDetail()));
		jDetailyKesoidu.put(EKesoidKind.WAYMARK, factory.init(new JWaymarkDetail()));
		jDetailyKesoidu.put(EKesoidKind.SIMPLEWAYPOINT, factory.init(new JSimpleWaypointDetail()));
		jDetailyKesoidu.put(EKesoidKind.MUNZEE, factory.init(new JMunzeeDetail()));
		// TODO : change this
		jDetailyKesoidu.put(EKesoidKind.PHOTO, factory.init(new JKesoidDetail0() {
			private static final long serialVersionUID = 7253981658581992444L;

			@Override
			public void napln(final Wpt wpt) {}
		}));
		for (final EKesoidKind kind : EKesoidKind.values()) {
			final JKesoidDetail0 detail = jDetailyKesoidu.get(kind);
			detail.setVisible(false);
			hlav.add(detail);
		}
	}

	private String vzdalenost(final Wpt wpt) {
		return refbodyModel.getHc().vzdalenostStr(wpt.getWgs());
	}

}
